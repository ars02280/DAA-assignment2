package dataStr;

import benchmark.Benchmark;
import benchmark.BuildHeapBenchmark;
import benchmark.CsvWriter;
import benchmark.MemoryBenchmark;
import benchmark.Result;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the benchmark with tiny parameters: checks the CSV layout and that the counters are not zero. */
class BenchmarkSmokeTest {

    private static final int[] SIZES = {100, 1000};

    private static Result find(List<Result> all, String workload, String variant, String structure, int n) {
        for (Result r : all) {
            if (r.workload().equals(workload) && r.variant().equals(variant)
                    && r.structure().equals(structure) && r.n() == n) {
                return r;
            }
        }
        throw new AssertionError("missing row " + workload + "/" + variant + "/" + structure + "/" + n);
    }

    @Test
    void csvHasAllRowsAndTheRequiredColumns() throws IOException {
        List<Result> results = Benchmark.runAll(SIZES, 1, 3, null);
        // W1: 2 structures, W2: 2, W3: 2 structures x 2 variants, W4: 1 -> 9 rows per size
        assertEquals(9 * SIZES.length, results.size());

        Path file = Files.createTempFile("results", ".csv");
        CsvWriter.write(file, results);
        List<String> lines = Files.readAllLines(file);
        assertEquals("workload,variant,structure,n,time_ms,steps,moves,comparisons", lines.get(0));
        assertEquals(results.size() + 1, lines.size());
        for (String line : lines.subList(1, lines.size())) {
            assertEquals(8, line.split(",").length, line);
        }
        Files.delete(file);
    }

    @Test
    void variantIsDashExceptForW3() {
        for (Result r : Benchmark.runAll(SIZES, 1, 3, null)) {
            if (r.workload().equals("W3")) {
                assertTrue(r.variant().equals("head") || r.variant().equals("middle"));
            } else {
                assertEquals("-", r.variant());
            }
        }
    }

    @Test
    void countersMatchTheTheory() {
        List<Result> results = Benchmark.runAll(SIZES, 1, 3, null);
        for (int n : SIZES) {
            // W1: one step per get() in the array, about n/2 hops per get() in the list
            assertEquals(10_000, find(results, "W1", "-", "DynamicArray", n).steps());
            assertTrue(find(results, "W1", "-", "MyLinkedList", n).steps() > 10_000L * n / 4);

            // W2: the same number of comparisons in both structures
            assertEquals(find(results, "W2", "-", "DynamicArray", n).comparisons(),
                    find(results, "W2", "-", "MyLinkedList", n).comparisons());

            // W3 head: the array shifts, the list only updates links ("zero counters" must not happen)
            assertTrue(find(results, "W3", "head", "DynamicArray", n).moves() > 1000L * n);
            assertEquals(3000, find(results, "W3", "head", "MyLinkedList", n).moves());
            assertEquals(0, find(results, "W3", "head", "MyLinkedList", n).steps());

            // W3 middle: the list has to walk to the middle
            assertTrue(find(results, "W3", "middle", "MyLinkedList", n).steps() > 1000L * (n / 2 - 1));
            assertTrue(find(results, "W3", "middle", "MyLinkedList", n).moves() > 0);

            // W4: the heap counts everything
            Result heap = find(results, "W4", "-", "MinHeap", n);
            assertTrue(heap.steps() > 0 && heap.moves() > 0 && heap.comparisons() > 0);
        }
        for (Result r : results) {
            assertTrue(r.timeMs() >= 0);
        }
    }

    @Test
    void buildHeapUsesFewerComparisonsOnWorstCaseInput() {
        List<Result> results = BuildHeapBenchmark.run(SIZES, 1, 3, null);
        for (int n : SIZES) {
            long insert = 0;
            long build = 0;
            for (Result r : results) {
                if (r.workload().equals("BONUS_B_DESC") && r.n() == n) {
                    if (r.variant().equals("insert")) {
                        insert = r.comparisons();
                    } else {
                        build = r.comparisons();
                    }
                }
            }
            assertTrue(build > 0 && build < insert, "n=" + n + ": build " + build + " vs insert " + insert);
        }
    }

    @Test
    void memoryBenchmarkReportsGrowingSizes() {
        List<MemoryBenchmark.Row> rows = MemoryBenchmark.run(new int[]{5000, 20000}, null);
        assertEquals(6, rows.size());
        assertFalse(rows.isEmpty());
        for (MemoryBenchmark.Row r : rows) {
            assertTrue(r.bytes() > 0, r.toString());
        }
    }
}
