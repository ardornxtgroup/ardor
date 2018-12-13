package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.responses.TransactionResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class AllowedActionsTest extends AbstractContractTest {

    @Test
    public void tryAllowedActions() {
        ContractTestHelper.deployContract(AllowedActions.class);

        // Verify that all actions worked
        generateBlock();
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        List<TransactionResponse> childTransactions = GetExecutedTransactionsCall.create(IGNIS.getId()).type(1).subtype(0).height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getTransactions();
        Assert.assertTrue(childTransactions.size() > 0);
        childTransactions.forEach(t -> {
            JO attachment = t.getAttachmentJson();
            JO action = JO.parse(attachment.getString("message"));
            Assert.assertEquals(action.getString("type"),"worked", action.getString("status"));
        });
    }

}
