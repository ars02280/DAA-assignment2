package dataStr;

/**
 * Singly linked list of primitive {@code int} values, written from scratch
 * (no {@code java.util} collections, no boxing).
 * <p>
 * Counting rules: a <b>step</b> is one move to the next node, a <b>move</b> is one
 * link update (every assignment to a {@code next} pointer or to {@code head}),
 * a <b>comparison</b> is one comparison of two elements.
 */
public class MyLinkedList implements IntList {

    private static final class Node {
        final int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public Metrics metrics() {
        return metrics;
    }

    @Override
    public int size() {
        return size;
    }

    /** Appends {@code x}: walks to the last node, so this version is O(n). */
    @Override
    public void add(int x) {
        Node node = new Node(x);
        if (head == null) {
            head = node;
            metrics.moves++;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
                metrics.steps++;
            }
            current.next = node;
            metrics.moves++;
        }
        size++;
    }

    @Override
    public int get(int index) {
        checkElementIndex(index);
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
            metrics.steps++;
        }
        return current.value;
    }

    @Override
    public boolean contains(int x) {
        Node current = head;
        while (current != null) {
            metrics.comparisons++;
            if (current.value == x) {
                return true;
            }
            current = current.next;
            if (current != null) {
                metrics.steps++;
            }
        }
        return false;
    }

    @Override
    public int remove(int index) {
        checkElementIndex(index);
        if (index == 0) {
            int removed = head.value;
            head = head.next;
            metrics.moves++;
            size--;
            return removed;
        }
        Node previous = head;
        for (int i = 0; i < index - 1; i++) {
            previous = previous.next;
            metrics.steps++;
        }
        int removed = previous.next.value;
        previous.next = previous.next.next;
        metrics.moves++;
        size--;
        return removed;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node node = new Node(x);
        if (index == 0) {
            node.next = head;
            head = node;
            metrics.moves += 2;
        } else {
            Node previous = head;
            for (int i = 0; i < index - 1; i++) {
                previous = previous.next;
                metrics.steps++;
            }
            node.next = previous.next;
            previous.next = node;
            metrics.moves += 2;
        }
        size++;
    }

    @Override
    public int[] toArray() {
        int[] copy = new int[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            copy[i] = current.value;
            current = current.next;
        }
        return copy;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
