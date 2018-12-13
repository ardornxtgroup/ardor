package com.jelurida.ardor.contracts;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.http.APICall;
import nxt.util.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class PropertyBasedLotteryTest extends AbstractContractTest {

    @Test
    public void lotteryTest() {
        String contractName = ContractTestHelper.deployContract(PropertyBasedLottery.class);
        String propertyKey = "lottery1";
        APICall apiCall = new APICall.Builder("setAccountProperty").
                secretPhrase(ALICE.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", BOB.getRsAccount()).
                param("property", propertyKey).
                param("value", "").
                feeNQT(IGNIS.ONE_COIN).
                build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("setAccountProperty: " + response);

        apiCall = new APICall.Builder("setAccountProperty").
                secretPhrase(ALICE.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", CHUCK.getRsAccount()).
                param("property", propertyKey).
                param("value", "").
                feeNQT(IGNIS.ONE_COIN).
                build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("setAccountProperty: " + response);

        apiCall = new APICall.Builder("setAccountProperty").
                secretPhrase(ALICE.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("recipient", DAVE.getRsAccount()).
                param("property", propertyKey).
                param("value", "").
                feeNQT(IGNIS.ONE_COIN).
                build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("setAccountProperty: " + response);
        generateBlock();

        // Send a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        JO params = new JO();
        params.put("property", propertyKey);
        messageJson.put("params", params);
        String message = messageJson.toJSONString();
        ContractTestHelper.messageTriggerContract(message, ALICE.getSecretPhrase());
        generateBlock(); // And now the reward transaction is processed

        apiCall = new APICall.Builder("getAccountProperties").
                param("setter", ALICE.getRsAccount()).
                param("property", propertyKey).
                build();
        response = new JO(apiCall.invoke());
        List<JO> properties = new JA(response.get("properties")).objects();
        int h1 = getHeight() - 1;
        int winners = 0;
        int losers = 0;
        for (JO property : properties) {
            if (("winner:" + h1).equals(property.getString("value"))) {
                winners++;
            } else {
                losers++;
            }
        }
        Assert.assertEquals(1, winners);
        Assert.assertEquals(2, losers);

        // Send another message to trigger another contract execution
        ContractTestHelper.messageTriggerContract(message, ALICE.getSecretPhrase());
        generateBlock(); // And now the reward transaction is processed

        apiCall = new APICall.Builder("getAccountProperties").
                param("setter", ALICE.getRsAccount()).
                param("property", propertyKey).
                build();
        response = new JO(apiCall.invoke());
        properties = new JA(response.get("properties")).objects();
        winners = 0;
        losers = 0;
        for (JO property : properties) {
            String value = property.getString("value");
            if (value != null && value.startsWith("winner:")) {
                winners++;
            } else {
                losers++;
            }
        }
        Assert.assertEquals(2, winners);
        Assert.assertEquals(1, losers);
    }

}
