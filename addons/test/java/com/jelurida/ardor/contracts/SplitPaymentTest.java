package com.jelurida.ardor.contracts;

import nxt.Nxt;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.http.APICall;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class SplitPaymentTest extends AbstractContractTest {

    @Test
    public void splitPayment() {
        String contractName = SplitPayment.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        JO params = new JO();
        params.put(CHUCK.getRsAccount(), "0.2");
        params.put(DAVE.getRsAccount(), "0.3");
        params.put(FORGY.getRsAccount(), "0.5");
        messageJson.put("params", params);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract paid all recipients
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        FxtTransaction parentTransaction = lastBlock.getFxtTransactions().get(0);
        List<? extends ChildTransaction> childTransactions = parentTransaction.getSortedChildTransactions();
        Assert.assertEquals(3, childTransactions.size());
        for (ChildTransaction childTransaction : childTransactions) {
            long recipientId = childTransaction.getRecipientId();
            long amount = childTransaction.getAmount();
            long fee = childTransaction.getFee();
            if (recipientId == CHUCK.getId()) {
                Assert.assertEquals(2000000000L - fee, amount);
            }
            if (recipientId == DAVE.getId()) {
                Assert.assertEquals(3000000000L - fee, amount);
            }
            if (recipientId == FORGY.getId()) {
                Assert.assertEquals(5000000000L - fee, amount);
            }
        }

        // Now let's validate the operation of the contract
        APICall apiCall = new APICall.Builder("triggerContractByTransaction").
                param("chain", IGNIS.getId()).
                param("triggerFullHash", triggerFullHash).build();
        JO response = new JO(apiCall.invoke());
        JA transactions = new JA(response.get("transactions"));
        Assert.assertEquals(3, transactions.size());
    }

}
