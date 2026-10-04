package benchmark;

/**
 * One row of {@code results.csv}.
 *
 * @param workload    W1..W4 (or BONUS_B for the buildHeap comparison)
 * @param variant     {@code head} / {@code middle} for W3, {@code -} otherwise
 * @param structure   DynamicArray, MyLinkedList or MinHeap
 * @param n           number of elements the structure was filled with
 * @param timeMs      median wall-clock time of the measured part, in milliseconds
 * @param steps       exact number of steps counted inside the structure
 * @param moves       exact number of moves counted inside the structure
 * @param comparisons exact number of comparisons counted inside the structure
 */
public record Result(String workload, String variant, String structure, int n,
                     double timeMs, long steps, long moves, long comparisons) {

    public static final String CSV_HEADER = "workload,variant,structure,n,time_ms,steps,moves,comparisons";

    public String toCsvRow() {
        return String.format(java.util.Locale.ROOT, "%s,%s,%s,%d,%.6f,%d,%d,%d",
                workload, variant, structure, n, timeMs, steps, moves, comparisons);
    }
}
