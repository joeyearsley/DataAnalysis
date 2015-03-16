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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joe yearsley
 */
public class CosineSimilarity {

    static MongoClient mongoClient = null;
    static String fileLoc = null;
    static DB diss;
    static DBCollection self;
    static DBCollection diff;
    static DBCollection selfTask;
    static DBCollection diffTask;

    public CosineSimilarity() throws Exception {
        fileLoc = "/Users/josephyearsley/Documents/University/Data/Converted/";
        mongoClient = new MongoClient("localhost", 27017);
        diss = mongoClient.getDB("Dissertation");
        Set<String> colNames = diss.getCollectionNames();
        if (colNames.contains("selfTaskCosine")) {
            selfTask = diss.getCollection("selfTaskCosine");
        } else {
            selfTask = diss.createCollection("selfTaskCosine", new BasicDBObject());
        }
        if (colNames.contains("diffTaskCosine")) {
            diffTask = diss.getCollection("diffTaskCosine");
        } else {
            diffTask = diss.createCollection("diffTaskCosine", new BasicDBObject());
        }
        if (colNames.contains("selfCosine")) {
            self = diss.getCollection("selfCosine");
        } else {
            self = diss.createCollection("selfCosine", new BasicDBObject());
        }
        if (colNames.contains("diffCosine")) {
            diff = diss.getCollection("diffCosine");
        } else {
            diff = diss.createCollection("diffCosine", new BasicDBObject());
        }
    }

    /**
     * Calculate self similarity for each task for each user. Compare each
     * timeTask to each other, divide by number of comparisons. Possibly group
     * by name and task then run through group?
     */
    public static void selfSim() throws Exception {

        DBCollection cV = diss.getCollection("columnVector");
        /**
         * dbObjIdMap - To store aggregate key groupFields - To add fields to
         * output - set of DBObjects
         */
        Map<String, Object> dbObjIdMap = new HashMap<>();
        dbObjIdMap.put("subject", "$subject");
        dbObjIdMap.put("task", "$task");
        BasicDBObject groupFields = new BasicDBObject("_id", new BasicDBObject(dbObjIdMap));
        groupFields.append("alpha", new BasicDBObject("$push", "$alphaAvrg"));
        groupFields.append("beta", new BasicDBObject("$push", "$betaAvrg"));
        DBObject group = new BasicDBObject("$group", groupFields);
        List<DBObject> pipeline = Arrays.asList(group);
        AggregationOutput output = cV.aggregate(pipeline);
        HashMap<String, BigDecimal> keepTrack = new HashMap<>();
        DBCollection cVC = diss.getCollection("columnVectorConsolidated");
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
            Integer[] alpha = new Integer[a.size()];
            a.toArray(alpha);
            BasicDBList b = (BasicDBList) result.get("beta");
            Integer[] beta = new Integer[b.size()];
            b.toArray(beta);
            //Number of comparisons done.
            BigInteger runs = BigInteger.ZERO;
            BigDecimal runningTotal = BigDecimal.ZERO;
            for (int i = 0; i < alpha.length; i++) {
                Integer[] tempOne = new Integer[2];
                tempOne[0] = alpha[i];
                tempOne[1] = beta[i];
                //Don't want to compare to self
                if (i != alpha.length) {
                    for (int j = i + 1; j < alpha.length; j++) {
                        Integer[] tempTwo = new Integer[2];
                        tempTwo[0] = alpha[j];
                        tempTwo[1] = beta[j];
                        runs = runs.add(BigInteger.ONE);
                        runningTotal = runningTotal.add(cosineSimilarity(tempOne, tempTwo));
                    }
                }
            }
            String subject = (String) result.get("subject");
            String task = (String) result.get("task");
            BigDecimal value = BigDecimal.ZERO;
            if (keepTrack.get(subject) != null) {
                value = keepTrack.get(subject);
            }
            //for each subject divide by how many runs have been done.
            keepTrack.put(subject, value.add(runningTotal.divide(new BigDecimal(runs), 200, RoundingMode.HALF_UP)));
            DBObject search = new BasicDBObject("subject", subject);
            search.put("task", task);
            if (!selfTask.find(search)
                    .limit(1).hasNext()) {
                DBObject insert = new BasicDBObject("subject", subject);
                insert.put("task", task);
                insert.put("value", runningTotal.divide(new BigDecimal(runs), 200, RoundingMode.HALF_UP).toString());
                selfTask.insert(insert);
            }
        }
        Iterator<Map.Entry<String, BigDecimal>> iterator = keepTrack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BigDecimal> entry = iterator.next();
            BigDecimal val = entry.getValue().divide(new BigDecimal(5), 200, RoundingMode.HALF_UP);
            FileWriter writer = new FileWriter(fileLoc + "/Similarity/selfSim/" + entry.getKey() + ".csv");
            if (!self.find(new BasicDBObject("subject", entry.getKey())).limit(1).hasNext()) {
                DBObject insert = new BasicDBObject("subject", entry.getKey());
                insert.put("value", val.toString());
                self.insert(insert);
            }
            try {
                writer.write(val.toString());
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
    public static void diffSim() throws Exception {

        DBCollection cVC = diss.getCollection("columnVectorConsolidated");
        /**
         * dbObjIdMap - To store aggregate key groupFields - To add fields to
         * output - set of DBObjects
         */
        DBCursor curs = cVC.find();
        HashMap<String, BigDecimal> keepTrack = new HashMap<>();
        while (curs.hasNext()) {
            DBObject result = curs.next();
            BasicDBList a = (BasicDBList) result.get("alpha");
            Integer[] alpha = new Integer[a.size()];
            a.toArray(alpha);
            BasicDBList b = (BasicDBList) result.get("beta");
            Integer[] beta = new Integer[b.size()];
            b.toArray(beta);
            String subject = (String) result.get("subject");
            String task = (String) result.get("task");
            DBCollection cV = diss.getCollection("columnVectorConsolidated");
            DBCursor cursInner = cV.find();
            BigInteger runs = BigInteger.ZERO;
            BigDecimal runningTotal = BigDecimal.ZERO;
            while (cursInner.hasNext()) {
                DBObject resultInner = cursInner.next();
                String subjectInner = (String) resultInner.get("subject");
                String taskInner = (String) resultInner.get("task");
                BasicDBList a2 = (BasicDBList) result.get("alpha");
                Integer[] alphaInner = new Integer[a2.size()];
                a2.toArray(alphaInner);
                BasicDBList b2 = (BasicDBList) result.get("beta");
                Integer[] betaInner = new Integer[b2.size()];
                b2.toArray(betaInner);
                if (!subjectInner.equals(subject) && taskInner.equals(task)) {
                    //Number of comparisons done.
                    for (int i = 0; i < alpha.length; i++) {
                        Integer[] tempOne = new Integer[2];
                        tempOne[0] = alpha[i];
                        tempOne[1] = beta[i];
                        //Don't want to compare to self
                        for (int j = 0; j < alpha.length; j++) {
                            Integer[] tempTwo = new Integer[2];
                            tempTwo[0] = alphaInner[j];
                            tempTwo[1] = betaInner[j];
                            runs = runs.add(BigInteger.ONE);
                            runningTotal = runningTotal.add(cosineSimilarity(tempOne, tempTwo));
                        }
                    }
                }
            }
            BigDecimal value = BigDecimal.ZERO;
            if (keepTrack.get(subject) != null) {
                value = keepTrack.get(subject);
            }
            BigDecimal val = runningTotal.divide(new BigDecimal(runs), 200, RoundingMode.HALF_UP);
            //for each subject divide by how many runs have been done.
            keepTrack.put(subject, value.add(val));
            DBObject insert = new BasicDBObject("subject", subject);
            insert.put("task", task);
            insert.put("value", val.toString());
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
        Iterator<Map.Entry<String, BigDecimal>> iterator = keepTrack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BigDecimal> entry = iterator.next();
            BigDecimal val = entry.getValue().divide(new BigDecimal(5), 200, RoundingMode.HALF_UP);
            DBObject insert = new BasicDBObject("subject", entry.getKey());
            insert.put("value", val.toString());
            DBObject find = new BasicDBObject("subject", entry.getKey());
            if(diff.find(find).hasNext()){
                diff.update(find, insert);
            }else{
                diff.insert(insert);
            }
            FileWriter writer = new FileWriter(fileLoc + "/Similarity/diffSim/" + entry.getKey() + ".csv");
            try {
                writer.write(val.toString());
            } finally {
                writer.close();
            }
        }
    }

    protected static BigDecimal cosineSimilarity(Integer[] a, Integer[] b) {
        int size = a.length;
        BigInteger magA = BigInteger.ZERO;
        BigInteger magB = BigInteger.ZERO;
        BigInteger dot = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            magA = magA.add(BigInteger.valueOf(a[i]).multiply(BigInteger.valueOf(a[i])));
            magB = magB.add(BigInteger.valueOf(b[i]).multiply(BigInteger.valueOf(b[i])));
            dot = dot.add(BigInteger.valueOf(a[i]).multiply(BigInteger.valueOf(b[i])));
        }
        BigDecimal A = BigSqrt.sqrt(magA).get();
        BigDecimal B = BigSqrt.sqrt(magB).get();
        BigDecimal d = new BigDecimal(dot);
        return d.divide(A.multiply(B), 200, RoundingMode.HALF_UP);
    }

