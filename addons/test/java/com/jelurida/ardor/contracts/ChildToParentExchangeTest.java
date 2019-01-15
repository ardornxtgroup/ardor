package com.jelurida.ardor.contracts;

import nxt.addons.JO;
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
        testAndGetLastChildTransaction(2, 0, 0,
                a -> a == 9998000000L, 2000000L,
                ALICE, BOB, null);

        // TODO Let's generate some coin orders and the calculation
    }

}
