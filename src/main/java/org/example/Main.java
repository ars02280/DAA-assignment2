package org.example;

import benchmark.Benchmark;
import benchmark.BuildHeapBenchmark;
import benchmark.CsvWriter;
import benchmark.MemoryBenchmark;
import benchmark.Result;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point. One command runs the whole benchmark and writes the CSV files:
 * <pre>
 *     mvn compile exec:java            (optional argument: output directory, default "results")
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");

        System.out.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");
        List<Result> results = Benchmark.runAll(
                Benchmark.SIZES, Benchmark.WARMUP_RUNS, Benchmark.MEASURED_RUNS, System.out::println);
        CsvWriter.write(outDir.resolve("results.csv"), results);

        System.out.println("Written: " + outDir.resolve("results.csv"));

        // Bonus B: n x insert vs Floyd's buildHeap.
        System.out.println("\n# Bonus B: insert vs buildHeap");
        List<Result> buildHeap = BuildHeapBenchmark.run(
                Benchmark.SIZES, Benchmark.WARMUP_RUNS, Benchmark.MEASURED_RUNS, System.out::println);
        CsvWriter.write(outDir.resolve("buildheap.csv"), buildHeap);

        // Bonus A: memory footprint (JOL if available).
        System.out.println("\n# Bonus A: memory footprint");
        MemoryBenchmark.write(outDir.resolve("memory.csv"), MemoryBenchmark.run(Benchmark.SIZES, System.out::println));

        System.out.println("\nDone. Plots: python3 scripts/plot.py");
    }
}
