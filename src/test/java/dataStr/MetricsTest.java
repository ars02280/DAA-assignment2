package dataStr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsTest {

    @Test
    void newMetricsAreZero() {
        Metrics m = new Metrics();
        assertEquals(0, m.getSteps());
        assertEquals(0, m.getMoves());
        assertEquals(0, m.getComparisons());
    }

    @Test
    void resetClearsAllCounters() {
        Metrics m = new Metrics();
        m.steps = 5;
        m.moves = 6;
        m.comparisons = 7;
        m.reset();
        assertEquals(0, m.getSteps());
        assertEquals(0, m.getMoves());
        assertEquals(0, m.getComparisons());
    }

    @Test
    void toStringContainsAllCounters() {
        Metrics m = new Metrics();
        m.steps = 1;
        m.moves = 2;
        m.comparisons = 3;
        String s = m.toString();
        assertTrue(s.contains("steps=1") && s.contains("moves=2") && s.contains("comparisons=3"));
    }
}
