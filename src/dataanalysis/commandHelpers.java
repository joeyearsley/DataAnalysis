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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author joe yearsley
 */
public class commandHelpers {

    Process start, shut, proc;
    MongoClient mongoClient;

    public commandHelpers() throws Exception {
        //Define a process to start mongodb
        start = null;
        shut = null;
        mongoClient = new MongoClient("localhost", 27017);
    }

    public void startMongo(Boolean DEV_MODE) throws Exception {

        //try connecting to DB it will throw exception if not connected
        try {
            //will throw an exception if not connected
            mongoClient.getDatabaseNames();
            Set<String> diss = mongoClient.getDB("Dissertation").getCollectionNames();
            if (diss.isEmpty()) {
                mongoClient.getDB("Dissertation").createCollection("empty", new BasicDBObject("created", true));
            }
        } catch (Exception e) {
            System.err.println("Ensure MongoDB is running!");
            //Only run automatically for me
            if (DEV_MODE) {
                start = Runtime.getRuntime().exec("/usr/local/bin/mongod -quiet");
            }
        }
    }

    public void closeMongo(Boolean DEV_MODE) {
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
            if (start != null) {
                start.destroy();
            }
            mongoClient.close();
        }
    }

    public void graphSimilarites(String fN, String fT, String gN, String gT, String r) throws Exception {
        CommandLine cmdLine = new CommandLine("Rscript");
        cmdLine.addArgument("Graph.R" , false);
        cmdLine.addArgument("--args" , false);
        cmdLine.addArgument(fN , true);
        cmdLine.addArgument(fT , true);
        cmdLine.addArgument(gN , true);
        cmdLine.addArgument(gT , true);
        cmdLine.addArgument(r , false);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(psh);
        executor.setWorkingDirectory(new File("/Users/josephyearsley/documents/university/dissertation/r"));
        executor.execute(cmdLine);
        System.out.println(stdout.toString());
        
    }
}
