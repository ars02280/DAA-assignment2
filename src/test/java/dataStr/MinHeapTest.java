package dataStr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Edge cases and counter checks for {@link MinHeap}. */
class MinHeapTest {

    private static MinHeap of(int... values) {
        MinHeap heap = new MinHeap();
        for (int v : values) {
            heap.insert(v);
        }
        return heap;
    }

    // ---------- edge cases ----------

    @Test
    void emptyHeapThrowsIllegalState() {
        MinHeap heap = new MinHeap();
        assertTrue(heap.isEmpty());
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
    }

    @Test
    void heapBecomesEmptyAgainAndThrowsAgain() {
        MinHeap heap = of(4);
        assertEquals(4, heap.extractMin());
        assertTrue(heap.isEmpty());
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertThrows(IllegalStateException.class, heap::peekMin);
    }

    @Test
    void singleElement() {
        MinHeap heap = of(42);
        assertEquals(1, heap.size());
        assertEquals(42, heap.peekMin());
        assertEquals(42, heap.extractMin());
        assertEquals(0, heap.size());
    }

    @Test
    void duplicateValues() {
        MinHeap heap = of(3, 1, 3, 1, 2, 2);
        int[] expected = {1, 1, 2, 2, 3, 3};
        for (int e : expected) {
            assertEquals(e, heap.extractMin());
        }
    }

    @Test
    void extremeValues() {
        MinHeap heap = of(0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
        assertEquals(Integer.MIN_VALUE, heap.extractMin());
        assertEquals(-1, heap.extractMin());
        assertEquals(0, heap.extractMin());
        assertEquals(Integer.MAX_VALUE, heap.extractMin());
    }

    @Test
    void peekDoesNotRemove() {
        MinHeap heap = of(5, 2, 8);
        assertEquals(2, heap.peekMin());
        assertEquals(2, heap.peekMin());
        assertEquals(3, heap.size());
    }

    @Test
    void growsBeyondInitialCapacity() {
        MinHeap heap = new MinHeap(1);
        for (int i = 1000; i > 0; i--) {
            heap.insert(i);
            assertTrue(heap.isValidHeap());
        }
        assertEquals(1000, heap.size());
        assertEquals(1, heap.peekMin());
    }

    @Test
    void invalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MinHeap(-1));
    }

    @Test
    void heapPropertyAfterEveryOperationOnFixedInput() {
        int[] values = {9, 4, 7, 1, 8, 2, 2, 6, 3, 5, 0, 10, -3};
        MinHeap heap = new MinHeap();
        for (int v : values) {
            heap.insert(v);
            assertTrue(heap.isValidHeap(), "after insert " + v);
        }
        while (!heap.isEmpty()) {
            heap.extractMin();
            assertTrue(heap.isValidHeap(), "after extractMin");
        }
    }

    @Test
    void emptyAndSmallHeapsAreValid() {
        assertTrue(new MinHeap().isValidHeap());
        assertTrue(of(3, 1, 2).isValidHeap());
    }

    // ---------- counters ----------

    @Test
    void insertIntoEmptyHeapCostsOneMove() {
        MinHeap heap = new MinHeap();
        heap.insert(5);
        assertEquals(1, heap.getMoves());
        assertEquals(0, heap.getSteps());
        assertEquals(0, heap.getComparisons());
    }

    @Test
    void insertCountsOneComparisonPerLevel() {
        MinHeap heap = of(5);
        heap.resetMetrics();
        heap.insert(3);                        // 1 level: 1 read, 1 comparison, parent moves down, then store
        assertEquals(1, heap.getSteps());
        assertEquals(1, heap.getComparisons());
        assertEquals(2, heap.getMoves());
    }

    @Test
    void insertOfLargeValueStopsImmediately() {
        MinHeap heap = of(1, 2, 3);
        heap.resetMetrics();
        heap.insert(100);                      // parent 2 <= 100: one comparison, no parent moves
        assertEquals(1, heap.getComparisons());
        assertEquals(1, heap.getSteps());
        assertEquals(1, heap.getMoves());
    }

    @Test
    void peekMinCostsOneStep() {
        MinHeap heap = of(3, 1, 2);
        heap.resetMetrics();
        heap.peekMin();
        assertEquals(1, heap.getSteps());
        assertEquals(0, heap.getMoves());
        assertEquals(0, heap.getComparisons());
    }

    @Test
    void extractMinOfSingleElementOnlyReadsIt() {
        MinHeap heap = of(7);
        heap.resetMetrics();
        heap.extractMin();
        assertEquals(1, heap.getSteps());
        assertEquals(0, heap.getMoves());
        assertEquals(0, heap.getComparisons());
    }

    @Test
    void extractMinCountsTheSiftDown() {
        MinHeap heap = of(1, 2, 3);            // array [1, 2, 3]
        heap.resetMetrics();
        assertEquals(1, heap.extractMin());    // last = 3 is sifted down: child 2 < 3 moves up, 3 is stored
        assertEquals(3, heap.getSteps());      // min, last element, left child
        assertEquals(1, heap.getComparisons());
        assertEquals(2, heap.getMoves());
        assertEquals(2, heap.peekMin());
    }

    @Test
    void invalidOperationsDoNotTouchCounters() {
        MinHeap heap = new MinHeap();
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertEquals(0, heap.getSteps());
        assertEquals(0, heap.getMoves());
        assertEquals(0, heap.getComparisons());
    }
}
