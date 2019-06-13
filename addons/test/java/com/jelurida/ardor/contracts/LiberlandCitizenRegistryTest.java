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

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.http.callers.GetAccountPropertiesCall;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class LiberlandCitizenRegistryTest extends AbstractContractTest {

    @Test
    public void test() {
        String contractName = ContractTestHelper.deployContract(LiberlandCitizenRegistry.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        JO params = new JO();
        params.put("id", 4339515);
        messageJson.put("params", params);
        String message = messageJson.toJSONString();
        ContractTestHelper.bobPaysContract(message, IGNIS);

        // Contract should submit transaction now
        generateBlock();

        // Process account property transaction submitted by the contract
        generateBlock();

        // Check that the contract has registered the citizen on the blockchain
        JO call = GetAccountPropertiesCall.create().setter(ALICE.getRsAccount()).property("liberlandId").recipient(BOB.getRsAccount()).call();
        JA properties = call.getArray("properties");
        Assert.assertEquals(1, properties.size());
        JO property = properties.get(0);
        JO value = JO.parse(property.getString("value"));
        Assert.assertEquals("M***", value.getString("firstName"));
        Assert.assertEquals("G***", value.getString("lastName"));
        Assert.assertEquals("m***", value.getString("email"));
    }

}
