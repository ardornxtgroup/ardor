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

import nxt.account.AccountPropertyAttachment;
import nxt.account.AccountPropertyTransactionType;
import nxt.addons.JO;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.ChildTransactionType;
import nxt.http.APICall;
import nxt.messaging.MessagingTransactionType;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class IgnisArdorRatesTest extends AbstractContractTest {

    @Test
    public void ignisArdorBittrexRate() {
        String contractName = ContractTestHelper.deployContract(IgnisArdorRates.class);
        executeContract(contractName);
    }

    void executeContract(String contractName) {
        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();
        // Verify that the contract sent back a message
        boolean isMessageFound = false;
        boolean isPropertyFound = false;
        List<? extends ChildTransaction> lastBlockTransactions = getLastBlockChildTransactions(2);
        for (ChildTransaction childTransaction : lastBlockTransactions) {
            if (ChildTransactionType.findTransactionType(childTransaction.getType().getType(), childTransaction.getType().getSubtype()) == MessagingTransactionType.ARBITRARY_MESSAGE)  {
                isMessageFound = true;
                Assert.assertEquals(2, childTransaction.getChain().getId());
                PrunableEncryptedMessageAppendix appendix = childTransaction.getAppendages()
                        .stream()
                        .filter(PrunableEncryptedMessageAppendix.class::isInstance)
                        .map(PrunableEncryptedMessageAppendix.class::cast)
                        .findFirst()
                        .get();
                Assert.assertEquals(ALICE.getId(), childTransaction.getSenderId());
                Assert.assertEquals(BOB.getId(), childTransaction.getRecipientId());
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
        Assert.assertTrue(isMessageFound);
        Assert.assertTrue(isPropertyFound);
        APICall apiCall = new APICall.Builder("triggerContractByRequest").
                param("contractName", contractName).
                build();
        JO response = new JO(apiCall.invoke());
        Assert.assertNotNull(response.get("BTRX"));
    }
}
