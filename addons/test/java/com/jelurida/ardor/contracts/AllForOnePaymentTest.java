package com.jelurida.ardor.contracts;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.ChildTransaction;
import nxt.http.APICall;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Convert;
import nxt.util.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static nxt.blockchain.ChildChain.IGNIS;

public class AllForOnePaymentTest extends AbstractContractTest {

    @Test
    public void randomProbabilisticDistribution() {
        Random r = new Random(0);
        DistributedRandomNumberGenerator distribution = new DistributedRandomNumberGenerator();
        Map<String, Long> collect = new HashMap<>();
        collect.put(ALICE.getStrId(), 300L);
        collect.put(BOB.getStrId(), 500L);
        collect.put(CHUCK.getStrId(), 200L);
        Map<String, Long> hitMap = new HashMap<>();
        hitMap.put(ALICE.getStrId(), 0L);
        hitMap.put(BOB.getStrId(), 0L);
        hitMap.put(CHUCK.getStrId(), 0L);
        for (int i = 0; i < 100000; i++) {
            String account = distribution.processInvocationImpl(r.nextDouble() ,collect);
            long value = hitMap.get(account);
            hitMap.put(account, ++value);
        }
        Assert.assertEquals(29927L, (long)hitMap.get(ALICE.getStrId()));
        Assert.assertEquals(49893L, (long)hitMap.get(BOB.getStrId()));
        Assert.assertEquals(20180L, (long)hitMap.get(CHUCK.getStrId()));
    }

    @Test
    public void allForOnePayment() {
        JO setupParams = new JO();
        setupParams.put("frequency", "Value from properties will override this setting");
        String contractName = AllForOnePayment.class.getSimpleName();
        ContractTestHelper.deployContract(AllForOnePayment.class, setupParams, false);
        ContractTestHelper.deployContract(DistributedRandomNumberGenerator.class, null, true);
        JO messageJson = new JO();
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson))); // Specify a random seed
        String message = messageJson.toJSONString();
        APICall apiCall = new APICall.Builder("sendMoney").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", ALICE.getRsAccount()).
                param("amountNQT", 500 * IGNIS.ONE_COIN).
                param("encryptedMessageIsPrunable", true).
                param("messageToEncrypt", message).
                feeNQT(IGNIS.ONE_COIN).
                build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();

        messageJson = new JO();
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson))); // Specify a random seed
        message = messageJson.toJSONString();
        apiCall = new APICall.Builder("sendMoney").
                secretPhrase(CHUCK.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", ALICE.getRsAccount()).
                param("amountNQT", 300 * IGNIS.ONE_COIN).
                param("encryptedMessageIsPrunable", true).
                param("messageToEncrypt", message).
                feeNQT(IGNIS.ONE_COIN).
                build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney: " + response);
        // Same account pays twice in different blocks
        apiCall = new APICall.Builder("sendMoney").
                secretPhrase(DAVE.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", ALICE.getRsAccount()).
                param("amountNQT", 100 * IGNIS.ONE_COIN).
                feeNQT(IGNIS.ONE_COIN).
                build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();

        apiCall = new APICall.Builder("sendMoney").
                secretPhrase(DAVE.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", ALICE.getRsAccount()).
                param("amountNQT", 100 * IGNIS.ONE_COIN).
                feeNQT(IGNIS.ONE_COIN).
                build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();

        // Check the state of the contract before making a payment
        apiCall = new APICall.Builder("triggerContractByRequest").
                param("contractName", contractName).
                build();
        response = new JO(apiCall.invoke());
        JA payments = new JA(response.get("payments"));
        Assert.assertEquals(4, payments.size());
        Assert.assertEquals(100000000000L, response.getLong("paymentAmountNQT"));

        generateBlock(); // Now the distribution takes place (height 6)
        generateBlock(); // And now the reward transaction is processed

        List<Long> participants = new ArrayList<>(Arrays.asList(ALICE.getId(), BOB.getId(), CHUCK.getId(), DAVE.getId()));
        ChildTransaction childTransaction = testAndGetLastChildTransaction(2, 0, 0, a -> a == 99998000000L, 2000000L, ALICE, null, null);
        Assert.assertTrue(participants.contains(childTransaction.getRecipientId()));
        childTransaction.getAppendages().stream()
                .filter(PrunablePlainMessageAppendix.class::isInstance)
                .findFirst()
                .get();

        // Trigger the contract based on the transaction it just submitted
        apiCall = new APICall.Builder("triggerContractByTransaction").
                param("chain", IGNIS.getId()).
                param("triggerFullHash", Convert.toHexString(childTransaction.getFullHash())).
                param("apply", "true").
                param("validate", "true").
                build();
        response = new JO(apiCall.invoke());
        Assert.assertTrue(response.getString("errorDescription").startsWith("Cannot approve transaction, validatorSecretPhrase not specified"));

        // Trigger the contract based on a specific height without actually submitting the transactions
        apiCall = new APICall.Builder("triggerContractByHeight").
                param("contractName", contractName).
                param("height", 6).
                build();
        response = new JO(apiCall.invoke());
        JA transactions = new JA(response.get("transactions"));
        Assert.assertEquals(1, transactions.size());
        JO transactionJson = transactions.get(0).getJo("transactionJSON");
        Assert.assertEquals(99998000000L, transactionJson.getLong("amountNQT"));
        Assert.assertEquals(2000000L, transactionJson.getLong("feeNQT"));
        Assert.assertEquals(ALICE.getAccount().getId(), transactionJson.getEntityId("sender"));
        long recipient = transactionJson.getEntityId("recipient");
        Assert.assertTrue(participants.contains(recipient));
    }

}
