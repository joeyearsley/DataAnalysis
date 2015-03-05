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

/**
 *
 * @author joe yearsley
 */
public class DataConvert {

    public static void Convert() throws IOException {
        CSVReader reader = null;
        int alpha = 0;
        int beta = 0;
        int numberOfLines = 0;
        int betaAvrg = 0;
        int alphaAvrg = 0;
        String fileLoc = "/Users/josephyearsley/Documents/University/Data/";
        /**
         * Loop through data directory, check its not empty.
         * Go through each file, ensuring its not hidden or directory.
         * Then check if its already converted, if not then convert and write
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
                    //Check that the file hasn't already been converted
                    File ex = new File(fileLoc + "Converted/" + name + "cv.csv");
                    if (!ex.exists()) {
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
                            e.printStackTrace();
                        } finally {
                            /**
                             * Close Reader, make new file
                             * Write the string values
                             * Close the writer
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
                        }
                    } else {
                        //System.out.println(ex + " EXISTS");
                    }
                }
            }
        }
    }
}
