/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
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
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author joe yearsley
 */
public class Similarity {

    static Map<String, Task> selfSim = Collections.synchronizedMap(new HashMap<String, Task>());
    static Map<String, Task> diffSim = Collections.synchronizedMap(new HashMap<String, Task>());
    static Map<String, Pair> selfSimDTW = Collections.synchronizedMap(new HashMap<String, Pair>());
    static Map<String, Pair> diffSimDTW = Collections.synchronizedMap(new HashMap<String, Pair>());

    static String fileLoc = "/Users/josephyearsley/Documents/University/Data/Converted/";

    static void similarity() {
        //FileWriter writer;
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
                    FileWriter writer = new FileWriter(fileLoc + "/Similarity/diffSim/" + entry.getKey() + ".csv");
                    //already averaged out each individual file, now to average out every file added
                    writer.write(value.divide(new BigDecimal(5), 200, RoundingMode.HALF_UP).toString());
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
                    FileWriter writer = new FileWriter(fileLoc + "/Similarity/selfSim/" + entry.getKey() + ".csv"); 
                    try {
                        writer.write(value.divide(new BigDecimal(5), 200, RoundingMode.HALF_UP).toString());
                    }finally{
                        writer.close();
                    }
                }
            }
            synchronized (selfSimDTW) {
                Iterator<Map.Entry<String, Pair>> iterator = selfSimDTW.entrySet().iterator();
                System.out.println(selfSimDTW);
                while (iterator.hasNext()) {
                    Map.Entry<String, Pair> entry = iterator.next();
                    double alpha = entry.getValue().first / (25*5);
                    double beta = entry.getValue().first / (25*5);
                    FileWriter writer = new FileWriter(fileLoc + "/TimeWarping/Similarity/selfSim/" + entry.getKey() + ".csv");
                    writer.write(String.valueOf(alpha) + ',' + beta);
                    writer.close();
                }
            }
            synchronized (diffSimDTW) {
                Iterator<Map.Entry<String, Pair>> iterator = diffSimDTW.entrySet().iterator();
                System.out.println(diffSimDTW);
                while (iterator.hasNext()) {
                    Map.Entry<String, Pair> entry = iterator.next();
                    double alpha = entry.getValue().first / ((diffSimDTW.size() - 1) * 25 * 5);
                    double beta = entry.getValue().first / ((diffSimDTW.size() - 1) * 25 * 5);
                    FileWriter writer = new FileWriter(fileLoc + "/TimeWarping/Similarity/diffSim/" + entry.getKey() + ".csv");
                    writer.write(String.valueOf(alpha) + ',' + beta);
                    writer.close();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
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
        BigDecimal x = BigDecimal.ZERO;
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

    //Just a basic example of dtw for one person, need to expand still
    static void DTWSim() throws FileNotFoundException, IOException {
        DTWSimilarity d = new DTWSimilarity();
        File dir = new File("/Users/josephyearsley/Documents/University/Data/TimeWarping/");
        File[] directoryListing = dir.listFiles();
        /**
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
                    List<Double> v1Alpha = new ArrayList<Double>();
                    List<Double> v1Beta = new ArrayList<Double>();
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
                            }else{
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

    public static void calcSim() throws IOException {
        CSVReader reader = null;
        BigDecimal runningAverageSelf = BigDecimal.ZERO;
        BigDecimal runningAverageDiff = BigDecimal.ZERO;
        String[] signal1 = new String[2];
        String[] signal2 = new String[2];
        /**
         * Loop through data directory, check its not empty. Go through each
         * file, ensuring its not hidden or directory. Then check if its already
         * converted, if not then convert and write
         */
        File dir = new File(fileLoc);
        File[] directoryListing = dir.listFiles();
        /**
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
                    File temp = new File(fileLoc + "/Similarity/selfSim/" + checkFirst + checkSecond + ".csv");
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
                        if (!child2.isHidden() & !child2.isDirectory() & !child.getName().equals(child2.getName())) {
                            //Get child 2 Name
                            char first = child2.getName().charAt(0);
                            char second = child2.getName().charAt(1);
                            char tFirst = child2.getName().charAt(2);
                            char tSecond = child2.getName().charAt(3);
                            reader = new CSVReader(new FileReader(child2), ',');
                            signal2[0] = reader.readNext()[0];
                            signal2[1] = reader.readNext()[0];
                            if (!temp.exists()) {
                                //Same first name
                                if (first == checkFirst & second == checkSecond) {
                                    //now check that tasks are same, so only checking against same tasks
                                    if (taskFirst == tFirst & taskSecond == tSecond) {
                                        runningAverageSelf = runningAverageSelf.add(cosineSim(signal1, signal2));
                                    }
                                }
                            }
                            //Different Names
                            if (first != checkFirst & second != checkSecond) {
                                //now check that tasks are same, so only checking against same tasks
                                if (taskFirst == tFirst & taskSecond == tSecond) {
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
}
