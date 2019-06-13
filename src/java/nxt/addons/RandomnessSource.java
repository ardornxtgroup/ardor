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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public interface RandomnessSource {

        void setSeed(long seed);

        long getSeed();

        void nextBytes(byte[] bytes);

        int nextInt();

        int nextInt(int bound);

        long nextLong();

        boolean nextBoolean();

        float nextFloat();

        double nextDouble();

        IntStream ints(long streamSize);

        IntStream ints();

        IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound);

        IntStream ints(int randomNumberOrigin, int randomNumberBound);

        LongStream longs(long streamSize);

        LongStream longs();

        LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound);

        LongStream longs(long randomNumberOrigin, long randomNumberBound);

        DoubleStream doubles(long streamSize);

        DoubleStream doubles();

        DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound);

        DoubleStream doubles(double randomNumberOrigin, double randomNumberBound);
}
