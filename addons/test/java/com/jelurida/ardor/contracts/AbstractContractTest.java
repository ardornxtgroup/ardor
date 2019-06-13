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

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.Tester;
import nxt.addons.AddOns;
import nxt.addons.ContractRunner;
import nxt.blockchain.Block;
import nxt.blockchain.Blockchain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.util.Convert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

public abstract class AbstractContractTest extends BlockchainTest {

    private final Blockchain blockchain;
    {
        blockchain = AccessController.doPrivileged((PrivilegedAction<Blockchain>)Nxt::getBlockchain);
    }

    @BeforeClass
    public static void init() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Map<String, String> properties = new HashMap<>();
            properties.put("nxt.addOns", "nxt.addons.ContractRunner");
            properties.put("addon.contractRunner.configFile", "./addons/resources/contracts.json");
            properties.put("addon.contractRunner.secretPhrase", BlockchainTest.aliceSecretPhrase);
            properties.put("addon.contractRunner.feeRateNQTPerFXT.IGNIS", "200000000");
            properties.put("nxt.testnetLeasingDelay", "2");
            properties.put("nxt.isLightClient", "false");
            properties.put("contract.manager.secretPhrase", BlockchainTest.aliceSecretPhrase);
            properties.put("contract.manager.serverAddress", "");
            properties.put("contract.manager.feeNQT", "100000000");
            properties.put("contract.AllForOnePayment.param.frequency", "6");
            initNxt(properties);
            initBlockchainTest();
            return null;
        });
    }

    @AfterClass
    public static void reset() {
        ContractRunner contractRunner = (ContractRunner)AddOns.getAddOn(ContractRunner.class);
        contractRunner.reset();
    }

    public int getHeight() {
        return blockchain.getHeight();
    }

    public Block getLastBlock() {
        return blockchain.getLastBlock();
    }

    /**
     * Test that the last block includes a parent transaction with the expected properties
     * @return the transaction submitted by the contract
     */
    protected FxtTransaction testAndGetLastParentTransaction(int chainId, int type, int subtype, LongPredicate amountValidator, long fee, Tester sender, Tester recipient) {
        List<? extends FxtTransaction> transactions = getLastBlockParentTransactions();
        Assert.assertTrue(transactions.size() > 0);
        FxtTransaction fxtTransaction = transactions.get(transactions.size() - 1);
        Assert.assertEquals(chainId, fxtTransaction.getChain().getId());
        Assert.assertEquals(type, fxtTransaction.getType().getType());
        Assert.assertEquals(subtype, fxtTransaction.getType().getSubtype());
        Assert.assertTrue(amountValidator.test(fxtTransaction.getAmount()));
        Assert.assertEquals(fee, fxtTransaction.getFee());
        Assert.assertEquals(sender.getId(), fxtTransaction.getSenderId());
        Assert.assertEquals(recipient.getId(), fxtTransaction.getRecipientId());
        return fxtTransaction;
    }

    protected List<? extends FxtTransaction> getLastBlockParentTransactions() {
        Block lastBlock = getLastBlock();
        return lastBlock.getFxtTransactions();
    }

    /**
     * Test that the last block includes a child transaction with the expected properties
     * @return the transaction submitted by the contract
     */
    protected ChildTransaction testAndGetLastChildTransaction(int chainId, int type, int subtype, LongPredicate amountValidator, long fee, Tester sender, Tester recipient, String referenceTransactionFullHash) {
        List<? extends ChildTransaction> transactions = getLastBlockChildTransactions(chainId);
        Assert.assertTrue(transactions.size() > 0);
        ChildTransaction childTransaction = transactions.get(transactions.size() - 1);
        Assert.assertEquals(chainId, childTransaction.getChain().getId());
        Assert.assertEquals(type, childTransaction.getType().getType());
        Assert.assertEquals(subtype, childTransaction.getType().getSubtype());
        Assert.assertTrue(amountValidator.test(childTransaction.getAmount()));
        Assert.assertEquals(fee, childTransaction.getFee());
        Assert.assertEquals(sender.getId(), childTransaction.getSenderId());
        if (recipient != null) {
            Assert.assertEquals(recipient.getId(), childTransaction.getRecipientId());
        }
        if (referenceTransactionFullHash != null) {
            ChainTransactionId triggerTransactionId = new ChainTransactionId(chainId, Convert.parseHexString(referenceTransactionFullHash));
            Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransactionId());
        }
        return childTransaction;
    }

    protected List<? extends ChildTransaction> getLastBlockChildTransactions(int chainId) {
        return getLastBlock().getFxtTransactions().stream()
                .map(FxtTransaction::getSortedChildTransactions)
                .flatMap(Collection::stream)
                .filter(childTransaction -> childTransaction.getChain().getId() == chainId)
                .collect(Collectors.toList());
    }
}
