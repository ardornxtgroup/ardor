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
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetCoinExchangeTradesCall;
import nxt.http.responses.CoinExchangeTradeResponse;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;
import static nxt.blockchain.FxtChain.FXT;

/**
 * Calculate the trade volume of the ARDR/IGNIS pair on the Ardor coin exchange.
 * Note the usage of paging while iterating over the trades.
 */
public class CoinExchange24HVolume {

    private static URL url;
    static {
        try {
            url = new URL("https://ardor.jelurida.com/nxt");
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    public static void main(String[] args) {
        new CoinExchange24HVolume().calculate();
    }

    private void calculate() {
        int currentTime = GetBlockCall.create().remote(url).trustRemoteCertificate(true).call().getInt("timestamp");
        int fromTime = currentTime - 60 * 60 * 24; // 24 hours ago
        int page = 0;
        int pageSize = 10;
        BigInteger sum = BigInteger.ZERO;
        BigInteger counterSum = BigInteger.ZERO;
        int count = 0;
        while (currentTime >= fromTime) {
            List<JO> trades = GetCoinExchangeTradesCall.create(FXT.getId()).exchange(IGNIS.getId()).firstIndex(page * pageSize).lastIndex(page * pageSize + pageSize - 1).
                    remote(url).trustRemoteCertificate(true).call().getJoList("trades");
            for (JO trade : trades) {
                CoinExchangeTradeResponse tradeResponse = CoinExchangeTradeResponse.create(trade);
                currentTime = tradeResponse.getTimestamp();
                if (currentTime < fromTime) {
                    break;
                }
                count++;
                BigInteger quantity = BigInteger.valueOf(tradeResponse.getQuantityQNT());
                sum = sum.add(quantity);
                counterSum = counterSum.add(quantity.multiply(BigInteger.valueOf(tradeResponse.getPriceNQTPerCoin())));
            }
            page++;
        }
        System.out.println(sum.divide(BigInteger.valueOf(IGNIS.ONE_COIN)).longValue() + " " + IGNIS.getName());
        System.out.println(counterSum.divide(BigInteger.valueOf(IGNIS.ONE_COIN)).divide(BigInteger.valueOf(FXT.ONE_COIN)).longValue() + " " + FXT.getName());
        System.out.println("" + count + " trades");
    }
}
