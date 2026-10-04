package dataStr;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Random tests of {@link MinHeap} against java.util.PriorityQueue (allowed in tests only). */
class MinHeapRandomTest {

    @Test
    void heapPropertyHoldsAfterEveryInsertAndExtractMin() {
        for (long seed = 1; seed <= 20; seed++) {
            Random rnd = new Random(seed);
            MinHeap mine = new MinHeap(1);
            PriorityQueue<Integer> expected = new PriorityQueue<>();

            for (int step = 0; step < 1500; step++) {
                if (expected.isEmpty() || rnd.nextInt(3) != 0) {
                    int value = rnd.nextInt(100) - 50;      // duplicates and negative values
                    mine.insert(value);
                    expected.add(value);
                } else {
                    assertEquals(expected.poll().intValue(), mine.extractMin());
                }
                assertTrue(mine.isValidHeap(), "heap property broken, seed " + seed + ", step " + step);
                assertEquals(expected.size(), mine.size());
                if (!expected.isEmpty()) {
                    assertEquals(expected.peek().intValue(), mine.peekMin());
                }
            }
        }
    }

    @Test
    void nExtractMinCallsReturnSortedOutput() {
        for (int n : new int[]{1, 2, 10, 100, 1000, 10000}) {
            Random rnd = new Random(42);
            int[] values = new int[n];
            MinHeap heap = new MinHeap();
            for (int i = 0; i < n; i++) {
                values[i] = rnd.nextInt();
                heap.insert(values[i]);
            }
            int[] sorted = values.clone();
            Arrays.sort(sorted);

            int previous = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                int x = heap.extractMin();
                assertTrue(x >= previous, "output must be non-decreasing");
                assertEquals(sorted[i], x);
                previous = x;
            }
            assertTrue(heap.isEmpty());
        }
    }

    @Test
    void sortedAndReverseSortedInput() {
        int n = 2000;
        MinHeap asc = new MinHeap();
        MinHeap desc = new MinHeap();
        for (int i = 0; i < n; i++) {
            asc.insert(i);
            desc.insert(n - i);
            assertTrue(asc.isValidHeap());
            assertTrue(desc.isValidHeap());
        }
        for (int i = 0; i < n; i++) {
            assertEquals(i, asc.extractMin());
            assertEquals(i + 1, desc.extractMin());
        }
    }
}
