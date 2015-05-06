
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
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Command line scripts to help with automation of running the program.
 * @author joe yearsley
 */
public class CommandHelpers {
    
    //Processes to start mongo, shut down mongo and 
    Process start, shut;
    MongoClient mongoClient;

    /**
     * Sets up the instance of the helper.
     * @throws Exception if can't create a mongo client
     */
    public CommandHelpers() throws Exception {
        start = null;
        shut = null;
        mongoClient = new MongoClient("localhost", 27017);
    }
    /**
     * Helper to run mongoDB automatically.
     * Only for use in dev mode where location of mongods location is known.
     * @param DEV_MODE Just for development use. run mongod to start mongoDB normally.
     * @throws Exception - process fails for some reason.
     */
    public void startMongo(Boolean DEV_MODE) throws Exception {
        try {
            //Will throw an exception if not connected
            mongoClient.getDatabaseNames();
            Set<String> diss = mongoClient.getDB("Dissertation").getCollectionNames();
            if (diss.isEmpty()) {
                mongoClient.getDB("Dissertation").createCollection("empty", new BasicDBObject("created", true));
            }
        } catch (Exception e) {
            //Tell user if mongoDB is not running
            System.err.println("Ensure MongoDB is running!");
            if (DEV_MODE) {
                start = Runtime.getRuntime().exec("/usr/local/bin/mongod -quiet");
            }
        }
    }

    /**
     * Helper to shutdown mongoDB automatically.
     * Only for use in dev mode where location of mongo's location is known.
     * @param DEV_MODE Just for development use. run mongo's shutdownServer to stop mongoDB normally.
     */
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
                writer.write("db.shutdownServer({timeoutSecs: 10})");
                writer.write("\n");
                writer.flush();
                writer.write("exit\n");
                writer.flush();
                writer.close();
                Scanner scanner = new Scanner(stdout);
                while (scanner.hasNextLine()) {
                    System.out.println(scanner.nextLine());
                }
                //Destroy the shutdown process
                shut.destroy();
            } catch (Exception shutdown) {
                System.err.println(shutdown);
            }
            //Destroy the start process
            if (start != null) {
                start.destroy();
            }
            //Close the client connection
            mongoClient.close();
        }
    }

    /**
     * Helps call the Rscript to build up the graphs
     * @param fN  File Name of which to save the graph too
     * @param fT File Type of which to save the graph
     * @param gN Graph Name for title of the graph
     * @param gT What type of graph
     * @param r How many decimal points to round too
     * @throws Exception If command line has an error
     */
    public void graphSimilarites(String fN, String fT, String gN, String gT, String r) throws Exception {
        //Build up the command line starting with Rscript command
        CommandLine cmdLine = new CommandLine("Rscript");
        //add the arguments, true or false if we want quotes
        cmdLine.addArgument("Graph.R" , false);
        cmdLine.addArgument("--args" , false);
        cmdLine.addArgument(fN , true);
        cmdLine.addArgument(fT , true);
        cmdLine.addArgument(gN , true);
        cmdLine.addArgument(gT , true);
        cmdLine.addArgument(r , false);
        //add argument to say where to save the graphs
        cmdLine.addArgument(System.getProperty("user.dir")+"/../R",false);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(psh);
        //set directory
        executor.setWorkingDirectory(new File("../R"));
        //Run the commands
        executor.execute(cmdLine);
        //Print out all info returned
        System.out.println(stdout.toString());
        
    }
    
}
