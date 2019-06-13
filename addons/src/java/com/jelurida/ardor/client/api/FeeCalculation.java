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

package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.blockchain.Chain;
import nxt.crypto.Crypto;
import nxt.http.callers.GetBundlerRatesCall;
import nxt.http.callers.SendMoneyCall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Sample Java program which demonstrates how to calculate best bundling fee for child chain transactions
 */
public class FeeCalculation {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) throws MalformedURLException {
        URL remoteUrl = new URL("https://testardor.jelurida.com/nxt");
        FeeCalculation feeCalculation = new FeeCalculation();
        Chain chain = Chain.getChain("IGNIS");
        JO transactionResponse = feeCalculation.prepare(remoteUrl, chain.getId());
        long minimumParentChainFeeFQT = transactionResponse.getLong("minimumFeeFQT");
        long feeRateNQTPerFXT = feeCalculation.getBestBundlingFee(remoteUrl, minimumParentChainFeeFQT, chain.getId());
        long feeNQT = BigDecimal.valueOf(minimumParentChainFeeFQT).multiply(BigDecimal.valueOf(feeRateNQTPerFXT)).divide(BigDecimal.valueOf(chain.ONE_COIN), RoundingMode.HALF_EVEN).longValue();
        System.out.printf("calculatedFee: %d\n", feeNQT);
        JO submittedTransaction = feeCalculation.submit(remoteUrl, chain.getId(), feeNQT);
        System.out.printf("submittedTransaction: %s\n", submittedTransaction);
    }

    private JO prepare(URL remoteUrl, int chainId) {
        // Prepare the transaction but do not broadcast it. This will calculate the parent chain fee
        return SendMoneyCall.create(chainId).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                deadline(15).
                broadcast(false).
                publicKey(Crypto.getPublicKey(SECRET_PHRASE)).
                remote(remoteUrl).
                message("012345678901234567890123456789012"). // permanent attached message of more than 32 bytes increases the ARDR fee
                call();
    }

    private long getBestBundlingFee(URL remoteUrl, long minBundlerBalanceFXT, int chainId) {
        JO response = GetBundlerRatesCall.create().minBundlerBalanceFXT(minBundlerBalanceFXT).remote(remoteUrl).call();
        List<JO> rates = response.getArray("rates").objects();
        Long bestRate = rates.stream().
                filter(r -> r.getInt("chain") == chainId).
                map(r -> r.getLong("minRateNQTPerFXT")).
                sorted().findFirst().orElse(null);
        if (bestRate == null) {
            throw new IllegalStateException("Best bundling fee cannot be determined");
        }
        return bestRate;
    }

    private JO submit(URL remoteUrl, int chainId, long feeNQT) {
        return SendMoneyCall.create(chainId).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                deadline(15).
                broadcast(true).
                secretPhrase(SECRET_PHRASE).
                remote(remoteUrl).
                feeNQT(feeNQT).
                message("012345678901234567890123456789012").
                call();
    }

}
