
package dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Converts all the data from CSV to mongo documents and files.
 * @author joe yearsley
 */
public class DataConvert {

    static String fileLoc;
    static MongoClient mongoClient;
    static DB diss;

    /**
     * Sets up the instance
     * @throws Exception if database isn't open
     */
    public DataConvert() throws Exception {
        fileLoc = "/Users/josephyearsley/Documents/University/Data/";
        mongoClient = new MongoClient("localhost", 27017);
        diss = mongoClient.getDB("Dissertation");
    }

    /**
     * Converts to a cosine similarity compatible file.
     * @throws Exception Writer has an issue.
     */
    public void convertCS() throws Exception {
        CSVReader reader = null;
        int alpha = 0;
        int beta = 0;
        int numberOfLines = 0;
        int betaAvrg = 0;
        int alphaAvrg = 0;
        DBCollection cV = null;
        try{
            Set<String> colNames = diss.getCollectionNames();
            //Create collection if it doesn't exist
            if (colNames.contains("columnVector")) {
                cV = diss.getCollection("columnVector");
            } else {
                cV = diss.createCollection("columnVector", new BasicDBObject());
            }
        }catch(Exception noDB){
            System.err.println(noDB);
        }
        /*
         * Loop through data directory, check its not empty. Go through each
         * file, ensuring its not hidden or directory. Then check if its already
         * converted, if not then convert and write/insert.
         */
        File dir = new File(fileLoc);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (!child.isHidden() && !child.isDirectory()) {
                    //Get BaseName
                    String name = child.getName();
                    int pos = name.lastIndexOf(".");
                    if (pos > 0) {
                        name = name.substring(0, pos);
                    }
                    //Break file name into database fields
                    String subject = name.charAt(0) + "" + name.charAt(1);
                    String task = name.charAt(2) + "" + name.charAt(3);
                    int timesDone = Integer.parseInt(name.charAt(4) + "");

                    //Check that the file hasn't already been converted
                    DBCursor curs = null;
                    //Find task file
                    BasicDBObject file = new BasicDBObject("subject", subject)
                            .append("task", task)
                            .append("timesDone", timesDone);
                    curs = cV.find(file).limit(1);
                    //no file returned
                    if (!curs.hasNext()) {
                        // Do something with child
                        try {
                            //Get the CSVReader instance with specifying the delimiter to be used
                            reader = new CSVReader(new FileReader(child), ',');
                            String[] nextLine;
                            //Skip first line
                            nextLine = reader.readNext();
                            while ((nextLine = reader.readNext()) != null) {
                                //increment the number of lines
                                numberOfLines++;
                                //Total up low and high alpha values
                                alpha += Integer.parseInt(nextLine[1]) + Integer.parseInt(nextLine[2]);
                                beta += Integer.parseInt(nextLine[3]) + Integer.parseInt(nextLine[4]);
                            }
                            //Calc the avrgs
                            alphaAvrg = alpha / (numberOfLines * 2);
                            betaAvrg = beta / (numberOfLines * 2);
                        } catch (Exception e) {
                            //For debuging
                            e.printStackTrace();
                        } finally {
                            /*
                             * Close reader, make new file write the string
                             * values. Close the writer, insert into DB.
                             */
                            reader.close();
                            FileWriter writer = new FileWriter(fileLoc + "Converted/" + name + "cv.csv");
                            String av = String.valueOf(alphaAvrg);
                            String bv = String.valueOf(betaAvrg);
                            writer.write(av);
                            writer.write('\n');
                            writer.write(bv);
                            writer.flush();
                            writer.close();
                            file.append("alphaAvrg", alphaAvrg);
                            file.append("betaAvrg", betaAvrg);
                            cV.insert(file);
                        }
                    }
                }
            }
        }
        //Close up the client connection to preserve memory.
        mongoClient.close();
    }

    /**
     * Convert to a TimeWarping compatible file.
     * @throws IOException Writer has encountered a problem.
     */
    public void convertTW() throws IOException {
        CSVReader reader = null;
        double alpha = 0;
        double beta = 0;
        DBCollection tW = null;
        try {
            //Make colletion if it doesn't exist.
            Set<String> colNames = diss.getCollectionNames();
            if (colNames.contains("timeWarping")) {
                tW = diss.getCollection("timeWarping");
            } else {
                tW = diss.createCollection("timeWarping", new BasicDBObject());
            }
        } catch (Exception noDB) {
            System.err.println(noDB);
        }
        /*
         * Loop through data directory, check its not empty. Go through each
         * file, ensuring its not hidden or directory. Then check if its already
         * converted, if not then convert and write
         */
        File dir = new File(fileLoc);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (!child.isHidden() && !child.isDirectory()) {
                    //Get BaseName
                    String name = child.getName();
                    int pos = name.lastIndexOf(".");
                    if (pos > 0) {
                        name = name.substring(0, pos);
                    }
                    //Break file name into database fields
                    String subject = name.charAt(0) + "" + name.charAt(1);
                    String task = name.charAt(2) + "" + name.charAt(3);
                    int timesDone = Integer.parseInt(name.charAt(4) + "");

                    //Check that the file hasn't already been converted
                    DBCursor curs = null;
                    //Find task file
                    BasicDBObject file = new BasicDBObject("subject", subject)
                            .append("task", task)
                            .append("timesDone", timesDone);
                    curs = tW.find(file).limit(1);
                    //Alpha
                    List<Double> alphaList = new ArrayList<>();
                    //Beta
                    List<Double> betaList = new ArrayList<>();
                    if (!curs.hasNext()) {
                        // Do something with child
                        FileWriter writer = new FileWriter(fileLoc + "TimeWarping/" + name + ".csv");
                        try {
                            //Get the CSVReader instance with specifying the delimiter to be used
                            reader = new CSVReader(new FileReader(child), ',');
                            String[] nextLine;
                            //Skip first line
                            reader.readNext();
                            while ((nextLine = reader.readNext()) != null) {
                                //Total up low and high alpha values
                                alpha = Double.valueOf(nextLine[1]) + Double.valueOf(nextLine[2]);
                                beta = Double.valueOf(nextLine[3]) + Double.valueOf(nextLine[4]);
                                //Get average for that time between high and low
                                String av = String.valueOf((alpha+'d' / 2));
                                String bv = String.valueOf((beta+'d' / 2));
                                alphaList.add(alpha / 2);
                                betaList.add(beta / 2);
                                
                                writer.write(av);
                                writer.write(',');
                                writer.write(bv);
                                writer.write(',');
                            }
                        } catch (Exception e) {
                            //For debugging
                            e.printStackTrace();
                        } finally {
                            /*
                             * Close reader, make new file write the string
                             * values. Close the writer, insert into DB.
                             */
                            reader.close();
                            writer.flush();
                            writer.close();
                            file.append("alpha", alphaList);
                            file.append("beta", betaList);
                            tW.insert(file);
                        }
                    }
                }
            }
        }
        //Close the client to ensure memory preservation.
        mongoClient.close();
    }

}
