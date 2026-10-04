package dataStr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Compares {@link MyLinkedList} with java.util.ArrayList (allowed in tests only) on random operations. */
class MyLinkedListRandomTest {

    @Test
    void matchesArrayListOnRandomOperations() {
        for (long seed = 1; seed <= 20; seed++) {
            Random rnd = new Random(seed);
            MyLinkedList mine = new MyLinkedList();
            ArrayList<Integer> expected = new ArrayList<>();

            for (int step = 0; step < 2000; step++) {
                int op = rnd.nextInt(5);
                int value = rnd.nextInt(50);       // small range -> many duplicates
                switch (op) {
                    case 0 -> {
                        mine.add(value);
                        expected.add(value);
                    }
                    case 1 -> {
                        int idx = rnd.nextInt(expected.size() + 1);
                        mine.add(idx, value);
                        expected.add(idx, value);
                    }
                    case 2 -> {
                        if (!expected.isEmpty()) {
                            int idx = rnd.nextInt(expected.size());
                            assertEquals(expected.remove(idx).intValue(), mine.remove(idx));
                        }
                    }
                    case 3 -> {
                        if (!expected.isEmpty()) {
                            int idx = rnd.nextInt(expected.size());
                            assertEquals(expected.get(idx).intValue(), mine.get(idx));
                        }
                    }
                    default -> assertEquals(expected.contains(value), mine.contains(value));
                }
                assertEquals(expected.size(), mine.size());
            }

            int[] actual = mine.toArray();
            for (int i = 0; i < actual.length; i++) {
                assertEquals(expected.get(i).intValue(), actual[i]);
            }
        }
    }

    /** Both IntList implementations must behave identically when driven through the common interface. */
    @Test
    void listAndArrayAgree() {
        Random rnd = new Random(7);
        IntList a = new DynamicArray();
        IntList b = new MyLinkedList();
        for (int step = 0; step < 3000; step++) {
            int value = rnd.nextInt(100);
            int op = rnd.nextInt(3);
            if (op == 0 || a.size() == 0) {
                int idx = rnd.nextInt(a.size() + 1);
                a.add(idx, value);
                b.add(idx, value);
            } else if (op == 1) {
                int idx = rnd.nextInt(a.size());
                assertEquals(a.remove(idx), b.remove(idx));
            } else {
                assertEquals(a.contains(value), b.contains(value));
            }
        }
        assertEquals(a.size(), b.size());
        int[] x = a.toArray();
        int[] y = b.toArray();
        for (int i = 0; i < x.length; i++) {
            assertEquals(x[i], y[i]);
        }
    }
}
