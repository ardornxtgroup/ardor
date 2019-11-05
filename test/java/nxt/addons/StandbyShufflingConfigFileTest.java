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

package nxt.addons;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.account.HoldingType;
import nxt.http.APICall;
import nxt.util.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import static nxt.blockchain.ChildChain.IGNIS;

public class StandbyShufflingConfigFileTest extends BlockchainTest {
    @BeforeClass
    public static void init() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Map<String, String> properties = new HashMap<>();
            properties.put("nxt.addOns", "nxt.addons.StandbyShuffling; nxt.addons.StartStandbyShuffling");
            properties.put("nxt.disableSecurityPolicy", "true");
            initNxt(properties);
            initBlockchainTest();
            return null;
        });
    }

    @After
    public void stopAll() {
        JO response = new APICall.Builder("stopStandbyShuffler").call();
        Logger.logDebugMessage("Stopped %d StandbyShufflers.", response.get("stopped"));
        Assert.assertNotNull(response.get("stopped"));
    }

    @Test
    public void test() {
        String recipientPublicKey = "285c4f326fffd59460d1374888ae0219f3f4706586db3510b514353f8a410306";

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipientPublicKey)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        JO standbyShufflersJSON = new APICall.Builder("getStandbyShufflers")
                .chain(IGNIS.getId())
                .unsignedLongParam("account", ALICE.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .call();

        Assert.assertNull(standbyShufflersJSON.get("errorCode"));
        JA standbyShufflers = standbyShufflersJSON.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(1, standbyShufflers.size());
        JO standbyShuffler = standbyShufflers.get(0);
        Assert.assertNotNull(standbyShuffler);

        response = new APICall.Builder("stopStandbyShuffler").call();
        Assert.assertNull(response.get("errorCode"));
        Assert.assertEquals(1, response.getInt("stopped"));

        standbyShuffler.put("secretPhrase", ALICE.getSecretPhrase());
        StartStandbyShuffling.startStandbyShufflers(standbyShufflersJSON.toJSONObject());

        response = new APICall.Builder("getStandbyShufflers")
                .chain(IGNIS.getId())
                .unsignedLongParam("account", ALICE.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .call();

        Assert.assertNull(response.get("errorCode"));
        standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(1, standbyShufflers.size());
        standbyShuffler = standbyShufflers.get(0);
        Assert.assertNotNull(standbyShuffler);
        Assert.assertEquals(ALICE.getId(), standbyShuffler.getEntityId("account"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShuffler.getString("accountRS"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("chain"));
        Assert.assertEquals(HoldingType.COIN.getCode(), standbyShuffler.getByte("holdingType"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("holding"));
        Assert.assertEquals("0", standbyShuffler.getString("minAmount"));
        Assert.assertEquals("0", standbyShuffler.getString("maxAmount"));
        Assert.assertEquals(Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, standbyShuffler.getInt("minParticipants"));
        JA recipientPublicKeys = standbyShuffler.getArray("recipientPublicKeys");
        Assert.assertEquals(1, recipientPublicKeys.size());
        Assert.assertEquals(recipientPublicKey, recipientPublicKeys.getObject(0));
    }
}
