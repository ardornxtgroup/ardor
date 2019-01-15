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

package nxt.http.shuffling;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.account.HoldingType;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;

class ShufflingUtil {

    static final Tester ALICE_RECIPIENT = new Tester("oiketrdgfxyjqhwds");
    static final Tester BOB_RECIPIENT = new Tester("5ehtrd9oijnkter");
    static final Tester CHUCK_RECIPIENT = new Tester("sdfxbejytdgfqrwefsrd");
    static final Tester DAVE_RECIPIENT = new Tester("gh-=e49rsiufzn4^");

    static final long defaultShufflingAmount = 1500000000;
    static final long defaultHoldingShufflingAmount = 40000;
    static long shufflingAsset;
    static long shufflingCurrency;
    static final int chainId = ChildChain.IGNIS.getId();
    static final long SHUFFLING_REGISTER_FEE = ChildChain.IGNIS.ONE_COIN/100;
    static final long SHUFFLING_PROCESSING_FEE = ChildChain.IGNIS.ONE_COIN/10;
    static final long SHUFFLING_TOTAL_FEE = 2 * SHUFFLING_REGISTER_FEE + SHUFFLING_PROCESSING_FEE;
    static final long SHUFFLING_RECIPIENTS_FEE = (ChildChain.IGNIS.ONE_COIN * 11)/100;

    static JSONObject create(Tester creator) {
        return create(creator, 4);
    }

    static JSONObject create(Tester creator, int participantCount) {
        APICall apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                param("amount", String.valueOf(defaultShufflingAmount)).
                param("participantCount", String.valueOf(participantCount)).
                param("registrationPeriod", 10).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject createAssetShuffling(Tester creator) {
        APICall apiCall = new APICall.Builder("issueAsset")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("name", "phased")
                .param("description", "shuffling transaction testing")
                .param("quantityQNT", 1000000)
                .param("decimals", 2)
                .param("feeNQT", 1000 * ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        JSONObject response = apiCall.invoke();
        shufflingAsset = Long.parseUnsignedLong(Tester.responseToStringId(response));
        BlockchainTest.generateBlock();
        apiCall = new APICall.Builder("transferAsset")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.BOB.getRsAccount())
                .param("asset", Long.toUnsignedString(shufflingAsset))
                .param("quantityQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferAssetResponse: " + response.toJSONString());
        apiCall = new APICall.Builder("transferAsset")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.CHUCK.getRsAccount())
                .param("asset", Long.toUnsignedString(shufflingAsset))
                .param("quantityQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferAssetResponse: " + response.toJSONString());
        apiCall = new APICall.Builder("transferAsset")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.DAVE.getRsAccount())
                .param("asset", Long.toUnsignedString(shufflingAsset))
                .param("quantityQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferAssetResponse: " + response.toJSONString());
        BlockchainTest.generateBlock();
        apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                param("amount", String.valueOf(defaultHoldingShufflingAmount)).
                param("participantCount", "4").
                param("registrationPeriod", 10).
                param("holding", Long.toUnsignedString(shufflingAsset)).
                param("holdingType", String.valueOf(HoldingType.ASSET.getCode())).
                build();
        response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject createCurrencyShuffling(Tester creator) {
        APICall apiCall = new APICall.Builder("issueCurrency")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("name", "shfle")
                .param("code", "SHFL")
                .param("description", "phased transaction testing")
                .param("type", 1)
                .param("initialSupplyQNT", 10000000)
                .param("maxSupplyQNT", 10000000)
                .param("decimals", 2)
                .param("feeNQT", 1000 * ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        JSONObject response = apiCall.invoke();
        shufflingCurrency = Long.parseUnsignedLong(Tester.responseToStringId(response));
        BlockchainTest.generateBlock();
        apiCall = new APICall.Builder("transferCurrency")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.BOB.getRsAccount())
                .param("currency", Long.toUnsignedString(shufflingCurrency))
                .param("unitsQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferCurrencyResponse: " + response.toJSONString());
        apiCall = new APICall.Builder("transferCurrency")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.CHUCK.getRsAccount())
                .param("currency", Long.toUnsignedString(shufflingCurrency))
                .param("unitsQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferCurrencyResponse: " + response.toJSONString());
        apiCall = new APICall.Builder("transferCurrency")
                .param("secretPhrase", creator.getSecretPhrase())
                .param("recipient", BlockchainTest.DAVE.getRsAccount())
                .param("currency", Long.toUnsignedString(shufflingCurrency))
                .param("unitsQNT", 100000)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        response = apiCall.invoke();
        Logger.logMessage("transferCurrencyResponse: " + response.toJSONString());
        BlockchainTest.generateBlock();
        apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                param("amount", String.valueOf(defaultHoldingShufflingAmount)).
                param("participantCount", "4").
                param("registrationPeriod", 10).
                param("holding", Long.toUnsignedString(shufflingCurrency)).
                param("holdingType", String.valueOf(HoldingType.CURRENCY.getCode())).
                build();
        response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject register(String shufflingFullHash, Tester tester) {
        APICall apiCall = new APICall.Builder("shufflingRegister").
                secretPhrase(tester.getSecretPhrase()).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingRegisterResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject getShuffling(String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("getShuffling").
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject getShufflingResponse = apiCall.invoke();
        Logger.logMessage("getShufflingResponse: " + getShufflingResponse.toJSONString());
        return getShufflingResponse;
    }

    static JSONObject getShufflingParticipants(String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("getShufflingParticipants").
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject getParticipantsResponse = apiCall.invoke();
        Logger.logMessage("getShufflingParticipantsResponse: " + getParticipantsResponse.toJSONString());
        return getParticipantsResponse;
    }

    static JSONObject process(String shufflingFullHash, Tester tester, Tester recipient) {
        return process(shufflingFullHash, tester, recipient, true);
    }

    static JSONObject process(String shufflingFullHash, Tester tester, Tester recipient, boolean broadcast) {
        APICall.Builder builder = new APICall.Builder("shufflingProcess").
                param("shufflingFullHash", shufflingFullHash).
                param("secretPhrase", tester.getSecretPhrase()).
                param("recipientSecretPhrase", recipient.getSecretPhrase()).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN);
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject shufflingProcessResponse = apiCall.invoke();
        Logger.logMessage("shufflingProcessResponse: " + shufflingProcessResponse.toJSONString());
        return shufflingProcessResponse;
    }

