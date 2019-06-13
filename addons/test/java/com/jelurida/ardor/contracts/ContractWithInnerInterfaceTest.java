/*
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

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