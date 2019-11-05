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
import nxt.http.callers.GetConstantsCall;
import nxt.http.responses.CoinExchangeTradeResponse;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
        CoinExchange24HVolume coinExchange = new CoinExchange24HVolume();
        coinExchange.calculate(1, 2);
    }

    private void calculate(int from, int to) {
        JO constants = GetConstantsCall.create().remote(url).trustRemoteCertificate(true).call();
        JO chainProperties = constants.getJo("chainProperties");
        JO fromChain = chainProperties.getJo("" + from);
        JO toChain = chainProperties.getJo("" + to);
        int currentTime = GetBlockCall.create().remote(url).trustRemoteCertificate(true).call().getInt("timestamp");
        int fromTime = currentTime - 60 * 60 * 24; // 24 hours ago
        int page = 0;
        int pageSize = 10;
        BigInteger sum = BigInteger.ZERO;
        BigInteger counterSum = BigInteger.ZERO;
        int count = 0;
        while (currentTime >= fromTime) {
            List<JO> trades = GetCoinExchangeTradesCall.create(fromChain.getInt("id")).exchange(toChain.getInt("id")).firstIndex(page * pageSize).lastIndex(page * pageSize + pageSize - 1).
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
        System.out.println(sum.divide(BigInteger.valueOf(toChain.getLong("ONE_COIN"))).longValue() + " " + toChain.getString("name"));
        System.out.println(counterSum.divide(BigInteger.valueOf(toChain.getLong("ONE_COIN"))).divide(BigInteger.valueOf(fromChain.getLong("ONE_COIN"))).longValue() + " " + fromChain.getString("name"));
        System.out.println("" + count + " trades");
    }
}
