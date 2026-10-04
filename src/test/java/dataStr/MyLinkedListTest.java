package dataStr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Edge cases and counter checks for {@link MyLinkedList}. */
class MyLinkedListTest {

    private static MyLinkedList of(int... values) {
        MyLinkedList list = new MyLinkedList();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    // ---------- edge cases ----------

    @Test
    void emptyList() {
        MyLinkedList list = new MyLinkedList();
        assertEquals(0, list.size());
        assertFalse(list.contains(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void singleElement() {
        MyLinkedList list = of(7);
        assertEquals(7, list.get(0));
        assertTrue(list.contains(7));
        assertEquals(7, list.remove(0));
        assertEquals(0, list.size());
        list.add(8);                           // the tail pointer must be valid again
        assertArrayEquals(new int[]{8}, list.toArray());
    }

    @Test
    void duplicateValues() {
        MyLinkedList list = of(5, 5, 5, 1, 5);
        assertTrue(list.contains(5));
        assertEquals(5, list.remove(0));
        assertEquals(5, list.remove(3));       // after the first removal the list is [5, 5, 1, 5]
        assertArrayEquals(new int[]{5, 5, 1}, list.toArray());
    }

    @Test
    void firstAndLastIndex() {
        MyLinkedList list = of(10, 20, 30);
        assertEquals(10, list.get(0));
        assertEquals(30, list.get(2));
        assertEquals(30, list.remove(2));
        list.add(40);                          // append after removing the last node (tail must be updated)
        assertArrayEquals(new int[]{10, 20, 40}, list.toArray());
        assertEquals(10, list.remove(0));
        assertArrayEquals(new int[]{20, 40}, list.toArray());
    }

    @Test
    void insertAtBoundaries() {
        MyLinkedList list = new MyLinkedList();
        list.add(0, 2);                        // into an empty list
        list.add(0, 1);                        // head
        list.add(2, 3);                        // tail (index == size)
        list.add(1, 99);                       // middle
        list.add(4);                           // append after an insertion at the tail
        assertArrayEquals(new int[]{1, 99, 2, 3, 4}, list.toArray());
    }

    @Test
    void invalidIndexThrows() {
        MyLinkedList list = of(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(3));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(4, 0));
        assertArrayEquals(new int[]{1, 2, 3}, list.toArray());   // failed calls must not change anything
    }

    // ---------- counters ----------

    @Test
    void appendCostsTwoLinkUpdatesAndNoSteps() {
        MyLinkedList list = new MyLinkedList();
        list.add(1);                           // head = tail = node
        assertEquals(2, list.getMoves());
        list.add(2);                           // tail.next = node; tail = node
        assertEquals(4, list.getMoves());
        assertEquals(0, list.getSteps());
        assertEquals(0, list.getComparisons());
    }

    @Test
    void getCostsIndexSteps() {
        MyLinkedList list = of(1, 2, 3, 4, 5);
        list.resetMetrics();
        list.get(0);
        assertEquals(0, list.getSteps());
        list.get(4);
        assertEquals(4, list.getSteps());
        assertEquals(0, list.getMoves());
        assertEquals(0, list.getComparisons());
    }

    @Test
    void containsCountsComparisonsAndHops() {
        MyLinkedList list = of(1, 2, 3, 4, 5);
        list.resetMetrics();
        assertTrue(list.contains(3));          // 3 nodes compared, 2 hops
        assertEquals(3, list.getComparisons());
        assertEquals(2, list.getSteps());
        list.resetMetrics();
        assertFalse(list.contains(42));        // 5 nodes compared, 4 hops
        assertEquals(5, list.getComparisons());
        assertEquals(4, list.getSteps());
        assertEquals(0, list.getMoves());
    }

    @Test
    void insertAtHeadIsConstantTime() {
        MyLinkedList list = of(1, 2, 3, 4);
        list.resetMetrics();
        list.add(0, 9);
        assertEquals(2, list.getMoves());
        assertEquals(0, list.getSteps());
    }

    @Test
    void insertInTheMiddleWalksToThePredecessor() {
        MyLinkedList list = of(1, 2, 3, 4, 5, 6);
        list.resetMetrics();
        list.add(3, 9);                        // hops to node #2 (2 steps), then 2 link updates
        assertEquals(2, list.getSteps());
        assertEquals(2, list.getMoves());
    }

    @Test
    void insertAtTailIsConstantTime() {
        MyLinkedList list = of(1, 2, 3, 4);
        list.resetMetrics();
        list.add(4, 9);
        assertEquals(0, list.getSteps());
        assertEquals(2, list.getMoves());
    }

    @Test
    void removeAtHeadCostsOneLinkUpdate() {
        MyLinkedList list = of(1, 2, 3);
        list.resetMetrics();
        list.remove(0);
        assertEquals(1, list.getMoves());
        assertEquals(0, list.getSteps());
    }

    @Test
    void removeInTheMiddleWalksToThePredecessor() {
        MyLinkedList list = of(1, 2, 3, 4, 5, 6);
        list.resetMetrics();
        list.remove(3);                        // hops to node #2 (2 steps), then 1 link update
        assertEquals(2, list.getSteps());
        assertEquals(1, list.getMoves());
    }

    @Test
    void removeLastAlsoUpdatesTail() {
        MyLinkedList list = of(1, 2, 3);
        list.resetMetrics();
        list.remove(2);                        // 1 hop, prev.next = null, tail = prev
        assertEquals(1, list.getSteps());
        assertEquals(2, list.getMoves());
    }

    @Test
    void invalidIndexDoesNotTouchCounters() {
        MyLinkedList list = of(1, 2, 3);
        list.resetMetrics();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(10));
        assertEquals(0, list.getSteps());
        assertEquals(0, list.getMoves());
        assertEquals(0, list.getComparisons());
    }
}
