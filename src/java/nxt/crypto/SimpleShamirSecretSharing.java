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

package nxt.crypto;

import java.math.BigInteger;
import java.util.Random;

public final class SimpleShamirSecretSharing implements SecretSharing {

    SimpleShamirSecretSharing() {}

    /**
     * Given a secret, split it into "available" shares where providing "needed" shares is enough to reproduce the secret.
     * All calculations are performed mod p, where p is a large prime number.
     * @param secret the secret
     * @param needed the number of shares needed to reproduce it
     * @param available the total number of shares
     * @param prime the prime number
     * @param random the random source
     * @return the secret shares
     */
    @Override
    public SecretShare[] split(final BigInteger secret, int needed, int available, BigInteger prime, Random random) {
        // Create a polynomial of degree representing the number of needed pieces
        final BigInteger[] coeff = new BigInteger[needed];
        coeff[0] = secret; // our secret is encoded in the polynomial free term

        // The rest of the coefficients are selected randomly as integers mod p
        for (int i = 1; i < needed; i++) {
            BigInteger r;
            do {
                r = new BigInteger(prime.bitLength(), random);
            } while (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(prime) >= 0);
            // Reduce the coefficient size from the scale of the prime field to the scale of the secret itself
            // the rational is that there is no point to hold huge pieces if the secret itself is small.
            // For example a 400 bit secret will use 4096 bit prime field so this reduces the secret size by a scale
            // 1:10
            coeff[i] = r.mod(secret);
        }

        // Clearly the value of this polynomial at x=0 is our secret
        // We generate the shares by running x from 1 to the number of available shares calculating the polynomial value
        // mod p at each point
        final SecretShare[] shares = new SecretShare[available];
        for (int x = 1; x <= available; x++) {
            BigInteger accum = secret;
            for (int exp = 1; exp < needed; exp++) {
                accum = accum.add(coeff[exp].multiply(BigInteger.valueOf(x).pow(exp).mod(prime))).mod(prime);
            }
            shares[x - 1] = new SecretShare(x, accum);
        }

        // The resulting points represent the secret shares
        return shares;
    }

    /**
     * Given the needed number of shares or more, reproduce the original polynomial and extract the secret from its free
     * term. All calculations are performed mod p, where p is a large prime number
     * @param shares the shares represention points over the polynomial
     * @param prime the prime number
     * @return the original secret reproduced from the free term of the polynomial which passes through these points
     */
    @Override
    public BigInteger combine(final SecretShare[] shares, final BigInteger prime) {
        // An optimized approach to using Lagrange polynomials to find L(0) (the free term)
        // See https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing "Computationally Efficient Approach"
        BigInteger accum = BigInteger.ZERO;
        for (int formula = 0; formula < shares.length; formula++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            for (int count = 0; count < shares.length; count++) {
                if (formula == count)
                    continue; // If not the same value

                int startposition = shares[formula].getX();
                int nextposition = shares[count].getX();

                numerator = numerator.multiply(BigInteger.valueOf(nextposition).negate()).mod(prime); // (numerator * -nextposition) % prime;
                denominator = denominator.multiply(BigInteger.valueOf(startposition - nextposition)).mod(prime); // (denominator * (startposition - nextposition)) % prime;
            }
            BigInteger value = shares[formula].getShare();
            BigInteger tmp = value.multiply(numerator).multiply(denominator.modInverse(prime));
            accum = prime.add(accum).add(tmp).mod(prime); //  (prime + accum + (value * numerator * modInverse(denominator))) % prime;
        }
        return accum;
    }
}