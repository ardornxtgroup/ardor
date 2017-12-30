/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
 
 package nxt.http.accountControl;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.Tester;
import nxt.account.AccountRestrictions;
import nxt.blockchain.ChildChain;
import nxt.crypto.HashFunction;
import nxt.http.APICall;
import nxt.util.JSONAssert;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import nxt.voting.VoteWeighting.VotingModel;

public class CompositePhasingOnlyTest extends BlockchainTest {

    @Test
    public void testSetAndGet() throws Exception {
        ACTestUtils.assertNoPhasingOnlyControl();

        setSimpleCompositeControl("A", BOB);
        JSONAssert jsonAssert;

        APICall.Builder queryBuilder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(ALICE.getId()));

        jsonAssert = new JSONAssert(queryBuilder.build().invoke());
        jsonAssert.subObj("controlParams").subObj("phasingSubPolls").subObj("A");
    }

    @Test
    public void testUpdateCompositeControl() throws Exception {

        ACTestUtils.assertNoPhasingOnlyControl();

        setSimpleCompositeControl("A", BOB);

        APICall.Builder queryBuilder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(ALICE.getId()));

        JSONAssert jsonAssert = new JSONAssert(queryBuilder.build().invoke());
        JSONAssert subPollsJSON = jsonAssert.subObj("controlParams").subObj("phasingSubPolls");
        subPollsJSON.subObj("A");
        Assert.assertEquals(1, subPollsJSON.getJson().size());

        ACTestUtils.PhasingBuilder builder = createCompositeBuilder();
        builder.feeNQT(3 * ChildChain.IGNIS.ONE_COIN).param("controlExpression", "B");
        builder.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(CHUCK);

        //set the phasing to satisfy the previous account control
        builder.startPhasingParams().votingModel(VotingModel.COMPOSITE).param("phasingExpression", "A").param("phasingQuorum", 1);
        builder.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);

        jsonAssert = new JSONAssert(builder.build().invoke());
        jsonAssert.str("fullHash");
        jsonAssert.subObj("transactionJSON").subObj("attachment").subObj("phasingControlParams").subObj("phasingSubPolls").subObj("B");

        generateBlock();

        approveUpdate(jsonAssert);

        //check
        jsonAssert = new JSONAssert(queryBuilder.build().invoke());
        subPollsJSON = jsonAssert.subObj("controlParams").subObj("phasingSubPolls");
        subPollsJSON.subObj("B");
        Assert.assertEquals(1, subPollsJSON.getJson().size());
    }

    @Test
    public void testRemoveCompositeControl() throws Exception {
        final String varName = "RemoveTest";
        setSimpleCompositeControl(varName, BOB);

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder(ALICE);

        builder.feeNQT(3 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VotingModel.NONE);

        //set the phasing to satisfy the previous account control
        builder.startPhasingParams().votingModel(VotingModel.COMPOSITE).param("phasingExpression", varName).param("phasingQuorum", 1);
        builder.startSubPoll(varName).votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);

        JSONAssert jsonAssert = new JSONAssert(builder.build().invoke());
        generateBlock();

        approveUpdate(jsonAssert);

        int heightAfterRemoval = Nxt.getBlockchain().getHeight();

        APICall.Builder queryBuilder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(ALICE.getId()));

        JSONObject response = queryBuilder.build().invoke();
        Assert.assertEquals(0, response.size());

        String varName2 = "RmTest2";
        setSimpleCompositeControl(varName2, BOB);

        jsonAssert = new JSONAssert(queryBuilder.build().invoke());
        JSONAssert subPolls = jsonAssert.subObj("controlParams").subObj("phasingSubPolls");
        subPolls.subObj(varName2);
        Assert.assertEquals(1, subPolls.getJson().size());

        Nxt.getBlockchainProcessor().popOffTo(heightAfterRemoval);

        response = queryBuilder.build().invoke();
        Assert.assertEquals(0, response.size());

        setSimpleCompositeControl(varName2, BOB);
        jsonAssert = new JSONAssert(queryBuilder.build().invoke());
        subPolls = jsonAssert.subObj("controlParams").subObj("phasingSubPolls");
        subPolls.subObj(varName2);
        Assert.assertEquals(1, subPolls.getJson().size());
    }

    @Test
    public void testSubParamsEqualCheck() {
        ACTestUtils.PhasingBuilder builder = createCompositeBuilder();

        builder.param("controlExpression", "A & B");

        builder.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);
        builder.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(CHUCK, DAVE);

        builder.build().invoke();

        generateBlock();

        builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);

        builder.param("recipient", BOB.getStrId()).param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN);

        builder.votingModel(VotingModel.COMPOSITE).param("phasingExpression", "A & B").param("phasingQuorum", 1);

        builder.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);
        builder.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(CHUCK);

        JSONAssert jsonAssert = new JSONAssert(builder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Sub-poll for variable \"B\" does not match"));

        builder.startSubPoll("B").whitelist(CHUCK, DAVE).quorum(2);
        jsonAssert = new JSONAssert(builder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Sub-poll for variable \"B\" does not match"));

        builder.startSubPoll("B").quorum(1);
        jsonAssert = new JSONAssert(builder.build().invoke());
        jsonAssert.str("fullHash");
        generateBlock();
    }

    @Test
    public void testCompositeControlImplication() {
        ACTestUtils.PhasingBuilder control = createCompositeBuilder();

        control.param("controlExpression", "A & B");

        control.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);
        control.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(CHUCK, DAVE);

        control.build().invoke();

        generateBlock();

        ACTestUtils.PhasingBuilder txBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);

        txBuilder.param("recipient", BOB.getStrId()).param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN);

        txBuilder.votingModel(VotingModel.COMPOSITE).param("phasingExpression", "A").param("phasingQuorum", 1);

        txBuilder.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);

        JSONAssert jsonAssert = new JSONAssert(txBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Phasing expression does not imply the account control expression"));

        txBuilder.param("phasingExpression", "A & B | C");
        txBuilder.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(CHUCK, DAVE);
        txBuilder.startSubPoll("C").votingModel(VotingModel.HASH).quorum(1).hashedSecret("somesecret", HashFunction.SHA256);

        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Phasing expression does not imply the account control expression"));

        txBuilder.param("phasingExpression", "A & B & C");
        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        jsonAssert.str("fullHash");
    }

    @Test
    public void testSimpleControl() {
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder(ALICE);
        builder.votingModel(VotingModel.ACCOUNT).whitelist(BOB, CHUCK).quorum(1);
        new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        ACTestUtils.PhasingBuilder txBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        txBuilder.param("recipient", DAVE.getStrId()).param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN);

        final String AC_VAR = AccountRestrictions.PhasingOnly.DEFAULT_ACCOUNT_CONTROL_VARIABLE;
        txBuilder.votingModel(VotingModel.COMPOSITE).param("phasingExpression", AC_VAR).param("phasingQuorum", 1);
        txBuilder.startSubPoll(AC_VAR).votingModel(VotingModel.ACCOUNT).quorum(2).whitelist(BOB, CHUCK);

        JSONAssert jsonAssert = new JSONAssert(txBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Sub-poll for variable \"" + AC_VAR + "\" does not match"));

        txBuilder.param("phasingExpression", AC_VAR + " | B");
        txBuilder.startSubPoll(AC_VAR).quorum(1);
        txBuilder.startSubPoll("B").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(DAVE);

        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Phasing expression does not imply the account control expression"));

        txBuilder.param("phasingExpression", AC_VAR + " & B");

        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        jsonAssert.str("fullHash");
    }

    @Test
    public void testPropertyVoting() {
        ACTestUtils.PhasingBuilder control = createCompositeBuilder();
        JSONAssert jsonAssert;

        control.param("controlExpression", "A & B");

        String propertyName = "propac2";
        String propertyValue = "valX";

        control.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);
        control.startSubPoll("B").votingModel(VotingModel.PROPERTY).quorum(1);
        control.phasingParam("SenderPropertySetter", CHUCK.getStrId());
        control.phasingParam("SenderPropertyName", propertyName);
        control.phasingParam("SenderPropertyValue", propertyValue);

        jsonAssert = new JSONAssert(control.build().invoke());
        jsonAssert.str("fullHash");

        generateBlock();

        ACTestUtils.PhasingBuilder txBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);

        txBuilder.param("recipient", DAVE.getStrId()).param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN);

        txBuilder.votingModel(VotingModel.COMPOSITE).param("phasingExpression", "A & B").param("phasingQuorum", 1);
        txBuilder.startSubPoll("A").votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(BOB);
        txBuilder.startSubPoll("B").votingModel(VotingModel.PROPERTY).quorum(1);
        txBuilder.phasingParam("SenderPropertySetter", CHUCK.getStrId());
        txBuilder.phasingParam("SenderPropertyName", propertyName);
        txBuilder.phasingParam("SenderPropertyValue", propertyValue + "a");

        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").startsWith("Sub-poll for variable \"B\" does not match"));

        txBuilder.phasingParam("SenderPropertyValue", propertyValue);
        jsonAssert = new JSONAssert(txBuilder.build().invoke());
        jsonAssert.str("fullHash");
    }

    private ACTestUtils.PhasingBuilder createCompositeBuilder() {
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder(ALICE);

        ACTestUtils.setControlPhasingParams(builder, VotingModel.COMPOSITE, null, 1L,
                null, null, null, 10 * ChildChain.IGNIS.ONE_COIN, 5, 1440);
        return builder;
    }


    private ACTestUtils.PhasingBuilder setSimpleCompositeControl(String variableName, Tester whitelisted) {
        ACTestUtils.PhasingBuilder builder = createCompositeBuilder();

        builder.param("controlExpression", variableName);

        builder.startSubPoll(variableName).votingModel(VotingModel.ACCOUNT).quorum(1).whitelist(whitelisted);

        JSONAssert jsonAssert = new JSONAssert(builder.build().invoke());
        jsonAssert.str("fullHash");
        jsonAssert.subObj("transactionJSON").subObj("attachment").subObj("phasingControlParams").subObj("phasingSubPolls").subObj(variableName);

        generateBlock();

        return builder;
    }


    private void approveUpdate(JSONAssert updateResponse) {
        //approve the update
        APICall.Builder approveBuilder = new ACTestUtils.Builder("approveTransaction", BOB.getSecretPhrase()).
                param("phasedTransaction", "" + updateResponse.subObj("transactionJSON").integer("chain") + ":" + updateResponse.str("fullHash"));
        updateResponse = new JSONAssert(approveBuilder.build().invoke());
        updateResponse.str("fullHash");
        generateBlock();
    }


}
