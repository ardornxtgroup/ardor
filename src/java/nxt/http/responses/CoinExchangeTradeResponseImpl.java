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

package nxt.http.responses;

import nxt.addons.JO;
import nxt.util.Convert;
import org.json.simple.JSONObject;

public class CoinExchangeTradeResponseImpl implements CoinExchangeTradeResponse {

    private final byte[] orderFullHash;
    private final byte[] matchFullHash;
    private final int chainId;
    private final int exchangeChainId;
    private final long accountId;
    private final long quantityQNT;
    private final long priceNQTPerCoin;
    private final double exchangeRate;
    private final long blockId;
    private final int height;
    private final int timestamp;

    CoinExchangeTradeResponseImpl(JSONObject response) {
        this(new JO(response));
    }

    CoinExchangeTradeResponseImpl(JO tradeJson) {
        orderFullHash = tradeJson.parseHexString("orderFullHash");
        matchFullHash = tradeJson.parseHexString("matchFullHash");
        chainId = tradeJson.getInt("chain");
        exchangeChainId = tradeJson.getInt("exchange");
        accountId = tradeJson.getEntityId("account");
        quantityQNT = tradeJson.getLong("quantityQNT");
        priceNQTPerCoin = tradeJson.getLong("priceNQTPerCoin");
        exchangeRate = tradeJson.getDouble("exchangeRate");
        timestamp = tradeJson.getInt("timestamp");
        blockId = tradeJson.getEntityId("block");
        height = tradeJson.getInt("height");
    }

    public byte[] getOrderFullHash() {
        return orderFullHash;
    }

    public byte[] getMatchFullHash() {
        return matchFullHash;
    }

    public int getChainId() {
        return chainId;
    }

    public int getExchangeChainId() {
        return exchangeChainId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccount() {
        return Long.toUnsignedString(accountId);
    }

    public String getAccountRs() {
        if (accountId == 0) {
            return null;
        }
        return Convert.rsAccount(accountId);
    }

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public long getPriceNQTPerCoin() {
        return priceNQTPerCoin;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public long getBlockId() {
        return blockId;
    }

    public int getHeight() {
        return height;
    }

    public int getTimestamp() {
        return timestamp;
    }
}