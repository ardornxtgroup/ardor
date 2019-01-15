package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import nxt.http.callers.TriggerContractByRequestCall;
import org.junit.Assert;
import org.junit.Test;

public class GetRandomNumberTest extends AbstractContractTest {

    @Test
    public void submitRequest() {
        String contractName = ContractTestHelper.deployContract(GetRandomNumber.class);
        JO response = TriggerContractByRequestCall.create().contractName(contractName).
                setParamValidation(false).param("seed", 1234).call();
        int randomValue = response.getInt("random");
        Assert.assertTrue(randomValue >= 0 && randomValue < 1000);
    }

}
