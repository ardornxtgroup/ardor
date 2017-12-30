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
 
 package nxt.ae;

import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import nxt.voting.PhasingParams;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public class SetPhasingAssetControlAttachment extends Attachment.AbstractAttachment {
    private final long assetId;
    private final PhasingParams phasingParams;

    SetPhasingAssetControlAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.assetId = buffer.getLong();
        this.phasingParams = new PhasingParams(buffer);
    }

    SetPhasingAssetControlAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.assetId = Convert.parseUnsignedLong((String)attachmentData.get("asset"));
        JSONObject phasingControlParams = (JSONObject) attachmentData.get("phasingControlParams");
        phasingParams = new PhasingParams(phasingControlParams);
    }

    public SetPhasingAssetControlAttachment(long assetId, PhasingParams phasingParams) {
        this.assetId = assetId;
        this.phasingParams = phasingParams;
    }

    @Override
    protected int getMySize() {
        return 8 + phasingParams.getMySize();
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(assetId);
        phasingParams.putMyBytes(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        json.put("asset", Long.toUnsignedString(assetId));
        JSONObject phasingControlParams = new JSONObject();
        phasingParams.putMyJSON(phasingControlParams);
        json.put("phasingControlParams", phasingControlParams);
    }

    @Override
    public TransactionType getTransactionType() {
        return AssetExchangeTransactionType.SET_PHASING_CONTROL;
    }

    public long getAssetId() {
        return assetId;
    }

    public PhasingParams getPhasingParams() {
        return phasingParams;
    }
}
