package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import org.junit.Test;

public class HelloWorldTest extends AbstractContractTest {

    @Test
    public void helloWorld() {
        String contractName = ContractTestHelper.deployContract(HelloWorld.class);

        // Send message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.messageTriggerContract(message);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract send back a message
        testAndGetLastChildTransaction(2, 1, 0,
                a -> true, 4000000L,
                ALICE, BOB, triggerFullHash);
    }

}
