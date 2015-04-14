package testing.dataanalysis;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.junit.Test;
import static org.junit.Assert.*;
import dataanalysis.CommandHelpers;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * To test the helpers which start external processes.
 *
 * @author josephyearsley
 */
public class CommandHelpersTest {

    CommandHelpers c;
    MongoClient mongoClient;

    public CommandHelpersTest() {
        try {
            c = new CommandHelpers();
            try {
                mongoClient = new MongoClient("localhost", 27017);
            } catch (Exception e) {
            };
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Test that mongo hasn't opened.
     */
    @Test(expected = MongoException.class)
    public void mongoOpenFail() {
        //Ensure no open connection
        try {
            c.closeMongo(Boolean.TRUE);
        } catch (Exception e) {
        }
        try {
            c.startMongo(Boolean.FALSE);
        } catch (Exception e) {
        }
        Set<String> db = mongoClient.getDB("Dissertation").getCollectionNames();
        System.out.println(db);
    }

    /**
     * Test that mongo has opened.
     */
    @Test
    public void mongoOpenWorking() {
        try {
            c.startMongo(true);
            Set<String> diss = mongoClient.getDB("Dissertation").getCollectionNames();
            assertFalse("Ensure mongo starts", diss.isEmpty());
        } catch (Exception e) {

        }
    }

    /**
     * Test that mongo has closed.
     */
    @Test(expected = MongoException.class)
    public void mongoClose() {
        try {
            c.closeMongo(true);
        } catch (Exception e) {
        }
        List<String> db = mongoClient.getDatabaseNames();
    }

    /**
     * Test that mongo hasn't closed.
     */
    @Test
    public void mongoCloseFail() {
        try {
            c.startMongo(true);
            c.closeMongo(false);
        } catch (Exception e) {
        }
        Set<String> db = mongoClient.getDB("Dissertation").getCollectionNames();
        assertFalse("Ensure mongo doesn't close", db.isEmpty());
    }

    /**
     * Test that the rscript has correctly output the graphs.
     */
    @Test
    public void graphing() {
        try {
            c.graphSimilarites("test graph", "eps", "test", "dtw", "2");
        } catch (Exception e) {
            System.out.println(e);
        }
        File f = new File("../R/test graph.eps");
        assertTrue("Assert that the test graph is made.", f.exists());
    }
}
