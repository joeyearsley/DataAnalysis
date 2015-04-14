package testing.dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import dataanalysis.CommandHelpers;
import dataanalysis.DataConvert;
import java.io.File;
import java.io.FileReader;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To test that the raw data is converted properly.
 *
 * @author josephyearsley
 */
public class ConversionTest {

    MongoClient mongoClient;
    CommandHelpers c;

    public ConversionTest() {
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
     * Test that the Cosine conversion has worked correctly.
     */
    @Test
    public void testCosineConvert() {
        DataConvert dc;
        try {
            dc = new DataConvert();
            dc.convertCS();
        } catch (Exception e) {
        }
        File f = new File("../Data/Converted/Cosine/jysj1.csv");
        assertTrue("See if the file exists", f.exists());
        //See that the file is in the right format!
        BasicDBObject file = new BasicDBObject("subject", "jy")
                .append("task", "sj")
                .append("timesDone", 1);
        assertEquals("See data is put into the db", mongoClient.getDB("Dissertation").getCollection("columnVector").find(file).count(), 1);
        try {
            CSVReader reader = new CSVReader(new FileReader(f), ',');
            String[] nextLine;
            int iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                iteration++;
            }
            assertEquals("Should have 2 numbers", iteration, 2);
        } catch (Exception e) {

        }
    }

    /**
     * Test that the DTW conversion has worked correctly.
     */
    @Test
    public void testDtwConvert() {
        DataConvert dc;
        try {
            dc = new DataConvert();
            dc.convertTW();
        } catch (Exception e) {
        }
        File f = new File("../Data/Converted/TimeWarping/jysj1.csv");
        assertTrue("See if the file exist", f.exists());
        BasicDBObject file = new BasicDBObject("subject", "jy")
                .append("task", "sj")
                .append("timesDone", 1);
        assertEquals("See data is put into the DB", mongoClient.getDB("Dissertation").getCollection("timeWarping").find(file).count(), 1);
    }
}
