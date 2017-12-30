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

package nxt.account;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class EffectiveBalanceLeasingAttachment extends Attachment.AbstractAttachment {

    private final int period;

    EffectiveBalanceLeasingAttachment(ByteBuffer buffer) {
        super(buffer);
        this.period = Short.toUnsignedInt(buffer.getShort());
    }

    EffectiveBalanceLeasingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.period = ((Long) attachmentData.get("period")).intValue();
    }

    public EffectiveBalanceLeasingAttachment(int period) {
        this.period = period;
    }

    @Override
    protected int getMySize() {
        return 2;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putShort((short)period);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("period", period);
    }

    @Override
    public TransactionType getTransactionType() {
        return AccountControlFxtTransactionType.EFFECTIVE_BALANCE_LEASING;
    }

    public int getPeriod() {
        return period;
    }
}
