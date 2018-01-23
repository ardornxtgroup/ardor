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

package nxt.voting;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PhasingVoteCastingAttachment extends Attachment.AbstractAttachment {

    private final List<ChainTransactionId> phasedTransactionsIds;
    private final List<byte[]> revealedSecrets;

    PhasingVoteCastingAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        byte length = buffer.get();
        phasedTransactionsIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            phasedTransactionsIds.add(ChainTransactionId.parse(buffer));
        }
        int secretsCount = buffer.getShort();
        if (secretsCount > 0) {
            revealedSecrets = new ArrayList<>(secretsCount);
            for (int i = 0; i < secretsCount; i++) {
                short secretLength = buffer.getShort();
                if (secretLength <= 0 || secretLength > Constants.MAX_PHASING_REVEALED_SECRET_LENGTH) {
                    throw new NxtException.NotValidException("Invalid revealed secret length " + secretLength);
                }
                byte[] revealedSecret = new byte[secretLength];
                buffer.get(revealedSecret);
                revealedSecrets.add(revealedSecret);
            }
        } else {
            revealedSecrets = Collections.emptyList();
        }
    }

    PhasingVoteCastingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        JSONArray phasedTransactionsJson = (JSONArray) attachmentData.get("phasedTransactions");
        phasedTransactionsIds = new ArrayList<>(phasedTransactionsJson.size());
        phasedTransactionsJson.forEach(json -> phasedTransactionsIds.add(ChainTransactionId.parse((JSONObject)json)));

        JSONArray revealedSecretsJson = (JSONArray) attachmentData.get("revealedSecrets");
        if (revealedSecretsJson == null) {
            this.revealedSecrets = Collections.emptyList();
        } else {
            this.revealedSecrets = new ArrayList<>(revealedSecretsJson.size());
            revealedSecretsJson.forEach(json -> revealedSecrets.add(Convert.parseHexString((String) json)));
        }
    }

    public PhasingVoteCastingAttachment(List<ChainTransactionId> phasedTransactionIds, List<byte[]> revealedSecret) {
        this.phasedTransactionsIds = phasedTransactionIds;
        this.revealedSecrets = revealedSecret;
    }

    @Override
    protected int getMySize() {
        int size = 1 + ChainTransactionId.BYTE_SIZE * phasedTransactionsIds.size();
        size += 2;
        for (byte[] secret : revealedSecrets) {
            size += 2 + secret.length;
        }
        return size;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put((byte) phasedTransactionsIds.size());
        phasedTransactionsIds.forEach(phasedTransaction -> phasedTransaction.put(buffer));
        buffer.putShort((short) revealedSecrets.size());
        revealedSecrets.forEach(revealedSecret -> {
            buffer.putShort((short) revealedSecret.length);
            buffer.put(revealedSecret);
        });
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        JSONArray phasedTransactionsJSON = new JSONArray();
        phasedTransactionsIds.forEach(phasedTransaction -> phasedTransactionsJSON.add(phasedTransaction.getJSON()));
        attachment.put("phasedTransactions", phasedTransactionsJSON);

        if (!revealedSecrets.isEmpty()) {
            JSONArray revealedSecretsJSON = new JSONArray();
            revealedSecrets.forEach(revealedSecret -> revealedSecretsJSON.add(Convert.toHexString(revealedSecret)));
            attachment.put("revealedSecrets", revealedSecretsJSON);
        }
    }

    @Override
    public TransactionType getTransactionType() {
        return VotingTransactionType.PHASING_VOTE_CASTING;
    }

    public List<ChainTransactionId> getPhasedTransactionsIds() {
        return phasedTransactionsIds;
    }

    public List<byte[]> getRevealedSecrets() {
        return revealedSecrets;
    }
}
