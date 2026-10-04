package dataStr;

/**
 * Resizable array of primitive {@code int} values, written from scratch
 * (no {@code java.util} collections, no boxing).
 * <p>
 * When the backing array is full it grows by a factor of 2, which gives
 * amortized O(1) for {@link #add(int)}.
 */
public class DynamicArray implements IntList {

    private static final int DEFAULT_CAPACITY = 10;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Negative capacity: " + initialCapacity);
        }
        // Capacity 0 would never grow with "length * 2", so keep at least one cell.
        this.data = new int[Math.max(1, initialCapacity)];
        this.size = 0;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    @Override
    public int size() {
        return size;
    }

    /** Appends {@code x}; amortized O(1). */
    @Override
    public void add(int x) {
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        metrics.moves++;               // store the new element
        size++;
    }

    /** Doubles the capacity and copies all elements (counted as one step + one move each). */
    private void grow() {
        if (data.length > Integer.MAX_VALUE / 2) {
            throw new IllegalStateException("Capacity overflow");
        }
        int[] newData = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            metrics.steps++;           // read data[i]
            metrics.moves++;           // write newData[i]
        }
        data = newData;
    }

    @Override
    public int get(int index) {
        checkElementIndex(index);
        metrics.steps++;               // one array cell read
        return data[index];
    }

    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.steps++;           // read data[i]
            metrics.comparisons++;     // compare data[i] with x
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the element at {@code index} and shifts the tail one cell to the left.
     * <p>
     * Loop invariant of the shifting loop (a0 = content before the call):
     * before the iteration with value i (index &lt;= i &lt;= size - 1)
     * <ol>
     *   <li>data[k] == a0[k] for 0 &lt;= k &lt; index (the prefix is untouched);</li>
     *   <li>data[k] == a0[k + 1] for index &lt;= k &lt; i (already shifted part);</li>
     *   <li>data[k] == a0[k] for i &lt;= k &lt; size (not yet overwritten).</li>
     * </ol>
     */
    @Override
    public int remove(int index) {
        checkElementIndex(index);
        int removed = data[index];
        metrics.steps++;               // read the removed element
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            metrics.steps++;           // read data[i + 1]
            metrics.moves++;           // shift one element
        }
        size--;
        return removed;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (size == data.length) {
            grow();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            metrics.steps++;           // read data[i - 1]
            metrics.moves++;           // shift one element
        }
        data[index] = x;
        metrics.moves++;               // store the new element
        size++;
    }

    @Override
    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = data[i];
        }
        return copy;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
