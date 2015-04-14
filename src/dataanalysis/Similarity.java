
package dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The initial class to do analysis, which was wrong.
 * @author joe yearsley
 */
public class Similarity {
    
    //Create synced hashmaps
    static Map<String, Task> selfSim = Collections.synchronizedMap(new HashMap<String, Task>());
    static Map<String, Task> diffSim = Collections.synchronizedMap(new HashMap<String, Task>());
    static Map<String, Pair> selfSimDTW = Collections.synchronizedMap(new HashMap<String, Pair>());
    static Map<String, Pair> diffSimDTW = Collections.synchronizedMap(new HashMap<String, Pair>());

    static DBCollection selfCosineWrong;
    static DBCollection diffCosineWrong;
    static DBCollection selfDTWWrong;
    static DBCollection diffDTWWrong;
    static MongoClient mongoClient = null;
    static DB diss;
    
    final static String FILE_LOC = "../Data/Converted/Wrong/";
    
    /**
     * Constructor to init everything.
     * if the collection doesn't exist, make it.
     * @throws Exception Database isn't found.
     */
    public Similarity() throws Exception {
        mongoClient = new MongoClient("localhost", 27017);
        diss = mongoClient.getDB("Dissertation");
        if(!new File(FILE_LOC + "Cosine/Similarity/selfSim/").exists()){
            new File(FILE_LOC + "Cosine/Similarity/selfSim/").mkdirs();
        }
        if(!new File(FILE_LOC + "Cosine/Similarity/diffSim/").exists()){
            new File(FILE_LOC + "Cosine/Similarity/diffSim/").mkdirs();
        }
        if(!new File(FILE_LOC + "TimeWarping/Similarity/selfSim/").exists()){
            new File(FILE_LOC + "TimeWarping/Similarity/selfSim/").mkdirs();
        }
        if(!new File(FILE_LOC + "TimeWarping/Similarity/diffSim/").exists()){
            new File(FILE_LOC + "TimeWarping/Similarity/diffSim/").mkdirs();
        }
        Set<String> colNames = diss.getCollectionNames();
        if (colNames.contains("selfCosineWrong")) {
            selfCosineWrong= diss.getCollection("selfCosineWrong");
        } else {
            selfCosineWrong = diss.createCollection("selfCosineWrong", new BasicDBObject());
        }
        if (colNames.contains("diffCosineWrong")) {
            diffCosineWrong = diss.getCollection("diffCosineWrong");
        } else {
            diffCosineWrong = diss.createCollection("diffCosineWrong", new BasicDBObject());
        }
        if (colNames.contains("selfDTWWrong")) {
            selfDTWWrong= diss.getCollection("selfDTWWrong");
        } else {
            selfDTWWrong = diss.createCollection("selfDTWWrong", new BasicDBObject());
        }
        if (colNames.contains("diffDTWWrong")) {
            diffDTWWrong = diss.getCollection("diffDTWWrong");
        } else {
            diffDTWWrong = diss.createCollection("diffDTWWrong", new BasicDBObject());
        }
    }
    
