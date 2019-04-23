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

package nxt.crypto;

import nxt.BlockchainTest;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class SimpleShamirSecretSharingTest extends BlockchainTest {

    private static final BigInteger ALICE_SECRET_PHRASE_128_BIT = new BigInteger("298106192037605529109565170145082624171");

    @Test
    public void wikipediaExample() {
        BigInteger prime = new BigInteger("1613");
        BigInteger secret = new BigInteger("1234");
        SecretShare[] allShares = new SimpleShamirSecretSharing().split(secret, 3, 5, prime, Crypto.getSecureRandom());

        // Works with 3 shares
        SecretShare[] shares1 = { allShares[0], allShares[2], allShares[3] };
        BigInteger reproducedSecret = new SimpleShamirSecretSharing().combine(shares1, prime);
        Assert.assertEquals(reproducedSecret, secret);

        // Works with 3 shares
        SecretShare[] shares2 = { allShares[0], allShares[1], allShares[4] };
        reproducedSecret = new SimpleShamirSecretSharing().combine(shares2, prime);
        Assert.assertEquals(reproducedSecret, secret);

        // Fails with 2 shares
        SecretShare[] shares3 = { allShares[0], allShares[4] };
        reproducedSecret = new SimpleShamirSecretSharing().combine(shares3, prime);
        Assert.assertNotEquals(reproducedSecret, secret);
    }

    @Test
    public void splitAndCombine() {
        SimpleShamirSecretSharing shamirSecretSharing = new SimpleShamirSecretSharing();
        SecretShare[] split = shamirSecretSharing.split(ALICE_SECRET_PHRASE_128_BIT, 3, 5, SecretSharingGenerator.PRIME_4096_BIT, new SecureRandom());
        Arrays.stream(split).forEach(s -> Assert.assertTrue(s.getShare().compareTo(BigInteger.ZERO) > 0 && s.getShare().compareTo(SecretSharingGenerator.PRIME_4096_BIT) < 0));
        BigInteger secretPhrase = shamirSecretSharing.combine(split, SecretSharingGenerator.PRIME_4096_BIT);
        Assert.assertEquals(ALICE_SECRET_PHRASE_128_BIT, secretPhrase);

        split = shamirSecretSharing.split(ALICE_SECRET_PHRASE_128_BIT, 2, 3, SecretSharingGenerator.PRIME_384_BIT, new SecureRandom());
        Arrays.stream(split).forEach(s -> Assert.assertTrue(s.getShare().compareTo(BigInteger.ZERO) > 0 && s.getShare().compareTo(SecretSharingGenerator.PRIME_384_BIT) < 0));
        secretPhrase = shamirSecretSharing.combine(split, SecretSharingGenerator.PRIME_384_BIT);
        Assert.assertEquals(ALICE_SECRET_PHRASE_128_BIT, secretPhrase);

        split = shamirSecretSharing.split(ALICE_SECRET_PHRASE_128_BIT, 2, 3, SecretSharingGenerator.PRIME_192_BIT, new SecureRandom());
        Arrays.stream(split).forEach(s -> Assert.assertTrue(s.getShare().compareTo(BigInteger.ZERO) > 0 && s.getShare().compareTo(SecretSharingGenerator.PRIME_192_BIT) < 0));
        secretPhrase = shamirSecretSharing.combine(split, SecretSharingGenerator.PRIME_192_BIT);
        Assert.assertEquals(ALICE_SECRET_PHRASE_128_BIT, secretPhrase);
    }

    @Test
    public void splitAndCombineRandomSecretPhrase() {
        // Generate the pieces
        SimpleShamirSecretSharing shamirSecretSharing = new SimpleShamirSecretSharing();
        SecretShare[] pieces = shamirSecretSharing.split(new BigInteger(chuckSecretPhrase.getBytes(StandardCharsets.UTF_8)), 4, 7, SecretSharingGenerator.PRIME_4096_BIT, new SecureRandom());

        // Select pieces and combine
        SecretShare[] selectedPieces = new SecretShare[]{pieces[1], pieces[3], pieces[5], pieces[6]};
        BigInteger combinedSecret = shamirSecretSharing.combine(selectedPieces, SecretSharingGenerator.PRIME_4096_BIT);
        Assert.assertEquals(chuckSecretPhrase, new String(combinedSecret.toByteArray(), StandardCharsets.UTF_8));

        // Select other pieces 5 out of 7 and combine
        selectedPieces = new SecretShare[]{pieces[0], pieces[2], pieces[3], pieces[6], pieces[4]};
        combinedSecret = shamirSecretSharing.combine(selectedPieces, SecretSharingGenerator.PRIME_4096_BIT);
        Assert.assertEquals(chuckSecretPhrase, new String(combinedSecret.toByteArray(), StandardCharsets.UTF_8));

        // Select only 3 out of 7 and combine
        selectedPieces = new SecretShare[]{pieces[2], pieces[3], pieces[6]};
        combinedSecret = shamirSecretSharing.combine(selectedPieces, SecretSharingGenerator.PRIME_4096_BIT);
        Assert.assertNotEquals(chuckSecretPhrase, new String(combinedSecret.toByteArray(), StandardCharsets.UTF_8));
    }

}
