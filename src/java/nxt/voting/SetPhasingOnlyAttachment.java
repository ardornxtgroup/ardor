/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.voting;

import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public final class SetPhasingOnlyAttachment extends Attachment.AbstractAttachment {

    private final PhasingParams phasingParams;
    private final SortedMap<Integer, Long> maxFees;
    private final short minDuration;
    private final short maxDuration;

    public SetPhasingOnlyAttachment(PhasingParams params, SortedMap<Integer,Long> maxFees, short minDuration, short maxDuration) {
        this.phasingParams = params;
        this.maxFees = maxFees;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    SetPhasingOnlyAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.phasingParams = new PhasingParams(buffer);
        int count = (int)buffer.get() & 0xFF;
        maxFees = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            int chainId = buffer.getInt();
            if (maxFees.put(chainId, buffer.getLong()) != null) {
                throw new NxtException.NotValidException("Duplicate max fees chainId " + chainId);
            }
        }
        minDuration = buffer.getShort();
        maxDuration = buffer.getShort();
    }

    SetPhasingOnlyAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
        super(attachmentData);
        JSONObject phasingControlParams = (JSONObject) attachmentData.get("phasingControlParams");
        phasingParams = new PhasingParams(phasingControlParams);
        JSONObject maxFeesJSON = (JSONObject) attachmentData.get("controlMaxFees");
        maxFees = new TreeMap<>();
        for (Map.Entry entry : (Set<Map.Entry>)maxFeesJSON.entrySet()) {
            int chainId = Math.toIntExact(Convert.parseLong(entry.getKey()));
            long fees = Convert.parseLong(entry.getValue());
            if (maxFees.put(chainId, fees) != null) {
                throw new NxtException.NotValidException("Duplicate max fees chainId " + chainId);
            }
        }
        minDuration = ((Long)attachmentData.get("controlMinDuration")).shortValue();
        maxDuration = ((Long)attachmentData.get("controlMaxDuration")).shortValue();
    }

    @Override
    public TransactionType getTransactionType() {
        return AccountControlTransactionType.SET_PHASING_ONLY;
    }

    @Override
    protected int getMySize() {
        return phasingParams.getMySize() + 1 + maxFees.size() * (4 + 8) + 2 + 2;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        phasingParams.putMyBytes(buffer);
        buffer.put((byte)maxFees.size());
        maxFees.forEach((chainId,maxFee) -> {
            buffer.putInt(chainId);
            buffer.putLong(maxFee);
        });
        buffer.putShort(minDuration);
        buffer.putShort(maxDuration);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        JSONObject phasingControlParams = new JSONObject();
        phasingParams.putMyJSON(phasingControlParams);
        json.put("phasingControlParams", phasingControlParams);
        JSONObject maxFeesJSON = new JSONObject();
        maxFees.forEach(maxFeesJSON::put);
        json.put("controlMaxFees", maxFeesJSON);
        json.put("controlMinDuration", minDuration);
        json.put("controlMaxDuration", maxDuration);
    }

    public PhasingParams getPhasingParams() {
        return phasingParams;
    }

    public SortedMap<Integer, Long> getMaxFees() {
        return maxFees;
    }

    public short getMinDuration() {
        return minDuration;
    }

    public short getMaxDuration() {
        return maxDuration;
    }

}
