package testing.dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import dataanalysis.CommandHelpers;
import org.junit.Test;
import dataanalysis.Similarity;
import java.io.File;
import java.io.FileReader;
import static org.junit.Assert.*;

/**
 * To test that similarity outputs correctly.
 *
 * @author josephyearsley
 */
public class SimilarityTest {

    MongoClient mongoClient;
    CommandHelpers c;

    public SimilarityTest() {
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
     * Tests that the file and DB are correctly output.
     */
    @Test
    public void TestCosineWrong() {
        Similarity s;
        try {
            s = new Similarity();
            s.calcSim();
        } catch (Exception e) {
        }
        File f1 = new File("../Data/Converted/Cosine/Similarity/diffSim/jy.csv");
        File f2 = new File("../Data/Converted/Cosine/Similarity/selfSim/jy.csv");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        //See that the file is in the right format!
        BasicDBObject file = new BasicDBObject("subject", "jy");
        assertEquals(mongoClient.getDB("Dissertation").getCollection("diffCosineWrong").find(file).count(), 1);
        assertEquals(mongoClient.getDB("Dissertation").getCollection("selfCosineWrong").find(file).count(), 1);
        try {
            CSVReader reader = new CSVReader(new FileReader(f1), ',');
            String[] nextLine;
            int iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                iteration++;
            }
            assertEquals(iteration, 1);
            reader = new CSVReader(new FileReader(f2), ',');
            iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                iteration++;
            }
            assertEquals(iteration, 1);
        } catch (Exception e) {

        }
    }

    /**
     * Tests that the file and DB are correctly output.
     */
    @Test
    public void TestDTWWrong() {
        Similarity s;
        try {
            s = new Similarity();
            s.similarityCalc();
        } catch (Exception e) {
        }
        File f1 = new File("../Data/Converted/TimeWarping/Similarity/selfSim/jy.csv");
        File f2 = new File("../Data/Converted/TimeWarping/Similarity/diffSim/jy.csv");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
        //See that the file is in the right format!
        BasicDBObject file = new BasicDBObject("subject", "jy");
        assertEquals(mongoClient.getDB("Dissertation").getCollection("diffDTWWrong").find(file).count(), 1);
        assertEquals(mongoClient.getDB("Dissertation").getCollection("selfDTWWrong").find(file).count(), 1);
        try {
            CSVReader reader = new CSVReader(new FileReader(f1), ',');
            String[] nextLine;
            int iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                iteration++;
            }
            assertEquals(iteration, 1);
            reader = new CSVReader(new FileReader(f2), ',');
            iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                iteration++;
            }
            assertEquals(iteration, 1);
        } catch (Exception e) {

        }
    }
}
