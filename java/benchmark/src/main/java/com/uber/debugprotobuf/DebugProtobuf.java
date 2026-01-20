package com.uber.debugprotobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.uber.debugprotobuf.DebugProtobufProto.BenchmarkRequest;
import com.uber.debugprotobuf.DebugProtobufProto.NodeA;
import com.uber.debugprotobuf.DebugProtobufProto.NodeB;
import com.uber.debugprotobuf.DebugProtobufProto.NodeC;
import com.uber.debugprotobuf.DebugProtobufProto.NodeD;
import com.uber.debugprotobuf.DebugProtobufProto.NodeE;
import com.uber.debugprotobuf.DebugProtobufProto.NodeF;
import com.uber.debugprotobuf.DebugProtobufProto.NodeG;
import com.uber.debugprotobuf.DebugProtobufProto.NodeH;
import java.util.ArrayList;
import java.util.List;

public class DebugProtobuf {

  private static final int WARMUP_ITERATIONS = 100;
  private static final int MEASUREMENT_ITERATIONS = 100;
  private static final int BENCHMARK_ROUNDS = 5;
  private static volatile Object blackhole; // Prevent dead code elimination

  public static void main(String[] args) throws InvalidProtocolBufferException {
    System.out.println("=== Protobuf Parsing Benchmark ===\n");

    System.out.println("Preparing benchmark request data...");
    BenchmarkRequest request =
        BenchmarkRequest.newBuilder().setRoot(createNodeA("root", 0L)).build();
    byte[] requestBytes = request.toByteArray();

    System.out.println("Data prepared:");
    System.out.println("  Serialized size: " + requestBytes.length + " bytes");
    System.out.println("  Tree depth: 8 levels");
    System.out.println("  Branching factor: 8 children per node\n");

    // Warmup phase - ensure JIT compilation to C2
    System.out.println("Warming up (" + WARMUP_ITERATIONS + " iterations)...");
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      blackhole = BenchmarkRequest.parseFrom(requestBytes);
    }
    System.out.println("Warmup complete.\n");

    // Force GC before measurements
    System.gc();
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Measurement phase
    System.out.println("Running benchmark (" + BENCHMARK_ROUNDS + " rounds of " + MEASUREMENT_ITERATIONS + " iterations each)...\n");
    List<Long> latencies = new ArrayList<>(BENCHMARK_ROUNDS);

    for (int round = 0; round < BENCHMARK_ROUNDS; round++) {
      long startTime = System.nanoTime();
      for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
        blackhole = BenchmarkRequest.parseFrom(requestBytes);
      }
      long endTime = System.nanoTime();
      long durationNanos = endTime - startTime;
      latencies.add(durationNanos);

