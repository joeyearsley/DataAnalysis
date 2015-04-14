package testing.dataanalysis;

import org.junit.Test;
import dataanalysis.Pair;
import dataanalysis.Task;
import java.math.BigDecimal;
import static org.junit.Assert.*;

/**
 * To test helper instance classes.
 *
 * @author josephyearsley
 */
public class TaskAndPairTest {

    /**
     * Tests that the pair class works correctly.
     */
    @Test
    public void PairTest() {
        Pair p = new Pair(1, 2);
        assertEquals("1 is first in the pair", p.first, 1, 0);
        assertEquals("2 is second in the pair", p.second, 2, 0);
    }

    /**
     * Test that the task class works correctly.
     */
    @Test
    public void TaskTest() {
        Task t = new Task();
        t.put("t", new BigDecimal(1));
        t.put("t", t.taskKeeper.get("t").add(new BigDecimal(2)));
        assertEquals("Assert average of 1+2/1 is 3", t.getAverage(1).compareTo(new BigDecimal(3)), 0);
        t.put("t", t.taskKeeper.get("t").add(new BigDecimal(1)));
        assertEquals("Average is not 2", t.getAverage(2).compareTo(new BigDecimal(2)), 0);
    }
}
