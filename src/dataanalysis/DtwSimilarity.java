
package dataanalysis;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Correctly works out the DTW similarity.
 * @author joe yearsley
 */


public class DtwSimilarity {

    static MongoClient mongoClient;
    static String fileLoc;
    static DB diss;
    static DBCollection self;
    static DBCollection diff;
    static DBCollection selfTask;
    static DBCollection diffTask;
    static Boolean wS = false;
    static Boolean aT = false;
    static Boolean sAT = false;
    
    /**
     * Constructor to init the class and ensure everything is correctly setup.
     * @param withSelf Do we want to compare to exactly the same data.
     * @param allTasks Do we want to compare to all task types.
     * @param selfAllTasks Do we want to do both.
     * @throws Exception Database isn't open.
     */
    public DtwSimilarity(String withSelf, String allTasks, String selfAllTasks) throws Exception {
        if (withSelf.length() > 0) {
            wS = true;
        }
        if (allTasks.length() > 0) {
            aT = true;
        }
        if (selfAllTasks.length() > 0) {
            sAT = true;
        }
        fileLoc = "../Data/Converted/TimeWarping/";
        if(!new File(fileLoc + "Similarity/selfSim/").exists()){
            new File(fileLoc + "Similarity/selfSim/").mkdirs();
        }
        if(!new File(fileLoc + "Similarity/diffSim/").exists()){
            new File(fileLoc + "Similarity/diffSim/").mkdirs();
        }
        mongoClient = new MongoClient("localhost", 27017);
        diss = mongoClient.getDB("Dissertation");
        Set<String> colNames = diss.getCollectionNames();
        if (colNames.contains("selfTaskDTW" + withSelf+selfAllTasks)) {
            selfTask = diss.getCollection("selfTaskDTW" + withSelf+selfAllTasks);
        } else {
            selfTask = diss.createCollection("selfTaskDTW" + withSelf+selfAllTasks, new BasicDBObject());
        }
        if (colNames.contains("diffTaskDTW" + allTasks)) {
            diffTask = diss.getCollection("diffTaskDTW" + allTasks);
        } else {
            diffTask = diss.createCollection("diffTaskDTW" + allTasks, new BasicDBObject());
        }
        if (colNames.contains("selfDTW" + withSelf+selfAllTasks)) {
            self = diss.getCollection("selfDTW" + withSelf+selfAllTasks);
        } else {
            self = diss.createCollection("selfDTW" + withSelf+selfAllTasks, new BasicDBObject());
        }
        if (colNames.contains("diffDTW" + allTasks)) {
            diff = diss.getCollection("diffDTW" + allTasks);
        } else {
            diff = diss.createCollection("diffDTW" + allTasks, new BasicDBObject());
        }
    }

    /**
     * Calculates the self DTW similarity for all subjects.
     * @throws Exception Database isn't open.
     */
    public void selfSim() throws Exception {
        DTWSimilarity d = new DTWSimilarity();
        HashMap<String, Double> keepTrack = new HashMap<>();
        DBCollection cVC = diss.getCollection("timeWarping");
        DBCursor curs = cVC.find();
        /*
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
            Integer times = (Integer) result.get("timesDone");
            DBCollection cV = diss.getCollection("timeWarping");
            DBCursor cursInner = cV.find();
            double runs = 0;
            double runningTotal = 0;
            while (cursInner.hasNext()) {
                DBObject resultInner = cursInner.next();
                String subjectInner = (String) resultInner.get("subject");
                String taskInner = (String) resultInner.get("task");
                Integer timesInner = (Integer) resultInner.get("timesDone");
                BasicDBList a2 = (BasicDBList) resultInner.get("alpha");
                Double[] alphaInner = new Double[a2.size()];
                a2.toArray(alphaInner);
                BasicDBList b2 = (BasicDBList) resultInner.get("beta");
                Double[] betaInner = new Double[b2.size()];
                b2.toArray(betaInner);
                //Don't want to sway average so don't compare self tasks.
                if (subjectInner.equals(subject)) {
                    //Allow all tasks
                    if (taskInner.equals(task) | sAT) {
                        //Allow comparison to self task
                        if (!timesInner.equals(times) | wS) {
                            Instance iAlpha = new DenseInstance(ArrayUtils.toPrimitive(alpha));
                            Instance iAlphaInner = new DenseInstance(ArrayUtils.toPrimitive(alphaInner));
                            Instance iBeta = new DenseInstance(ArrayUtils.toPrimitive(beta));
                            Instance iBetaInner = new DenseInstance(ArrayUtils.toPrimitive(betaInner));
                            runs++;
                            runningTotal += ((d.measure(iAlpha, iAlphaInner)
                                    + d.measure(iBeta, iBetaInner)) / 2);
                        }
                    }
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
            FileWriter writer = new FileWriter(fileLoc + "Similarity/selfSim/" + entry.getKey() + ".csv");
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
     * Calculates all cross DTW similarities.
     * @throws Exception Database isn't open.
     */
    public void diffSim() throws Exception {
        DTWSimilarity d = new DTWSimilarity();
        DBCollection cVC = diss.getCollection("timeWarping");
        /*
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
                //not the subject but is the task
                if (!subjectInner.equals(subject)) {
                    //or compare to all tasks
                    if (taskInner.equals(task) | aT) {
                        Instance iAlpha = new DenseInstance(ArrayUtils.toPrimitive(alpha));
                        Instance iAlphaInner = new DenseInstance(ArrayUtils.toPrimitive(alphaInner));
                        Instance iBeta = new DenseInstance(ArrayUtils.toPrimitive(beta));
                        Instance iBetaInner = new DenseInstance(ArrayUtils.toPrimitive(betaInner));
                        runs++;
                        runningTotal += ((d.measure(iAlpha, iAlphaInner)
                                + d.measure(iBeta, iBetaInner)) / 2);
                    }
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
            find.put("task", task);
            if (diffTask.find(find).hasNext()) {
                diffTask.update(find, insert);
            } else {
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
            if (diff.find(find).hasNext()) {
                diff.update(find, insert);
            } else {
                diff.insert(insert);
            }
            FileWriter writer = new FileWriter(fileLoc + "Similarity/diffSim/" + entry.getKey() + ".csv");
            try {
                writer.write(Double.toString(val));
            } finally {
                writer.close();
            }
        }
    }
    
    /**
     * Closes the mongo connection to save memory.
     */
    public void closeMongoClient(){
        //Close the client to ensure memory preservation.
        mongoClient.close();
    }

}