    /**
     * Runs all calculations.
     */
    public void similarityCalc() {
        try {

            calcSim();
            DTWSim();

            //Go through all entries and work out average.
            //Has to be synchronized to ensure no data overlaps
            synchronized (diffSim) {
                Iterator<Map.Entry<String, Task>> iterator = diffSim.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Task> entry = iterator.next();
                    //each task has N-1 * 25 tasks
                    BigDecimal value = entry.getValue().getAverage((diffSim.size() - 1) * 25);
                    value = value.divide(new BigDecimal(5), 200, RoundingMode.HALF_UP);
                    DBObject insert = new BasicDBObject("subject", entry.getKey());
                    insert.put("value", value.toString());
                    DBObject find = new BasicDBObject("subject", entry.getKey());
                    if (diffCosineWrong.find(find).hasNext()) {
                        diffCosineWrong.update(find, insert);
                    } else {
                        diffCosineWrong.insert(insert);
                    }
                    FileWriter writer = new FileWriter(FILE_LOC + "Cosine/Similarity/diffSim/" + entry.getKey() + ".csv");
                    //already averaged out each individual file, now to average out every file added
                    writer.write(value.toString());
                    writer.close();
                }
            }
            //Go through all entries and work out average.
            //Has to be synchronized to ensure no data overlaps
            synchronized (selfSim) {
                Iterator<Map.Entry<String, Task>> iterator = selfSim.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Task> entry = iterator.next();
                    BigDecimal value = entry.getValue().getAverage(25);
                    value = value.divide(new BigDecimal(5), 200, RoundingMode.HALF_UP);
                    DBObject insert = new BasicDBObject("subject", entry.getKey());
                    insert.put("value", value.toString());
                    DBObject find = new BasicDBObject("subject", entry.getKey());
                    if (selfCosineWrong.find(find).hasNext()) {
                        selfCosineWrong.update(find, insert);
                    } else {
                        selfCosineWrong.insert(insert);
                    }
                    FileWriter writer = new FileWriter(FILE_LOC + "Cosine/Similarity/selfSim/" + entry.getKey() + ".csv");
                    try {
                        writer.write(value.toString());
                    } finally {
                        writer.close();
                    }
                }
            }
            synchronized (selfSimDTW) {
                Iterator<Map.Entry<String, Pair>> iterator = selfSimDTW.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Pair> entry = iterator.next();
                    double alpha = entry.getValue().first / (25 * 5);
                    double beta = entry.getValue().first / (25 * 5);
                    DBObject insert = new BasicDBObject("subject", entry.getKey());
                    insert.put("value", ((alpha+beta)/2) );
                    DBObject find = new BasicDBObject("subject", entry.getKey());
                    if (selfDTWWrong.find(find).hasNext()) {
                        selfDTWWrong.update(find, insert);
                    } else {
                        selfDTWWrong.insert(insert);
                    }
                    FileWriter writer = new FileWriter(FILE_LOC + "/TimeWarping/Similarity/selfSim/" + entry.getKey() + ".csv");
                    writer.write(String.valueOf(alpha) + ',' + beta);
                    writer.close();
                }
            }
            synchronized (diffSimDTW) {
                Iterator<Map.Entry<String, Pair>> iterator = diffSimDTW.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Pair> entry = iterator.next();
                    double alpha = entry.getValue().first / ((diffSimDTW.size() - 1) * 25 * 5);
                    double beta = entry.getValue().first / ((diffSimDTW.size() - 1) * 25 * 5);
                    DBObject insert = new BasicDBObject("subject", entry.getKey());
                    insert.put("value", ((alpha+beta)/2) );
                    DBObject find = new BasicDBObject("subject", entry.getKey());
                    if (diffDTWWrong.find(find).hasNext()) {
                        diffDTWWrong.update(find, insert);
                    } else {
                        diffDTWWrong.insert(insert);
                    }
                    FileWriter writer = new FileWriter(FILE_LOC + "/TimeWarping/Similarity/diffSim/" + entry.getKey() + ".csv");
                    writer.write(String.valueOf(alpha) + ',' + beta);
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes use of external library as BigDecimal needs square rooting, Works
     * out cosineSimilarity and returns.
     *
     * @param a - Array A
     * @param b - Array B
     * @return cosine similarity
     */
    protected static BigDecimal cosineSim(String[] a, String[] b) {
        BigDecimal x;
        int size = a.length;
        BigInteger magA = BigInteger.ZERO;
        BigInteger magB = BigInteger.ZERO;
        BigInteger dot = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            magA = magA.add(new BigInteger(a[i]).multiply(new BigInteger(a[i])));
            magB = magB.add(new BigInteger(b[i]).multiply(new BigInteger(b[i])));
            dot = dot.add(new BigInteger(a[i]).multiply(new BigInteger(b[i])));
        }
        BigDecimal A = BigSqrt.sqrt(magA).get();
        BigDecimal B = BigSqrt.sqrt(magB).get();
        BigDecimal d = new BigDecimal(dot);
        x = d.divide(A.multiply(B), 200, RoundingMode.HALF_UP);
        return x;
    }

    /**
     * Calculates the DTW similarities.
     * @throws FileNotFoundException The file doesn't exist.
     * @throws IOException Error in creation of the file.
     */
    static void DTWSim() throws FileNotFoundException, IOException {
        DTWSimilarity d = new DTWSimilarity();
        File dir = new File("../Data/Converted/TimeWarping/");
        File[] directoryListing = dir.listFiles();
        /*
         * Split into persons name, test name, and test attempt Then go through
         * comparing similarities to each other. Try not to repeat for loops,
         * i.e. don't over compare! Once done output all similarities Do self
         * similarity for every task, then the average over all tasks
         */
        if (directoryListing != null) {
            for (File child : directoryListing) {
                //if Not a directory or hidden
                if (!child.isHidden() && !child.isDirectory()) {
                    CSVReader reader = new CSVReader(new FileReader(child), ',');
                    //Get initials
                    char checkFirst = child.getName().charAt(0);
                    char checkSecond = child.getName().charAt(1);
                    char taskFirst = child.getName().charAt(2);
                    char taskSecond = child.getName().charAt(3);
                    List<Double> v1Alpha = new ArrayList<>();
                    List<Double> v1Beta = new ArrayList<>();
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        v1Alpha.add(Double.valueOf(nextLine[0]));
                        v1Beta.add(Double.valueOf(nextLine[1]));
                    }
                    Double[] temp1a = v1Alpha.toArray(new Double[v1Alpha.size()]);
                    Double[] temp1b = v1Beta.toArray(new Double[v1Beta.size()]);
                    Instance i1Alpha = new DenseInstance(ArrayUtils.toPrimitive(temp1a));
                    Instance i1Beta = new DenseInstance(ArrayUtils.toPrimitive(temp1b));
                    Pair tempCheckSelf = selfSimDTW.get(checkFirst + "" + checkSecond);
                    Pair tempCheckDiff = diffSimDTW.get(checkFirst + "" + checkSecond);
                    if (tempCheckSelf == null) {
                        tempCheckSelf = new Pair(0, 0);
                    }
                    if (tempCheckDiff == null) {
                        tempCheckDiff = new Pair(0, 0);
                    }
                    //Go through everything else, making sure not same fileName
                    for (File child2 : directoryListing) {
                        //if Not a directory or hidden
                        if (!child2.isHidden() && !child2.isDirectory() && !child.getName().equals(child2.getName())) {
                            //Get child 2 Name
                            char first = child2.getName().charAt(0);
                            char second = child2.getName().charAt(1);
                            char tFirst = child2.getName().charAt(2);
                            char tSecond = child2.getName().charAt(3);
                            reader = new CSVReader(new FileReader(child2), ',');
                            List<Double> v2Alpha = new ArrayList<>();
                            List<Double> v2Beta = new ArrayList<>();
                            String[] nextLine2;
                            while ((nextLine2 = reader.readNext()) != null) {
                                v2Alpha.add(Double.valueOf(nextLine2[0]));
                                v2Beta.add(Double.valueOf(nextLine2[1]));
                            }
                            Double[] temp2a = v2Alpha.toArray(new Double[v2Alpha.size()]);
                            Double[] temp2b = v2Beta.toArray(new Double[v2Beta.size()]);
                            Instance i2Alpha = new DenseInstance(ArrayUtils.toPrimitive(temp2a));
                            Instance i2Beta = new DenseInstance(ArrayUtils.toPrimitive(temp2b));

                            if (first == checkFirst && checkSecond == second) {
                                tempCheckSelf.first += d.measure(i1Alpha, i2Alpha);
                                tempCheckSelf.second += d.measure(i1Beta, i2Beta);
                                selfSimDTW.put(checkFirst + "" + checkSecond, tempCheckSelf);
                            } else {
                                tempCheckDiff.first += d.measure(i1Alpha, i2Alpha);
                                tempCheckDiff.second += d.measure(i1Beta, i2Beta);
                                diffSimDTW.put(checkFirst + "" + checkSecond, tempCheckSelf);
                            }

                        }
                        reader.close();

                    }
                    reader.close();
                }

            }
        }

    }

    /**
     * Calculates the cosine similarities.
     * @throws IOException Something has gone wrong whilst writing the file.
     */
    public static void calcSim() throws IOException {
        CSVReader reader;
        BigDecimal runningAverageSelf;
        BigDecimal runningAverageDiff;
        String[] signal1 = new String[2];
        String[] signal2 = new String[2];
        /*
         * Loop through data directory, check its not empty. Go through each
         * file, ensuring its not hidden or directory. Then check if its already
         * converted, if not then convert and write
         */
        File dir = new File("../Data/Converted/Cosine/");
        File[] directoryListing = dir.listFiles();
        /*
         * Split into persons name, test name, and test attempt Then go through
         * comparing similarities to each other. Try not to repeat for loops,
         * i.e. don't over compare! Once done output all similarities Do self
         * similiarity for every task, then the average over all tasks
         */
        if (directoryListing != null) {
            for (File child : directoryListing) {
                //if Not a directory or hidden
                if (!child.isHidden() && !child.isDirectory()) {
                    reader = new CSVReader(new FileReader(child), ',');
                    //Get initials
                    char checkFirst = child.getName().charAt(0);
                    char checkSecond = child.getName().charAt(1);
                    char taskFirst = child.getName().charAt(2);
                    char taskSecond = child.getName().charAt(3);
                    //See if file already exists, if it does miss all code out
                    File temp = new File(FILE_LOC + "/Similarity/selfSim/" + checkFirst + checkSecond + ".csv");
                    Task tempCheckSelf = selfSim.get(checkFirst + "" + checkSecond);
                    if (tempCheckSelf == null) {
                        tempCheckSelf = new Task();
                    }
                    if (tempCheckSelf.taskKeeper.get(taskFirst + "" + taskSecond) == null) {
                        tempCheckSelf.taskKeeper.put(taskFirst + "" + taskSecond, BigDecimal.ZERO);
                        //no such task so runningAverage is 0
                        runningAverageSelf = new BigDecimal(0);
                    } else {
                        //get current average for that task for that user
                        runningAverageSelf = tempCheckSelf.taskKeeper.get(taskFirst + "" + taskSecond);
                    }
                    Task tempCheckDiff = diffSim.get(checkFirst + "" + checkSecond);
                    if (tempCheckDiff == null) {
                        tempCheckDiff = new Task();
                    }
                    if (tempCheckDiff.taskKeeper.get(taskFirst + "" + taskSecond) == null) {
                        tempCheckDiff.taskKeeper.put(taskFirst + "" + taskSecond, BigDecimal.ZERO);
                        //no such task so runningAverage is 0
                        runningAverageDiff = new BigDecimal(0);
                    } else {
                        //get current average for that task for that user
                        runningAverageDiff = tempCheckDiff.taskKeeper.get(taskFirst + "" + taskSecond);
                    }
                    signal1[0] = reader.readNext()[0];
                    signal1[1] = reader.readNext()[0];
                    //Go through everything else, making sure not same fileName
                    for (File child2 : directoryListing) {
                        //if Not a directory or hidden
                        if (!child2.isHidden() && !child2.isDirectory() && !child.getName().equals(child2.getName())) {
                            //Get child 2 Name
                            char first = child2.getName().charAt(0);
                            char second = child2.getName().charAt(1);
                            char tFirst = child2.getName().charAt(2);
                            char tSecond = child2.getName().charAt(3);
                            reader = new CSVReader(new FileReader(child2), ',');
                            signal2[0] = reader.readNext()[0];
                            signal2[1] = reader.readNext()[0];
                            //Same first name
                                if (first == checkFirst && second == checkSecond) {
                                    //now check that tasks are same, so only checking against same tasks
                                    if (taskFirst == tFirst && taskSecond == tSecond) {
                                        runningAverageSelf = runningAverageSelf.add(cosineSim(signal1, signal2));
                                    }
                                }
                            
                            //Different Names
                            if (first != checkFirst && second != checkSecond) {
                                //now check that tasks are same, so only checking against same tasks
                                if (taskFirst == tFirst && taskSecond == tSecond) {
                                    runningAverageDiff = runningAverageDiff.add(cosineSim(signal1, signal2));
                                }
                            }
                            reader.close();
                        }
                    }
                    //put back the altered average
                    tempCheckSelf.put(taskFirst + "" + taskSecond, runningAverageSelf);
                    tempCheckDiff.put(taskFirst + "" + taskSecond, runningAverageDiff);
                    //Put into hashmap for averaging later
                    selfSim.put(checkFirst + "" + checkSecond, tempCheckSelf);
                    diffSim.put(checkFirst + "" + checkSecond, tempCheckDiff);
                    reader.close();
                }
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
