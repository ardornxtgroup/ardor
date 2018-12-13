package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class ChildToParentExchangeTest extends AbstractContractTest {

    @Test
    public void childToParentExchange() {
        JO setupParams = new JO();
        setupParams.put("maxAmountNXT", 20);
        ContractTestHelper.deployContract(ChildToParentExchange.class, setupParams);

        // Pay the contract account without message
        ContractTestHelper.bobPaysContract(null, IGNIS);

        // Wait for the transaction to confirm 6 times
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();

        // Contract should submit transaction now
        generateBlock();

        // Since there are no coin orders the amount of IGNIS is returned
        Block lastBlock = getLastBlock();
        boolean isFound = false;
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                isFound = true;
                Assert.assertEquals(2, childTransaction.getChain().getId());
                Assert.assertEquals(0, childTransaction.getType().getType());
                Assert.assertEquals(0, childTransaction.getType().getSubtype());
                Assert.assertEquals(9998000000L, childTransaction.getAmount());
                Assert.assertEquals(2000000L, childTransaction.getFee());
                Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
            }
        }
        Assert.assertTrue(isFound);

        // TODO Let's generate some coin orders and the calculation
    }

}
