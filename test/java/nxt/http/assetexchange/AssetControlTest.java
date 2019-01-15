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

package nxt.http.assetexchange;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.http.client.IssueAssetBuilder;
import nxt.http.client.TransferAssetBuilder;
import nxt.http.twophased.TestPropertyVoting;
import nxt.util.JSONAssert;
import nxt.voting.VoteWeighting.VotingModel;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class AssetControlTest extends BlockchainTest {
    @Test
    public void testSetAndGet() {
        String assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", BOB);
        builder.votingModel(VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);
        builder.param("asset", assetId);

        JSONAssert jsonAssert = new JSONAssert(builder.build().invoke());

        Assert.assertEquals("Asset control can only be set by the asset issuer", jsonAssert.str("errorDescription"));

        builder.secretPhrase(ALICE.getSecretPhrase());
        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        APICall.Builder queryBuilder = new APICall.Builder("getPhasingAssetControl")
                .param("asset", assetId);

        JSONAssert controlParams = new JSONAssert(queryBuilder.build().invoke()).subObj("controlParams");
        Assert.assertEquals(VotingModel.ACCOUNT.getCode(), ((Long) controlParams.integer("phasingVotingModel")).byteValue());
    }

    @Test
    public void testSimpleTransfer() {
        String assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", ALICE);
        builder.votingModel(VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);
        builder.param("asset", assetId);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        int amount = 100 * 10000;
        String errorDescription = new TransferAssetBuilder(assetId, ALICE, BOB)
                .setQuantityQNT(amount)
                .transferWithError()
                .getErrorDescription();
        Assert.assertEquals("Non-phased transaction when phasing asset control is enabled", errorDescription);

        APICall apiCall = new ACTestUtils.PhasingBuilder("transferAsset", ALICE)
                .votingModel(VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1)
                .param("recipient", BOB.getRsAccount())
                .param("asset", assetId)
                .param("quantityQNT", amount)
                .build();
        String fullHash = new JSONAssert(apiCall.invoke()).str("fullHash");

        generateBlock();

        ACTestUtils.approve(fullHash, CHUCK, null);

        generateBlocks(4);

        Assert.assertEquals(amount, BOB.getAssetQuantityDiff(Long.parseUnsignedLong(assetId)));
    }

    @Test
    public void testSimpleAssetAndAccountControl() {
        String assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", ALICE);
        builder.votingModel(VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);
        builder.param("asset", assetId);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        //also set account control on alice
        builder = new ACTestUtils.PhasingBuilder(ALICE);
        builder.votingModel(VotingModel.ACCOUNT).whitelist(DAVE).quorum(1);
        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        //now, in order to transfer the asset, alice needs to issue the transaction with composite phasing which implies
        //both the asset and the account controls
        int amount = 100 * 10000;

        APICall apiCall = new ACTestUtils.PhasingBuilder("transferAsset", ALICE)
                .votingModel(VotingModel.COMPOSITE).phasingParam("Expression", "ACC & ASC").quorum(1)
                .startSubPoll("ASC").votingModel(VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1)
                .startSubPoll("ACC").votingModel(VotingModel.ACCOUNT).whitelist(DAVE).quorum(1)
                .param("recipient", BOB.getRsAccount())
                .param("asset", assetId)
                .param("quantityQNT", amount)
                .feeNQT(ChildChain.IGNIS.ONE_COIN * 5)
                .build();
        String fullHash = new JSONAssert(apiCall.invoke()).str("fullHash");

        generateBlock();

        ACTestUtils.approve(fullHash, CHUCK, null);
        ACTestUtils.approve(fullHash, DAVE, null);

        generateBlocks(4);

        Assert.assertEquals(amount, BOB.getAssetQuantityDiff(Long.parseUnsignedLong(assetId)));
    }

    @Test
    public void testAssetControlByProperty() {
        String propertyName = "propac2";
        String propertyValue = "valX";

        String assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder control = createByPropertyPhasingBuilder(propertyName, propertyValue, assetId);

        new JSONAssert(control.build().invoke()).str("fullHash");

        generateBlock();

        TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue).build().invokeNoError();

        TestPropertyVoting.createSetPropertyBuilder(CHUCK, BOB, propertyName, propertyValue).build().invokeNoError();

        generateBlock();

        int amount = 100 * 10000;
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("transferAsset", ALICE)
                .votingModel(VotingModel.PROPERTY).quorum(1)
                .phasingParam("SenderPropertySetter", CHUCK.getStrId())
                .phasingParam("SenderPropertyName", propertyName)
                .phasingParam("SenderPropertyValue", propertyValue);

        builder.param("recipient", BOB.getRsAccount())
                .param("asset", assetId)
                .param("quantityQNT", amount);

        Assert.assertTrue( new JSONAssert(builder.build().invoke()).str("errorDescription").
                startsWith("Phasing parameters do not match phasing asset control."));

        builder.phasingParam("RecipientPropertySetter", CHUCK.getStrId())
                .phasingParam("RecipientPropertyName", propertyName)
                .phasingParam("RecipientPropertyValue", propertyValue);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(amount, BOB.getAssetQuantityDiff(Long.parseUnsignedLong(assetId)));
    }

    @Test
    public void testDividendPaymentOnControlledAsset() {
        String propertyName = "propac2";
        String propertyValue = "valX";
        String propertyValue2 = "valY";

        String controlledAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder control = createByPropertyPhasingBuilder(propertyName, propertyValue, controlledAssetId);

        new JSONAssert(control.build().invoke()).str("fullHash");

        generateBlock();

        String dividendAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetD").getAssetIdString();

        APICall.Builder propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        propBuilder.build().invoke();

        propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, BOB, propertyName, propertyValue);
        propBuilder.build().invoke();

        generateBlock();

        int amount = 100 * 10000;

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("transferAsset", ALICE);
        builder.param("recipient", BOB.getRsAccount())
                .param("asset", controlledAssetId)
                .param("quantityQNT", amount);
        setupByPropertyPhasing(propertyName, propertyValue, builder);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(amount, BOB.getAssetQuantityDiff(Long.parseUnsignedLong(controlledAssetId)));

        propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue2);
        propBuilder.build().invoke();

        propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, BOB, propertyName, propertyValue2);
        propBuilder.build().invoke();

        //asset control is not enforced on the asset for which a dividend is payed, but on the asset with which it is payed (if any)
        new JSONAssert(AssetExchangeTest.payDividend(controlledAssetId, ALICE, Nxt.getBlockchain().getHeight(), 1,
                ChildChain.IGNIS, HoldingType.ASSET.getCode(), dividendAssetId)).str("fullHash");

        //BOB owns all the distributed control assets, which is (amount / 10 ^ AssetExchangeTest.ASSET_DECIMALS)
        Assert.assertEquals(BigDecimal.valueOf(amount, IssueAssetBuilder.ASSET_DECIMALS).longValue(), BOB.getAssetQuantityDiff(Long.parseUnsignedLong(dividendAssetId)));

        dividendAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetE").getAssetIdString();

        control = createByPropertyPhasingBuilder(propertyName, propertyValue, dividendAssetId);

        new JSONAssert(control.build().invoke()).str("fullHash");

        generateBlocks(10);

        JSONAssert payment = new JSONAssert(AssetExchangeTest.payDividend(controlledAssetId, ALICE, Nxt.getBlockchain().getHeight(), 1,
                ChildChain.IGNIS, HoldingType.ASSET.getCode(), dividendAssetId));

        Assert.assertEquals("Non-phased transaction when phasing asset control is enabled", payment.str("errorDescription"));

        propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        propBuilder.build().invoke();

        builder = new ACTestUtils.PhasingBuilder("dividendPayment", ALICE);
        setupByPropertyPhasing(propertyName, propertyValue, builder);
        builder.param("asset", controlledAssetId)
                .param("height", Nxt.getBlockchain().getHeight())
                .param("holdingType", HoldingType.ASSET.getCode())
                .param("holding", dividendAssetId)
                .param("amountNQTPerShare", 1);

        Assert.assertEquals("Dividend payment with asset under by-recipient property control is not supported",
                new JSONAssert(builder.build().invoke()).str("errorDescription"));
    }

    @Test
    public void testShufflingOfControlledAsset() {
        String propertyName = "propac2";
        String propertyValue = "valX";

        String controlledAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();

        ACTestUtils.PhasingBuilder control = createByPropertyPhasingBuilder(propertyName, propertyValue, controlledAssetId);

        new JSONAssert(control.build().invoke()).str("fullHash");

        generateBlock();

        int amount = 100 * 10000;

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("transferAsset", ALICE);
        setupByPropertyPhasing(propertyName, propertyValue, builder);
        builder.param("recipient", BOB.getRsAccount())
                .param("asset", controlledAssetId)
                .param("quantityQNT", amount);
        new JSONAssert(builder.build().invoke()).str("fullHash");

        builder = new ACTestUtils.PhasingBuilder("shufflingCreate", ALICE);
        setupByPropertyPhasing(propertyName, propertyValue, builder);

        builder.param("amount", String.valueOf(10*1000)).
                param("holding", controlledAssetId).
                param("holdingType", String.valueOf(HoldingType.ASSET.getCode())).
                param("participantCount", String.valueOf(3)).
                param("registrationPeriod", 10);
        Assert.assertEquals("Shuffling of asset under asset control is not supported",
                new JSONAssert(builder.build().invoke()).str("errorDescription"));
    }

    @Test
    public void testSetOnDistributedAsset() {
        String propertyName = "propac2";
        String propertyValue = "valX";

        String controlledAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();
        AssetExchangeTest.transfer(controlledAssetId, ALICE, BOB, 10*10000).getFullHash();

        ACTestUtils.PhasingBuilder control = createByPropertyPhasingBuilder(propertyName, propertyValue, controlledAssetId);

        Assert.assertEquals("Adding asset control requires the asset issuer to own all asset units",
                new JSONAssert(control.build().invoke()).str("errorDescription"));
    }

    @Test
    public void testRemoveControl() {
        String propertyName = "propac2";
        String propertyValue = "valX";
        ACTestUtils.PhasingBuilder control;

        String controlledAssetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetIdString();
        control = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", ALICE).votingModel(VotingModel.NONE);
        control.param("asset", controlledAssetId);

        Assert.assertEquals("Phasing asset control is not currently enabled",
                new JSONAssert(control.build().invoke()).str("errorDescription"));

        setupByPropertyPhasing(propertyName, propertyValue, control);
        new JSONAssert(control.build().invoke()).str("fullHash");
        generateBlock();

        //remove the asset control
        control = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", ALICE).votingModel(VotingModel.NONE);
        control.param("asset", controlledAssetId);
        new JSONAssert(control.build().invoke()).str("fullHash");
        generateBlock();

        AssetExchangeTest.transfer(controlledAssetId, ALICE, BOB, 10*10000).getFullHash();

    }

    private ACTestUtils.PhasingBuilder createByPropertyPhasingBuilder(String propertyName, String propertyValue, String assetId) {
        ACTestUtils.PhasingBuilder control = new ACTestUtils.PhasingBuilder("setPhasingAssetControl", ALICE);
        setupByPropertyPhasing(propertyName, propertyValue, control);
        control.param("asset", assetId);
        return control;
    }

    private void setupByPropertyPhasing(String propertyName, String propertyValue, ACTestUtils.PhasingBuilder builder) {
        builder.votingModel(VotingModel.PROPERTY).quorum(1);
        builder.phasingParam("SenderPropertySetter", CHUCK.getStrId());
        builder.phasingParam("SenderPropertyName", propertyName);
        builder.phasingParam("SenderPropertyValue", propertyValue);
        builder.phasingParam("RecipientPropertySetter", CHUCK.getStrId());
        builder.phasingParam("RecipientPropertyName", propertyName);
        builder.phasingParam("RecipientPropertyValue", propertyValue);
    }

}
