/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

import java.nio.ByteBuffer;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;

import org.json.simple.JSONObject;

/**
 * Coin exchange order cancel attachment for child chains
 */
public class OrderCancelAttachment extends Attachment.AbstractAttachment {

    private final byte[] orderHash;
    private final long orderId;

    public OrderCancelAttachment(byte[] orderHash) {
        this.orderHash = orderHash;
        this.orderId = Convert.fullHashToId(this.orderHash);
    }

    OrderCancelAttachment(ByteBuffer buffer) {
        super(buffer);
        orderHash = new byte[32];
        buffer.get(orderHash);
        orderId = Convert.fullHashToId(orderHash);
    }

    OrderCancelAttachment(JSONObject data) {
        super(data);
        orderHash = Convert.parseHexString((String)data.get("orderHash"));
        orderId = Convert.fullHashToId(orderHash);
    }

    @Override
    protected int getMySize() {
        return 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put(orderHash);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("orderHash", Convert.toHexString(orderHash));
    }

    /**
     * Return the exchange order hash
     *
     * @return                      Exchange order hash
     */
    public byte[] getOrderHash() {
        return orderHash;
    }

    /**
     * Return the exchange order identifier
     *
     * @return                      Exchange order identifier
     */
    public long getOrderId() {
        return orderId;
    }

    /**
     * Return the transaction type
     *
     * @return                      Transaction type
     */
    @Override
    public TransactionType getTransactionType() {
        return CoinExchangeTransactionType.ORDER_CANCEL;
    }
}
