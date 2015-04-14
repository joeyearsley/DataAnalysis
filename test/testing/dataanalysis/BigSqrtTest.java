package testing.dataanalysis;

import dataanalysis.BigSqrt;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To test the square rooting class.
 *
 * @author josephyearsley
 */
public class BigSqrtTest {

    public BigSqrtTest() {
    }

    @Test
    public void normal() {
        assertEquals("Square root of 4 is 2", BigSqrt.sqrt(BigInteger.valueOf(4)).get().compareTo(BigDecimal.valueOf(2)), 0);
        assertEquals("Square root of 9 is 3", BigSqrt.sqrt(BigInteger.valueOf(9)).get().compareTo(BigDecimal.valueOf(3)), 0);
        assertEquals("Square root of 1 million is 1000", BigSqrt.sqrt(BigInteger.valueOf(1000000)).get().compareTo(BigDecimal.valueOf(1000)), 0);
    }

    @Test
    public void notNormal() {
        assertFalse("Square root of 4 is not 1", BigSqrt.sqrt(BigInteger.valueOf(4)).get().toString().equals("1"));
        assertFalse("Square root of 5 is not 10", BigSqrt.sqrt(BigInteger.valueOf(5)).get().toString().equals("10"));
        assertFalse("Square root of 1000000 is not 1000.3", BigSqrt.sqrt(BigInteger.valueOf(1000000)).get().toString().equals("1000.3"));
    }
}
