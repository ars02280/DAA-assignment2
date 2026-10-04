package dataStr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Compares {@link DynamicArray} with java.util.ArrayList (allowed in tests only) on random operations. */
class DynamicArrayRandomTest {

    @Test
    void matchesArrayListOnRandomOperations() {
        for (long seed = 1; seed <= 20; seed++) {
            Random rnd = new Random(seed);
            DynamicArray mine = new DynamicArray(1);
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
}
