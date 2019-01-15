package com.jelurida.ardor.contracts;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.ChildTransaction;
import nxt.http.callers.GetPrunableMessageCall;
import nxt.http.callers.GetSupportedContractsCall;
import org.junit.Assert;
import org.junit.Test;

public class HelloWorldForwarderTest extends AbstractContractTest {

    @Test
    public void forwardGreeting() {
        String contractName = ContractTestHelper.deployContract(HelloWorldForwarder.class);
        String triggerFullHash = sendTriggerMessage(contractName);

        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract send back a message
        ChildTransaction childTransaction = testAndGetLastChildTransaction(2, 1, 0,
                a -> true, 4000000L,
                ALICE, CHUCK, triggerFullHash);
        // Load the attached message
        JO prunableMessageResponse = GetPrunableMessageCall.create(2).transactionFullHash(childTransaction.getFullHash()).call();
        JO contractResponse = JO.parse(prunableMessageResponse.getString("message"));
        Assert.assertEquals("Hi", contractResponse.getString("text"));
    }

    private String sendTriggerMessage(String contractName) {
        // Send message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        JO greeting = new JO();
        greeting.put("message", "Hi");
        JO params = new JO();
        params.put("greeting", greeting);
        params.put("recipientAccount", CHUCK.getRsAccount());
        messageJson.put("params", params);
        String message = messageJson.toJSONString();
        return ContractTestHelper.messageTriggerContract(message);
    }

    /**
     * Check that the fields annotated as invocation params are returned by the getSupportedContracts API
     */
    @Test
    public void dataReturnedByGetSupportedContracts() {
        String contractName = ContractTestHelper.deployContract(HelloWorldForwarder.class);
        sendTriggerMessage(contractName);
        JO contractsResponse = GetSupportedContractsCall.create().call();
        JA contracts = contractsResponse.getArray("supportedContracts");
        Assert.assertEquals(1, contracts.size());
        JO contract = contracts.objects().stream().filter(jo -> jo.getString("name").equals("HelloWorldForwarder")).map(jo -> jo.getJo("contract")).findFirst().get();
        JA invocationParams = contract.getArray("supportedInvocationParams");
        Assert.assertEquals(2, invocationParams.size());
        JA validationAnnotations = contract.getArray("validityChecks");
        Assert.assertEquals(3, validationAnnotations.size());
        Assert.assertEquals("ValidateTransactionType", validationAnnotations.get(2).getString("name"));
        Assert.assertEquals("CHILD_PAYMENT,PARENT_PAYMENT,SEND_MESSAGE", validationAnnotations.get(2).getString("accept"));
        Assert.assertEquals("ASSET_TRANSFER", validationAnnotations.get(2).getString("reject"));
        Assert.assertEquals("processTransaction", validationAnnotations.get(2).getString("forMethod"));
        JA invocationTypes = contract.getArray("invocationTypes");
        Assert.assertEquals(1, invocationTypes.size());
        JO invocationType = invocationTypes.get(0);
        Assert.assertEquals("TRANSACTION", invocationType.getString("type"));
        JO normalStat = invocationType.getJo("stat").getJo("normal");
        Assert.assertTrue(normalStat.getInt("count") > 0); // 1 - if only running this test, 2 - more if running all tests in this class
        JO errorStat = invocationType.getJo("stat").getJo("error");
        Assert.assertEquals(0, errorStat.getFloat("max"), 0);
    }

}
