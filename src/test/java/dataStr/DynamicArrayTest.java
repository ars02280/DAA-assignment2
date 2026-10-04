package dataStr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Edge cases and counter checks for {@link DynamicArray}. */
class DynamicArrayTest {

    private static DynamicArray of(int... values) {
        DynamicArray a = new DynamicArray();
        for (int v : values) {
            a.add(v);
        }
        return a;
    }

    // ---------- edge cases ----------

    @Test
    void emptyArray() {
        DynamicArray a = new DynamicArray();
        assertEquals(0, a.size());
        assertFalse(a.contains(1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(0));
    }

    @Test
    void singleElement() {
        DynamicArray a = of(7);
        assertEquals(1, a.size());
        assertEquals(7, a.get(0));
        assertTrue(a.contains(7));
        assertEquals(7, a.remove(0));
        assertEquals(0, a.size());
        assertFalse(a.contains(7));
    }

    @Test
    void duplicateValues() {
        DynamicArray a = of(5, 5, 5, 1, 5);
        assertTrue(a.contains(5));
        assertEquals(5, a.remove(0));
        assertEquals(5, a.remove(3));          // after the first removal the array is [5, 5, 1, 5]
        assertArrayEquals(new int[]{5, 5, 1}, a.toArray());
    }

    @Test
    void firstAndLastIndex() {
        DynamicArray a = of(10, 20, 30);
        assertEquals(10, a.get(0));
        assertEquals(30, a.get(2));
        assertEquals(30, a.remove(2));
        assertEquals(10, a.remove(0));
        assertArrayEquals(new int[]{20}, a.toArray());
    }

    @Test
    void insertAtBoundaries() {
        DynamicArray a = new DynamicArray();
        a.add(0, 2);                           // into an empty array: index == size is allowed
        a.add(0, 1);                           // head
        a.add(2, 3);                           // tail (index == size)
        a.add(1, 99);                          // middle
        assertArrayEquals(new int[]{1, 99, 2, 3}, a.toArray());
    }

    @Test
    void invalidIndexThrows() {
        DynamicArray a = of(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(3));
        assertThrows(IndexOutOfBoundsException.class, () -> a.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> a.add(4, 0));
        assertArrayEquals(new int[]{1, 2, 3}, a.toArray());   // failed calls must not change anything
    }

    @Test
    void invalidIndexDoesNotTouchCounters() {
        DynamicArray a = of(1, 2, 3);
        a.resetMetrics();
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(10));
        assertEquals(0, a.getSteps());
        assertEquals(0, a.getMoves());
        assertEquals(0, a.getComparisons());
    }

    @Test
    void invalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicArray(-1));
    }

    @Test
    void growsByFactorTwoWithoutLosingData() {
        DynamicArray a = new DynamicArray(1);
        for (int i = 0; i < 1000; i++) {
            a.add(i);
        }
        assertEquals(1000, a.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, a.get(i));
        }
    }

    @Test
    void zeroCapacityStillWorks() {
        DynamicArray a = new DynamicArray(0);
        a.add(1);
        a.add(2);
        a.add(3);
        assertArrayEquals(new int[]{1, 2, 3}, a.toArray());
    }

    // ---------- counters ----------

    @Test
    void appendCountsOneMoveAndGrowthCopiesEveryElement() {
        DynamicArray a = new DynamicArray(1);
        a.add(1);                              // fits: 1 move
        assertEquals(1, a.getMoves());
        a.add(2);                              // full -> grow copies 1 element (1 step + 1 move), then stores (1 move)
        assertEquals(1, a.getSteps());
        assertEquals(3, a.getMoves());
        assertEquals(0, a.getComparisons());
    }

    @Test
    void getCostsExactlyOneStep() {
        DynamicArray a = of(1, 2, 3, 4, 5);
        a.resetMetrics();
        a.get(0);
        a.get(4);
        assertEquals(2, a.getSteps());
        assertEquals(0, a.getMoves());
        assertEquals(0, a.getComparisons());
    }

    @Test
    void containsCountsStepAndComparisonPerVisitedCell() {
        DynamicArray a = of(1, 2, 3, 4, 5);
        a.resetMetrics();
        assertTrue(a.contains(3));             // stops at index 2 -> 3 cells visited
        assertEquals(3, a.getSteps());
        assertEquals(3, a.getComparisons());
        a.resetMetrics();
        assertFalse(a.contains(42));           // visits all 5 cells
        assertEquals(5, a.getSteps());
        assertEquals(5, a.getComparisons());
        assertEquals(0, a.getMoves());
    }

    @Test
    void removeCountsTheShift() {
        DynamicArray a = of(1, 2, 3, 4, 5);
        a.resetMetrics();
        a.remove(1);                           // shifts 3 elements, plus 1 read of the removed cell
        assertEquals(3, a.getMoves());
        assertEquals(4, a.getSteps());
    }

    @Test
    void removeLastShiftsNothing() {
        DynamicArray a = of(1, 2, 3);
        a.resetMetrics();
        a.remove(2);
        assertEquals(0, a.getMoves());
        assertEquals(1, a.getSteps());
    }

    @Test
    void insertAtHeadShiftsEverything() {
        DynamicArray a = of(1, 2, 3, 4);
        a.resetMetrics();
        a.add(0, 9);                           // 4 shifts + 1 store
        assertEquals(5, a.getMoves());
        assertEquals(4, a.getSteps());
    }

    @Test
    void insertAtTailShiftsNothing() {
        DynamicArray a = of(1, 2, 3, 4);
        a.resetMetrics();
        a.add(4, 9);                           // only the store
        assertEquals(1, a.getMoves());
        assertEquals(0, a.getSteps());
    }

    @Test
    void resetMetricsClearsEverything() {
        DynamicArray a = of(1, 2, 3);
        a.contains(3);
        a.resetMetrics();
        assertEquals(0, a.getSteps());
        assertEquals(0, a.getMoves());
        assertEquals(0, a.getComparisons());
    }
}
