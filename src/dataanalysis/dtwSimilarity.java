/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dataanalysis;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joe yearsley
 */
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import org.apache.commons.lang3.ArrayUtils;
public class dtwSimilarity {

    static MongoClient mongoClient = null;
    static String fileLoc = null;
    static DB diss;
    static DBCollection self;
    static DBCollection diff;
    static DBCollection selfTask;
    static DBCollection diffTask;

    public dtwSimilarity() throws Exception {
        fileLoc = "/Users/josephyearsley/Documents/University/Data/Converted/";
        mongoClient = new MongoClient("localhost", 27017);
        diss = mongoClient.getDB("Dissertation");
        Set<String> colNames = diss.getCollectionNames();
        if (colNames.contains("selfTaskDTW")) {
            selfTask = diss.getCollection("selfTaskDTW");
        } else {
            selfTask = diss.createCollection("selfTaskDTW", new BasicDBObject());
        }
        if (colNames.contains("diffTaskDTW")) {
            diffTask = diss.getCollection("diffTaskDTW");
        } else {
            diffTask = diss.createCollection("diffTaskDTW", new BasicDBObject());
        }
        if (colNames.contains("selfDTW")) {
            self = diss.getCollection("selfDTW");
        } else {
            self = diss.createCollection("selfDTW", new BasicDBObject());
        }
        if (colNames.contains("diffDTW")) {
            diff = diss.getCollection("diffDTW");
        } else {
            diff = diss.createCollection("diffDTW", new BasicDBObject());
        }
    }

