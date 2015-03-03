/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dataanalysis;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 *
 * @author joe yearsley
 */
public class Similarity {

     public static void Self() throws IOException {
        CSVReader reader = null;
        CSVReader reader2 = null;
        String alpha, alpha2, beta, beta2;
        int numberOfLines = 0;
        int betaAvrg = 0;
        int alphaAvrg = 0;
        String fileLoc = "/Users/josephyearsley/Documents/University/Data/";
        /**
         * Loop through data directory, check its not empty.
         * Go through each file, ensuring its not hidden or directory.
         * Then check if its already converted, if not then convert and write
         */
        File dir = new File(fileLoc);
        File[] directoryListing = dir.listFiles();
        /**
         * Split into persons name, test name, and test attempt
         * Then go through comparing similarities to each other.
         * Try not to repeat for loops, i.e. don't over compare!
         * Once done output all similarities
         * Do self similiarity for every task, then the average over all tasks
         */
        if (directoryListing != null) {
            char checkFirst = 'f' ;
            char checkSecond = 'm' ;
            for (File child : directoryListing) {
                if (!child.isHidden() && !child.isDirectory()) {
                    char first = child.getName().charAt(0);
                    char second = child.getName().charAt(1);
                    if(first == checkFirst && second == checkSecond){
                        alpha = reader.readNext()[0];
                        beta  =   reader.readNext()[0];
                        for (File child2 : directoryListing) {
                            if(child2.getName() != child.getName()){
                                //if not equal then add to working out similarity
                                alpha2 = reader2.readNext()[0];
                                beta2 = reader2.readNext()[0];
                            }
                        }
                    }
                    //Get BaseName
                    /* 
                    String name = child.getName();
                    int pos = name.lastIndexOf(".");
                    if (pos > 0) {
                        //Initials of person
                        String checkName = Character.toString(name.charAt(0)) 
                               + Character.toString(name.charAt(1));
                    }*/
                    //Check that the file hasn't already been converted
                    File ex = new File(fileLoc + "Similarity/" + "fm" + "sim.csv");
                    if (!ex.exists()) {
                        
                    }
                }
            }
        }
     }
     
     /**
      * 
      * @param a - Array A
      * @param b - Array B
      * @return cosine similarity
      */
     protected double cosineSim(int[] a,int[]  b){
         double x = 0;
         int size = a.length;
         double magA = 0;
         double magB = 0;
         double dot = 0;
         for(int i=0; i<size; i++){
             magA += Math.sqrt(a[i]*a[i]);
             magB += Math.sqrt(b[i] * b[i]);
             dot += a[i] * b[i];
         }
         x = dot /(magA*magB);
         return x;
     }
}
