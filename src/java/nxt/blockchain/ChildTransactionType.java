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

package nxt.blockchain;

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountPropertyTransactionType;
import nxt.account.PaymentTransactionType;
import nxt.ae.AssetExchangeTransactionType;
import nxt.aliases.AliasTransactionType;
import nxt.ce.CoinExchangeTransactionType;
import nxt.dgs.DigitalGoodsTransactionType;
import nxt.lightcontracts.LightContractTransactionType;
import nxt.messaging.MessagingTransactionType;
import nxt.ms.MonetarySystemTransactionType;
import nxt.shuffling.ShufflingTransactionType;
import nxt.taggeddata.TaggedDataTransactionType;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.AccountControlTransactionType;
import nxt.voting.VotingTransactionType;

public abstract class ChildTransactionType extends TransactionType {

    protected static final byte TYPE_PAYMENT = 0;
    protected static final byte TYPE_MESSAGING = 1;
    protected static final byte TYPE_ASSET_EXCHANGE = 2;
    protected static final byte TYPE_DIGITAL_GOODS = 3;
    protected static final byte TYPE_ACCOUNT_CONTROL = 4;
    protected static final byte TYPE_MONETARY_SYSTEM = 5;
    protected static final byte TYPE_DATA = 6;
    protected static final byte TYPE_SHUFFLING = 7;
    protected static final byte TYPE_ALIASES = 8;
    protected static final byte TYPE_VOTING = 9;
    protected static final byte TYPE_ACCOUNT_PROPERTY = 10;
    protected static final byte TYPE_COIN_EXCHANGE = 11;
    protected static final byte TYPE_LIGHT_CONTRACT = 12;

    public static TransactionType findTransactionType(byte type, byte subtype) {
        switch (type) {
            case TYPE_PAYMENT:
                return PaymentTransactionType.findTransactionType(subtype);
            case TYPE_MESSAGING:
                return MessagingTransactionType.findTransactionType(subtype);
            case TYPE_ASSET_EXCHANGE:
                return AssetExchangeTransactionType.findTransactionType(subtype);
            case TYPE_DIGITAL_GOODS:
                return DigitalGoodsTransactionType.findTransactionType(subtype);
            case TYPE_ACCOUNT_CONTROL:
                return AccountControlTransactionType.findTransactionType(subtype);
            case TYPE_MONETARY_SYSTEM:
                return MonetarySystemTransactionType.findTransactionType(subtype);
            case TYPE_DATA:
                return TaggedDataTransactionType.findTransactionType(subtype);
            case TYPE_SHUFFLING:
                return ShufflingTransactionType.findTransactionType(subtype);
            case TYPE_ALIASES:
                return AliasTransactionType.findTransactionType(subtype);
            case TYPE_VOTING:
                return VotingTransactionType.findTransactionType(subtype);
            case TYPE_ACCOUNT_PROPERTY:
                return AccountPropertyTransactionType.findTransactionType(subtype);
            case TYPE_COIN_EXCHANGE:
                return CoinExchangeTransactionType.findTransactionType(subtype);
            case TYPE_LIGHT_CONTRACT:
                return LightContractTransactionType.findTransactionType(subtype);
            default:
                return null;
        }
    }

    public Fee getBaselineFee(Transaction transaction) {
        return Fee.DEFAULT_CHILD_FEE;
    }

    @Override
    public final boolean applyUnconfirmed(TransactionImpl transaction, Account senderAccount) {
        ChildChain childChain = (ChildChain) transaction.getChain();
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        long amount = transaction.getAmount();
        long fee = transaction.getFee();
        long deposit = 0;
        if (((ChildTransactionImpl)transaction).getReferencedTransactionId() != null) {
            if (FxtChain.FXT.getBalanceHome().getBalance(senderAccount.getId()).getUnconfirmedBalance() < Constants.UNCONFIRMED_POOL_DEPOSIT_FQT) {
                Logger.logInfoMessage("account %s must have enough %s to pay the unconfirmed pool deposit", Convert.rsAccount(senderAccount.getId()), FxtChain.FXT_NAME);
                return false;
            }
            deposit = Constants.UNCONFIRMED_POOL_DEPOSIT_FQT;
            senderAccount.addToUnconfirmedBalance(FxtChain.FXT, getLedgerEvent(), eventId, (long) 0, -deposit);
        }
        long totalAmount = Math.addExact(amount, fee);
        if (childChain.getBalanceHome().getBalance(senderAccount.getId()).getUnconfirmedBalance() < totalAmount) {
            senderAccount.addToUnconfirmedBalance(FxtChain.FXT, getLedgerEvent(), eventId, (long) 0, deposit);
            return false;
        }
        senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, -amount, -fee);
        if (!applyAttachmentUnconfirmed(transaction, senderAccount)) {
            senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, amount, fee);
            senderAccount.addToUnconfirmedBalance(FxtChain.FXT, getLedgerEvent(), eventId, (long) 0, deposit);
            return false;
        }
        return true;
    }

    @Override
    public final void apply(TransactionImpl transaction, Account senderAccount, Account recipientAccount) {
        ChildChain childChain = (ChildChain) transaction.getChain();
        long amount = transaction.getAmount();
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        if (!transaction.attachmentIsPhased()) {
            senderAccount.addToBalance(childChain, getLedgerEvent(), eventId, -amount, -transaction.getFee());
        } else {
            senderAccount.addToBalance(childChain, getLedgerEvent(), eventId, -amount);
        }
        if (recipientAccount != null) {
            recipientAccount.addToBalanceAndUnconfirmedBalance(childChain, getLedgerEvent(), eventId, amount);
        }
        applyAttachment(transaction, senderAccount, recipientAccount);
    }

    @Override
    public final void undoUnconfirmed(TransactionImpl transaction, Account senderAccount) {
        ChildChain childChain = (ChildChain) transaction.getChain();
        undoAttachmentUnconfirmed(transaction, senderAccount);
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId,
                transaction.getAmount(), transaction.getFee());
        if (((ChildTransactionImpl)transaction).getReferencedTransactionId() != null) {
            senderAccount.addToUnconfirmedBalance(FxtChain.FXT, getLedgerEvent(), eventId, (long) 0, Constants.UNCONFIRMED_POOL_DEPOSIT_FQT);
        }
    }

    @Override
    public final void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
        validateAttachment((ChildTransactionImpl)transaction);
    }

    protected abstract void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException;

    @Override
    public final boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        return applyAttachmentUnconfirmed((ChildTransactionImpl)transaction, senderAccount);
    }

    protected abstract boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount);

    @Override
    public final void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
        applyAttachment((ChildTransactionImpl)transaction, senderAccount, recipientAccount);
    }

    protected abstract void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount);

    @Override
    public final void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        undoAttachmentUnconfirmed((ChildTransactionImpl)transaction, senderAccount);
    }

    protected abstract void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount);

    @Override
    protected final void validateId(Transaction transaction) throws NxtException.ValidationException {
        validateId((ChildTransactionImpl)transaction);
    }

    protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
    }

}
