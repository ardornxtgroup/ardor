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
package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.util.JSONAssert;
import nxt.voting.VoteWeighting;
import org.junit.Assert;
import org.junit.Test;

public class TestPropertyVoting extends BlockchainTest {
    @Test
    public void testFinishAtCreation() {
        String propertyName = "prop1";
        String propertyValue = "prop_val";

        APICall.Builder builder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        builder.build().invoke();
        generateBlock();

        builder = createGenericBuilder();
        builder.param("phasingSenderPropertySetter", CHUCK.getStrId());
        builder.param("phasingSenderPropertyName", propertyName);
        builder.param("phasingSenderPropertyValue", propertyValue);

        builder.build().invoke();
        generateBlock();

        //Finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

    }

    @Test
    public void testApproveAfterCreation() {
        String propertyName = "prop2";
        String propertyValue = "prop_val";

        APICall.Builder builder = createGenericBuilder();
        builder.param("phasingSenderPropertySetter", CHUCK.getStrId());
        builder.param("phasingSenderPropertyName", propertyName);
        builder.param("phasingSenderPropertyValue", propertyValue);

        builder.build().invoke();
        generateBlock();

        builder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        builder.build().invoke();
        generateBlock();

        //Not finished yet
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(5);
        //Finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testRevokeApproval() {
        String propertyName = "prop2";
        String propertyValue = "prop_val";

        APICall.Builder builder = createGenericBuilder();
        builder.param("phasingSenderPropertySetter", CHUCK.getStrId());
        builder.param("phasingSenderPropertyName", propertyName);
        builder.param("phasingSenderPropertyValue", propertyValue);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        builder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        builder.build().invoke();
        generateBlock();

        Assert.assertEquals(ACTestUtils.PhasingStatus.PENDING, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        builder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, "");
        builder.build().invoke();
        generateBlock();

        generateBlocks(5);

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testRecipientProperty() {
        String propertyName = "prop1";
        String propertyValue = "prop_val";

        APICall.Builder builder;
        APICall.Builder setPropertyBuilder;

        //set the property on the sender
        setPropertyBuilder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        setPropertyBuilder.build().invoke();
        generateBlock();

        builder = createGenericBuilder();
        builder.param("phasingRecipientPropertySetter", CHUCK.getStrId());
        builder.param("phasingRecipientPropertyName", propertyName);
        builder.param("phasingRecipientPropertyValue", propertyValue);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlocks(7);

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        //set the property on the recipient
        setPropertyBuilder = createSetPropertyBuilder(CHUCK, BOB, propertyName, propertyValue);
        setPropertyBuilder.build().invoke();
        generateBlock();

        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 7);
        fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(ACTestUtils.PhasingStatus.APPROVED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        //transaction without recipient must be executed immediately when phased by recipient property only
        propertyName = "prop2";
        String accountName = "AliceName";
        builder = createGenericBuilder();
        builder.param("requestType", "setAccountInfo");
        builder.param("name", accountName);
        builder.param("phasingRecipientPropertySetter", CHUCK.getStrId());
        builder.param("phasingRecipientPropertyName", propertyName);
        builder.param("phasingRecipientPropertyValue", propertyValue);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        JSONAssert aliceInfo = new JSONAssert(new APICall.Builder("getAccount").
                param("account", ALICE.getStrId()).build().invoke());

        Assert.assertEquals(accountName, aliceInfo.str("name"));
    }

    @Test
    public void testEmptyPropertyValue() {
        String propertyName = "prop1";

        APICall.Builder builder;

        //set the property on the sender
        builder = createSetPropertyBuilder(CHUCK, ALICE, propertyName, "doesn't matter");
        builder.build().invoke();
        generateBlock();

        ACTestUtils.PhasingBuilder phasingBuilder = createGenericBuilder();
        phasingBuilder.property("Sender", CHUCK, propertyName, "");

        String fullHash = new JSONAssert(phasingBuilder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(ACTestUtils.PhasingStatus.APPROVED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    public static APICall.Builder createSetPropertyBuilder(Tester setter, Tester recipient, String name, String value) {
        return new APICall.Builder("setAccountProperty").
                param("secretPhrase", setter.getSecretPhrase()).
                param("recipient", recipient.getStrId()).
                param("feeNQT", 3 * ChildChain.IGNIS.ONE_COIN).
                param("property", name).
                param("value", value);
    }

    public static ACTestUtils.PhasingBuilder createGenericBuilder() {
        ACTestUtils.PhasingBuilder result = new ACTestUtils.PhasingBuilder("sendMoney", ALICE).
                votingModel(VoteWeighting.VotingModel.PROPERTY).quorum(1);
        result.feeNQT(3 * ChildChain.IGNIS.ONE_COIN).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 7);
        return result;
    }
}
