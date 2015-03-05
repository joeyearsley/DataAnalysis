/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author joe yearsley
 */
public class Similarity {

    static Map<String, Task> selfSim = Collections.synchronizedMap(new HashMap<String, Task>());
    static Map<String, Task> diffSim = Collections.synchronizedMap(new HashMap<String, Task>());

    static String fileLoc = "/Users/josephyearsley/Documents/University/Data/Converted/";

    public static void calcSelf() throws IOException {
        CSVReader reader = null;
        int numberOfFiles = 0;
        BigDecimal runningAverage = BigDecimal.ZERO;
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
                    if (!temp.exists()) {
                        Task tempCheck = selfSim.get(checkFirst + "" + checkSecond);
                        if (tempCheck == null) {
                            tempCheck = new Task();
                        }
                        if (tempCheck.taskKeeper.get(taskFirst + "" + taskSecond) == null) {
                            tempCheck.taskKeeper.put(taskFirst + "" + taskSecond, BigDecimal.ZERO);
                            runningAverage = new BigDecimal(0);
                        } else {
                            runningAverage = tempCheck.taskKeeper.get(taskFirst + "" + taskSecond);
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

                                //Same first name
                                if (first == checkFirst && second == checkSecond) {
                                    //now check that tasks are same, so only checking against same tasks
                                    if (taskFirst == tFirst && taskSecond == tSecond) {
                                        signal2[0] = reader.readNext()[0];
                                        signal2[1] = reader.readNext()[0];
                                        runningAverage = runningAverage.add(cosineSim(signal1, signal2));
                                    }
                                }
                            }
                        }
                        BigDecimal t = tempCheck.taskKeeper.get(taskFirst + "" + taskSecond);
                        tempCheck.taskKeeper.put(taskFirst + "" + taskSecond, t.add(runningAverage));
                        //added another task so increment.
                        tempCheck.taskNumber++;
                        //Put into hashmap for averaging later
                        selfSim.put(checkFirst + "" + checkSecond, tempCheck);
                    }
                }
            }
        }
    }

    static void selfSimilarity() {
        //FileWriter writer;
        try {
            calcSelf();
            //Go through all entries and work out average.
            //Has to be synchronized to ensure no data overlaps
            synchronized (selfSim) {
                Iterator<Map.Entry<String, Task>> iterator = selfSim.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Task> entry = iterator.next();
                    BigDecimal value = entry.getValue().getAverage(25);
                    FileWriter writer = new FileWriter(fileLoc + "/Similarity/selfSim/" + entry.getKey() + ".csv");
                    writer.write(value.divide(new BigDecimal(25), 200, RoundingMode.HALF_UP).toString());
                    writer.close();
                }
            }
        } catch (IOException e) {

        }
    }

    public static void calcDiff() throws IOException {
        CSVReader reader = null;
        int numberOfFiles = 0;
        BigDecimal runningAverage = BigDecimal.ZERO;
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
                    Task tempCheck = diffSim.get(checkFirst + "" + checkSecond);
                        if (tempCheck == null) {
                            tempCheck = new Task();
                        }
                        if (tempCheck.taskKeeper.get(taskFirst + "" + taskSecond) == null) {
                            tempCheck.taskKeeper.put(taskFirst + "" + taskSecond, BigDecimal.ZERO);
                            runningAverage = new BigDecimal(0);
                        } else {
                            runningAverage = tempCheck.taskKeeper.get(taskFirst + "" + taskSecond);
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

                                //Same first name
                                if (first == checkFirst && second == checkSecond) {
                                    //now check that tasks are same, so only checking against same tasks
                                    if (taskFirst == tFirst && taskSecond == tSecond) {
                                        signal2[0] = reader.readNext()[0];
                                        signal2[1] = reader.readNext()[0];
                                        runningAverage = runningAverage.add(cosineSim(signal1, signal2));
                                    }
                                }
                            }
                        }
                        BigDecimal t = tempCheck.taskKeeper.get(taskFirst + "" + taskSecond);
                        tempCheck.taskKeeper.put(taskFirst + "" + taskSecond, t.add(runningAverage));
                        //added another task so increment.
                        tempCheck.taskNumber++;
                        //Put into hashmap for averaging later
                        diffSim.put(checkFirst + "" + checkSecond, tempCheck);
                    }
            }
        }
    }

    static void diffSimilarity() {
        //FileWriter writer;
        try {
            calcDiff();
            //Go through all entries and work out average.
            //Has to be synchronized to ensure no data overlaps
            synchronized (diffSim) {
                Iterator<Map.Entry<String, Task>> iterator = diffSim.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Task> entry = iterator.next();
                    //each task has 75 other 
                    BigDecimal value = entry.getValue().getAverage(75);
                    FileWriter writer = new FileWriter(fileLoc + "/Similarity/diffSim/" + entry.getKey() + ".csv");
                    //already averaged out each individual file, now to average out every file added
                    writer.write(value.divide(new BigDecimal(25), 200, RoundingMode.HALF_UP).toString());
                    writer.close();
                }
            }
        } catch (IOException e) {

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
}
