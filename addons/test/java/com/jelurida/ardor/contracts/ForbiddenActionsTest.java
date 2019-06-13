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
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.responses.TransactionResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class ForbiddenActionsTest extends AbstractContractTest {

    @Test
    public void tryForbiddenActions() {
        ContractTestHelper.deployContract(ForbiddenActions.class);

        // Give the bundler a chance to submit create a child block transaction before we generate a block
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        // Verify that all attacks were blocked
        generateBlock();
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        List<TransactionResponse> childTransactions = GetExecutedTransactionsCall.create(IGNIS.getId()).type(1).subtype(0).height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getTransactions();
        Assert.assertTrue(childTransactions.size() > 0);
        childTransactions.forEach(t -> {
            JO attachment = t.getAttachmentJson();
            JO attack = JO.parse(attachment.getString("message"));
            Assert.assertEquals(attack.getString("type"),"blocked", attack.getString("status"));
        });
    }

}
