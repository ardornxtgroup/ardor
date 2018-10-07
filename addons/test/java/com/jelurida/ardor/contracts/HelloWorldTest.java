package com.jelurida.ardor.contracts;

import nxt.Nxt;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class HelloWorldTest extends AbstractContractTest {

    @Test
    public void helloWorld() {
        String contractName = HelloWorld.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);

        // Send message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.messageTriggerContract(message);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract send back a message
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        ChainTransactionId contractResultTransactionId = null;
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                Assert.assertEquals(2, childTransaction.getChain().getId());
                Assert.assertEquals(1, childTransaction.getType().getType());
                Assert.assertEquals(0, childTransaction.getType().getSubtype());
                Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
                ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
                Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransactionId());
                contractResultTransactionId = new ChainTransactionId(childTransaction.getChain().getId(), childTransaction.getFullHash());
            }
        }
        Assert.assertNotNull(contractResultTransactionId);
    }

}
