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

package nxt.lightcontracts;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import nxt.util.bbh.StringRw;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

import static nxt.util.bbh.LengthRwPrimitiveType.UBYTE;

public final class ContractReferenceAttachment extends Attachment.AbstractAttachment {
    public static final StringRw NAME_RW = new StringRw(Constants.MAX_CONTRACT_NAME_LENGTH);
    public static final StringRw PARAMS_RW = new StringRw(UBYTE, Constants.MAX_CONTRACT_PARAMS_LENGTH);

    private final String contractName;
    private final String contractParams;
    private final ChainTransactionId contractId;

    ContractReferenceAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.contractName = NAME_RW.readFromBuffer(buffer).trim();
        this.contractParams = PARAMS_RW.readFromBuffer(buffer).trim();
        this.contractId = ChainTransactionId.parse(buffer);
    }

    ContractReferenceAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.contractName = Convert.nullToEmpty((String) attachmentData.get("contractName")).trim();
        this.contractParams = Convert.nullToEmpty((String) attachmentData.get("contractParams")).trim();
        this.contractId = ChainTransactionId.parse((JSONObject) attachmentData.get("contract"));
    }

    public ContractReferenceAttachment(String contractName, String contractParams, ChainTransactionId contractId) {
        this.contractName = contractName.trim();
        this.contractParams = Convert.nullToEmpty(contractParams).trim();
        this.contractId = contractId;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(contractName).length + 1 + Convert.toBytes(contractParams).length + ChainTransactionId.BYTE_SIZE;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        NAME_RW.writeToBuffer(contractName, buffer);
        PARAMS_RW.writeToBuffer(contractParams, buffer);
        contractId.put(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("contractName", contractName);
        attachment.put("contractParams", contractParams);
        attachment.put("contract", contractId.getJSON());
    }

    @Override
    public TransactionType getTransactionType() {
        return LightContractTransactionType.CONTRACT_REFERENCE_SET;
    }

    public String getContractName() {
        return contractName;
    }

    public String getContractParams() {
        return contractParams;
    }

    public ChainTransactionId getContractId() {
        return contractId;
    }

}
