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
import java.util.Arrays;

public class SecretSharingGeneratorTest {
    private static final BigInteger ALICE_SECRET_PHRASE_128_BIT = new BigInteger("298106192037605529109565170145082624171");
    private static final String aliceSecretPhrase = BlockchainTest.aliceSecretPhrase;
    private static final String chuckSecretPhrase = BlockchainTest.chuckSecretPhrase;

    @Test
    public void secretPhraseTwelveWordsToBigIntegerAndBack() {
        BigInteger bigInteger = SecretSharingGenerator.to128bit(aliceSecretPhrase.split(" "));
        String[] secretPhraseWords = SecretSharingGenerator.from128bit(bigInteger);
        String secretPhrase = String.join(" ", secretPhraseWords);
        Assert.assertEquals(aliceSecretPhrase, secretPhrase);
        BigInteger n128 = SecretSharingGenerator.to128bit(secretPhraseWords);
        Assert.assertEquals(ALICE_SECRET_PHRASE_128_BIT, n128);
        secretPhraseWords = SecretSharingGenerator.from128bit(n128);
        secretPhrase = String.join(" ", secretPhraseWords);
        Assert.assertEquals(aliceSecretPhrase, secretPhrase);
    }

    @Test
    public void splitAndCombine12wordsSecretPhrase() {
        // Generate the pieces
        String[] pieces = SecretSharingGenerator.split(aliceSecretPhrase, 5, 3, BigInteger.ZERO);

        // Select pieces and combine
        String[] selectedPieces = new String[]{pieces[0], pieces[2], pieces[4]};
        String combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals(aliceSecretPhrase, combinedSecret);

        // Select pieces and combine
        selectedPieces = new String[]{pieces[0], pieces[2], pieces[4]};
        combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals(aliceSecretPhrase, combinedSecret);

        // Again with 2 out of 3
        pieces = SecretSharingGenerator.split(aliceSecretPhrase, 3, 2, BigInteger.ZERO);
        selectedPieces = new String[]{pieces[0], pieces[2]};
        combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals(aliceSecretPhrase, combinedSecret);
    }

    @Test
    public void splitAndCombineRandomSecretPhrase() {
        // Generate the pieces
        String[] pieces = SecretSharingGenerator.split(chuckSecretPhrase, 7, 4, BigInteger.ZERO);

        // Select pieces and combine
        String[] selectedPieces = new String[]{pieces[1], pieces[3], pieces[5], pieces[6]};
        String combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals(chuckSecretPhrase, combinedSecret);

        // Select pieces and combine
        selectedPieces = new String[]{pieces[1], pieces[2], pieces[4], pieces[6]};
        combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals(chuckSecretPhrase, combinedSecret);
    }

    @Test
    public void shortPassphrase() {
        String[] pieces = SecretSharingGenerator.split("aaa", 7, 4, BigInteger.ZERO);
        System.out.println(Arrays.toString(pieces));
        String[] selectedPieces = new String[]{pieces[1], pieces[2], pieces[4], pieces[6]};
        String combinedSecret = SecretSharingGenerator.combine(selectedPieces);
        Assert.assertEquals("aaa", combinedSecret);
    }

    @Test
    public void validityChecks() {
        try {
            SecretSharingGenerator.split(aliceSecretPhrase, 4, 1, BigInteger.ZERO);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        try {
            SecretSharingGenerator.split(aliceSecretPhrase, 4, 5, BigInteger.ZERO);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
