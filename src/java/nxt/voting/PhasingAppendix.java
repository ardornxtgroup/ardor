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

package nxt.voting;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Appendix;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionProcessor;
import nxt.blockchain.TransactionProcessorImpl;
import nxt.blockchain.TransactionType;
import nxt.util.BooleanExpression;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PhasingAppendix extends Appendix.AbstractAppendix {

    public static final int appendixType = 64;
    public static final String appendixName = "Phasing";

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException {
            return new PhasingAppendix(buffer);
        }

        @Override
        public AbstractAppendix parse(JSONObject attachmentData) throws NxtException.NotValidException {
            if (!Appendix.hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new PhasingAppendix(attachmentData);
        }
    };

    private static final Fee PHASING_FEE = (transaction, appendage) -> {
        PhasingParams phasingParams = ((PhasingAppendix)appendage).params;

        long fee;
        if (phasingParams.getVoteWeighting().getVotingModel() == VoteWeighting.VotingModel.COMPOSITE) {
            long feePerLen = Math.max(phasingParams.getExpressionStr().length() / 32, phasingParams.getExpression().getLiteralsCount());
            fee = (2 + feePerLen) * Constants.ONE_FXT / 100;

            for (PhasingParams subPoll : phasingParams.getSubPolls().values()) {
                fee += getFeePerPoll(subPoll);
            }
        } else {
            fee = getFeePerPoll(phasingParams);
        }
        return fee;
    };

    private static long getFeePerPoll(PhasingParams phasingParams) {
        long fee = 0;
        if (!phasingParams.getVoteWeighting().isBalanceIndependent()) {
            fee += 20 * Constants.ONE_FXT / 100;
        } else {
            fee += Constants.ONE_FXT / 100;
        }
        byte[] hashedSecret = phasingParams.getHashedSecret();
        if (hashedSecret.length > 0) {
            fee += (1 + (hashedSecret.length - 1) / 32) * Constants.ONE_FXT / 100;
        }
        fee += Constants.ONE_FXT * phasingParams.getLinkedTransactionsIds().size() / 100;

        fee += Constants.ONE_FXT * phasingParams.getSenderPropertyVoting().getValue().length() / (32 * 100);
        fee += Constants.ONE_FXT * phasingParams.getRecipientPropertyVoting().getValue().length() / (32 * 100);

        return fee;
    }

    private final int finishHeight;
    private final PhasingParams params;


    private PhasingAppendix(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        finishHeight = buffer.getInt();
        params = new PhasingParams(buffer);
    }

    private PhasingAppendix(JSONObject attachmentData) {
        super(attachmentData);
        finishHeight = ((Long) attachmentData.get("phasingFinishHeight")).intValue();
        params = new PhasingParams(attachmentData);
    }

    public PhasingAppendix(int finishHeight, PhasingParams phasingParams) {
        this.finishHeight = finishHeight;
        this.params = phasingParams;
    }

    @Override
    public int getAppendixType() {
        return appendixType;
    }

    @Override
    public String getAppendixName() {
        return appendixName;
    }

    @Override
    protected int getMySize() {
        return 4 + params.getMySize();
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putInt(finishHeight);
        params.putMyBytes(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        json.put("phasingFinishHeight", finishHeight);
        params.putMyJSON(json);
    }

    @Override
    public void validate(Transaction transaction) throws NxtException.ValidationException {
        params.validate(transaction);
        int currentHeight = Nxt.getBlockchain().getHeight();
        if (finishHeight <= currentHeight + (params.getVoteWeighting().acceptsVotes() ? 2 : 1)
                || finishHeight >= currentHeight + Constants.MAX_PHASING_DURATION) {
            throw new NxtException.NotCurrentlyValidException("Invalid finish height " + finishHeight);
        }
    }

    @Override
    public void validateAtFinish(Transaction transaction) throws NxtException.ValidationException {
        params.checkApprovable();
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        ((ChildChain) transaction.getChain()).getPhasingPollHome().addPoll(transaction, this);
    }

    @Override
    public boolean isPhasable() {
        return false;
    }

    @Override
    public boolean isAllowed(Chain chain) {
        return chain instanceof ChildChain;
    }

    @Override
    public Fee getBaselineFee(Transaction transaction) {
        return PHASING_FEE;
    }

    private void release(TransactionImpl transaction) {
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        Account recipientAccount = transaction.getRecipientId() == 0 ? null : Account.getAccount(transaction.getRecipientId());
        transaction.getAppendages().forEach(appendage -> {
            if (appendage.isPhasable()) {
                appendage.apply(transaction, senderAccount, recipientAccount);
            }
        });
        TransactionProcessorImpl.getInstance().notifyListeners(Collections.singletonList(transaction), TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        Logger.logDebugMessage("Transaction " + transaction.getStringId() + " has been released");
    }

    public void reject(ChildTransactionImpl transaction) {
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        transaction.getType().undoAttachmentUnconfirmed(transaction, senderAccount);
        transaction.getChain().getBalanceHome().getBalance(transaction.getSenderId())
                .addToUnconfirmedBalance(AccountLedger.LedgerEvent.REJECT_PHASED_TRANSACTION, AccountLedger.newEventId(transaction),
                                                 transaction.getAmount());
        TransactionProcessorImpl.getInstance()
                .notifyListeners(Collections.singletonList(transaction), TransactionProcessor.Event.REJECT_PHASED_TRANSACTION);
        Logger.logDebugMessage("Transaction " + transaction.getStringId() + " has been rejected");
    }

    public void countVotes(ChildTransactionImpl transaction) {
        if (PhasingPollHome.getResult(transaction) != null) {
            return;
        }
        PhasingPollHome phasingPollHome = transaction.getChain().getPhasingPollHome();
        PhasingPollHome.PhasingPoll poll = phasingPollHome.getPoll(transaction);
        long result = poll.countVotes();
        poll.finish(result);
        if (result >= poll.getQuorum()) {
            try {
                release(transaction);
            } catch (RuntimeException e) {
                Logger.logErrorMessage("Failed to release phased transaction " + JSON.toJSONString(transaction.getJSONObject()), e);
                reject(transaction);
            }
        } else {
            reject(transaction);
        }
    }

    public void tryCountVotes(ChildTransactionImpl transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        PhasingPollHome.PhasingPoll poll = transaction.getChain().getPhasingPollHome().getPoll(transaction);
        long result;
        BooleanExpression.Value compositeVotingResult = BooleanExpression.Value.UNKNOWN;
        if (poll.isCompositeVoting()) {
            compositeVotingResult = poll.getCompositeVotingResult();
            result = compositeVotingResult == BooleanExpression.Value.TRUE ? 1 : 0;
        } else {
            result = poll.countVotes();
        }
        if (result >= poll.getQuorum() || compositeVotingResult != BooleanExpression.Value.UNKNOWN) {
            if (compositeVotingResult == BooleanExpression.Value.FALSE) {
                reject(transaction);
                poll.finish(result);
                Logger.logDebugMessage("Composite voting expression of transaction " + transaction.getStringId()
                        + " evaluates to FALSE at height " + Nxt.getBlockchain().getHeight());
            } else {
                if (!transaction.attachmentIsDuplicate(duplicates, false)) {
                    try {
                        release(transaction);
                        poll.finish(result);
                        Logger.logDebugMessage("Early finish of transaction " + transaction.getStringId() + " at height " + Nxt.getBlockchain().getHeight());
                    } catch (RuntimeException e) {
                        Logger.logErrorMessage("Failed to release phased transaction " + JSON.toJSONString(transaction.getJSONObject()), e);
                    }
                } else {
                    Logger.logDebugMessage("At height " + Nxt.getBlockchain().getHeight() + " phased transaction " + transaction.getStringId()
                            + " is duplicate, cannot finish early");
                }
            }
        } else {
            Logger.logDebugMessage("At height " + Nxt.getBlockchain().getHeight() + " phased transaction " + transaction.getStringId()
                    + " does not yet meet quorum, cannot finish early");
        }
    }

    public int getFinishHeight() {
        return finishHeight;
    }

    public List<ChainTransactionId> getLinkedTransactionsIds() {
        return params.getLinkedTransactionsIds();
    }

    public PhasingParams getParams() {
        return params;
    }
}
