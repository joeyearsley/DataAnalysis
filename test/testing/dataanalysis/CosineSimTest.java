package testing.dataanalysis;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import dataanalysis.CommandHelpers;
import dataanalysis.CosineSimilarity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To test that the Cosine outputs the files and DB correctly.
 *
 * @author josephyearsley
 */
public class CosineSimTest {

    MongoClient mongoClient;
    CommandHelpers c;

    public CosineSimTest() {
        try {
            c = new CommandHelpers();
            try {
                mongoClient = new MongoClient("localhost", 27017);
                try {
                    c.startMongo(Boolean.TRUE);
                } catch (Exception e) {
                }
            } catch (Exception e) {
            };
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Tests the Cosine with extras.
     */
    @Test
    public void testCosineSimWithExtras() {
        CosineSimilarity s;
        try {
            s = new CosineSimilarity("WithSelf", "AllTasks");
            s.consolidate();
            BasicDBObject file = new BasicDBObject("subject", "jy");
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("columnVectorConsolidated").find(file).count(), 5);
            s.diffSim();
            file = new BasicDBObject("subject", "jy");
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffTaskCosineAllTasks").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffCosineAllTasks").find(file).count(), 1);
            s.selfSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfTaskCosineWithSelf").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfCosineWithSelf").find(file).count(), 1);
            s.selfSimAllSame();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("sATaskCosine").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("sACosine").find(file).count(), 1);
        } catch (Exception e) {

        }

    }

    /**
     * Tests the Cosine without extras.
     */
    @Test
    public void testCosineSim() {
        CosineSimilarity s;
        try {
            s = new CosineSimilarity("", "");
            BasicDBObject file = new BasicDBObject("subject", "jy");
            s.diffSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffTaskCosine").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("diffCosine").find(file).count(), 1);
            s.selfSim();
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfTaskCosine").find(file).count(), 5);
            assertEquals("Ensure the right number of documents are in the DB", mongoClient.getDB("Dissertation").getCollection("selfCosine").find(file).count(), 1);
            Integer[] a = new Integer[]{1, 2, 3};
            Integer[] b = new Integer[]{1, 2, 3};
            assertEquals("Ensure that the cosine similarity is correct", s.cosineSimilarity(a, b).doubleValue(), 1, 0.00001);
        } catch (Exception e) {

        }
    }
}
