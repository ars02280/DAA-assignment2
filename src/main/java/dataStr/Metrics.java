package dataStr;

/**
 * Counters for the physical operations executed inside a data structure.
 * <p>
 * The structures increment these fields <b>inside</b> their own methods, so the
 * numbers are exact and are never estimated afterwards.
 * <ul>
 *   <li><b>steps</b> - one read of an array cell, or one move to the next node of a list;</li>
 *   <li><b>moves</b> - one element shifted/stored inside an array, or one link (pointer) update in a list;</li>
 *   <li><b>comparisons</b> - one comparison of two elements.</li>
 * </ul>
 */
public final class Metrics {
    // Package-private on purpose: only the structures in this package may increment them.
    long steps;
    long moves;
    long comparisons;

    public long getSteps() {
        return steps;
    }

    public long getMoves() {
        return moves;
    }

    public long getComparisons() {
        return comparisons;
    }

    /** Sets all three counters back to zero. */
    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }

    @Override
    public String toString() {
        return "Metrics{steps=" + steps + ", moves=" + moves + ", comparisons=" + comparisons + '}';
    }
}
