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
import nxt.Tester;
import nxt.account.HoldingType;
import nxt.http.APICall;
import nxt.http.callers.GetShufflersCall;
import nxt.http.shuffling.ShufflingUtil;
import nxt.shuffling.ShufflingParticipantHome;
import nxt.shuffling.ShufflingStage;
import nxt.util.Convert;
import nxt.util.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nxt.blockchain.ChildChain.BITSWIFT;
import static nxt.blockchain.ChildChain.IGNIS;
import static nxt.blockchain.FxtChain.FXT;

public class StandbyShufflingTest extends BlockchainTest {

    private static final String recipient1SecretPhrase = "ZFRvcc_2gEBtAZHr9Y9aVEJWYGedt91veClwKHTePQq5kNsLkL";
    private static final String recipient2SecretPhrase = "Q_O-8IHc2yE_oahBIW_q0NmEQerVOQW9q-iPeKd4ArPAFcpliI";
    private static final String recipient3SecretPhrase = "f2PpG_t8Fa7OJxiL9my3jij2aV-HY-hmLDeOMqcafjJGRJXnFK";
    private static final String recipient4SecretPhrase = "1Qqb9eXuQHumsIEQVPEO1GRrZQXyV_xq3mZfJNRoJt17gqKAxp";
    private static final String recipient5SecretPhrase = "V6anxZWGtktMW8Uo8fllVQik9Hg4008yDK8JbS_rJvLfxmOrlo";

    private static Tester RECIPIENT1;
    private static Tester RECIPIENT2;
    private static Tester RECIPIENT3;
    private static Tester RECIPIENT4;
    private static Tester RECIPIENT5;

    @BeforeClass
    public static void init() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Map<String, String> properties = new HashMap<>();
            properties.put("nxt.addOns", "nxt.addons.StandbyShuffling");
            properties.put("nxt.disableSecurityPolicy", "true");
            initNxt(properties);
            initBlockchainTest();

            RECIPIENT1 = new Tester(recipient1SecretPhrase);
            RECIPIENT2 = new Tester(recipient2SecretPhrase);
            RECIPIENT3 = new Tester(recipient3SecretPhrase);
            RECIPIENT4 = new Tester(recipient4SecretPhrase);
            RECIPIENT5 = new Tester(recipient5SecretPhrase);