    /**
     * Consolidate the tasks into a single subject and task doc in the DB. Then
     * I can use in both self and diff easier.
     */
    public static void consolidate() throws Exception {
        DBCollection cV = diss.getCollection("columnVector");
        /**
         * dbObjIdMap - To store aggregate key groupFields - To add fields to
         * output - set of DBObjects
         */
        Map<String, Object> dbObjIdMap = new HashMap<>();
        dbObjIdMap.put("subject", "$subject");
        dbObjIdMap.put("task", "$task");
        BasicDBObject groupFields = new BasicDBObject("_id", new BasicDBObject(dbObjIdMap));
        groupFields.append("alpha", new BasicDBObject("$push", "$alphaAvrg"));
        groupFields.append("beta", new BasicDBObject("$push", "$betaAvrg"));
        DBObject group = new BasicDBObject("$group", groupFields);
        List<DBObject> pipeline = Arrays.asList(group);
        AggregationOutput output = cV.aggregate(pipeline);
        DBCollection cVC = null;
        Set<String> colNames = diss.getCollectionNames();
        if (colNames.contains("columnVector")) {
            cVC = diss.getCollection("columnVectorConsolidated");
        } else {
            cVC = diss.createCollection("columnVectorConsolidated", new BasicDBObject());
        }
        for (DBObject result : output.results()) {
            DBObject id = (DBObject) result.get("_id");
            BasicDBObject subject = new BasicDBObject("subject", id.get("subject"));
            subject.append("task", id.get("task"));
            if (!cVC.find(subject).limit(1).hasNext()) {
                subject.append("alpha", result.get("alpha"));
                subject.append("beta", result.get("beta"));
                cVC.insert(subject);
            }
        }
    }
}
