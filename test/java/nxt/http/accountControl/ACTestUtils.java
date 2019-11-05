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

package nxt.http.accountControl;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.crypto.HashFunction;
import nxt.http.APICall;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;
import org.junit.Assert;

import java.util.Arrays;

public class ACTestUtils {


    public static class Builder extends APICall.Builder {

        public Builder(String requestType, String secretPhrase) {
            super(requestType);
            secretPhrase(secretPhrase);
            feeNQT(ChildChain.IGNIS.ONE_COIN);
        }
    }

    public static class PhasingBuilder extends Builder {

        private boolean isBuildingControl;
        private String currentSubPollName = "";

        public PhasingBuilder(Tester sender) {
            this("setPhasingOnlyControl", sender);
        }

        public PhasingBuilder(String requestType, Tester sender) {
            super(requestType, sender.getSecretPhrase());
            isBuildingControl = "setPhasingOnlyControl".equals(requestType) || "setPhasingAssetControl".equals(requestType);
            if (!isBuildingControl) {
                startPhasingParams();
            }
        }

        public PhasingBuilder startPhasingParams() {
            param("phased", "true").param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 5);
            isBuildingControl = false;
            currentSubPollName = "";
            return this;
        }

        public PhasingBuilder startSubPoll(String subPollName) {
            setParamValidation(false);
            currentSubPollName = subPollName;
            return this;
        }

        public PhasingBuilder votingModel(VoteWeighting.VotingModel model) {
            param(getPrefix() + "VotingModel", model.getCode());
            return this;
        }

        public PhasingBuilder quorum(int quorum) {
            param(getPrefix() + "Quorum", (long)quorum);
            return this;
        }

        public PhasingBuilder quorum(long quorum) {
            param(getPrefix() + "Quorum", quorum);
            return this;
        }

        public PhasingBuilder whitelist(Tester... whitelist) {
            param(getPrefix() + "Whitelisted", Arrays.stream(whitelist).map(Tester::getStrId).toArray(String[]::new));
            return this;
        }

        public PhasingBuilder hashedSecret(String secret, HashFunction function) {
            param(getPrefix() + "HashedSecretAlgorithm", function.getId());
            param(getPrefix() + "HashedSecret", Convert.toHexString(function.hash(secret.getBytes())));
            return this;
        }

        public PhasingBuilder property(String prefix, Tester setter, String name, String value) {
            phasingParam(prefix + "PropertySetter", setter.getStrId());
            phasingParam(prefix + "PropertyName", name);
            phasingParam(prefix + "PropertyValue", value);
            return this;
        }

        public PhasingBuilder phasingParam(String key, String value) {
            param(getPrefix() + key, value);
            return this;
        }

