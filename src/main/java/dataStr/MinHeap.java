package dataStr;

/**
 * Array-based binary min-heap of primitive {@code int} values, written from scratch
 * (no {@code java.util.PriorityQueue}).
 * <p>
 * Layout: the children of node {@code i} are {@code 2i + 1} and {@code 2i + 2}, its parent is
 * {@code (i - 1) / 2}. Heap property: {@code data[parent] <= data[child]} for every node.
 */
public class MinHeap implements Measurable {

    private static final int DEFAULT_CAPACITY = 10;

    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Negative capacity: " + initialCapacity);
        }
        this.data = new int[Math.max(1, initialCapacity)];
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns the smallest element without removing it; O(1). */
    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        metrics.steps++;               // read data[0]
        return data[0];
    }

    /**
     * Adds {@code x} and bubbles it up; O(log n).
     * <p>
     * The new element is not swapped level by level: the parents that are larger than {@code x}
     * are moved down into the "hole" and {@code x} is stored once at the end (1 move per level).
     */
    public void insert(int x) {
        if (size == data.length) {
            grow();
        }
        int i = size;                  // the hole starts at the first free cell
        size++;
        while (i > 0) {
            int parent = (i - 1) / 2;
            metrics.steps++;           // read data[parent]
            metrics.comparisons++;     // compare data[parent] with x
            if (data[parent] <= x) {
                break;
            }
            data[i] = data[parent];    // move the larger parent down
            metrics.moves++;
            i = parent;
        }
        data[i] = x;
        metrics.moves++;               // final store of x
    }

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

    /**
     * Checks the heap property {@code data[parent] <= data[child]} for every node.
     * Not counted in the metrics; used by the tests after every operation.
     */
    public boolean isValidHeap() {
        for (int child = 1; child < size; child++) {
            if (data[(child - 1) / 2] > data[child]) {
                return false;
            }
        }
        return true;
    }
}
