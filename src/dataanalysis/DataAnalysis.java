/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataanalysis;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author josephyearsley
 */
public class DataAnalysis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //To auto start mongodb
        final Boolean DEV_MODE = true;
        /**
         * Implement DB, checking if not there add, same for docs. Only do
         * mongoClient stuff if in DEV mode, add dev Mode to others as
         * constructor.
         */

        commandHelpers helper = new commandHelpers();
        helper.startMongo(DEV_MODE);
        try {
            DataConvert dataConvert = new DataConvert();

            //Convert all data firstly
            //DataConvert.convertCS();
            dataConvert.convertTW();
            //Similarity.similarity();

            CosineSimilarity c = new CosineSimilarity();
            c.consolidate();
            c.selfSim();
            c.diffSim();
            dtwSimilarity d = new dtwSimilarity();
            d.selfSim();
            d.diffSim();
        } catch (Exception mongoDB) {
            System.err.println(mongoDB);
            System.err.println("Ensure MongoDB is running & try again!");
        }
        
        helper.graphSimilarites("cosine Similarity", "eps", "Cosine Similarity", "cos", "4");
        helper.graphSimilarites("dtw Similarity", "eps", "DTW Similarity", "dtw", "4");
        helper.closeMongo(DEV_MODE);
        /**
         * CALL R SCRIPT WITH ARGUMENTS FOR ROUND TYPE AND FILENAME.
         * Where type is dtw or cos.
         * filename is name of plot graph.
         * round is how much to round by.
         */
    }
}
