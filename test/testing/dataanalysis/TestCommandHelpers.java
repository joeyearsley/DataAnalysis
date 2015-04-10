/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing.dataanalysis;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import dataanalysis.CommandHelpers;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 *
 * @author josephyearsley
 */
public class TestCommandHelpers {

    CommandHelpers c;
    MongoClient mongoClient;

    public TestCommandHelpers() {
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

    @Test(expected = MongoException.class)
    public void mongoOpenFail() {
        //Ensure no open connection
        try{c.closeMongo(Boolean.TRUE);}catch(Exception e){}
        try {
            c.startMongo(Boolean.FALSE);
        } catch (Exception e) {
        }
        Set<String> db = mongoClient.getDB("Dissertation").getCollectionNames();
        System.out.println(db);
    }

    @Test
    public void mongoOpenWorking() {
        try {
            c.startMongo(true);
            Set<String> diss = mongoClient.getDB("Dissertation").getCollectionNames();
            assertFalse(diss.isEmpty());
        } catch (Exception e) {

        }
    }

    @Test(expected = MongoException.class)
    public void mongoClose() {
        try {
            c.closeMongo(true);
        } catch (Exception e) {
        }
        List<String> db = mongoClient.getDatabaseNames();
    }

    @Test
    public void mongoCloseFail() {
        try {
            c.startMongo(true);
            c.closeMongo(false);
        } catch (Exception e) {
        }
        Set<String> db = mongoClient.getDB("Dissertation").getCollectionNames();
        assertFalse(db.isEmpty());
    }
    
    @Test
    public void graphing(){
        try{
            c.graphSimilarites("test graph","eps","test","dtw","2");
        }catch(Exception e){
            System.out.println(e);
        }
        File f = new File("/Users/josephyearsley/Documents/University/Dissertation/R/test graph.eps");
        assertTrue(f.exists());
    }
}
