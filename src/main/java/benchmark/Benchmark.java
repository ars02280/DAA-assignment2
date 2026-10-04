package benchmark;

import dataStr.DynamicArray;
import dataStr.IntList;
import dataStr.Measurable;
import dataStr.MinHeap;
import dataStr.MyLinkedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The four workloads of the assignment. Every case is executed
 * {@code warmups + runs} times, the first {@code warmups} runs are discarded (JIT warm-up)
 * and the median of the remaining {@code runs} timings is reported.
 * <p>
 * Reproducibility: all random data comes from {@code new Random(42)} and is generated once per
 * workload and size, so both structures of a workload receive exactly the same data.
 * The timed part never includes filling the structure, and the counters are reset after filling.
 */
public final class Benchmark {

    public static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    public static final int WARMUP_RUNS = 3;
    public static final int MEASURED_RUNS = 5;
    public static final long SEED = 42;

    static final int ACCESSES = 10_000;      // W1: get(index) calls
    static final int SEARCHES = 1_000;       // W2: contains(x) queries
    static final int EDITS = 1_000;          // W3: insertions, and the same number of removals
    static final int VALUE_RANGE = 1_000_000;

    static final String ARRAY = "DynamicArray";
    static final String LIST = "MyLinkedList";
    static final String HEAP = "MinHeap";

    /** Keeps the JIT from removing the measured code because its result is never used. */
    private static volatile long sink;

    private Benchmark() {
    }

    /** A prepared case: {@link #run()} is the timed part and returns the structure that holds the counters. */
    @FunctionalInterface
    interface Trial {
        Measurable run();
    }

    // ------------------------------------------------------------------ driver

    public static List<Result> runAll(int[] sizes, int warmups, int runs, Consumer<String> log) {
        if (warmups > 0) {
            globalWarmup();
        }
        List<Result> results = new ArrayList<>();
        for (int n : sizes) {
            results.addAll(w1RandomAccess(n, warmups, runs, log));
        }
        for (int n : sizes) {
            results.addAll(w2Search(n, warmups, runs, log));
        }
        for (int n : sizes) {
            results.addAll(w3InsertRemove(n, warmups, runs, log));
        }
        for (int n : sizes) {
            results.add(w4PriorityProcessing(n, warmups, runs, log));
        }
        return results;
    }

    /**
     * Executes every workload once on a mid-size input and throws the results away, so that the JVM
     * has already compiled all hot methods before the first measured case (otherwise n = 100,
     * which runs first, would look slower than n = 1 000).
     */
    static void globalWarmup() {
        for (int round = 0; round < 3; round++) {
            w1RandomAccess(1_000, 2, 2, null);
            w2Search(1_000, 2, 2, null);
            w3InsertRemove(1_000, 2, 2, null);
            w4PriorityProcessing(1_000, 2, 2, null);
        }
    }

    // ------------------------------------------------------------------ W1

