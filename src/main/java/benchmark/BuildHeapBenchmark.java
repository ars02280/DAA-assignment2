package benchmark;

import dataStr.MinHeap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Bonus task B: n separate {@code insert(x)} calls (O(n log n)) versus Floyd's bottom-up
 * {@link MinHeap#buildHeap(int[])} (O(n)) on the same data.
 * <ul>
 *   <li>workload {@code BONUS_B}: random data from {@code new Random(42)};</li>
 *   <li>workload {@code BONUS_B_DESC}: strictly decreasing data, the worst case for repeated
 *       insert (every new element bubbles up to the root).</li>
 * </ul>
 * The variant column is {@code insert} or {@code buildHeap}.
 */
public final class BuildHeapBenchmark {

    static final String WORKLOAD = "BONUS_B";
    static final String WORKLOAD_DESC = "BONUS_B_DESC";

    private static volatile long sink;

    private BuildHeapBenchmark() {
    }

    public static List<Result> run(int[] sizes, int warmups, int runs, Consumer<String> log) {
        List<Result> results = new ArrayList<>();
        for (int n : sizes) {
            results.addAll(compare(WORKLOAD, Benchmark.randomValues(new Random(Benchmark.SEED), n),
                    warmups, runs, log));
        }
        for (int n : sizes) {
            int[] descending = new int[n];
            for (int i = 0; i < n; i++) {
                descending[i] = n - i;
            }
            results.addAll(compare(WORKLOAD_DESC, descending, warmups, runs, log));
        }
        return results;
    }

    private static List<Result> compare(String workload, int[] values, int warmups, int runs, Consumer<String> log) {
        int n = values.length;
        List<Result> out = new ArrayList<>();

        out.add(Benchmark.measure(workload, "insert", Benchmark.HEAP, n, warmups, runs, log, () -> {
            MinHeap heap = new MinHeap();
            return () -> {
                for (int v : values) {
                    heap.insert(v);
                }
                sink += heap.peekMin();
                return heap;
            };
        }));

        out.add(Benchmark.measure(workload, "buildHeap", Benchmark.HEAP, n, warmups, runs, log, () -> () -> {
            MinHeap heap = MinHeap.buildHeap(values);
            sink += heap.peekMin();
            return heap;
        }));
        return out;
    }
}
