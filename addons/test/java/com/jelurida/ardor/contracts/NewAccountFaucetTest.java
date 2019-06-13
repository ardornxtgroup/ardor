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

import nxt.Tester;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.addons.JO;
import nxt.util.Logger;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class NewAccountFaucetTest extends AbstractContractTest {

    @Test
    public void fundNewAccount() {
        JO setupParams = new JO();
        setupParams.put("chain", 2);
        setupParams.put("thresholdAmountNQT", 1100000000);
        setupParams.put("thresholdBlocks", 10);
        setupParams.put("faucetAmountNQT", 500000000);
        String contractName = NewAccountFaucet.class.getSimpleName();
        ContractTestHelper.deployContract(NewAccountFaucet.class, setupParams);
        generateBlock();
        Tester newGuy = new Tester("rule chase pound passion whistle odd tumble joy howl reason crack turn");
        // newGuy generates a voucher
        JO voucher = new JO(getVoucher(newGuy));

        // newGuy requests funding from the contract
        APICall.Builder builder = new APICall.Builder("triggerContractByVoucher").
                parts("voucher", Convert.toBytes(voucher.toJSONString())).
                param("contractName", contractName);
        APICall apiCall = builder.build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("triggerContractByVoucher: " + response);
        generateBlock();

        // new Guy account is funded
        Assert.assertEquals(-500000000 - (100000000 + 100000000 + 202000000), ALICE.getChainBalanceDiff(2)); // SendMoney + fee + new account fee + deploy contract fee
        Assert.assertEquals(500000000, newGuy.getChainBalanceDiff(2)); // Faucet received
        Assert.assertEquals(402000000, FORGY.getChainBalanceDiff(2)); // Forging reward

        // newGuy requests funding from the contract again
        builder = new APICall.Builder("triggerContractByVoucher").
                parts("voucher", Convert.toBytes(voucher.toJSONString())).
                param("contractName", contractName);
        apiCall = builder.build();
        response = new JO(apiCall.invoke());
        Logger.logDebugMessage("triggerContractByVoucher: " + response);

        // No luck since the account already has a public key
        Assert.assertEquals(10001L, response.get("errorCode"));
    }

    @Test
    public void failToFundExistingAccount() {
        JO setupParams = new JO();
        setupParams.put("chain", 2);
        setupParams.put("thresholdAmountNQT", 1100000000);
        setupParams.put("thresholdBlocks", 10);
        setupParams.put("faucetAmountNQT", 500000000);
        String contractName = NewAccountFaucet.class.getSimpleName();
        ContractTestHelper.deployContract(NewAccountFaucet.class, setupParams);

        // BOB generates a voucher
        JO response = getVoucher(BOB);

        // Bob requests funding from the contract
        APICall.Builder builder = new APICall.Builder("triggerContractByVoucher").
                parts("voucher", Convert.toBytes(response.toJSONString())).
                param("contractName", contractName);
        APICall apiCall = builder.build();
        response = new JO(apiCall.invoke());

        // Funding failed since Bob's account already has a public key
        Logger.logDebugMessage("triggerContractByVoucher: " + response);
        Assert.assertEquals(10001L, response.get("errorCode"));
    }

    @Test
    public void enforceThreshold() {
        JO setupParams = new JO();
        setupParams.put("chain", 2);
        setupParams.put("thresholdAmountNQT", 1100000000);
        setupParams.put("thresholdBlocks", 10);
        setupParams.put("faucetAmountNQT", 500000000);
        String contractName = NewAccountFaucet.class.getSimpleName();
        ContractTestHelper.deployContract(NewAccountFaucet.class, setupParams);
        generateBlock();

        // contracts.json sets threshold of 11 IGNIS over 1440 blocks
        String baseSecretPhrase = "rule chase pound passion whistle odd tumble joy howl reason crack turn";
        JO paymentFromFaucet = getPaymentFromFaucet(contractName, baseSecretPhrase + "1");
        Assert.assertNotNull(paymentFromFaucet.get("transactions"));
        paymentFromFaucet = getPaymentFromFaucet(contractName, baseSecretPhrase + "2");
        Assert.assertNotNull(paymentFromFaucet.get("transactions"));
        paymentFromFaucet = getPaymentFromFaucet(contractName, baseSecretPhrase + "3");
        Assert.assertNotNull(paymentFromFaucet.get("transactions"));
        paymentFromFaucet = getPaymentFromFaucet(contractName, baseSecretPhrase + "4");
        Assert.assertEquals(10001L, paymentFromFaucet.get("errorCode"));

        // Now wait for the faucet to replenish its supply
        generateBlocks(10);
        paymentFromFaucet = getPaymentFromFaucet(contractName, baseSecretPhrase + "5");
        Assert.assertNotNull(paymentFromFaucet.get("transactions"));
    }

    private JO getPaymentFromFaucet(String contractName, String baseSecretPhrase) {
        Tester newGuy = new Tester(baseSecretPhrase);
        JO voucher = getVoucher(newGuy);
        APICall.Builder builder = new APICall.Builder("triggerContractByVoucher").
                parts("voucher", Convert.toBytes(voucher.toJSONString())).
                param("contractName", contractName);
        APICall apiCall = builder.build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("triggerContractByVoucher: " + response);
        generateBlock();
        return response;
    }

    private JO getVoucher(Tester newGuy) {
        APICall.Builder builder = new APICall.Builder("sendMoney").
                secretPhrase(newGuy.getSecretPhrase()).
                param("chain", IGNIS.getId()).
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", newGuy.getRsAccount()).
                param("amountNQT", 100 * IGNIS.ONE_COIN). // ignored by the contract
                param("voucher", "true");
        builder.feeNQT(0);
        APICall apiCall = builder.build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney voucher: " + response);
        return response;
    }


}