      // Print progress for each round
      double avgMs = durationNanos / 1_000_000.0;
      double perOpUs = durationNanos / (double) MEASUREMENT_ITERATIONS / 1_000.0;
      System.out.printf("Round %2d/%d: %.2f ms total, %.2f μs/op%n",
          round + 1, BENCHMARK_ROUNDS, avgMs, perOpUs);
    }

    // Calculate statistics
    printStatistics(latencies, requestBytes.length);
  }

  private static NodeA createNodeA(String name, long value) {

    return NodeA.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeB(name + ".1", value + 1))
        .setChild2(createNodeB(name + ".2", value + 2))
        .setChild3(createNodeB(name + ".3", value + 3))
        .setChild4(createNodeB(name + ".4", value + 4))
        .setChild5(createNodeB(name + ".5", value + 5))
        .setChild6(createNodeB(name + ".6", value + 6))
        .setChild7(createNodeB(name + ".7", value + 7))
        .setChild8(createNodeB(name + ".8", value + 8))
        .build();
  }

  private static NodeB createNodeB(String name, long value) {

    return NodeB.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeC(name + ".1", value + 1))
        .setChild2(createNodeC(name + ".2", value + 2))
        .setChild3(createNodeC(name + ".3", value + 3))
        .setChild4(createNodeC(name + ".4", value + 4))
        .setChild5(createNodeC(name + ".5", value + 5))
        .setChild6(createNodeC(name + ".6", value + 6))
        .setChild7(createNodeC(name + ".7", value + 7))
        .setChild8(createNodeC(name + ".8", value + 8))
        .build();
  }

  private static NodeC createNodeC(String name, long value) {

    return NodeC.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeD(name + ".1", value + 1))
        .setChild2(createNodeD(name + ".2", value + 2))
        .setChild3(createNodeD(name + ".3", value + 3))
        .setChild4(createNodeD(name + ".4", value + 4))
        .setChild5(createNodeD(name + ".5", value + 5))
        .setChild6(createNodeD(name + ".6", value + 6))
        .setChild7(createNodeD(name + ".7", value + 7))
        .setChild8(createNodeD(name + ".8", value + 8))
        .build();
  }

  private static NodeD createNodeD(String name, long value) {

    return NodeD.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeE(name + ".1", value + 1))
        .setChild2(createNodeE(name + ".2", value + 2))
        .setChild3(createNodeE(name + ".3", value + 3))
        .setChild4(createNodeE(name + ".4", value + 4))
        .setChild5(createNodeE(name + ".5", value + 5))
        .setChild6(createNodeE(name + ".6", value + 6))
        .setChild7(createNodeE(name + ".7", value + 7))
        .setChild8(createNodeE(name + ".8", value + 8))
        .build();
  }

  private static NodeE createNodeE(String name, long value) {

    return NodeE.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeF(name + ".1", value + 1))
        .setChild2(createNodeF(name + ".2", value + 2))
        .setChild3(createNodeF(name + ".3", value + 3))
        .setChild4(createNodeF(name + ".4", value + 4))
        .setChild5(createNodeF(name + ".5", value + 5))
        .setChild6(createNodeF(name + ".6", value + 6))
        .setChild7(createNodeF(name + ".7", value + 7))
        .setChild8(createNodeF(name + ".8", value + 8))
        .build();
  }

  private static NodeF createNodeF(String name, long value) {

    return NodeF.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeG(name + ".1", value + 1))
        .setChild2(createNodeG(name + ".2", value + 2))
        .setChild3(createNodeG(name + ".3", value + 3))
        .setChild4(createNodeG(name + ".4", value + 4))
        .setChild5(createNodeG(name + ".5", value + 5))
        .setChild6(createNodeG(name + ".6", value + 6))
        .setChild7(createNodeG(name + ".7", value + 7))
        .setChild8(createNodeG(name + ".8", value + 8))
        .build();
  }

  private static NodeG createNodeG(String name, long value) {

    return NodeG.newBuilder()
        .setName(name)
        .setValue(value)
        .setChild1(createNodeH(name + ".1", value + 1))
        .setChild2(createNodeH(name + ".2", value + 2))
        .setChild3(createNodeH(name + ".3", value + 3))
        .setChild4(createNodeH(name + ".4", value + 4))
        .setChild5(createNodeH(name + ".5", value + 5))
        .setChild6(createNodeH(name + ".6", value + 6))
        .setChild7(createNodeH(name + ".7", value + 7))
        .setChild8(createNodeH(name + ".8", value + 8))
        .build();
  }

  private static NodeH createNodeH(String name, long value) {

    return NodeH.newBuilder().setName(name).setValue(value).build();
  }

  private static void printStatistics(List<Long> latenciesNanos, int messageSize) {
    if (latenciesNanos.isEmpty()) {
      System.out.println("No data collected");
      return;
    }

    // Convert to per-operation microseconds
    List<Double> latenciesUs = new ArrayList<>(latenciesNanos.size());
    for (long nanos : latenciesNanos) {
      latenciesUs.add(nanos / (double) MEASUREMENT_ITERATIONS / 1_000.0);
    }

    latenciesUs.sort(Double::compareTo);

    double min = latenciesUs.get(0);
    double max = latenciesUs.get(latenciesUs.size() - 1);
    double median = percentile(latenciesUs, 50);
    double p95 = percentile(latenciesUs, 95);
    double p99 = percentile(latenciesUs, 99);
    double mean = latenciesUs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    // Calculate standard deviation
    double variance = latenciesUs.stream()
        .mapToDouble(d -> Math.pow(d - mean, 2))
        .average()
        .orElse(0.0);
    double stdDev = Math.sqrt(variance);

    // Calculate throughput
    double opsPerSecond = 1_000_000.0 / mean; // μs to ops/sec
    double mbPerSecond = (opsPerSecond * messageSize) / (1024.0 * 1024.0);

    System.out.println("\n=== Benchmark Results ===");
    System.out.println("\nLatency per operation (μs):");
    System.out.printf("  Min:    %8.2f μs%n", min);
    System.out.printf("  Mean:   %8.2f μs%n", mean);
    System.out.printf("  Median: %8.2f μs%n", median);
    System.out.printf("  P95:    %8.2f μs%n", p95);
    System.out.printf("  P99:    %8.2f μs%n", p99);
    System.out.printf("  Max:    %8.2f μs%n", max);
    System.out.printf("  StdDev: %8.2f μs%n", stdDev);

    System.out.println("\nThroughput:");
    System.out.printf("  Operations/sec: %,.0f ops/s%n", opsPerSecond);
    System.out.printf("  Throughput:     %.2f MB/s%n", mbPerSecond);

    System.out.println("\nConfiguration:");
    System.out.printf("  Warmup iterations:      %,d%n", WARMUP_ITERATIONS);
    System.out.printf("  Measurement iterations: %,d per round%n", MEASUREMENT_ITERATIONS);
    System.out.printf("  Benchmark rounds:       %d%n", BENCHMARK_ROUNDS);
    System.out.printf("  Total operations:       %,d%n", BENCHMARK_ROUNDS * MEASUREMENT_ITERATIONS);
    System.out.printf("  Message size:           %,d bytes%n", messageSize);
  }

  private static double percentile(List<Double> sorted, int percentile) {
    int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
    index = Math.max(0, Math.min(index, sorted.size() - 1));
    return sorted.get(index);
  }
}
