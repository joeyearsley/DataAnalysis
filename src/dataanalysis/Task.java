/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataanalysis;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Task Object To Make analysis easier
 *
 * @author joe yearsley
 */
public class Task {

    BigDecimal taskTotal;
    int taskNumber = 0;
    final Map<String, BigDecimal> taskKeeper = new HashMap<>();

    //put latest big decimal in
    void put(String s, BigDecimal d) {
        BigDecimal temp = taskKeeper.get(s);
        if (taskKeeper.get(s) == null) {
            taskKeeper.put(s, BigDecimal.ZERO);
        }
        taskKeeper.put(s, d);
    }

    BigDecimal getAverage(int i) {
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
