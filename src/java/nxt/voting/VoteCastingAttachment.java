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
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class VoteCastingAttachment extends Attachment.AbstractAttachment {

    private final long pollId;
    private final byte[] pollVote;

    public VoteCastingAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        pollId = buffer.getLong();
        int numberOfOptions = buffer.get();
        if (numberOfOptions > Constants.MAX_POLL_OPTION_COUNT) {
            throw new NxtException.NotValidException("More than " + Constants.MAX_POLL_OPTION_COUNT + " options in a vote");
        }
        pollVote = new byte[numberOfOptions];
        buffer.get(pollVote);
    }

    public VoteCastingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        pollId = Convert.parseUnsignedLong((String) attachmentData.get("poll"));
        JSONArray vote = (JSONArray) attachmentData.get("vote");
        pollVote = new byte[vote.size()];
        for (int i = 0; i < pollVote.length; i++) {
            pollVote[i] = ((Long) vote.get(i)).byteValue();
        }
    }

    public VoteCastingAttachment(long pollId, byte[] pollVote) {
        this.pollId = pollId;
        this.pollVote = pollVote;
    }

    @Override
    protected int getMySize() {
        return 8 + 1 + this.pollVote.length;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(this.pollId);
        buffer.put((byte) this.pollVote.length);
        buffer.put(this.pollVote);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("poll", Long.toUnsignedString(this.pollId));
        JSONArray vote = new JSONArray();
        if (this.pollVote != null) {
            for (byte aPollVote : this.pollVote) {
                vote.add(aPollVote);
            }
        }
        attachment.put("vote", vote);
    }

    @Override
    public TransactionType getTransactionType() {
        return VotingTransactionType.VOTE_CASTING;
    }

    public long getPollId() {
        return pollId;
    }

    public byte[] getPollVote() {
        return pollVote;
    }
}
