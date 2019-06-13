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
import nxt.http.callers.TriggerContractByRequestCall;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DatabaseAccessTest extends AbstractContractTest {

    @Test
    public void databaseAccessForbidden() {
        ContractTestHelper.deployContract(DatabaseAccess.class);
        JO response = TriggerContractByRequestCall.create().contractName("DatabaseAccess").call();
        Assert.assertTrue(response.isExist("errorCode"));
    }

    /**
     * Required permissions are:
     * permission nxt.util.security.BlockchainPermission "db";
     */
    @Test
    @Ignore
    public void databaseAccessAllowed() {
        ContractTestHelper.deployContract(DatabaseAccess.class);
        JO response = TriggerContractByRequestCall.create().contractName("DatabaseAccess").call();
        JA references = response.getArray("references");
        Assert.assertEquals(1, references.size());
        Assert.assertEquals("DatabaseAccess", references.get(0).getString("contractName"));
        Assert.assertFalse(references.get(0).isExist("contractTransaction"));
    }

    /**
     * Before running, uncomment the protection domain with the signedBy token in the ardor.policy file.
     * Required permissions are:
     * permission nxt.util.security.BlockchainPermission "db";
     * permission nxt.util.security.BlockchainPermission "getBlockchain";
     */
    @Test
    @Ignore
    public void databaseAndBlockchainAccessAllowed() {
        ContractTestHelper.deployContract(DatabaseAccess.class);
        JO response = TriggerContractByRequestCall.create().contractName("DatabaseAccess").call();
        JA references = response.getArray("references");
        Assert.assertEquals(1, references.size());
        Assert.assertEquals("DatabaseAccess", references.get(0).getString("contractName"));
        Assert.assertTrue(references.get(0).isExist("contractTransaction"));
    }

}
