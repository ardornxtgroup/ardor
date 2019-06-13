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
import org.json.simple.JSONObject;

public interface CoinExchangeTradeResponse {

    static CoinExchangeTradeResponse create(Object object) {
        if (object instanceof JSONObject) {
            return new CoinExchangeTradeResponseImpl((JSONObject) object);
        } else {
            return new CoinExchangeTradeResponseImpl((JO) object);
        }
    }

    byte[] getOrderFullHash();

    byte[] getMatchFullHash();

    int getChainId();

    int getExchangeChainId();

    long getAccountId();

    String getAccount();

    String getAccountRs();

    long getQuantityQNT();

    long getPriceNQTPerCoin();

    double getExchangeRate();

    long getBlockId();

    int getHeight();

    int getTimestamp();
}
