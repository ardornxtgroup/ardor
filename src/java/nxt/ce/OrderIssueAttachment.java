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

import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.Chain;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;

import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

/**
 * Coin exchange order issue attachment for a child chain
 */
public class OrderIssueAttachment extends Attachment.AbstractAttachment {

    private final Chain chain;
    private final Chain exchangeChain;
    private final long quantityQNT;
    private final long priceNQT;

    public OrderIssueAttachment(Chain chain, Chain exchangeChain, long quantityQNT, long priceNQT) {
        this.chain = chain;
        this.exchangeChain = exchangeChain;
        this.quantityQNT = quantityQNT;
        this.priceNQT = priceNQT;
    }

    OrderIssueAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        int chainId = buffer.getInt();
        this.chain = Chain.getChain(chainId);
        if (this.chain == null) {
            throw new NxtException.NotValidException("Chain '" + chainId + "' not defined");
        }
        chainId = buffer.getInt();
        this.exchangeChain = Chain.getChain(chainId);
        if (this.exchangeChain == null) {
            throw new NxtException.NotValidException("Exchange chain '" + chainId + "' not defined");
        }
        this.quantityQNT = buffer.getLong();
        this.priceNQT = buffer.getLong();
    }

    OrderIssueAttachment(JSONObject data) throws NxtException.NotValidException {
        super(data);
        int chainId = (int)Convert.parseLong(data.get("chain"));
        this.chain = Chain.getChain(chainId);
        if (this.chain == null) {
            throw new NxtException.NotValidException("Chain '" + chainId + "' not defined");
        }
        chainId = (int)Convert.parseLong(data.get("exchangeChain"));
        this.exchangeChain = Chain.getChain(chainId);
        if (this.exchangeChain == null) {
            throw new NxtException.NotValidException("Exchange chain '" + chainId + "' not defined");
        }
        this.quantityQNT = Convert.parseLong(data.get("quantityQNT"));
        this.priceNQT = Convert.parseLong(data.get("priceNQTPerCoin"));
    }

    @Override
    protected int getMySize() {
        return 4 + 4 + 8 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putInt(chain.getId())
              .putInt(exchangeChain.getId())
              .putLong(quantityQNT)
              .putLong(priceNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("chain", chain.getId());
        attachment.put("exchangeChain", exchangeChain.getId());
        attachment.put("quantityQNT", quantityQNT);
        attachment.put("priceNQTPerCoin", priceNQT);
    }

    /**
     * Return the chain
     *
     * @return                  Chain
     */
    public Chain getChain() {
        return chain;
    }

    /**
     * Return the exchange chain
     *
     * @return                  Exchange chain
     */
    public Chain getExchangeChain() {
        return exchangeChain;
    }

    /**
     * Return the exchange amount with an implied 8 decimal places
     *
     * @return                  Exchange amount
     */
    public long getQuantityQNT() {
        return quantityQNT;
    }

    /**
     * Return the exchange price with an implied 8 decimal places
     *
     * @return                  Exchange price
     */
    public long getPriceNQT() {
        return priceNQT;
    }

    /**
     * Return the transaction type
     *
     * @return                  Transaction type
     */
    @Override
    public TransactionType getTransactionType() {
        return CoinExchangeTransactionType.ORDER_ISSUE;
    }
}
