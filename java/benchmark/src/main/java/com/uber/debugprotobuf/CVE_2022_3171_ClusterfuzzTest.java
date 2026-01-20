package com.uber.debugprotobuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Test for CVE-2022-3171 using the actual clusterfuzz test case data.
 *
 * <p>This parses the real fuzzer-generated payload that triggers the vulnerability. The payload
 * contains field 3 (EmbeddedMessage) repeated many times in the wire format, with each embedded
 * message containing repeated fields, causing excessive GC pressure.
 */
public class CVE_2022_3171_ClusterfuzzTest {

  private static final String CLUSTERFUZZ_RESOURCE =
      "clusterfuzz-testcase-minimized-ProtobufFuzzer-4671272402944000";

  /** Parse the clusterfuzz test case and observe CVE-2022-3171 behavior */
  public static void main(String[] args) throws Exception {
    System.out.println("CVE-2022-3171 Clusterfuzz Test Case Parser");
    System.out.println("==========================================\n");

    // Parse args: iterations (default 1)
    int iterations = 1;
    if (args.length > 0) {
      try {
        iterations = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Usage: CVE_2022_3171_ClusterfuzzTest [iterations]");
        System.out.println(
            "  iterations: number of times to parse (default: 1, suggested: 1000-10000 for DoS)");
        System.exit(1);
      }
    }

    byte[] clusterfuzzData;
    try (InputStream is = CVE_2022_3171_ClusterfuzzTest.class.getClassLoader()
        .getResourceAsStream(CLUSTERFUZZ_RESOURCE)) {
      if (is == null) {
        throw new IllegalStateException("Resource not found: " + CLUSTERFUZZ_RESOURCE);
      }
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] temp = new byte[8192];
      int bytesRead;
      while ((bytesRead = is.read(temp)) != -1) {
        buffer.write(temp, 0, bytesRead);
      }
      clusterfuzzData = buffer.toByteArray();
    }
    System.out.println("Loaded clusterfuzz test case: " + clusterfuzzData.length + " bytes");
    System.out.println("Parse iterations: " + iterations);

    if (iterations > 1) {
      System.out.println(
          "\nWARNING: Running " + iterations + " iterations to simulate repeated DoS attack");
      System.out.println("Expect significant GC pressure and memory consumption!\n");
    }

    Runtime runtime = Runtime.getRuntime();
    runtime.gc();
    Thread.sleep(100);
    long memBefore = runtime.totalMemory() - runtime.freeMemory();

    System.out.println("\nParsing with VulnerableRequest (has repeated fields)...");
    long startTime = System.nanoTime();

    int successCount = 0;
    int failCount = 0;
    CVEProtobufProto.VulnerableRequest lastRequest = null;

    try {
      for (int i = 0; i < iterations; i++) {
        if (iterations > 100 && i % (iterations / 10) == 0) {
          long elapsed = (System.nanoTime() - startTime) / 1_000_000;
          System.out.println(
              "  Progress: " + i + "/" + iterations + " (" + elapsed + " ms elapsed)");
        }

        lastRequest = CVEProtobufProto.VulnerableRequest.parseFrom(clusterfuzzData);
        successCount++;
      }

      long endTime = System.nanoTime();
      long memAfter = runtime.totalMemory() - runtime.freeMemory();
      long totalTimeMs = (endTime - startTime) / 1_000_000;
      long memDeltaKB = (memAfter - memBefore) / 1024;

      System.out.println("\n" + "===========================================");
      System.out.println("Results:");
      System.out.println("=================================================");
      System.out.println("Total parse time: " + totalTimeMs + " ms");
      System.out.println("Average time per parse: " + (totalTimeMs / iterations) + " ms");
      System.out.println("Success count: " + successCount + "/" + iterations);
      System.out.println("Memory delta: " + memDeltaKB + " KB");

      System.out.println("\n" + "=================================================");
      System.out.println("CVE-2022-3171 Impact Analysis:");
      System.out.println("==========================================================");
      double memAmplification = (double) memDeltaKB / (clusterfuzzData.length / 1024.0);
      double throughputKBps =
          (clusterfuzzData.length / 1024.0 * iterations) / (totalTimeMs / 1000.0);

      System.out.println("  - Memory amplification: " + String.format("%.2fx", memAmplification));
      System.out.println("  - Parse throughput: " + String.format("%.2f KB/s", throughputKBps));
      System.out.println(
          "  - Total data processed: "
              + String.format(
                  "%.2f MB", ((long) clusterfuzzData.length * iterations) / (1024.0 * 1024.0)));


    } catch (IOException e) {
      long endTime = System.nanoTime();
      failCount++;
      System.out.println("\nParse time: " + (endTime - startTime) / 1_000_000 + " ms");
      System.out.println("Success count: " + successCount + "/" + iterations);
      System.out.println("FAILED at iteration " + (successCount + 1) + ": " + e.getMessage());
      System.err.println("Error details:");
      e.printStackTrace(System.err);
    }

    System.out.println("\n" + "==========================");
    System.out.println("Usage Tips:");
    System.out.println("=============================");
    System.out.println("1. Single parse (observe vulnerability in one payload):");
    System.out.println("   bazel run //experimental/users/amishra/debugprotobuf:bin_main");
    System.out.println("\n2. Repeated parse DoS simulation (amplify GC pressure):");
    System.out.println("   bazel run //experimental/users/amishra/debugprotobuf:bin_main -- 10");
    System.out.println("\n3. With GC logging:");
    System.out.println(
        "   bazel run //experimental/users/amishra/debugprotobuf:bin_main --jvmopt=-verbose:gc -- 5000");
  }
}
