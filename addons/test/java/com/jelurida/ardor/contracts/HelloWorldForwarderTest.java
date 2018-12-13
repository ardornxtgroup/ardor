package com.jelurida.ardor.contracts;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.http.callers.GetPrunableMessageCall;
import nxt.http.callers.GetSupportedContractsCall;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class HelloWorldForwarderTest extends AbstractContractTest {

    @Test
    public void forwardGreeting() {
        String contractName = ContractTestHelper.deployContract(HelloWorldForwarder.class);

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
        String triggerFullHash = ContractTestHelper.messageTriggerContract(message);

        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract send back a message
        Block lastBlock = getLastBlock();
        ChainTransactionId contractResultTransactionId = null;
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                Assert.assertEquals(2, childTransaction.getChain().getId());
                Assert.assertEquals(1, childTransaction.getType().getType());
                Assert.assertEquals(0, childTransaction.getType().getSubtype());
                Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                Assert.assertEquals(CHUCK.getAccount().getId(), childTransaction.getRecipientId()); // message forwarded
                ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
                Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransactionId());
                contractResultTransactionId = new ChainTransactionId(childTransaction.getChain().getId(), childTransaction.getFullHash());

                // Load the attached message
                JO prunableMessageResponse = GetPrunableMessageCall.create(2).transactionFullHash(childTransaction.getFullHash()).call();
                JO contractResponse = JO.parse(prunableMessageResponse.getString("message"));
                Assert.assertEquals("Hi", contractResponse.getString("text"));

            }
        }
        Assert.assertNotNull(contractResultTransactionId);
    }

    /**
     * Check that the fields annotated as invocation params are returned by the getSupportedContracts API
     */
    @Test
    public void dataReturnedByGetSupportedContracts() {
        ContractTestHelper.deployContract(HelloWorldForwarder.class);
        JO contractsResponse = GetSupportedContractsCall.create().call();
        JA contracts = contractsResponse.getArray("supportedContracts");
        Assert.assertEquals(1, contracts.size());
        JA invocationParams = contracts.objects().stream().filter(jo -> jo.getString("name").equals("HelloWorldForwarder")).
                map(jo -> jo.getArray("supportedInvocationParams")).findFirst().orElse(new JA());
        Assert.assertEquals(2, invocationParams.size());
        JA validationAnnotations = contracts.objects().stream().filter(jo -> jo.getString("name").equals("HelloWorldForwarder")).
                map(jo -> jo.getArray("validityChecks")).findFirst().orElse(new JA());
        Assert.assertEquals(3, validationAnnotations.size());
        Assert.assertEquals("ValidateTransactionType", validationAnnotations.get(2).getString("name"));
        Assert.assertEquals("CHILD_PAYMENT,PARENT_PAYMENT,SEND_MESSAGE", validationAnnotations.get(2).getString("accept"));
        Assert.assertEquals("ASSET_TRANSFER", validationAnnotations.get(2).getString("reject"));
        Assert.assertEquals("processTransaction", validationAnnotations.get(2).getString("forMethod"));
    }

}