        private String getPrefix() {
            return (isBuildingControl ? "control" : "phasing") + currentSubPollName;
        }
    }



    public static class CurrencyBuilder extends TestCurrencyIssuance.Builder {
        public CurrencyBuilder() {
            params.remove("minReservePerUnitNQT");
            params.remove("minDifficulty");
            params.remove("maxDifficulty");
            params.remove("algorithm");
        }
    }

    public static class CurrencyExchangeBuilder extends APICall.Builder {

        public CurrencyExchangeBuilder(String currencyId, String secretPhrase, int height) {
            super("publishExchangeOffer");
            param("currency", currencyId);
            param("buyRateNQTPerUnit", 10 * ChildChain.IGNIS.ONE_COIN);
            param("sellRateNQTPerUnit", 10 * ChildChain.IGNIS.ONE_COIN);
            param("totalBuyLimit", 0);
            param("totalSellLimit", 50);
            param("initialBuySupply", 0);
            param("initialSellSupply", 5);
            param("expirationHeight", height);
            secretPhrase(secretPhrase);
            feeNQT(ChildChain.IGNIS.ONE_COIN);
        }
    }
    
    public static class AssetBuilder extends APICall.Builder {

        public AssetBuilder(String secretPhrase, String assetName) {
            super("issueAsset");
            param("name", assetName);
            param("description", "Unit tests asset");
            param("quantityQNT", 10000);
            param("decimals", 4);
            secretPhrase(secretPhrase);
            feeNQT(1000 * ChildChain.IGNIS.ONE_COIN);
        }

    }

    public static void assertNoPhasingOnlyControl() {
        APICall.Builder builder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(BlockchainTest.ALICE.getId()));

        JSONObject response = builder.build().invoke();
        Assert.assertTrue(response.isEmpty());
    }

    public static JSONObject assertTransactionSuccess(APICall.Builder builder) {
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage(builder.getParam("requestType") + " response: " + response.toJSONString());
        Assert.assertNull(response.get("error"));
        String result = (String) response.get("fullHash");
        Assert.assertNotNull(result);
        return response;
    }
    
    public static void assertTransactionBlocked(APICall.Builder builder) {
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage(builder.getParam("requestType") + " response: " + response.toJSONString());
        
        //Assert.assertNotNull("Transaction wasn't even created", response.get("transaction"));
        
        String errorMsg = (String) response.get("error");
        Assert.assertNotNull("Transaction should fail, but didn't", errorMsg);
        Assert.assertTrue(errorMsg.contains("nxt.NxtException$AccountControlException"));
    }
    
    public static long getAccountBalance(long account, String balance) {
        APICall.Builder builder = new APICall.Builder("getBalance").param("account", Long.toUnsignedString(account));
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage("getBalance response: " + response.toJSONString());
        
        return Long.parseLong(((String)response.get(balance)));
    }

    public static void setControlPhasingParams(APICall.Builder builder,
                                               VoteWeighting.VotingModel votingModel, String holdingId, Long quorum,
                                               Long minBalance, VoteWeighting.MinBalanceModel minBalanceModel, long[] whitelist,
                                               long maxFees, int minDuration, int maxDuration) {
        setControlPhasingParams(builder, "", votingModel, holdingId, quorum, minBalance, minBalanceModel, whitelist, maxFees, minDuration, maxDuration);
    }

    public static void setControlPhasingParams(APICall.Builder builder,
                                               String subPollName, VoteWeighting.VotingModel votingModel, String holdingId, Long quorum,
                                               Long minBalance, VoteWeighting.MinBalanceModel minBalanceModel, long[] whitelist,
                                               long maxFees, int minDuration, int maxDuration) {
        if (votingModel != null) {
            builder.param("control" + subPollName + "VotingModel", votingModel.getCode());
        }

        if (holdingId != null) {
            builder.param("control" + subPollName + "Holding", holdingId);
        }

        if (quorum != null) {
            builder.param("control" + subPollName + "Quorum", quorum);
        }

        if (minBalance != null) {
            builder.param("control" + subPollName + "MinBalance", minBalance);
        }

        if (minBalanceModel != null) {
            builder.param("control" + subPollName + "MinBalanceModel", minBalanceModel.getCode());
        }

        if (whitelist != null) {
            builder.param("control" + subPollName + "Whitelisted", Arrays.stream(whitelist).mapToObj(Long::toUnsignedString).toArray(String[]::new));
        }

        if (subPollName.isEmpty() && maxFees >= 0) {
            builder.param("controlMaxFees", ChildChain.IGNIS.getId() + ":" + maxFees);
        }

        if (subPollName.isEmpty() && minDuration > 0) {
            builder.param("controlMinDuration", minDuration);
        }

        if (subPollName.isEmpty() && maxDuration > 0) {
            builder.param("controlMaxDuration", maxDuration);
        }
    }

    public static JSONObject approve(Object fullHash, Tester approver, String secret) {
        JSONObject response;
        response = approveBuilder(fullHash, approver, secret).build().invoke();

        Assert.assertNull(response.get("errorCode"));

        return response;
    }

    public static APICall.Builder approveBuilder(Object fullHash, Tester approver, String secret) {
        APICall.Builder builder = new APICall.Builder("approveTransaction")
                .param("secretPhrase", approver.getSecretPhrase())
                .param("revealedSecretText", secret)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN);
        if (fullHash != null) {
            builder = builder.param("phasedTransaction", ChildChain.IGNIS.getId() + ":" + fullHash);
        }
        return builder;
    }

    public enum PhasingStatus {
        NONE, //no transaction
        PENDING,
        REJECTED,
        APPROVED
    }
    public static PhasingStatus getPhasingStatus(Object fullHash) {
        JSONObject resp = new APICall.Builder("getPhasingPoll")
                .param("transactionFullHash", (String)fullHash)
                .param("countVotes", "true")
                .build().invoke();

        Object approved = resp.get("approved");

        if (approved != null) {
            if (Boolean.TRUE.equals(approved)) {
                return PhasingStatus.APPROVED;
            } else {
                return PhasingStatus.REJECTED;
            }
        } else {
            Long finishHeight = (Long) resp.get("finishHeight");
            if (finishHeight != null) {
                return PhasingStatus.PENDING;
            } else {
                return PhasingStatus.NONE;
            }
        }

    }
}
