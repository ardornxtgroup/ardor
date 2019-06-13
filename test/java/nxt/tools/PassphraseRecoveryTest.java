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

package nxt.tools;

import nxt.BlockchainTest;
import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.tools.PassphraseRecovery.Scanner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static nxt.tools.PassphraseRecovery.getDefaultDictionary;

public class PassphraseRecoveryTest {
    private static Map<Long, byte[]> publicKeys;

    @BeforeClass
    public static void loadPublicKeys() {
        publicKeys = Stream.of(
                BlockchainTest.aliceSecretPhrase,
                BlockchainTest.bobSecretPhrase2,
                BlockchainTest.chuckSecretPhrase,
                BlockchainTest.daveSecretPhrase,
                BlockchainTest.rikerSecretPhrase
        ).map(Crypto::getPublicKey)
                .collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    @Test
    public void searchAnyPassphrase() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        int[] positions = {9, 18};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        Scanner scanner = new Scanner(publicKeys, positions, wildcard, getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }

    @Test
    public void searchAnyPassphraseNotFound() {
        char[] wildcard = "some non existing account".toCharArray();
        int[] positions = {2, 7};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        Scanner scanner = new Scanner(publicKeys, positions, wildcard, getDefaultDictionary());
        Assert.assertEquals(PassphraseRecovery.NO_SOLUTION, scanner.scan());
    }

    @Test
    public void searchAnyPassphraseEmptyKeys() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        int[] positions = {9, 18};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        Scanner scanner = new Scanner(emptyMap(), positions, wildcard, getDefaultDictionary());
        Assert.assertEquals(PassphraseRecovery.NO_SOLUTION, scanner.scan());
    }

    @Test
    public void searchSpecificPassphrase() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        int[] positions = {27, 9};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        long id = 5873880488492319831L;
        Map<Long, byte[]> singleton = Collections.singletonMap(id, publicKeys.get(id));
        Scanner scanner = new Scanner(singleton, positions, wildcard, getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }

    @Test
    public void searchSingleTypo() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        wildcard[18] = '*';
        Scanner scanner = new Scanner(publicKeys, new int[0], wildcard, getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }
}
