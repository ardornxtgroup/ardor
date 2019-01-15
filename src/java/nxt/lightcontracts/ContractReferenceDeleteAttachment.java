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

package nxt.lightcontracts;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class ContractReferenceDeleteAttachment extends Attachment.AbstractAttachment {

    private final long contractReferenceId;

    ContractReferenceDeleteAttachment(ByteBuffer buffer) {
        super(buffer);
        this.contractReferenceId = buffer.getLong();
    }

    ContractReferenceDeleteAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.contractReferenceId = Convert.parseUnsignedLong((String)attachmentData.get("contractReference"));
    }

    public ContractReferenceDeleteAttachment(long contractReferenceId) {
        this.contractReferenceId = contractReferenceId;
    }

    @Override
    protected int getMySize() {
        return 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(contractReferenceId);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("contractReference", Long.toUnsignedString(contractReferenceId));
    }

    @Override
    public TransactionType getTransactionType() {
        return LightContractTransactionType.CONTRACT_REFERENCE_DELETE;
    }

    public long getContractReferenceId() {
        return contractReferenceId;
    }

}
