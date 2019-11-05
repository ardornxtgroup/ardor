/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class ConvertTest {

    @Test
    public void testLongValueExact() {
        BigInteger longMaxValue = new BigInteger("" + Long.MAX_VALUE);
        BigInteger longMinValue = new BigInteger("" + Long.MIN_VALUE);

        // Non-overflow
        BigInteger bigInteger = BigInteger.ZERO;
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = BigInteger.ONE;
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = BigInteger.ZERO.subtract(BigInteger.ONE);
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = longMaxValue;
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = longMinValue;
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = new BigInteger("" + Integer.MIN_VALUE);
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = new BigInteger("" + Integer.MIN_VALUE).subtract(BigInteger.ONE);
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = new BigInteger("" + Integer.MAX_VALUE);
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        bigInteger = new BigInteger("" + Integer.MAX_VALUE).add(BigInteger.ONE);
        Assert.assertEquals(bigInteger.longValueExact(), androidLongValueExact(bigInteger));
        Assert.assertEquals(isLongValueExactOverflow(BigInteger.ONE), isAndroidLongValueExactOverflow(BigInteger.ONE));

        // Overflow
        Assert.assertEquals(isLongValueExactOverflow(longMaxValue.add(BigInteger.ONE)), isAndroidLongValueExactOverflow(longMaxValue.add(BigInteger.ONE)));
        Assert.assertEquals(isLongValueExactOverflow(longMaxValue.multiply(longMaxValue)), isAndroidLongValueExactOverflow(longMaxValue.multiply(longMaxValue)));
        Assert.assertEquals(isLongValueExactOverflow(longMinValue.subtract(BigInteger.ONE)), isAndroidLongValueExactOverflow(longMinValue.subtract(BigInteger.ONE)));
        Assert.assertEquals(isLongValueExactOverflow(longMinValue.multiply(longMinValue)), isAndroidLongValueExactOverflow(longMinValue.multiply(longMinValue)));
        Assert.assertEquals(isLongValueExactOverflow(longMinValue.multiply(longMaxValue)), isAndroidLongValueExactOverflow(longMinValue.multiply(longMaxValue)));
    }

    private long androidLongValueExact(BigInteger bigInteger) {
        long result = bigInteger.longValue();
        if (BigInteger.valueOf(result).equals(bigInteger)) {
            return result;
        } else {
            throw new ArithmeticException("BigInteger out of long range");
        }
    }

    private boolean isLongValueExactOverflow(BigInteger bigInteger) {
        try {
            bigInteger.longValueExact();
            return false;
        } catch (ArithmeticException e) {
            return true;
        }
    }

    private boolean isAndroidLongValueExactOverflow(BigInteger bigInteger) {
        try {
            androidLongValueExact(bigInteger);
            return false;
        } catch (ArithmeticException e) {
            return true;
        }
    }

    private static final int ITERATIONS = 100000000;

    @Test
    public void fullHashToIdOptimization() {
        Random r = new Random();
        byte[] hash = new byte[32];
        r.nextBytes(hash);

        long time = System.nanoTime();
        long newId = 0;
        for (int i = 0; i< ITERATIONS; i++) {
            newId += Convert.fullHashToId(hash);
        }
        long newTime = System.nanoTime() - time;
        time = System.nanoTime();
        long oldId = 0;
        for (int i = 0; i< ITERATIONS; i++) {
            oldId += fullHashToIdOld(hash);
        }
        long oldTime = System.nanoTime() - time;
        Assert.assertEquals(newId, oldId);
        Logger.logInfoMessage("fullHashToIdOptimization new time %f old time %f factor %f", (float)newTime/(float)1000000000, (float)oldTime/(float)1000000000, (float)oldTime/(float)newTime);
    }

    private static long fullHashToIdOld(byte[] hash) {
        if (hash == null || hash.length < 8) {
            throw new IllegalArgumentException("Invalid hash: " + Arrays.toString(hash));
        }
        BigInteger bigInteger = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
        return bigInteger.longValue();
    }
}