    /** W1 - Random Access: fill with n values, then 10 000 get(index) calls with a random index. */
    static List<Result> w1RandomAccess(int n, int warmups, int runs, Consumer<String> log) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        int[] indexes = new int[ACCESSES];
        for (int i = 0; i < ACCESSES; i++) {
            indexes[i] = rnd.nextInt(n);
        }
        List<Result> out = new ArrayList<>();
        for (String structure : new String[]{ARRAY, LIST}) {
            out.add(measure("W1", "-", structure, n, warmups, runs, log, () -> {
                IntList list = filled(structure, values);
                return () -> {
                    long sum = 0;
                    for (int index : indexes) {
                        sum += list.get(index);
                    }
                    sink += sum;
                    return list;
                };
            }));
        }
        return out;
    }

    // ------------------------------------------------------------------ W2

    /** W2 - Search: 1 000 contains(x) queries, half of the values are present and half are absent. */
    static List<Result> w2Search(int n, int warmups, int runs, Consumer<String> log) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        // Stored values are >= 0, so negative queries are guaranteed to be absent.
        int[] queries = new int[SEARCHES];
        for (int i = 0; i < SEARCHES; i++) {
            queries[i] = (i % 2 == 0) ? values[rnd.nextInt(n)] : -1 - rnd.nextInt(VALUE_RANGE);
        }
        shuffle(queries, rnd);
        List<Result> out = new ArrayList<>();
        for (String structure : new String[]{ARRAY, LIST}) {
            out.add(measure("W2", "-", structure, n, warmups, runs, log, () -> {
                IntList list = filled(structure, values);
                return () -> {
                    int hits = 0;
                    for (int q : queries) {
                        if (list.contains(q)) {
                            hits++;
                        }
                    }
                    if (hits != SEARCHES / 2) {
                        throw new IllegalStateException("W2: expected " + SEARCHES / 2 + " hits but got " + hits);
                    }
                    sink += hits;
                    return list;
                };
            }));
        }
        return out;
    }

    // ------------------------------------------------------------------ W3

    /**
     * W3 - Insert &amp; Remove: 1 000 insertions followed by 1 000 removals, at index 0 ("head")
     * or at index n / 2 ("middle"). The index is fixed, so the size is back to n at the end.
     */
    static List<Result> w3InsertRemove(int n, int warmups, int runs, Consumer<String> log) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        int[] inserts = randomValues(rnd, EDITS);
        List<Result> out = new ArrayList<>();
        for (String variant : new String[]{"head", "middle"}) {
            int index = variant.equals("head") ? 0 : n / 2;
            for (String structure : new String[]{ARRAY, LIST}) {
                out.add(measure("W3", variant, structure, n, warmups, runs, log, () -> {
                    IntList list = filled(structure, values);
                    return () -> {
                        for (int x : inserts) {
                            list.add(index, x);
                        }
                        long sum = 0;
                        for (int i = 0; i < EDITS; i++) {
                            sum += list.remove(index);
                        }
                        if (list.size() != n) {
                            throw new IllegalStateException("W3: size " + list.size() + " instead of " + n);
                        }
                        sink += sum;
                        return list;
                    };
                }));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ W4

    /** W4 - Priority Processing: insert n values, call extractMin() n times, check the order. */
    static Result w4PriorityProcessing(int n, int warmups, int runs, Consumer<String> log) {
        int[] values = randomValues(new Random(SEED), n);
        return measure("W4", "-", HEAP, n, warmups, runs, log, () -> {
            MinHeap heap = new MinHeap();
            return () -> {
                for (int v : values) {
                    heap.insert(v);
                }
                int previous = Integer.MIN_VALUE;
                long sum = 0;
                for (int i = 0; i < n; i++) {
                    int x = heap.extractMin();
                    if (x < previous) {
                        throw new IllegalStateException("W4: output is not sorted");
                    }
                    previous = x;
                    sum += x;
                }
                sink += sum;
                return heap;
            };
        });
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Runs one case {@code warmups + runs} times. {@code setup} builds a fresh, filled structure
     * with zeroed counters and returns the timed part. Returns the median time and the (deterministic) counters.
     */
    static Result measure(String workload, String variant, String structure, int n,
                          int warmups, int runs, Consumer<String> log, Supplier<Trial> setup) {
        long[] times = new long[runs];
        Measurable last = null;
        for (int run = 0; run < warmups + runs; run++) {
            Trial trial = setup.get();                  // not timed
            long start = System.nanoTime();
            last = trial.run();                         // timed
            long elapsed = System.nanoTime() - start;
            if (run >= warmups) {
                times[run - warmups] = elapsed;
            }
        }
        Result result = new Result(workload, variant, structure, n, medianMs(times),
                last.getSteps(), last.getMoves(), last.getComparisons());
        if (log != null) {
            log.accept(result.toCsvRow());
        }
        return result;
    }

    static double medianMs(long[] nanos) {
        long[] sorted = nanos.clone();
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        double median = (sorted.length % 2 == 1) ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2.0;
        return median / 1_000_000.0;
    }

    static int[] randomValues(Random rnd, int count) {
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = rnd.nextInt(VALUE_RANGE);       // non-negative
        }
        return values;
    }

    /** Creates the structure, fills it with {@code values} and resets the counters. */
    static IntList filled(String structure, int[] values) {
        IntList list = structure.equals(ARRAY) ? new DynamicArray() : new MyLinkedList();
        for (int v : values) {
            list.add(v);
        }
        list.resetMetrics();
        return list;
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }
}
