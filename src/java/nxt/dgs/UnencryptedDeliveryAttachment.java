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

package nxt.dgs;

import nxt.NxtException;
import nxt.blockchain.Appendix;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class UnencryptedDeliveryAttachment extends DeliveryAttachment implements Appendix.Encryptable {

    private final byte[] goodsToEncrypt;
    private final byte[] recipientPublicKey;

    UnencryptedDeliveryAttachment(JSONObject attachmentData) {
        super(attachmentData);
        setGoods(null);
        String goodsToEncryptString = (String)attachmentData.get("goodsToEncrypt");
        this.goodsToEncrypt = goodsIsText() ? Convert.toBytes(goodsToEncryptString)
                : Convert.parseHexString(goodsToEncryptString);
        this.recipientPublicKey = Convert.parseHexString((String)attachmentData.get("recipientPublicKey"));
    }

    public UnencryptedDeliveryAttachment(long purchaseId, byte[] goodsToEncrypt, boolean goodsIsText, long discountNQT, byte[] recipientPublicKey) {
        super(purchaseId, null, goodsIsText, discountNQT);
        this.goodsToEncrypt = goodsToEncrypt;
        this.recipientPublicKey = recipientPublicKey;
    }

    @Override
    protected int getMySize() {
        if (getGoods() == null) {
            return 8 + 4 + EncryptedData.getEncryptedSize(getPlaintext()) + 8;
        }
        return super.getMySize();
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        if (getGoods() == null) {
            throw new NxtException.NotYetEncryptedException("Goods not yet encrypted");
        }
        super.putMyBytes(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        if (getGoods() == null) {
            attachment.put("goodsToEncrypt", goodsIsText() ? Convert.toString(goodsToEncrypt) : Convert.toHexString(goodsToEncrypt));
            attachment.put("recipientPublicKey", Convert.toHexString(recipientPublicKey));
            attachment.put("purchase", Long.toUnsignedString(getPurchaseId()));
            attachment.put("discountNQT", getDiscountNQT());
            attachment.put("goodsIsText", goodsIsText());
        } else {
            super.putMyJSON(attachment);
        }
    }

    @Override
    public void encrypt(String secretPhrase) {
        setGoods(EncryptedData.encrypt(getPlaintext(), secretPhrase, recipientPublicKey));
    }

    @Override
    int getGoodsDataLength() {
        return EncryptedData.getEncryptedDataLength(getPlaintext());
    }

    private byte[] getPlaintext() {
        return Convert.compress(goodsToEncrypt);
    }

}
