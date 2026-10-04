package benchmark;

import dataStr.DynamicArray;
import dataStr.MinHeap;
import dataStr.MyLinkedList;

import java.lang.ref.Reference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Bonus task A: how many bytes do n elements really take in each structure?
 * <p>
 * The size of the whole object graph is measured with JOL (Java Object Layout). JOL is called by
 * reflection so that the project still builds and runs if the jar is missing or unusable on the
 * running JDK; in that case the size is estimated from the heap usage before and after building the
 * structure (column {@code method} says which one was used).
 */
public final class MemoryBenchmark {

    public static final String CSV_HEADER = "structure,n,bytes,mb,method";

    private MemoryBenchmark() {
    }

    /** One row of memory.csv. */
    public record Row(String structure, int n, long bytes, String method) {
        public String toCsvRow() {
            return String.format(Locale.ROOT, "%s,%d,%d,%.6f,%s", structure, n, bytes, bytes / (1024.0 * 1024.0), method);
        }
    }

    public static List<Row> run(int[] sizes, Consumer<String> log) {
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            int[] values = Benchmark.randomValues(new Random(Benchmark.SEED), n);
            rows.add(measure(Benchmark.ARRAY, n, () -> {
                DynamicArray a = new DynamicArray();
                for (int v : values) {
                    a.add(v);
                }
                return a;
            }, log));
            rows.add(measure(Benchmark.LIST, n, () -> {
                MyLinkedList l = new MyLinkedList();
                for (int v : values) {
                    l.add(v);
                }
                return l;
            }, log));
            rows.add(measure(Benchmark.HEAP, n, () -> {
                MinHeap h = new MinHeap();
                for (int v : values) {
                    h.insert(v);
                }
                return h;
            }, log));
        }
        return rows;
    }

    public static void write(Path file, List<Row> rows) {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Row r : rows) {
            lines.add(r.toCsvRow());
        }
        CsvWriter.writeLines(file, lines);
    }

    private static Row measure(String structure, int n, Supplier<Object> builder, Consumer<String> log) {
        Row row;
        try {
            Object built = builder.get();
            row = new Row(structure, n, jolTotalSize(built), "jol");
            Reference.reachabilityFence(built);
        } catch (Throwable jolUnavailable) {
            row = new Row(structure, n, heapDelta(builder), "heap-delta");
        }
        if (log != null) {
            log.accept(row.toCsvRow());
        }
        return row;
    }

    /** {@code GraphLayout.parseInstance(root).totalSize()}, called by reflection. */
    private static long jolTotalSize(Object root) throws Exception {
        Class<?> graphLayout = Class.forName("org.openjdk.jol.info.GraphLayout");
        Object layout = graphLayout.getMethod("parseInstance", Object[].class)
                .invoke(null, (Object) new Object[]{root});
        return (Long) graphLayout.getMethod("totalSize").invoke(layout);
    }

    /** Fallback: live heap before/after building the structure, median of 5 attempts. */
    private static long heapDelta(Supplier<Object> builder) {
        long[] deltas = new long[5];
        for (int i = 0; i < deltas.length; i++) {
            long before = usedHeap();
            Object built = builder.get();
            long after = usedHeap();
            Reference.reachabilityFence(built);
            deltas[i] = after - before;
        }
        Arrays.sort(deltas);
        return Math.max(0, deltas[deltas.length / 2]);
    }

    private static long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
        return rt.totalMemory() - rt.freeMemory();
    }
}
