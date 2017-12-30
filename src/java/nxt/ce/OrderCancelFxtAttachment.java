/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
package nxt.ce;

import nxt.blockchain.TransactionType;

import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

/**
 * Coin exchange order cancel attachment for the Fxt chain
 */
public class OrderCancelFxtAttachment extends OrderCancelAttachment {

    public OrderCancelFxtAttachment(byte[] orderHash) {
        super(orderHash);
    }

    OrderCancelFxtAttachment(ByteBuffer buffer) {
        super(buffer);
    }

    OrderCancelFxtAttachment(JSONObject data) {
        super(data);
    }

    /**
     * Return the transaction type
     *
     * @return                      Transaction type
     */
    @Override
    public TransactionType getTransactionType() {
        return CoinExchangeFxtTransactionType.ORDER_CANCEL;
    }
}
