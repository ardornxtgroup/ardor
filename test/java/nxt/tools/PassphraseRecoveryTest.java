/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PassphraseRecoveryTest {

    private static Map<Long, byte[]> publicKeys;

    @BeforeClass
    public static void loadPublicKeys() {
        publicKeys = PassphraseRecovery.getPublicKeys();
    }

    @Test
    public void searchAnyPassphrase() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        int[] positions = {9, 18};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        PassphraseRecovery.Scanner scanner = new PassphraseRecovery.Scanner(publicKeys, positions, wildcard, PassphraseRecovery.getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }

    @Test
    public void searchSpecificPassphrase() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        int[] positions = {27, 9};
        for (Integer position : positions) {
            wildcard[position] = '*';
        }
        String rsAccount = "ARDOR-XK4R-7VJU-6EQG-7R335";
        long id = Convert.parseAccountId(rsAccount);
        byte[] publicKey = Account.getPublicKey(id);
        Map<Long, byte[]> publicKeys = new HashMap<>();
        publicKeys.put(id, publicKey);
        PassphraseRecovery.Scanner scanner = new PassphraseRecovery.Scanner(publicKeys, positions, wildcard, PassphraseRecovery.getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }

    @Test
    public void searchSingleTypo() {
        char[] wildcard = BlockchainTest.aliceSecretPhrase.toCharArray();
        wildcard[18] = '*';
        PassphraseRecovery.Scanner scanner = new PassphraseRecovery.Scanner(publicKeys, new int[0], wildcard, PassphraseRecovery.getDefaultDictionary());
        PassphraseRecovery.Solution solution = scanner.scan();
        Assert.assertEquals("ARDOR-XK4R-7VJU-6EQG-7R335", solution.getRsAccount());
    }

}
