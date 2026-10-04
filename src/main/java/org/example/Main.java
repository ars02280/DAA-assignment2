package org.example;

import benchmark.Benchmark;
import benchmark.CsvWriter;
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
    }
}
