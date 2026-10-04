package dataStr;

/** A structure that counts its own physical operations (see {@link Metrics}). */
public interface Measurable {

    /** The live counters of this structure. */
    Metrics metrics();

    default long getSteps() {
        return metrics().getSteps();
    }

    default long getMoves() {
        return metrics().getMoves();
    }

    default long getComparisons() {
        return metrics().getComparisons();
    }

    /** Resets the counters, e.g. after filling the structure and before the measured workload. */
    default void resetMetrics() {
        metrics().reset();
    }
}