            return null;
        });
    }

    private static List<String> allRecipientsPublicKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(RECIPIENT1.getPublicKeyStr());
        keys.add(RECIPIENT2.getPublicKeyStr());
        keys.add(RECIPIENT3.getPublicKeyStr());
        keys.add(RECIPIENT4.getPublicKeyStr());
        keys.add(RECIPIENT5.getPublicKeyStr());
        return keys;
    }

    @After
    public void stopAll() {
        JO response = new APICall.Builder("stopStandbyShuffler").call();
        Logger.logDebugMessage("Stopped %d StandbyShufflers.", response.get("stopped"));
        Assert.assertNotNull(response.get("stopped"));
        response = new APICall.Builder("stopShuffler").call();
        Assert.assertTrue(response.getBoolean("stoppedAllShufflers"));
        Logger.logDebugMessage("Stopped all Shufflers.");
    }

    @Test
    public void startCoin() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));
        JO standbyShuffler = response.getJo("standbyShuffler");
        Assert.assertNotNull(standbyShuffler);
        Assert.assertEquals(ALICE.getId(), standbyShuffler.getEntityId("account"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShuffler.getString("accountRS"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("chain"));
        Assert.assertEquals(HoldingType.COIN.getCode(), standbyShuffler.getByte("holdingType"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("holding"));
        Assert.assertEquals("0", standbyShuffler.getString("minAmount"));
        Assert.assertEquals("0", standbyShuffler.getString("maxAmount"));
        Assert.assertEquals(Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, standbyShuffler.getInt("minParticipants"));
        Assert.assertEquals(recipients, standbyShuffler.get("recipientPublicKeys"));
    }

    @Test
    public void startAsset() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.ASSET.getCode())
                .param("holding", "4348103880042995903")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));
        JO standbyShuffler = response.getJo("standbyShuffler");
        Assert.assertNotNull(standbyShuffler);
        Assert.assertEquals(ALICE.getId(), standbyShuffler.getEntityId("account"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShuffler.getString("accountRS"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("chain"));
        Assert.assertEquals(HoldingType.ASSET.getCode(), standbyShuffler.getByte("holdingType"));
        Assert.assertEquals("4348103880042995903", standbyShuffler.getString("holding"));
        Assert.assertEquals("0", standbyShuffler.getString("minAmount"));
        Assert.assertEquals("0", standbyShuffler.getString("maxAmount"));
        Assert.assertEquals(Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, standbyShuffler.getInt("minParticipants"));
        Assert.assertEquals(recipients, standbyShuffler.get("recipientPublicKeys"));
    }

    @Test
    public void startCurrency() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.CURRENCY.getCode())
                .param("holding", "1")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));
        JO standbyShuffler = response.getJo("standbyShuffler");
        Assert.assertNotNull(standbyShuffler);
        Assert.assertEquals(ALICE.getId(), standbyShuffler.getEntityId("account"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShuffler.getString("accountRS"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("chain"));
        Assert.assertEquals(HoldingType.CURRENCY.getCode(), standbyShuffler.getByte("holdingType"));
        Assert.assertEquals("1", standbyShuffler.getString("holding"));
        Assert.assertEquals("0", standbyShuffler.getString("minAmount"));
        Assert.assertEquals("0", standbyShuffler.getString("maxAmount"));
        Assert.assertEquals(Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, standbyShuffler.getInt("minParticipants"));
        Assert.assertEquals(recipients, standbyShuffler.get("recipientPublicKeys"));
    }

    @Test
    public void startUnknownChain() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(FXT.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(5, response.getInt("errorCode"));
    }

    @Test
    public void startUnknownAccount() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase("an unknown account secret phrase")
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(5, response.getInt("errorCode"));
    }

    @Test
    public void startIncorrectHoldingType() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase("an unknown account secret phrase")
                .param("holdingType", 42)
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(4, response.getInt("errorCode"));
    }

    @Test
    public void startIncorrectHoldingCoin() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", "42")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(4, response.getInt("errorCode"));
    }

    @Test
    public void startIncorrectPublicKey() {
        byte[] badPublicKey = new byte[4];
        Arrays.fill(badPublicKey, (byte) 0);
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", Collections.singletonList(Convert.toHexString(badPublicKey)))
                .call();

        Assert.assertEquals(4, response.getInt("errorCode"));
    }

    @Test
    public void startUsedPublicKey() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", Collections.singletonList(BOB.getPublicKeyStr()))
                .call();

        Assert.assertEquals(4, response.getInt("errorCode"));
    }

    @Test
    public void startUnknownAsset() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.ASSET.getCode())
                .param("holding", 42)
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(5, response.getInt("errorCode"));
    }

    @Test
    public void startMinParticipantsZero() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .param("minParticipants", 0)
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(4, response.getInt("errorCode"));
    }

    @Test
    public void startUnknownCurrency() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.CURRENCY.getCode())
                .param("holding", 999999)
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", allRecipientsPublicKeys())
                .call();

        Assert.assertEquals(5, response.getInt("errorCode"));
    }

    @Test
    public void stop() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("stopStandbyShuffler")
                .chain(IGNIS.getId())
                .unsignedLongParam("account", ALICE.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertEquals(1, response.getInt("stopped"));

        JA standbyShufflers = getAllStandbyShufflers();
        Assert.assertTrue(standbyShufflers.isEmpty());
    }

    @Test
    public void get() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("getStandbyShufflers")
                .chain(IGNIS.getId())
                .unsignedLongParam("account", ALICE.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .call();

        Assert.assertNull(response.get("errorCode"));
        JA standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(1, standbyShufflers.size());
        JO standbyShuffler = standbyShufflers.get(0);
        Assert.assertNotNull(standbyShuffler);
        Assert.assertEquals(ALICE.getId(), standbyShuffler.getEntityId("account"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShuffler.getString("accountRS"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("chain"));
        Assert.assertEquals(HoldingType.COIN.getCode(), standbyShuffler.getByte("holdingType"));
        Assert.assertEquals(IGNIS.getId(), standbyShuffler.getInt("holding"));
        Assert.assertEquals("0", standbyShuffler.getString("minAmount"));
        Assert.assertEquals("0", standbyShuffler.getString("maxAmount"));
        Assert.assertEquals(Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, standbyShuffler.getInt("minParticipants"));
        Assert.assertEquals(recipients, standbyShuffler.get("recipientPublicKeys"));
    }

    @Test
    public void getAllSourceAccount() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.ASSET.getCode())
                .param("holding", "4348103880042995903")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(BOB.getSecretPhrase())
                .param("holdingType", HoldingType.CURRENCY.getCode())
                .param("holding", "1")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("getStandbyShufflers")
                .chain(IGNIS.getId())
                .unsignedLongParam("account", ALICE.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .call();

        Assert.assertNull(response.get("errorCode"));
        JA standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(2, standbyShufflers.size());
        Assert.assertEquals(ALICE.getRsAccount(), standbyShufflers.get(0).getString("accountRS"));
        Assert.assertEquals(ALICE.getRsAccount(), standbyShufflers.get(1).getString("accountRS"));
    }

    @Test
    public void getSameChainDifferentHoldingType() {
        List<String> recipients = allRecipientsPublicKeys();

        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(BOB.getSecretPhrase())
                .param("holdingType", HoldingType.CURRENCY.getCode())
                .param("holding", "1")
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", recipients)
                .call();

        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("getStandbyShufflers")
                .chain(IGNIS.getId())
                .call();

        Assert.assertNull(response.get("errorCode"));
        JA standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(2, standbyShufflers.size());

        JO standbyShuffler1 = standbyShufflers.get(0);
        JO standbyShuffler2 = standbyShufflers.get(1);
        Assert.assertEquals(
                Stream.of(ALICE.getRsAccount(), BOB.getRsAccount()).collect(Collectors.toSet()),
                Stream.of(standbyShuffler1.get("accountRS"), standbyShuffler2.get("accountRS")).collect(Collectors.toSet()));
    }

    @Test
    public void getDifferentChains() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", Collections.singletonList(RECIPIENT1.getPublicKeyStr()))
                .call();
        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        response = new APICall.Builder("startStandbyShuffler")
                .chain(BITSWIFT.getId())
                .secretPhrase(BOB.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", BITSWIFT.getId())
                .feeRateNQTPerFXT(0)
                .param("recipientPublicKeys", Collections.singletonList(RECIPIENT2.getPublicKeyStr()))
                .call();
        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        JA standbyShufflers = getAllStandbyShufflers();
        Assert.assertEquals(2, standbyShufflers.size());

        response = new APICall.Builder("getStandbyShufflers").chain(BITSWIFT.getId()).call();
        Assert.assertNull(response.get("errorCode"));
        standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        Assert.assertEquals(1, standbyShufflers.size());
    }

    @Test
    public void testShuffling() {
        JO response = new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(IGNIS.ONE_COIN)
                .param("recipientPublicKeys", Collections.singletonList(RECIPIENT1.getPublicKeyStr()))
                .call();
        Assert.assertNull(response.get("errorCode"));
        Assert.assertTrue(response.getBoolean("started"));

        JO shufflingCreate = ShufflingUtil.create(BOB, 3);
        String shufflingFullHash = shufflingCreate.getString("fullHash");
        generateBlock();
        generateBlock();

        JO shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.REGISTRATION.getCode(), shuffling.getByte("stage"));
        JO getParticipantsResponse = ShufflingUtil.getShufflingParticipants(shufflingFullHash);
        JA participants = getParticipantsResponse.getArray("participants");
        Assert.assertEquals(2, participants.size());

        response = GetShufflersCall.create()
                .shufflingFullHash(shufflingFullHash)
                .call();
        JA shufflers = response.getArray("shufflers");
        Assert.assertEquals(1, shufflers.size());
        JO shuffler = shufflers.get(0);
        Assert.assertEquals(ALICE.getStrId(), shuffler.getString("account"));
        Assert.assertEquals(ALICE.getRsAccount(), shuffler.getString("accountRS"));
        Assert.assertEquals(RECIPIENT1.getStrId(), shuffler.getString("recipient"));
        Assert.assertEquals(RECIPIENT1.getRsAccount(), shuffler.getString("recipientRS"));
    }

    @Test
    public void testNormalStandbyShufflerShutdown() {
        new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(IGNIS.ONE_COIN)
                .param("recipientPublicKeys", Collections.singletonList(RECIPIENT1.getPublicKeyStr()))
                .call();

        new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(BOB.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(IGNIS.ONE_COIN)
                .param("recipientPublicKeys", Arrays.asList(RECIPIENT2.getPublicKeyStr(), RECIPIENT3.getPublicKeyStr()))
                .call();

        JO shufflingCreate = ShufflingUtil.create(CHUCK, 3); // shuffling creation tx
        String shufflingFullHash = shufflingCreate.getString("fullHash");
        ShufflingUtil.startShuffler(CHUCK, RECIPIENT4, shufflingFullHash);

        generateBlockAndSleep(); // will include shuffling creation (with Chuck's registration as the shuffling creator)
                                 // and will trigger Alice & Bob's shufflers and generate both shuffling registration tx
        Assert.assertEquals(2, getAllStandbyShufflers().size());
        JO shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.REGISTRATION.getCode(), shuffling.getByte("stage"));
        JA participants = ShufflingUtil.getShufflingParticipants(shufflingFullHash).getArray("participants");
        Assert.assertEquals(1, participants.size());

        generateBlockAndSleep(); // will include registration from ALICE and BOB
        shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.PROCESSING.getCode(), shuffling.getByte("stage"));
        participants = ShufflingUtil.getShufflingParticipants(shufflingFullHash).getArray("participants");
        Assert.assertEquals(3, participants.size());
        for (JO participant : participants.objects()) {
            Assert.assertEquals(ShufflingParticipantHome.State.REGISTERED.getCode(), participant.getByte("state"));
        }

        for (int i = 0; i < 3; i++) {
            generateBlockAndSleep();
        }
        Assert.assertEquals(1, getAllStandbyShufflers().size());
        shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.VERIFICATION.getCode(), shuffling.getByte("stage"));
    }

    @Test
    public void testRecoverPublicKey() {
        new APICall.Builder("startStandbyShuffler")
                .chain(IGNIS.getId())
                .secretPhrase(ALICE.getSecretPhrase())
                .param("holdingType", HoldingType.COIN.getCode())
                .param("holding", IGNIS.getId())
                .feeRateNQTPerFXT(IGNIS.ONE_COIN)
                .param("recipientPublicKeys", Collections.singletonList(RECIPIENT1.getPublicKeyStr()))
                .call();

        JA standbyShufflers = getAllStandbyShufflers();
        Assert.assertEquals(1, standbyShufflers.size());
        Assert.assertEquals(1, standbyShufflers.get(0).getArray("recipientPublicKeys").size());

        JO shufflingCreate = ShufflingUtil.create(CHUCK, 3); // shuffling creation tx
        String shufflingFullHash = shufflingCreate.getString("fullHash");

        generateBlocks(9);

        standbyShufflers = getAllStandbyShufflers();
        Assert.assertEquals(1, standbyShufflers.size());
        Assert.assertEquals(0, standbyShufflers.get(0).getArray("recipientPublicKeys").size());
        JO shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.REGISTRATION.getCode(), shuffling.getByte("stage"));

        generateBlockAndSleep();

        standbyShufflers = getAllStandbyShufflers();
        Assert.assertEquals(1, standbyShufflers.size());
        Assert.assertEquals(0, standbyShufflers.get(0).getArray("recipientPublicKeys").size());
        shuffling = ShufflingUtil.getShuffling(shufflingFullHash);
        Assert.assertEquals(ShufflingStage.CANCELLED.getCode(), shuffling.getByte("stage"));

        generateBlock();
        standbyShufflers = getAllStandbyShufflers();
        Assert.assertEquals(1, standbyShufflers.size());
        Assert.assertEquals(1, standbyShufflers.get(0).getArray("recipientPublicKeys").size());
    }

    private JA getAllStandbyShufflers() {
        JO response = new APICall.Builder("getStandbyShufflers").chain("").call();
        Assert.assertNull(response.get("errorCode"));
        JA standbyShufflers = response.getArray("standbyShufflers");
        Assert.assertNotNull(standbyShufflers);
        return standbyShufflers;
    }
}
