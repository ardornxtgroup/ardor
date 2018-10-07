package com.jelurida.ardor.contracts;

import nxt.Nxt;
import nxt.account.AccountPropertyAttachment;
import nxt.account.AccountPropertyTransactionType;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.FxtTransaction;
import nxt.http.APICall;
import nxt.messaging.MessagingTransactionType;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class IgnisArdorRatesTest extends AbstractContractTest {

    @Test
    public void ignisArdorBittrexRate() {
        String contractName = IgnisArdorRates.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);
        executeContract(contractName);
    }

    public void executeContract(String contractName) {
        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();
        // Verify that the contract sent back a message
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        boolean isMessageFound = false;
        boolean isPropertyFound = false;
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                if (ChildTransactionType.findTransactionType(childTransaction.getType().getType(), childTransaction.getType().getSubtype()) == MessagingTransactionType.ARBITRARY_MESSAGE)  {
                    isMessageFound = true;
                    Assert.assertEquals(2, childTransaction.getChain().getId());
                    PrunableEncryptedMessageAppendix appendix = (PrunableEncryptedMessageAppendix)childTransaction.getAppendages().stream().filter(a -> a instanceof PrunableEncryptedMessageAppendix).findFirst().orElse(null);
                    if (appendix == null) {
                        Assert.fail("PrunableEncryptedMessageAppendix not found");
                    }
                    Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                    Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
                    continue;
                }
                if (ChildTransactionType.findTransactionType(childTransaction.getType().getType(), childTransaction.getType().getSubtype()) == AccountPropertyTransactionType.ACCOUNT_PROPERTY_SET) {
                    isPropertyFound = true;
                    Assert.assertEquals(2, childTransaction.getChain().getId());
                    Assert.assertTrue(childTransaction.getAttachment() instanceof AccountPropertyAttachment);
                    AccountPropertyAttachment accountPropertyAttachment = (AccountPropertyAttachment)childTransaction.getAttachment();
                    Assert.assertEquals("IgnisPerArdorRates", accountPropertyAttachment.getProperty());
                }
            }
        }
        Assert.assertTrue(isMessageFound);
        Assert.assertTrue(isPropertyFound);
        APICall apiCall = new APICall.Builder("triggerContractByRequest").
                param("contractName", contractName).
                build();
        JO response = new JO(apiCall.invoke());
        Assert.assertNotNull(response.get("BTRX"));
    }
}
