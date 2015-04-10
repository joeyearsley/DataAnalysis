/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing.dataanalysis;

import dataanalysis.BigSqrt;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To test the square rooting class.
 * @author josephyearsley
 */
public class TestBigSqrt {
    
    public TestBigSqrt() {
    }
    
    @Test
    public void normal(){
        assertEquals(BigSqrt.sqrt(BigInteger.valueOf(4)).get().compareTo(BigDecimal.valueOf(2)), 0);
        assertEquals(BigSqrt.sqrt(BigInteger.valueOf(9)).get().compareTo(BigDecimal.valueOf(3)), 0);
        assertEquals(BigSqrt.sqrt(BigInteger.valueOf(1000000)).get().compareTo(BigDecimal.valueOf(1000)), 0);
    }
    
    @Test
    public void notNormal(){
        assertFalse(BigSqrt.sqrt(BigInteger.valueOf(4)).get().toString().equals("1"));
        assertFalse(BigSqrt.sqrt(BigInteger.valueOf(5)).get().toString().equals("10"));
        assertFalse(BigSqrt.sqrt(BigInteger.valueOf(1000000)).get().toString().equals("1000.3"));
    }
}
