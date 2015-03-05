package dataanalysis;

/*
 *                    PDB web development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 *
 * Created on Aug 16, 2009
 * Created by Andreas Prlic
 *
 */

import java.math.BigDecimal;
import java.math.BigInteger;

/** calculate a more precise SQRT.
 *
 * Modified from:
 * http://www.merriampark.com/bigsqrt.htm
 *
 * @author Andreas Prlic
 *
 */
public final class BigSqrt {

    private static final BigDecimal ZERO = new BigDecimal("0");
    private static final BigDecimal ONE = new BigDecimal("1");
    private static final BigDecimal TWO = new BigDecimal("2");

    public static final int DEFAULT_MAX_ITERATIONS = 50;
    public static final int DEFAULT_SCALE = 10;

    private final BigDecimal number;
    private BigDecimal error = ZERO;
    private int iterations;

    private BigSqrt(BigDecimal number) {
        this.number = number;
    }

    public BigDecimal error() {
        return error;
    }

    public BigDecimal number() {
        return number;
    }

    public int iterations() {
        return iterations;
    }

    public BigDecimal get() {
        return get(DEFAULT_SCALE, DEFAULT_MAX_ITERATIONS);
    }

    public BigDecimal get(int scale, int maxIterations) {
        if (number.compareTo(ZERO) <= 0) throw new IllegalArgumentException();
        BigDecimal initialGuess = getInitialApproximation(number);
        BigDecimal lastGuess;
        BigDecimal guess = new BigDecimal(initialGuess.toString());
        iterations = 0;
        boolean more = true;
        while (more) {
            lastGuess = guess;
            guess = number.divide(guess, scale, BigDecimal.ROUND_HALF_UP);
            guess = guess.add(lastGuess);
            guess = guess.divide(TWO, scale, BigDecimal.ROUND_HALF_UP);
            error = number.subtract(guess.multiply(guess));
            if (++iterations >= maxIterations) more = false;
            else if (lastGuess.equals(guess)) more = error.abs().compareTo(ONE) >= 0;
        }
        return guess;
    }

    private static BigDecimal getInitialApproximation(BigDecimal n) {
        BigInteger integerPart = n.toBigInteger();
        int length = integerPart.toString().length();
        if ((length & 1) == 0) length--;
        length >>= 1;
        return ONE.movePointRight(length);
    }

    public static BigSqrt sqrt(BigDecimal bigDecimal) {
        return new BigSqrt(bigDecimal);
    }

    public static BigSqrt sqrt(BigInteger bigInteger) {
        return new BigSqrt(new BigDecimal(bigInteger));
    }
    
}