    /**
     * Calculate self similarity for each task for each user. Compare each
     * timeTask to each other, divide by number of comparisons. Possibly group
     * by name and task then run through group?
     */
    public void selfSim() throws Exception {
            DTWSimilarity d = new DTWSimilarity();
        HashMap<String, Double> keepTrack = new HashMap<>();
        DBCollection cVC = diss.getCollection("timeWarping");
        DBCursor curs = cVC.find();
        /**
         * Run through output and do comparisons. Achieve this by going from
         * initial index to last element if index != last element Then inner
         * loop going from index+1 element to last array1.push(alpha[i],beta[i])
         * array2.push(alpha[j],beta[j]) similarity +=
         * cosineSimilarity(array[i],array[j]);
         */
        while (curs.hasNext()) {
            DBObject result = curs.next();
            BasicDBList a = (BasicDBList) result.get("alpha");
            Double[] alpha = new Double[a.size()];
            a.toArray(alpha);
            BasicDBList b = (BasicDBList) result.get("beta");
            Double[] beta = new Double[b.size()];
            b.toArray(beta);
            String subject = (String) result.get("subject");
            String task = (String) result.get("task");
            DBCollection cV = diss.getCollection("timeWarping");
            DBCursor cursInner = cV.find();
            double runs = 0;
            double runningTotal = 0;
            while (cursInner.hasNext()) {
                DBObject resultInner = cursInner.next();
                String subjectInner = (String) resultInner.get("subject");
                String taskInner = (String) resultInner.get("task");
                BasicDBList a2 = (BasicDBList) resultInner.get("alpha");
                Double[] alphaInner = new Double[a2.size()];
                a2.toArray(alphaInner);
                BasicDBList b2 = (BasicDBList) resultInner.get("beta");
                Double[] betaInner = new Double[b2.size()];
                b2.toArray(betaInner);
                if (subjectInner.equals(subject) & taskInner.equals(task)) {
                    Instance iAlpha = new DenseInstance(ArrayUtils.toPrimitive(alpha));
                    Instance iAlphaInner = new DenseInstance(ArrayUtils.toPrimitive(alphaInner));
                    Instance iBeta = new DenseInstance(ArrayUtils.toPrimitive(beta));
                    Instance iBetaInner = new DenseInstance(ArrayUtils.toPrimitive(betaInner));
                    runs++;
                    runningTotal += ((d.measure(iAlpha,iAlphaInner) 
                                    + d.measure(iBeta, iBetaInner))/2);
                }
            }
            double value = 0;
            if (keepTrack.get(subject) != null) {
                value = keepTrack.get(subject);
            }
            //for each subject divide by how many runs have been done.
            keepTrack.put(subject, ((value + runningTotal) / runs));
            DBObject search = new BasicDBObject("subject", subject);
            search.put("task", task);
            if (!selfTask.find(search)
                    .limit(1).hasNext()) {
                DBObject insert = new BasicDBObject("subject", subject);
                insert.put("task", task);
                insert.put("value", (runningTotal / runs));
                selfTask.insert(insert);
            }
        }
        Iterator<Map.Entry<String, Double>> iterator = keepTrack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            double val = (entry.getValue() / 5);
            FileWriter writer = new FileWriter(fileLoc + "/TimeWarping/Similarity/selfSim/" + entry.getKey() + ".csv");
            if (!self.find(new BasicDBObject("subject", entry.getKey())).limit(1).hasNext()) {
                DBObject insert = new BasicDBObject("subject", entry.getKey());
                insert.put("value", val);
                self.insert(insert);
            }
            try {
                writer.write(Double.toString(val));
            } finally {
                writer.close();
            }
        }
        
    }

    /**
     * Calculate difference similarity for each task for user. So time 1
     * compared to all others, excluding same task and same user, then time 2
     * etc.. group into a not group and same group, run through both
     */
    public void diffSim() throws Exception {
        DTWSimilarity d = new DTWSimilarity();
        DBCollection cVC = diss.getCollection("timeWarping");
        /**
         * dbObjIdMap - To store aggregate key groupFields - To add fields to
         * output - set of DBObjects
         */
        DBCursor curs = cVC.find();
        HashMap<String, Double> keepTrack = new HashMap<>();
        while (curs.hasNext()) {
            DBObject result = curs.next();
            BasicDBList a = (BasicDBList) result.get("alpha");
            Double[] alpha = new Double[a.size()];
            a.toArray(alpha);
            BasicDBList b = (BasicDBList) result.get("beta");
            Double[] beta = new Double[b.size()];
            b.toArray(beta);
            String subject = (String) result.get("subject");
            String task = (String) result.get("task");
            DBCollection cV = diss.getCollection("timeWarping");
            DBCursor cursInner = cV.find();
            double runs = 0;
            double runningTotal = 0;
            while (cursInner.hasNext()) {
                DBObject resultInner = cursInner.next();
                String subjectInner = (String) resultInner.get("subject");
                String taskInner = (String) resultInner.get("task");
                BasicDBList a2 = (BasicDBList) resultInner.get("alpha");
                Double[] alphaInner = new Double[a2.size()];
                a2.toArray(alphaInner);
                BasicDBList b2 = (BasicDBList) resultInner.get("beta");
                Double[] betaInner = new Double[b2.size()];
                b2.toArray(betaInner);
                if (!subjectInner.equals(subject) & taskInner.equals(task)) {
                    Instance iAlpha = new DenseInstance(ArrayUtils.toPrimitive(alpha));
                    Instance iAlphaInner = new DenseInstance(ArrayUtils.toPrimitive(alphaInner));
                    Instance iBeta = new DenseInstance(ArrayUtils.toPrimitive(beta));
                    Instance iBetaInner = new DenseInstance(ArrayUtils.toPrimitive(betaInner));
                    runs++;
                    runningTotal += ((d.measure(iAlpha,iAlphaInner) 
                                    + d.measure(iBeta, iBetaInner))/2);
                }
            }
            double value = 0;
            if (keepTrack.get(subject) != null) {
                value = keepTrack.get(subject);
            }
            double val = runningTotal / runs;
            //for each subject divide by how many runs have been done.
            keepTrack.put(subject, (value + val));
            DBObject insert = new BasicDBObject("subject", subject);
            insert.put("task", task);
            insert.put("value", val);
            DBObject find = new BasicDBObject("subject", subject);
            find.put("task", task );
            if(diffTask.find(find).hasNext()){
                diffTask.update(find, insert);
            }else{
                diffTask.insert(insert);
            }
        }
        //Go through collection for alpha, then another for loop for the other 
        //files
        Iterator<Map.Entry<String, Double>> iterator = keepTrack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            double val = entry.getValue() / 5;
            DBObject insert = new BasicDBObject("subject", entry.getKey());
            insert.put("value", val);
            DBObject find = new BasicDBObject("subject", entry.getKey());
            if(diff.find(find).hasNext()){
                diff.update(find, insert);
            }else{
                diff.insert(insert);
            }
            FileWriter writer = new FileWriter(fileLoc + "/TimeWarping/Similarity/diffSim/" + entry.getKey() + ".csv");
            try {
                writer.write(Double.toString(val));
            } finally {
                writer.close();
            }
        }
    }

}