    static JSONObject verify(String shufflingFullHash, Tester tester, String shufflingStateHash) {
        APICall apiCall = new APICall.Builder("shufflingVerify").
                param("shufflingFullHash", shufflingFullHash).
                param("secretPhrase", tester.getSecretPhrase()).
                param("shufflingStateHash", shufflingStateHash).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingVerifyResponse:" + response);
        return response;
    }

    static JSONObject cancel(String shufflingFullHash, Tester tester, String shufflingStateHash, long cancellingAccountId) {
        return cancel(shufflingFullHash, tester, shufflingStateHash, cancellingAccountId, true);
    }

    static JSONObject cancel(String shufflingFullHash, Tester tester, String shufflingStateHash, long cancellingAccountId, boolean broadcast) {
        APICall.Builder builder = new APICall.Builder("shufflingCancel").
                param("shufflingFullHash", shufflingFullHash).
                param("secretPhrase", tester.getSecretPhrase()).
                param("shufflingStateHash", shufflingStateHash).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN);
        if (cancellingAccountId != 0) {
            builder.param("cancellingAccount", Long.toUnsignedString(cancellingAccountId));
        }
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingCancelResponse:" + response);
        return response;
    }

    static JSONObject broadcast(JSONObject transaction, Tester tester) {
        transaction.remove("signature");
        APICall apiCall = new APICall.Builder("signTransaction")
                .param("unsignedTransactionJSON", transaction.toJSONString())
                .param("validate", "false")
                .param("secretPhrase", tester.getSecretPhrase())
                .build();
        JSONObject response = apiCall.invoke();
        if (response.get("transactionJSON") == null) {
            return response;
        }
        apiCall = new APICall.Builder("broadcastTransaction").
                param("transactionJSON", ((JSONObject)response.get("transactionJSON")).toJSONString()).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("broadcastTransactionResponse:" + response);
        return response;
    }

    static JSONObject startShuffler(Tester tester, Tester recipient, String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("startShuffler").
                secretPhrase(tester.getSecretPhrase()).
                param("recipientPublicKey", Convert.toHexString(recipient.getPublicKey())).
                param("shufflingFullHash", shufflingFullHash).
                param("feeRateNQTPerFXT", ChildChain.IGNIS.ONE_COIN).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("startShufflerResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject stopShuffler(Tester tester, String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("stopShuffler").
                secretPhrase(tester.getSecretPhrase()).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("stopShufflerResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject sendMoney(Tester sender, Tester recipient, long amountNXT) {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", sender.getSecretPhrase()).
                param("recipient", recipient.getStrId()).
                param("amountNQT", amountNXT * ChildChain.IGNIS.ONE_COIN).
                feeRateNQTPerFXT(ChildChain.IGNIS.ONE_COIN).
                build().invoke();
        Logger.logMessage("sendMoneyResponse: " + response.toJSONString());
        return response;
    }

    private ShufflingUtil() {}

}
