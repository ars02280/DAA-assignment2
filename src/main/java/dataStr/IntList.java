package dataStr;

/**
 * Common interface of {@link DynamicArray} and {@link MyLinkedList}, so that
 * one benchmark can run both structures through exactly the same code.
 * Only primitive {@code int} values are stored (no generics, no boxing).
 */
public interface IntList extends Measurable {

    /** Appends {@code x} to the end of the list. */
    void add(int x);

    /**
     * Inserts {@code x} so that it gets the position {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index > size()}
     */
    void add(int index, int x);

    /**
     * Removes and returns the element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}
     */
    int remove(int index);

    /**
     * Returns the element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}
     */
    int get(int index);

    /** Linear search for {@code x}. */
    boolean contains(int x);

    /** Number of stored elements. */
    int size();

    /** Copy of the content as an array. Not counted in the metrics (meant for tests and debugging). */
    int[] toArray();
}
