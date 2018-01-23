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

package nxt.http.shuffling;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.crypto.AnonymouslyEncryptedData;
import nxt.shuffling.Shuffler;
import nxt.shuffling.ShufflingStage;
import nxt.shuffling.ShufflingTransactionType;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static nxt.http.shuffling.ShufflingUtil.ALICE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.BOB_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.CHUCK_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.DAVE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.broadcast;
import static nxt.http.shuffling.ShufflingUtil.cancel;
import static nxt.http.shuffling.ShufflingUtil.create;
import static nxt.http.shuffling.ShufflingUtil.createAssetShuffling;
import static nxt.http.shuffling.ShufflingUtil.createCurrencyShuffling;
import static nxt.http.shuffling.ShufflingUtil.defaultHoldingShufflingAmount;
import static nxt.http.shuffling.ShufflingUtil.defaultShufflingAmount;
import static nxt.http.shuffling.ShufflingUtil.getShuffling;
import static nxt.http.shuffling.ShufflingUtil.getShufflingParticipants;
import static nxt.http.shuffling.ShufflingUtil.process;
import static nxt.http.shuffling.ShufflingUtil.register;
import static nxt.http.shuffling.ShufflingUtil.shufflingAsset;
import static nxt.http.shuffling.ShufflingUtil.shufflingCurrency;
import static nxt.http.shuffling.ShufflingUtil.startShuffler;
import static nxt.http.shuffling.ShufflingUtil.stopShuffler;
import static nxt.http.shuffling.ShufflingUtil.verify;

public class TestAutomatedShuffling extends BlockchainTest {

    private static final int chainId = ChildChain.IGNIS.getId();

    @Before
    public void stopAllShufflers() {
        Shuffler.stopAllShufflers();
    }

