package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import nxt.http.callers.TriggerContractByRequestCall;
import org.junit.Assert;
import org.junit.Test;

public class ContractWithInnerInterfaceTest extends AbstractContractTest {

    @Test
    public void tryLoad() {
        String contractName = ContractTestHelper.deployContract(ContractWithInnerInterface.class);
        JO response = TriggerContractByRequestCall.create().contractName(contractName).call();
        Assert.assertEquals("prefix_myMessage", response.getString("text"));
    }
}