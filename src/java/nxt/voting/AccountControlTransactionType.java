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

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountRestrictions;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;

import static nxt.voting.VoteWeighting.VotingModel.*;

public abstract class AccountControlTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_ACCOUNT_CONTROL_PHASING_ONLY = 0;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_ACCOUNT_CONTROL_PHASING_ONLY:
                return AccountControlTransactionType.SET_PHASING_ONLY;
            default:
                return null;
        }
    }

    private AccountControlTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_ACCOUNT_CONTROL;
    }

    @Override
    public final boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    public final void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
    }

    @Override
    public final boolean isGlobal() {
        return true;
    }

    public static final TransactionType SET_PHASING_ONLY = new AccountControlTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_ACCOUNT_CONTROL_PHASING_ONLY;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ACCOUNT_CONTROL_PHASING_ONLY;
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return new Fee.ConstantFee(Constants.ONE_FXT);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new SetPhasingOnlyAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new SetPhasingOnlyAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            SetPhasingOnlyAttachment attachment = (SetPhasingOnlyAttachment)transaction.getAttachment();
            VoteWeighting.VotingModel votingModel = attachment.getPhasingParams().getVoteWeighting().getVotingModel();
            attachment.getPhasingParams().validateRestrictableParams();
            if (votingModel == NONE) {
                Account senderAccount = Account.getAccount(transaction.getSenderId());
                if (senderAccount == null || !senderAccount.getControls().contains(Account.ControlType.PHASING_ONLY)) {
                    throw new NxtException.NotCurrentlyValidException("Phasing only account control is not currently enabled");
                }
            } else if (votingModel == TRANSACTION || votingModel == HASH) {
                throw new NxtException.NotValidException("Invalid voting model " + votingModel + " for account control");
            }
            Map<Integer, Long> chainMaxFees = attachment.getMaxFees();
            for (Map.Entry<Integer, Long> entry : chainMaxFees.entrySet()) {
                ChildChain childChain = ChildChain.getChildChain(entry.getKey());
                if (childChain == null) {
                    throw new NxtException.NotValidException("Invalid child chain id " + entry.getKey());
                }
                long fees = entry.getValue() == null ? -1 : entry.getValue();
                long minFees = Nxt.getBlockchain().getHeight() >= Constants.MISSING_TX_SENDER_BLOCK ? 0 : 1;
                if (fees < minFees || fees > Constants.MAX_BALANCE_NQT) {
                    throw new NxtException.NotValidException(String.format("Invalid max fees %f for chain %s", ((double) fees) / childChain.ONE_COIN,
                            childChain.getName()));
                }
            }
            short minDuration = attachment.getMinDuration();
            if (minDuration < 0 || (minDuration > 0 && minDuration < 3) || minDuration >= Constants.MAX_PHASING_DURATION) {
                throw new NxtException.NotValidException("Invalid min duration " + attachment.getMinDuration());
            }
            short maxDuration = attachment.getMaxDuration();
            if (maxDuration < 0 || (maxDuration > 0 && maxDuration < 3) || maxDuration >= Constants.MAX_PHASING_DURATION) {
                throw new NxtException.NotValidException("Invalid max duration " + maxDuration);
            }
            if (minDuration > maxDuration) {
                throw new NxtException.NotValidException(String.format("Min duration %d cannot exceed max duration %d ",
                        minDuration, maxDuration));
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return TransactionType.isDuplicate(SET_PHASING_ONLY, Long.toUnsignedString(transaction.getSenderId()), duplicates, true);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            SetPhasingOnlyAttachment attachment = (SetPhasingOnlyAttachment)transaction.getAttachment();
            AccountRestrictions.PhasingOnly.set(senderAccount, attachment);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public String getName() {
            return "SetPhasingOnly";
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

}