    @Test
    public void successfulShuffling() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {}
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 11 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        for (int i = 0; i < 4; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.DONE.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void successfulRestartShuffling() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        stopShuffler(CHUCK, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {}
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 11 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        stopShuffler(CHUCK, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(BOB, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(ALICE, shufflingFullHash);
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.DONE.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(defaultShufflingAmount + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void successfulAssetShuffling() {
        JSONObject shufflingCreate = createAssetShuffling(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 6; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.DONE.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));

        Assert.assertEquals(1000000 - defaultHoldingShufflingAmount - 3 * 100000, ALICE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(1000000 - defaultHoldingShufflingAmount - 3 * 100000, ALICE.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, BOB.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, BOB.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, CHUCK.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, CHUCK.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, DAVE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, DAVE.getUnconfirmedAssetQuantityDiff(shufflingAsset));

    }

    @Test
    public void successfulCurrencyShuffling() {
        JSONObject shufflingCreate = createCurrencyShuffling(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 6; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.DONE.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(48 * ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

        Assert.assertEquals(10000000 - 3 * 100000 - defaultHoldingShufflingAmount, ALICE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(10000000 - 3 * 100000 - defaultHoldingShufflingAmount, ALICE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, BOB.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, BOB.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, CHUCK.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, CHUCK.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, DAVE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000 - defaultHoldingShufflingAmount, DAVE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

    }

    @Test
    public void registrationNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 9; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(2, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());

        Assert.assertEquals(2 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(2 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void registrationNotFinishedAsset() {
        JSONObject shufflingCreate = createAssetShuffling(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 9; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(2, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN - 1003 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN - 1003 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());

        Assert.assertEquals(1000000 - 3 * 100000, ALICE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(1000000 - 3 * 100000, ALICE.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000, BOB.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(100000, BOB.getUnconfirmedAssetQuantityDiff(shufflingAsset));

        Assert.assertEquals(2 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(2 * ChildChain.IGNIS.ONE_COIN + 1003 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void processingNotStarted() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(4 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(4 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void processingNotStartedCurrency() {
        JSONObject shufflingCreate = createCurrencyShuffling(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN + 3 * ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(10000000 - 3 * 100000, ALICE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(10000000 - 3 * 100000, ALICE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, BOB.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, BOB.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, CHUCK.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, CHUCK.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, DAVE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(100000, DAVE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

        Assert.assertEquals(4 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 3 *  ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(4 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 3 *  ChildChain.IGNIS.ONE_COIN + 1000 * ChildChain.IGNIS.ONE_COIN, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void tooManyParticipants() {
        JSONObject shufflingCreate = create(ALICE, 3);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingFullHash);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(3, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(3 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(3 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());
    }

    @Test
    public void processingNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(34 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void verifyNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(47 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(47 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void verifyNotFinishedRestart() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(BOB, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        stopShuffler(ALICE, shufflingFullHash);
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(47 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(47 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void cancelAfterVerifyChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingFullHash, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void cancelAfterVerifyChuckRestart() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        stopShuffler(BOB, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(ALICE, shufflingFullHash);
        stopShuffler(DAVE, shufflingFullHash);

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingFullHash, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeys() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingFullHash, CHUCK, shufflingStateHash, 0, false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingFullHash);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeysAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));
        process(shufflingFullHash, ALICE, ALICE_RECIPIENT);
        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);
        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingFullHash, ALICE, shufflingStateHash);
        generateBlock();
        cancel(shufflingFullHash, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        JSONObject cancelResponse = cancel(shufflingFullHash, ALICE, shufflingStateHash, CHUCK.getId(), false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 22 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 22 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-22 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainBalanceDiff(chainId));
        Assert.assertEquals(0, DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void badProcessDataAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, ALICE, ALICE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 11 * ChildChain.IGNIS.ONE_COIN), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(24 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void modifiedProcessDataBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, BOB, BOB_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, BOB);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingFullHash, BOB, shufflingStateHash, CHUCK.getId());
        boolean bobCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((bobCancelFailed ? 44 : 54) * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals((bobCancelFailed ? 44 : 54) * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void duplicateProcessDataBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, BOB, BOB_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");

        byte[] nonce = Convert.parseHexString(shufflingFullHash);
        byte[] bytesToEncrypt = BOB_RECIPIENT.getPublicKey();
        byte[] nonce2 = Convert.parseHexString(shufflingFullHash);
        nonce2[0] ^= 1;
        bytesToEncrypt = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), DAVE.getPublicKey(), nonce).getBytes();
        byte[] bobBytes = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), CHUCK.getPublicKey(), nonce).getBytes();
        byte[] modifiedBytes = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), CHUCK.getPublicKey(), nonce2).getBytes();

        if (Convert.byteArrayComparator.compare(bobBytes, modifiedBytes) < 0) {
            data.set(0, Convert.toHexString(bobBytes));
            data.set(1, Convert.toHexString(modifiedBytes));
        } else {
            data.set(0, Convert.toHexString(modifiedBytes));
            data.set(1, Convert.toHexString(bobBytes));
        }

        broadcast(transactionJSON, BOB);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        cancel(shufflingFullHash, BOB, shufflingStateHash, CHUCK.getId());

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 21 * ChildChain.IGNIS.ONE_COIN), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(54 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(54 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void modifiedProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingFullHash, CHUCK, shufflingStateHash, DAVE.getId());
        boolean chuckCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-21 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-12 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void modifiedRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        if (!s.equals(DAVE_RECIPIENT.getPublicKeyStr())) {
            data.set(0, "0000000000" + s.substring(10));
        } else {
            s = (String)data.get(1);
            data.set(1, "0000000000" + s.substring(10));
        }
        broadcast(transactionJSON, DAVE);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-65 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId) + BOB.getChainBalanceDiff(chainId) + CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(ALICE.getChainBalanceDiff(chainId), ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(BOB.getChainBalanceDiff(chainId), BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(CHUCK.getChainBalanceDiff(chainId), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getChainBalanceDiff(chainId) == 0);
        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId) == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getChainBalanceDiff(chainId) == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId) == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getChainBalanceDiff(chainId) == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId) == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getChainBalanceDiff(chainId) == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getChainUnconfirmedBalanceDiff(chainId) == 0);

        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(77 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void duplicateRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, DAVE);
        Assert.assertTrue(broadcastResponse.get("error") != null);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(34 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void duplicateProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, CHUCK);
        Assert.assertTrue(broadcastResponse.get("error") != null);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(chainId));
        Assert.assertEquals(-11 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), CHUCK.getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + ChildChain.IGNIS.ONE_COIN), CHUCK.getChainUnconfirmedBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(chainId));
        Assert.assertEquals(-1 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainUnconfirmedBalanceDiff(chainId));

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainBalanceDiff(chainId));
        Assert.assertEquals(24 * ChildChain.IGNIS.ONE_COIN + ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT, FORGY.getChainUnconfirmedBalanceDiff(chainId));

    }

    @Test
    public void duplicateRecipientsBobChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        JSONObject startShufflerResponse = startShuffler(CHUCK, BOB_RECIPIENT, shufflingFullHash);
        Assert.assertTrue(((String)startShufflerResponse.get("errorDescription")).startsWith("Incorrect \"recipientPublicKey\""));
    }

    @Test
    public void maxShufflingSize() {
        int n = Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS;
        Tester[] participants = new Tester[n];
        Tester[] recipients = new Tester[n];
        participants[0] = ALICE;
        recipients[0] = ALICE_RECIPIENT;
        for (int i = 1; i < n; i++) {
            participants[i] = new Tester("tester " + i);
            ShufflingUtil.sendMoney(ALICE, participants[i], 100);
            recipients[i] = new Tester("recipient " + i);
        }
        generateBlock();
        JSONObject shufflingCreate = create(ALICE, n);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();

        for (int i = 0; i < n - 1; i++) {
            startShuffler(participants[i], recipients[i], shufflingFullHash);
        }
        generateBlock();
        register(shufflingFullHash, participants[n - 1]);
        generateBlock();
        JSONObject getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        int maxProcessTransactionFullSize = 0;
        int maxCancelTransactionFullSize = 0;
        int maxProcessTransactionJsonSize = 0;
        int maxCancelTransactionJsonSize = 0;
        for (int i = 0; i < n + 5; i++) {
            generateBlock();
            Block block = Nxt.getBlockchain().getLastBlock();
            Logger.logMessage("Block tx count " + block.getFxtTransactions().size());
            for (Transaction transaction : block.getFxtTransactions()) {
                if (transaction.getType() == ShufflingTransactionType.SHUFFLING_PROCESSING) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxProcessTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxProcessTransactionJsonSize) {
                        maxProcessTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
                if (transaction.getType() == ShufflingTransactionType.SHUFFLING_CANCELLATION) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxCancelTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxCancelTransactionJsonSize) {
                        maxCancelTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject processResponse = process(shufflingFullHash, participants[n - 1], recipients[n - 1], false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        if (!s.equals(recipients[n - 1].getPublicKeyStr())) {
            data.set(0, "0000000000" + s.substring(10));
        } else {
            s = (String)data.get(1);
            data.set(1, "0000000000" + s.substring(10));
        }
        broadcast(transactionJSON, participants[n - 1]);
        generateBlock();

        for (int i = 0; i < n + 20; i++) {
            generateBlock();
            Block block = Nxt.getBlockchain().getLastBlock();
            Logger.logMessage("Block tx count " + block.getFxtTransactions().size());
            for (Transaction transaction : block.getFxtTransactions()) {
                if (transaction.getType() == ShufflingTransactionType.SHUFFLING_PROCESSING) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxProcessTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxProcessTransactionJsonSize) {
                        maxProcessTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
                if (transaction.getType() == ShufflingTransactionType.SHUFFLING_CANCELLATION) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxCancelTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxCancelTransactionJsonSize) {
                        maxCancelTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(participants[n - 1].getId()), shufflingAssignee);

        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN) + 100 * ChildChain.IGNIS.ONE_COIN, participants[n - 1].getChainBalanceDiff(chainId));
        Assert.assertEquals(-(ChildChain.IGNIS.SHUFFLING_DEPOSIT_NQT + 12 * ChildChain.IGNIS.ONE_COIN) + 100 * ChildChain.IGNIS.ONE_COIN, participants[n - 1].getChainUnconfirmedBalanceDiff(chainId));
        Logger.logMessage("Max process transaction full size " + maxProcessTransactionFullSize);
        Logger.logMessage("Max cancel transaction full size " + maxCancelTransactionFullSize);
        Logger.logMessage("Max process transaction json size " + maxProcessTransactionJsonSize);
        Logger.logMessage("Max cancel transaction json size " + maxCancelTransactionJsonSize);
    }

}
