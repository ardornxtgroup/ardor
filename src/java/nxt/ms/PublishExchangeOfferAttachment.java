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

package nxt.ms;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class PublishExchangeOfferAttachment extends Attachment.AbstractAttachment implements MonetarySystemAttachment {

    private final long currencyId;
    private final long buyRateNQT;
    private final long sellRateNQT;
    private final long totalBuyLimitQNT;
    private final long totalSellLimitQNT;
    private final long initialBuySupplyQNT;
    private final long initialSellSupplyQNT;
    private final int expirationHeight;

    public PublishExchangeOfferAttachment(ByteBuffer buffer) {
        super(buffer);
        this.currencyId = buffer.getLong();
        this.buyRateNQT = buffer.getLong();
        this.sellRateNQT = buffer.getLong();
        this.totalBuyLimitQNT = buffer.getLong();
        this.totalSellLimitQNT = buffer.getLong();
        this.initialBuySupplyQNT = buffer.getLong();
        this.initialSellSupplyQNT = buffer.getLong();
        this.expirationHeight = buffer.getInt();
    }

    public PublishExchangeOfferAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.currencyId = Convert.parseUnsignedLong((String)attachmentData.get("currency"));
        this.buyRateNQT = Convert.parseLong(attachmentData.get("buyRateNQTPerUnit"));
        this.sellRateNQT = Convert.parseLong(attachmentData.get("sellRateNQTPerUnit"));
        this.totalBuyLimitQNT = Convert.parseLong(attachmentData.get("totalBuyLimitQNT"));
        this.totalSellLimitQNT = Convert.parseLong(attachmentData.get("totalSellLimitQNT"));
        this.initialBuySupplyQNT = Convert.parseLong(attachmentData.get("initialBuySupplyQNT"));
        this.initialSellSupplyQNT = Convert.parseLong(attachmentData.get("initialSellSupplyQNT"));
        this.expirationHeight = Convert.parseInt(attachmentData.get("expirationHeight"));
    }

    public PublishExchangeOfferAttachment(long currencyId, long buyRateNQT, long sellRateNQT,
            long totalBuyLimitQNT, long totalSellLimitQNT, long initialBuySupplyQNT, long initialSellSupplyQNT,
            int expirationHeight) {
        this.currencyId = currencyId;
        this.buyRateNQT = buyRateNQT;
        this.sellRateNQT = sellRateNQT;
        this.totalBuyLimitQNT = totalBuyLimitQNT;
        this.totalSellLimitQNT = totalSellLimitQNT;
        this.initialBuySupplyQNT = initialBuySupplyQNT;
        this.initialSellSupplyQNT = initialSellSupplyQNT;
        this.expirationHeight = expirationHeight;
    }

    @Override
    protected int getMySize() {
        return 8 + 8 + 8 + 8 + 8 + 8 + 8 + 4;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(currencyId);
        buffer.putLong(buyRateNQT);
        buffer.putLong(sellRateNQT);
        buffer.putLong(totalBuyLimitQNT);
        buffer.putLong(totalSellLimitQNT);
        buffer.putLong(initialBuySupplyQNT);
        buffer.putLong(initialSellSupplyQNT);
        buffer.putInt(expirationHeight);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("currency", Long.toUnsignedString(currencyId));
        attachment.put("buyRateNQTPerUnit", buyRateNQT);
        attachment.put("sellRateNQTPerUnit", sellRateNQT);
        attachment.put("totalBuyLimitQNT", totalBuyLimitQNT);
        attachment.put("totalSellLimitQNT", totalSellLimitQNT);
        attachment.put("initialBuySupplyQNT", initialBuySupplyQNT);
        attachment.put("initialSellSupplyQNT", initialSellSupplyQNT);
        attachment.put("expirationHeight", expirationHeight);
    }

    @Override
    public TransactionType getTransactionType() {
        return MonetarySystemTransactionType.PUBLISH_EXCHANGE_OFFER;
    }

    @Override
    public long getCurrencyId() {
        return currencyId;
    }

    public long getBuyRateNQT() {
        return buyRateNQT;
    }

    public long getSellRateNQT() {
        return sellRateNQT;
    }

    public long getTotalBuyLimitQNT() {
        return totalBuyLimitQNT;
    }

    public long getTotalSellLimitQNT() {
        return totalSellLimitQNT;
    }

    public long getInitialBuySupplyQNT() {
        return initialBuySupplyQNT;
    }

    public long getInitialSellSupplyQNT() {
        return initialSellSupplyQNT;
    }

    public int getExpirationHeight() {
        return expirationHeight;
    }

}
