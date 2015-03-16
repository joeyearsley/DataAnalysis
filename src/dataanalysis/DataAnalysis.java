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

        //Define a process to start mongodb
        Process start = null;
        Process shut = null;
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        
        //try connecting to DB it will throw exception if not connected
        try {
            //will throw an exception if not connected
            mongoClient.getDatabaseNames();
            Set<String> diss = mongoClient.getDB("Dissertation").getCollectionNames();
            if(diss.isEmpty()){
                mongoClient.getDB("Dissertation").createCollection("empty", new BasicDBObject("created", true));
            }
        } catch (Exception e) {
            System.err.println("Ensure MongoDB is running!");
            //Only run automatically for me
            if (DEV_MODE) {
                start = Runtime.getRuntime().exec("/usr/local/bin/mongod -quiet");
            }
        }
        try{
        //Convert all data firstly
        //DataConvert.convertCS();
        //DataConvert.convertTW();
        //Similarity.similarity();
            CosineSimilarity c = new CosineSimilarity();
            c.selfSim();
            c.diffSim();
            c.consolidate();
        }catch(Exception mongoDB){
            System.err.println(mongoDB);
            System.err.println("Ensure MongoDB is running & try again!");
        }
        //Close everything we opened
        if (DEV_MODE) {
            //Everything done so close DB
            try {
                //close mongodb
                shut = Runtime.getRuntime().exec("/usr/local/bin/mongo");
                OutputStream stdin = shut.getOutputStream();
                InputStream stdout = shut.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
                writer.write("use admin\n");
                writer.flush();
                writer.write("db.shutdownServer({timeoutSecs: 60})");
                writer.write("\n");
                writer.flush();
                writer.write("exit\n");
                writer.flush();
                writer.close();
                Scanner scanner = new Scanner(stdout);
                while (scanner.hasNextLine()) {
                    System.out.println(scanner.nextLine());
                }
                shut.destroy();
            } catch (Exception shutdown) {
                System.err.println(shutdown);
            }
            if(start != null) start.destroy();
            mongoClient.close();
        }
    }
}
