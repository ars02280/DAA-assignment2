package dataStr;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of Floyd's bottom-up {@link MinHeap#buildHeap(int[])} (bonus task B). */
class MinHeapBuildHeapTest {

    @Test
    void emptyAndSingleElementArrays() {
        MinHeap empty = MinHeap.buildHeap(new int[0]);
        assertTrue(empty.isEmpty());
        assertTrue(empty.isValidHeap());

        MinHeap one = MinHeap.buildHeap(new int[]{9});
        assertEquals(9, one.peekMin());
        assertTrue(one.isValidHeap());
    }

    @Test
    void buildsAValidHeapAndKeepsAllValues() {
        for (int n : new int[]{2, 3, 7, 100, 1000, 12345}) {
            Random rnd = new Random(42);
            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = rnd.nextInt(1000);          // many duplicates
            }
            int[] original = values.clone();

            MinHeap heap = MinHeap.buildHeap(values);
            assertTrue(heap.isValidHeap());
            assertArrayEquals(original, values, "input array must not be modified");

            int[] sorted = original.clone();
            Arrays.sort(sorted);
            for (int i = 0; i < n; i++) {
                assertEquals(sorted[i], heap.extractMin());
            }
        }
    }

    @Test
    void usesAtMostTwoComparisonsPerElement() {
        int n = 50_000;
        Random rnd = new Random(42);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt();
        }
        MinHeap heap = MinHeap.buildHeap(values);
        assertTrue(heap.getComparisons() <= 2L * n, "Floyd's method needs O(n) comparisons: " + heap.getComparisons());
    }

    @Test
    void beatsRepeatedInsertOnWorstCaseInput() {
        int n = 20_000;
        int[] descending = new int[n];
        for (int i = 0; i < n; i++) {
            descending[i] = n - i;                      // every insert bubbles all the way to the root
        }
        MinHeap inserted = new MinHeap();
        for (int v : descending) {
            inserted.insert(v);
        }
        MinHeap built = MinHeap.buildHeap(descending);
        assertTrue(built.getComparisons() < inserted.getComparisons(),
                "buildHeap " + built.getComparisons() + " vs inserts " + inserted.getComparisons());
    }
}
