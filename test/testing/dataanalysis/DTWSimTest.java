package testing.dataanalysis;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import dataanalysis.CommandHelpers;
import dataanalysis.DtwSimilarity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To test that the DTW outputs the files and DB correctly.
 *
 * @author josephyearsley
 */
public class DTWSimTest {

    MongoClient mongoClient;
    CommandHelpers c;

    public DTWSimTest() {
        try {
            c = new CommandHelpers();
            try {
                mongoClient = new MongoClient("localhost", 27017);
                try {
                    c.startMongo(Boolean.TRUE);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            };
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Tests the DTW with extras.
     */
    @Test
    public void testDTWSimWithExtras() {
        DtwSimilarity s;
        try {
            s = new DtwSimilarity("withSelf", "allTasks", "");
            BasicDBObject file = new BasicDBObject("subject", "jy");
            s.selfSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfTaskDTWWithSelf").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfDTWWithSelf").find(file).count(), 1);
            s.diffSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffTaskDTWAllTasks").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffDTWAllTasks").find(file).count(), 1);
            s = new DtwSimilarity("", "", "SAT");
            s.selfSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfTaskDTWSAT").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfDTWSAT").find(file).count(), 1);
        } catch (Exception e) {

        }
    }

    /**
     * Tests the DTW without extras.
     */
    @Test
    public void testDTWSim() {
        DtwSimilarity s;
        try {
            s = new DtwSimilarity("", "", "");
            BasicDBObject file = new BasicDBObject("subject", "jy");
            s.selfSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfTaskDTW").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfDTW").find(file).count(), 1);
            s.diffSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffTaskDTW").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffDTW").find(file).count(), 1);
        } catch (Exception e) {

        }
    }
}
