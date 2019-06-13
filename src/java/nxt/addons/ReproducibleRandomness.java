/*
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

package nxt.addons;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * The purpose of this class is to make sure all contract runners will generate the same random numbers given the same random seed.
 * Unfortunately, the default SecureRandom implementation in Java 9+ does not respect this property i.e. given the same random seed
 * even two different secure random objects in the same process will generate different numbers.
 * The java.util.Random implementation is reproducible, but is trivial to predict.
 * We therefore rely on the "SHA1PRNG" algorithm (Java 8 default) which is reproducible and provides reasonable level
 * of security and interoperability between Java versions.
 * This class delegates all random number requests to the actual secure random.
 */
public class ReproducibleRandomness implements RandomnessSource {

    private long seed;
    SecureRandom secureRandom;

    {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized void setSeed(long seed) {
        this.seed = seed;
        secureRandom.setSeed(seed);
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public void nextBytes(byte[] bytes) {
        secureRandom.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return secureRandom.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return secureRandom.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return secureRandom.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return secureRandom.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return secureRandom.nextFloat();
    }

    @Override
    public double nextDouble() {
        return secureRandom.nextDouble();
    }

    @Override
    public IntStream ints(long streamSize) {
        return secureRandom.ints(streamSize);
    }

    @Override
    public IntStream ints() {
        return secureRandom.ints();
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return secureRandom.ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return secureRandom.ints(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long streamSize) {
        return secureRandom.longs(streamSize);
    }

    @Override
    public LongStream longs() {
        return secureRandom.longs();
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return secureRandom.longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return secureRandom.longs(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return secureRandom.doubles(streamSize);
    }

    @Override
    public DoubleStream doubles() {
        return secureRandom.doubles();
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return secureRandom.doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return secureRandom.doubles(randomNumberOrigin, randomNumberBound);
    }
}
