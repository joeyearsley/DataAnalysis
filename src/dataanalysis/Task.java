
package dataanalysis;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Task object to make cosine analysis easier.
 *
 * @author joe yearsley
 */
public class Task {

    public final Map<String, BigDecimal> taskKeeper = new HashMap<>();

    /**
     * To insert into hash map for each task. 
     * @param s The task name.
     * @param d The task value.
     */
    public void put(String s, BigDecimal d) {
        if (taskKeeper.get(s) == null) {
            taskKeeper.put(s, BigDecimal.ZERO);
        }
        taskKeeper.put(s, d);
    }

    /**
     * Averaging function for hash map.
     * @param i The number of comparisons it has done.
     * @return The average value.
     */
    public BigDecimal getAverage(int i) {
        BigDecimal value = BigDecimal.ZERO;
        synchronized (taskKeeper) {
            //go through each task 
            Iterator<Map.Entry<String, BigDecimal>> iterator = taskKeeper.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, BigDecimal> entry = iterator.next();
                //divide each task by how many comparisons it has done
                value = value.add(entry.getValue().divide(new BigDecimal(i), 200, RoundingMode.HALF_UP));
            }
        }
        return value;
    }
}
