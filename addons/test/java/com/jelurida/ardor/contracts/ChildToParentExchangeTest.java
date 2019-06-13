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
import nxt.http.callers.ExchangeCoinsCall;
import nxt.http.callers.SendMoneyCall;
import nxt.util.Logger;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class ChildToParentExchangeTest extends AbstractContractTest {

    private static final long ONE_COIN = 100000000L;

    @Test
    public void amountTooLow() {
        JO setupParams = new JO();
        setupParams.put("maxAmountNXT", 20);
        ContractTestHelper.deployContract(ChildToParentExchange.class, setupParams);

        // Pay the contract account without message
        ContractTestHelper.bobPaysContract(null, IGNIS);

        // Wait for the transaction to confirm 6 times
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();
        generateBlock();

        // Contract should submit transaction now
        generateBlock();

        // Since there are no coin orders the amount of IGNIS is returned
        testAndGetLastChildTransaction(2, 0, 0,
                a -> a == 9998000000L, 2000000L,
                ALICE, BOB, null);
    }

    @Test
    public void matchOnlyFirstExchange() {
        JO setupParams = new JO();
        setupParams.put("maxAmountNXT", 20000);
        ContractTestHelper.deployContract(ChildToParentExchange.class, setupParams);
        submitCoinExchangeTestData();

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", ChildToParentExchange.class.getSimpleName());
        String message = messageJson.toJSONString();
        JO response = SendMoneyCall.create(2).amountNQT(50 * ONE_COIN).feeNQT(1000000L).messageIsPrunable(true).message(message).recipient(ALICE.getRsAccount()).secretPhrase(BOB.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());

        generateBlock();

        // Contract submits transaction
        generateBlock();

        testAndGetLastParentTransaction(1, -2, 0, a -> a == (5 - 1) * ONE_COIN, ONE_COIN, ALICE, BOB);
    }

    @Test
    public void matchMultipleExchanges() {
        JO setupParams = new JO();
        setupParams.put("maxAmountNXT", 20000);
        ContractTestHelper.deployContract(ChildToParentExchange.class, setupParams);
        submitCoinExchangeTestData();

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", ChildToParentExchange.class.getSimpleName());
        String message = messageJson.toJSONString();
        JO response = SendMoneyCall.create(2).amountNQT(250 * ONE_COIN).feeNQT(1000000L).messageIsPrunable(true).message(message).recipient(ALICE.getRsAccount()).secretPhrase(BOB.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());

        generateBlock();

        // Contract submits transaction
        generateBlock();

        // 250 IGNIS converted as 100 -> 10 + 125 -> 10 + 25 -> 1.25 = 10 + 10 + 1.25 = 21.25 ARDR
        testAndGetLastParentTransaction(1, -2, 0, a -> a == (21.25 - 1) * ONE_COIN, ONE_COIN, ALICE, BOB);
    }

    private void submitCoinExchangeTestData() {
        // Generate exchanges from Ardor to Ignis
        JO response = ExchangeCoinsCall.create(1).exchange(2).quantityQNT(100 * ONE_COIN).priceNQTPerCoin((long)(0.1 * ONE_COIN)).secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());
        response = ExchangeCoinsCall.create(1).exchange(2).quantityQNT(125 * ONE_COIN).priceNQTPerCoin((long)(0.08 * ONE_COIN)).secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());
        response = ExchangeCoinsCall.create(1).exchange(2).quantityQNT(200 * ONE_COIN).priceNQTPerCoin((long)(0.05 * ONE_COIN)).secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());

        // Generate exchanges from Ignis to Ardor
        response = ExchangeCoinsCall.create(2).exchange(1).quantityQNT(100 * ONE_COIN).priceNQTPerCoin(8 * ONE_COIN).secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());
        response = ExchangeCoinsCall.create(2).exchange(1).quantityQNT(100 * ONE_COIN).priceNQTPerCoin(5 * ONE_COIN).secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());

        generateBlock();
    }

}
