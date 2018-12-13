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

package nxt.ae;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.NxtException.NotValidException;
import nxt.NxtException.ValidationException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.BalanceHome;
import nxt.account.HoldingType;
import nxt.blockchain.Appendix;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import nxt.ms.Currency;
import nxt.ms.CurrencyType;
import nxt.util.Convert;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

import static nxt.voting.VoteWeighting.VotingModel.HASH;
import static nxt.voting.VoteWeighting.VotingModel.NONE;
import static nxt.voting.VoteWeighting.VotingModel.TRANSACTION;

public abstract class AssetExchangeTransactionType<Att extends Attachment> extends ChildTransactionType {

    private static final byte SUBTYPE_ASSET_EXCHANGE_ASSET_ISSUANCE = 0;
    private static final byte SUBTYPE_ASSET_EXCHANGE_ASSET_TRANSFER = 1;
    private static final byte SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_PLACEMENT = 2;
    private static final byte SUBTYPE_ASSET_EXCHANGE_BID_ORDER_PLACEMENT = 3;
    private static final byte SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_CANCELLATION = 4;
    private static final byte SUBTYPE_ASSET_EXCHANGE_BID_ORDER_CANCELLATION = 5;
    private static final byte SUBTYPE_ASSET_EXCHANGE_DIVIDEND_PAYMENT = 6;
    private static final byte SUBTYPE_ASSET_EXCHANGE_ASSET_DELETE = 7;
    private static final byte SUBTYPE_ASSET_EXCHANGE_ASSET_INCREASE = 8;
    private static final byte SUBTYPE_ASSET_EXCHANGE_SET_PHASING_CONTROL = 9;
    private static final byte SUBTYPE_ASSET_EXCHANGE_PROPERTY_SET = 10;
    private static final byte SUBTYPE_ASSET_EXCHANGE_PROPERTY_DELETE = 11;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_ASSET_EXCHANGE_ASSET_ISSUANCE:
                return AssetExchangeTransactionType.ASSET_ISSUANCE;
            case SUBTYPE_ASSET_EXCHANGE_ASSET_TRANSFER:
                return AssetExchangeTransactionType.ASSET_TRANSFER;
            case SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_PLACEMENT:
                return AssetExchangeTransactionType.ASK_ORDER_PLACEMENT;
            case SUBTYPE_ASSET_EXCHANGE_BID_ORDER_PLACEMENT:
                return AssetExchangeTransactionType.BID_ORDER_PLACEMENT;
            case SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_CANCELLATION:
                return AssetExchangeTransactionType.ASK_ORDER_CANCELLATION;
            case SUBTYPE_ASSET_EXCHANGE_BID_ORDER_CANCELLATION:
                return AssetExchangeTransactionType.BID_ORDER_CANCELLATION;
            case SUBTYPE_ASSET_EXCHANGE_DIVIDEND_PAYMENT:
                return AssetExchangeTransactionType.DIVIDEND_PAYMENT;
            case SUBTYPE_ASSET_EXCHANGE_ASSET_DELETE:
                return AssetExchangeTransactionType.ASSET_DELETE;
            case SUBTYPE_ASSET_EXCHANGE_ASSET_INCREASE:
                return AssetExchangeTransactionType.ASSET_INCREASE;
            case SUBTYPE_ASSET_EXCHANGE_SET_PHASING_CONTROL:
                return AssetExchangeTransactionType.SET_PHASING_CONTROL;
            case SUBTYPE_ASSET_EXCHANGE_PROPERTY_SET:
                return ASSET_PROPERTY_SET;
            case SUBTYPE_ASSET_EXCHANGE_PROPERTY_DELETE:
                return ASSET_PROPERTY_DELETE;
            default:
                return null;
        }
    }

    /*
            AssetExchangeTransactionType.ASSET_TRANSFER,
            AssetExchangeTransactionType.ASK_ORDER_PLACEMENT,
            AssetExchangeTransactionType.BID_ORDER_PLACEMENT,
            AssetExchangeTransactionType.ASK_ORDER_CANCELLATION,
            AssetExchangeTransactionType.BID_ORDER_CANCELLATION

     */

    private AssetExchangeTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_ASSET_EXCHANGE;
    }

    //Note: for dividend payment, this returns the asset being distributed as dividend, if any
    public abstract long getAssetId(ChildTransaction transaction);

    @SuppressWarnings("unchecked")
    @Override
    protected final void validateAttachment(ChildTransactionImpl transaction) throws ValidationException {
        checkLiquid(transaction);
        validateAttachment(transaction, (Att) transaction.getAttachment());
    }

    protected void checkLiquid(ChildTransaction transaction) throws NxtException.NotCurrentlyValidException {
        AssetFreezeMonitor.checkLiquid(getAssetId(transaction));
    }

    protected abstract void validateAttachment(ChildTransactionImpl transaction, Att attachment) throws ValidationException;

    public static final TransactionType ASSET_ISSUANCE = new AssetExchangeTransactionType<AssetIssuanceAttachment>() {

        private final Fee SINGLETON_ASSET_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT, Constants.ONE_FXT, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                AssetIssuanceAttachment attachment = (AssetIssuanceAttachment) transaction.getAttachment();
                return attachment.getDescription().length();
            }
        };

        private final Fee ASSET_ISSUANCE_FEE = (transaction, appendage) -> 100 * Constants.ONE_FXT;

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASSET_ISSUANCE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_ISSUANCE;
        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            return transaction.getId();
        }

        @Override
        protected void checkLiquid(ChildTransaction transaction) {
        }

        @Override
        public String getName() {
            return "AssetIssuance";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return isSingletonIssuance(transaction) ? SINGLETON_ASSET_FEE : ASSET_ISSUANCE_FEE;
        }

        @Override
        public AssetIssuanceAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AssetIssuanceAttachment(buffer);
        }

        @Override
        public AssetIssuanceAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetIssuanceAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetIssuanceAttachment attachment = (AssetIssuanceAttachment) transaction.getAttachment();
            Asset.addAsset(transaction, attachment);
            senderAccount.addToAssetAndUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    transaction.getId(), attachment.getQuantityQNT());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        protected void validateAttachment(ChildTransactionImpl transaction, AssetIssuanceAttachment attachment) throws ValidationException {
            if (attachment.getName().length() < Constants.MIN_ASSET_NAME_LENGTH
                    || !AssetIssuanceAttachment.NAME_RW.validate(attachment.getName())
                    || !AssetIssuanceAttachment.DESCRIPTION_RW.validate(attachment.getDescription())
                    || attachment.getDecimals() < 0 || attachment.getDecimals() > 8
                    || attachment.getQuantityQNT() <= 0
                    || attachment.getQuantityQNT() > Constants.MAX_ASSET_QUANTITY_QNT
                    ) {
                throw new NxtException.NotValidException("Invalid asset issuance: " + attachment.getJSONObject());
            }
            String normalizedName = attachment.getName().toLowerCase(Locale.ROOT);
            for (int i = 0; i < normalizedName.length(); i++) {
                if (Constants.ALPHABET.indexOf(normalizedName.charAt(i)) < 0) {
                    throw new NxtException.NotValidException("Invalid asset name: " + normalizedName);
                }
            }
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (Asset.getAsset(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate asset id " + transaction.getStringId());
            }
        }

        @Override
        public boolean isBlockDuplicate(final Transaction transaction, final Map<TransactionType, Map<String, Integer>> duplicates) {
            return !isSingletonIssuance(transaction) && isDuplicate(AssetExchangeTransactionType.ASSET_ISSUANCE, getName(), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

        @Override
        public final boolean isGlobal() {
            return true;
        }

        private boolean isSingletonIssuance(Transaction transaction) {
            AssetIssuanceAttachment attachment = (AssetIssuanceAttachment) transaction.getAttachment();
            return attachment.getQuantityQNT() == 1 && attachment.getDecimals() == 0
                    && attachment.getDescription().length() <= Constants.MAX_SINGLETON_ASSET_DESCRIPTION_LENGTH;
        }

    };

    public static final TransactionType ASSET_TRANSFER = new AssetExchangeTransactionType<AssetTransferAttachment>() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASSET_TRANSFER;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_TRANSFER;
        }

        @Override
        public String getName() {
            return "AssetTransfer";
        }

        @Override
        public AssetTransferAttachment parseAttachment(ByteBuffer buffer) {
            return new AssetTransferAttachment(buffer);
        }

        @Override
        public AssetTransferAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetTransferAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AssetTransferAttachment attachment = (AssetTransferAttachment) transaction.getAttachment();
            long unconfirmedAssetBalance = senderAccount.getUnconfirmedAssetBalanceQNT(attachment.getAssetId());
            if (unconfirmedAssetBalance >= attachment.getQuantityQNT()) {
                senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getAssetId(), -attachment.getQuantityQNT());
                return true;
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetTransferAttachment attachment = (AssetTransferAttachment) transaction.getAttachment();
            AccountLedger.LedgerEventId ledgerEventId = AccountLedger.newEventId(transaction);
            senderAccount.addToAssetBalanceQNT(getLedgerEvent(), ledgerEventId, attachment.getAssetId(),
                    -attachment.getQuantityQNT());
            recipientAccount.addToAssetAndUnconfirmedAssetBalanceQNT(getLedgerEvent(), ledgerEventId,
                    attachment.getAssetId(), attachment.getQuantityQNT());
            AssetTransfer.addAssetTransfer(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AssetTransferAttachment attachment = (AssetTransferAttachment) transaction.getAttachment();
            senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    attachment.getAssetId(), attachment.getQuantityQNT());
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, AssetTransferAttachment attachment) throws ValidationException {
            if (transaction.getAmount() != 0 || attachment.getAssetId() == 0) {
                throw new NxtException.NotValidException("Invalid asset transfer amount or asset: " + attachment.getJSONObject());
            }
            long quantityQNT = attachment.getQuantityQNT();
            if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
                throw new NxtException.NotValidException("Invalid asset transfer quantity: " + attachment.getJSONObject());
            }
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId()) +
                        " does not exist yet");
            }
            if (quantityQNT > asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("Invalid asset transfer quantity: " + attachment.getJSONObject());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

        @Override
        public final boolean isGlobal() {
            return false;
        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            AssetTransferAttachment attachment = (AssetTransferAttachment) transaction.getAttachment();
            return attachment.getAssetId();
        }
    };

    public static final TransactionType ASSET_DELETE = new AssetExchangeTransactionType<AssetDeleteAttachment>() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASSET_DELETE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_DELETE;
        }

        @Override
        public String getName() {
            return "AssetDelete";
        }

        @Override
        public AssetDeleteAttachment parseAttachment(ByteBuffer buffer) {
            return new AssetDeleteAttachment(buffer);
        }

        @Override
        public AssetDeleteAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetDeleteAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AssetDeleteAttachment attachment = (AssetDeleteAttachment) transaction.getAttachment();
            long unconfirmedAssetBalance = senderAccount.getUnconfirmedAssetBalanceQNT(attachment.getAssetId());
            if (unconfirmedAssetBalance >= attachment.getQuantityQNT()) {
                senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getAssetId(), -attachment.getQuantityQNT());
                return true;
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetDeleteAttachment attachment = (AssetDeleteAttachment) transaction.getAttachment();
            senderAccount.addToAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction), attachment.getAssetId(),
                    -attachment.getQuantityQNT());
            Asset.deleteAsset(transaction, attachment.getAssetId(), attachment.getQuantityQNT());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AssetDeleteAttachment attachment = (AssetDeleteAttachment) transaction.getAttachment();
            senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    attachment.getAssetId(), attachment.getQuantityQNT());
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, AssetDeleteAttachment attachment) throws ValidationException {
            if (attachment.getAssetId() == 0) {
                throw new NxtException.NotValidException("Invalid asset identifier: " + attachment.getJSONObject());
            }
            long quantityQNT = attachment.getQuantityQNT();
            if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
                throw new NxtException.NotValidException("Invalid asset delete quantity: " + attachment.getJSONObject());
            }
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId()) +
                        " does not exist yet");
            }
            if (quantityQNT > asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("Invalid asset delete quantity: " + attachment.getJSONObject());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

        @Override
        public final boolean isGlobal() {
            return false;
        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            AssetDeleteAttachment attachment = (AssetDeleteAttachment) transaction.getAttachment();
            return attachment.getAssetId();
        }
    };

    public static final TransactionType ASSET_INCREASE = new AssetExchangeTransactionType<AssetIncreaseAttachment>() {

        @Override
        public long getAssetId(ChildTransaction transaction) {
            return ((AssetIncreaseAttachment) transaction.getAttachment()).getAssetId();
        }

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASSET_INCREASE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_INCREASE;
        }

        @Override
        public String getName() {
            return "AssetIncrease";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return new Fee.ConstantFee(Constants.ONE_FXT * 10);
        }

        @Override
        public AssetIncreaseAttachment parseAttachment(ByteBuffer buffer) {
            return new AssetIncreaseAttachment(buffer);
        }

        @Override
        public AssetIncreaseAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetIncreaseAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetIncreaseAttachment attachment = (AssetIncreaseAttachment) transaction.getAttachment();
            senderAccount.addToAssetAndUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction), attachment.getAssetId(),
                    attachment.getQuantityQNT());
            Asset.increaseAsset(transaction, attachment.getAssetId(), attachment.getQuantityQNT());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, AssetIncreaseAttachment attachment) throws ValidationException {
            if (attachment.getAssetId() == 0) {
                throw new NxtException.NotValidException("Invalid asset identifier: " + attachment.getJSONObject());
            }
            long quantityQNT = attachment.getQuantityQNT();
            if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
                throw new NxtException.NotValidException("Invalid asset increase quantity: " + attachment.getJSONObject());
            }
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId()) +
                        " does not exist yet");
            }
            if (Constants.MAX_ASSET_QUANTITY_QNT - quantityQNT < asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("Invalid asset increase quantity: " + attachment.getJSONObject());
            }
            if (asset.getQuantityQNT() == 1) {
                throw new NxtException.NotCurrentlyValidException("No quantity increase allowed for single share assets");
            }
            if (asset.getQuantityQNT() == 0) {
                throw new NxtException.NotCurrentlyValidException("No quantity increase allowed for deleted assets");
            }
            if (transaction.getSenderId() != asset.getAccountId()) {
                throw new NxtException.NotValidException("Only asset issuer can increase asset quantity");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            AssetIncreaseAttachment attachment = (AssetIncreaseAttachment) transaction.getAttachment();
            return isDuplicate(ASSET_INCREASE, Long.toUnsignedString(attachment.getAssetId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

        @Override
        public final boolean isGlobal() {
            return true;
        }

    };

    private abstract static class OrderPlacement extends AssetExchangeTransactionType<OrderPlacementAttachment> {

        @Override
        public final void validateAttachment(ChildTransactionImpl transaction, OrderPlacementAttachment attachment) throws ValidationException {
            if (attachment.getPriceNQT() <= 0 || attachment.getPriceNQT() > Constants.MAX_BALANCE_NQT
                    || attachment.getAssetId() == 0) {
                throw new NxtException.NotValidException("Invalid asset order placement: " + attachment.getJSONObject());
            }
            long quantityQNT = attachment.getQuantityQNT();
            if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
                throw new NxtException.NotValidException("Invalid asset order placement quantity: " + attachment.getJSONObject());
            }
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId()) +
                        " does not exist yet");
            }
            if (quantityQNT > asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("Invalid asset order placement quantity: " + attachment.getJSONObject());
            }
            long amount = Convert.unitRateToAmount(attachment.getQuantityQNT(), asset.getDecimals(), attachment.getPriceNQT(),
                    transaction.getChain().getDecimals());
            if (amount == 0) {
                throw new NxtException.NotValidException("Asset order has no value: " + attachment.getJSONObject());
            }
        }

        @Override
        public final boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return true;
        }

        @Override
        public final boolean isGlobal() {
            return false;
        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            OrderPlacementAttachment attachment = (OrderPlacementAttachment) transaction.getAttachment();
            return attachment.getAssetId();
        }
    }

    public static final TransactionType ASK_ORDER_PLACEMENT = new OrderPlacement() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_PLACEMENT;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_ASK_ORDER_PLACEMENT;
        }

        @Override
        public String getName() {
            return "AskOrderPlacement";
        }

        @Override
        public AskOrderPlacementAttachment parseAttachment(ByteBuffer buffer) {
            return new AskOrderPlacementAttachment(buffer);
        }

        @Override
        public AskOrderPlacementAttachment parseAttachment(JSONObject attachmentData) {
            return new AskOrderPlacementAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AskOrderPlacementAttachment attachment = (AskOrderPlacementAttachment) transaction.getAttachment();
            long unconfirmedAssetBalance = senderAccount.getUnconfirmedAssetBalanceQNT(attachment.getAssetId());
            if (unconfirmedAssetBalance >= 0 && unconfirmedAssetBalance >= attachment.getQuantityQNT()) {
                senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getAssetId(), -attachment.getQuantityQNT());
                return true;
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AskOrderPlacementAttachment attachment = (AskOrderPlacementAttachment) transaction.getAttachment();
            transaction.getChain().getOrderHome().addAskOrder(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            AskOrderPlacementAttachment attachment = (AskOrderPlacementAttachment) transaction.getAttachment();
            senderAccount.addToUnconfirmedAssetBalanceQNT(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    attachment.getAssetId(), attachment.getQuantityQNT());
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getOrderHome().getAskOrder(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate ask order id " + transaction.getStringId());
            }
        }

    };

    public final static TransactionType BID_ORDER_PLACEMENT = new OrderPlacement() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_BID_ORDER_PLACEMENT;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_BID_ORDER_PLACEMENT;
        }

        @Override
        public String getName() {
            return "BidOrderPlacement";
        }

        @Override
        public BidOrderPlacementAttachment parseAttachment(ByteBuffer buffer) {
            return new BidOrderPlacementAttachment(buffer);
        }

        @Override
        public BidOrderPlacementAttachment parseAttachment(JSONObject attachmentData) {
            return new BidOrderPlacementAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            BidOrderPlacementAttachment attachment = (BidOrderPlacementAttachment) transaction.getAttachment();
            ChildChain chain = transaction.getChain();
            Asset asset = Asset.getAsset(attachment.getAssetId());
            long amount = Convert.unitRateToAmount(attachment.getQuantityQNT(), asset.getDecimals(),
                    attachment.getPriceNQT(), chain.getDecimals());
            BalanceHome.Balance balance = chain.getBalanceHome().getBalance(senderAccount.getId());
            if (balance.getUnconfirmedBalance() >= amount) {
                balance.addToUnconfirmedBalance(getLedgerEvent(), AccountLedger.newEventId(transaction), -amount);
                return true;
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            BidOrderPlacementAttachment attachment = (BidOrderPlacementAttachment) transaction.getAttachment();
            transaction.getChain().getOrderHome().addBidOrder(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            BidOrderPlacementAttachment attachment = (BidOrderPlacementAttachment) transaction.getAttachment();
            ChildChain chain = transaction.getChain();
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                return;
            }
            long amount = Convert.unitRateToAmount(attachment.getQuantityQNT(), asset.getDecimals(),
                    attachment.getPriceNQT(), chain.getDecimals());
            senderAccount.addToUnconfirmedBalance(transaction.getChain(), getLedgerEvent(),
                    AccountLedger.newEventId(transaction), amount);
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getOrderHome().getBidOrder(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate bid order id " + transaction.getStringId());
            }
        }

    };

    private abstract static class OrderCancellation<Att extends Attachment> extends AssetExchangeTransactionType<Att> {

        @Override
        public long getAssetId(ChildTransaction transaction) {
            OrderCancellationAttachment attachment = (OrderCancellationAttachment) transaction.getAttachment();
            OrderHome orderHome = transaction.getChain().getOrderHome();
            OrderHome.Order order = getOrder(orderHome, attachment.getOrderId());
            return order != null ? order.getAssetId() : 0;
        }

        protected abstract OrderHome.Order getOrder(OrderHome orderHome, long orderId);

        @Override
        public final boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public final void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public boolean isUnconfirmedDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            OrderCancellationAttachment attachment = (OrderCancellationAttachment) transaction.getAttachment();
            return TransactionType.isDuplicate(AssetExchangeTransactionType.ASK_ORDER_CANCELLATION, Long.toUnsignedString(attachment.getOrderId()), duplicates, true);
        }

        @Override
        public final boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return true;
        }

        @Override
        public final boolean isGlobal() {
            return false;
        }

    }

    public static final TransactionType ASK_ORDER_CANCELLATION = new OrderCancellation<AskOrderCancellationAttachment>() {
        @Override
        protected OrderHome.Order getOrder(OrderHome orderHome, long orderId) {
            return orderHome.getAskOrder(orderId);
        }

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_ASK_ORDER_CANCELLATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_ASK_ORDER_CANCELLATION;
        }

        @Override
        public String getName() {
            return "AskOrderCancellation";
        }

        @Override
        public AskOrderCancellationAttachment parseAttachment(ByteBuffer buffer) {
            return new AskOrderCancellationAttachment(buffer);
        }

        @Override
        public AskOrderCancellationAttachment parseAttachment(JSONObject attachmentData) {
            return new AskOrderCancellationAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AskOrderCancellationAttachment attachment = (AskOrderCancellationAttachment) transaction.getAttachment();
            OrderHome orderHome = transaction.getChain().getOrderHome();
            OrderHome.Order order = orderHome.getAskOrder(attachment.getOrderId());
            if (order == null) {
                return;
            }
            order.cancelOrder(AccountLedger.newEventId(transaction));
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, AskOrderCancellationAttachment attachment) throws ValidationException {
            OrderHome.Order ask = transaction.getChain().getOrderHome().getAskOrder(attachment.getOrderId());
            if (ask == null) {
                throw new NxtException.NotCurrentlyValidException("Invalid ask order: " + Long.toUnsignedString(attachment.getOrderId()));
            }
            if (ask.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Order " + Long.toUnsignedString(attachment.getOrderId()) + " was created by account "
                        + Convert.rsAccount(ask.getAccountId()));
            }
        }

    };

    public static final TransactionType BID_ORDER_CANCELLATION = new OrderCancellation<BidOrderCancellationAttachment>() {

        @Override
        protected OrderHome.Order getOrder(OrderHome orderHome, long orderId) {
            return orderHome.getBidOrder(orderId);
        }

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_BID_ORDER_CANCELLATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_BID_ORDER_CANCELLATION;
        }

        @Override
        public String getName() {
            return "BidOrderCancellation";
        }

        @Override
        public BidOrderCancellationAttachment parseAttachment(ByteBuffer buffer) {
            return new BidOrderCancellationAttachment(buffer);
        }

        @Override
        public BidOrderCancellationAttachment parseAttachment(JSONObject attachmentData) {
            return new BidOrderCancellationAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            BidOrderCancellationAttachment attachment = (BidOrderCancellationAttachment) transaction.getAttachment();
            OrderHome orderHome = transaction.getChain().getOrderHome();
            OrderHome.Order order = orderHome.getBidOrder(attachment.getOrderId());
            if (order == null) {
                return;
            }
            order.cancelOrder(AccountLedger.newEventId(transaction));
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, BidOrderCancellationAttachment attachment) throws ValidationException {
            OrderHome.Order bid = transaction.getChain().getOrderHome().getBidOrder(attachment.getOrderId());
            if (bid == null) {
                throw new NxtException.NotCurrentlyValidException("Invalid bid order: " + Long.toUnsignedString(attachment.getOrderId()));
            }
            if (bid.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Order " + Long.toUnsignedString(attachment.getOrderId()) + " was created by account "
                        + Convert.rsAccount(bid.getAccountId()));
            }
        }

    };

    public static final TransactionType DIVIDEND_PAYMENT = new AssetExchangeTransactionType<DividendPaymentAttachment>() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_DIVIDEND_PAYMENT;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_DIVIDEND_PAYMENT;
        }

        @Override
        public String getName() {
            return "DividendPayment";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return new Fee.ConstantFee(Constants.ONE_FXT / 10);
        }

        @Override
        public DividendPaymentAttachment parseAttachment(ByteBuffer buffer) {
            return new DividendPaymentAttachment(buffer);
        }

        @Override
        public DividendPaymentAttachment parseAttachment(JSONObject attachmentData) {
            return new DividendPaymentAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            DividendPaymentAttachment attachment = (DividendPaymentAttachment) transaction.getAttachment();
            long assetId = attachment.getAssetId();
            Asset asset = Asset.getAsset(assetId, attachment.getHeight());
            if (asset == null) {
                return true;
            }
            HoldingType holdingType = attachment.getHoldingType();
            long quantityQNT = asset.getQuantityQNT() - senderAccount.getAssetBalanceQNT(assetId, attachment.getHeight());
            long totalDividendPayment = Convert.unitRateToAmount(quantityQNT, asset.getDecimals(),
                    attachment.getAmountNQT(), holdingType.getDecimals(attachment.getHoldingId()));
            if (totalDividendPayment == 0) {
                return true;
            }
            if (holdingType.getUnconfirmedBalance(senderAccount, attachment.getHoldingId()) >= totalDividendPayment) {
                holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getHoldingId(), -totalDividendPayment);
                return true;
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            DividendPaymentAttachment attachment = (DividendPaymentAttachment) transaction.getAttachment();
            transaction.getChain().getAssetDividendHome().payDividends(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            DividendPaymentAttachment attachment = (DividendPaymentAttachment) transaction.getAttachment();
            long assetId = attachment.getAssetId();
            Asset asset = Asset.getAsset(assetId, attachment.getHeight());
            if (asset == null) {
                return;
            }
            HoldingType holdingType = attachment.getHoldingType();
            long quantityQNT = asset.getQuantityQNT() - senderAccount.getAssetBalanceQNT(assetId, attachment.getHeight());
            long totalDividendPayment = Convert.unitRateToAmount(quantityQNT, asset.getDecimals(),
                    attachment.getAmountNQT(), holdingType.getDecimals(attachment.getHoldingId()));
            if (totalDividendPayment > 0) {
                holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getHoldingId(), totalDividendPayment);
            }
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction, DividendPaymentAttachment attachment) throws ValidationException {
            if (attachment.getHeight() > Nxt.getBlockchain().getHeight()) {
                throw new NxtException.NotCurrentlyValidException("Invalid dividend payment height: " + attachment.getHeight()
                        + ", must not exceed current blockchain height " + Nxt.getBlockchain().getHeight());
            }
            if (attachment.getHeight() <= attachment.getFinishValidationHeight(transaction) - Constants.MAX_DIVIDEND_PAYMENT_ROLLBACK) {
                throw new NxtException.NotCurrentlyValidException("Invalid dividend payment height: " + attachment.getHeight()
                        + ", must be less than " + Constants.MAX_DIVIDEND_PAYMENT_ROLLBACK
                        + " blocks before " + attachment.getFinishValidationHeight(transaction));
            }
            Asset asset = Asset.getAsset(attachment.getAssetId(), attachment.getHeight());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId())
                        + " for dividend payment doesn't exist yet");
            }
            if (asset.getAccountId() != transaction.getSenderId() || attachment.getAmountNQT() <= 0) {
                throw new NxtException.NotValidException("Invalid dividend payment sender or amount " + attachment.getJSONObject());
            }
            AssetDividendHome.AssetDividend lastDividend = transaction.getChain().getAssetDividendHome().getLastDividend(attachment.getAssetId());
            if (lastDividend != null && lastDividend.getHeight() > Nxt.getBlockchain().getHeight() - Constants.MIN_DIVIDEND_PAYMENT_INTERVAL) {
                throw new NxtException.NotCurrentlyValidException("Last dividend payment for asset " + Long.toUnsignedString(attachment.getAssetId())
                        + " was less than " + Constants.MIN_DIVIDEND_PAYMENT_INTERVAL + " blocks ago at " + lastDividend.getHeight()
                        + ", current height is " + Nxt.getBlockchain().getHeight());
            }
            HoldingType holdingType = attachment.getHoldingType();
            switch (holdingType) {
                case COIN:
                    if (attachment.getHoldingId() != transaction.getChain().getId()) {
                        throw new NxtException.NotValidException("Holding id " + Long.toUnsignedString(attachment.getHoldingId())
                                + " does not match chain id " + transaction.getChain().getId());
                    }
                    break;
                case ASSET:
                    Asset dividendAsset = Asset.getAsset(attachment.getHoldingId());
                    if (dividendAsset == null) {
                        throw new NxtException.NotCurrentlyValidException("Unknown asset " + Long.toUnsignedString(attachment.getHoldingId()));
                    }
                    break;
                case CURRENCY:
                    Currency currency = Currency.getCurrency(attachment.getHoldingId());
                    CurrencyType.validate(currency, transaction);
                    if (!currency.isActive()) {
                        throw new NxtException.NotCurrentlyValidException("Currency is not active: " + currency.getCode());
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported holding type " + holdingType);
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            DividendPaymentAttachment attachment = (DividendPaymentAttachment) transaction.getAttachment();
            return isDuplicate(AssetExchangeTransactionType.DIVIDEND_PAYMENT, Long.toUnsignedString(attachment.getAssetId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

        @Override
        public final boolean isGlobal() {
            return false;
        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            DividendPaymentAttachment attachment = (DividendPaymentAttachment) transaction.getAttachment();
            if (attachment.getHoldingType() == HoldingType.ASSET) {
                return attachment.getHoldingId();
            }
            return 0;
        }

        @Override
        protected void checkLiquid(ChildTransaction transaction) throws NxtException.NotCurrentlyValidException {
            long dividendAssetId = getAssetId(transaction);
            if (dividendAssetId != 0) {
                AssetFreezeMonitor.checkLiquid(dividendAssetId);
            }
            long thisAssetId = ((DividendPaymentAttachment)transaction.getAttachment()).getAssetId();
            AssetFreezeMonitor.checkLiquid(thisAssetId);
        }

    };

    public static final TransactionType SET_PHASING_CONTROL = new AssetExchangeTransactionType<SetPhasingAssetControlAttachment>() {

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return new Fee.ConstantFee(Constants.ONE_FXT * 10);
        }

        @Override
        public byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_SET_PHASING_CONTROL;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_SET_PHASING_CONTROL;
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new SetPhasingAssetControlAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new SetPhasingAssetControlAttachment(attachmentData);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

        @Override
        public final boolean isGlobal() {
            return true;
        }

        @Override
        public String getName() {
            return "SetPhasingAssetControl";
        }

        @Override
        protected void validateAttachment(ChildTransactionImpl transaction, SetPhasingAssetControlAttachment attachment) throws ValidationException {
            VoteWeighting.VotingModel votingModel = attachment.getPhasingParams().getVoteWeighting().getVotingModel();
            attachment.getPhasingParams().validateRestrictableParams();
            Asset asset = Asset.getAsset(attachment.getAssetId());
            if (asset == null) {
                throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(attachment.getAssetId()) +
                        " does not exist yet");
            }
            if (votingModel == NONE) {
                if (!asset.hasPhasingControl()) {
                    throw new NxtException.NotCurrentlyValidException("Phasing asset control is not currently enabled");
                }
            } else if (votingModel == TRANSACTION || votingModel == HASH) {
                throw new NxtException.NotValidException("Invalid voting model " + votingModel + " for asset control");
            }
            if (asset.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Asset control can only be set by the asset issuer");
            }
            if (!asset.hasPhasingControl()) {
                Account.AccountAsset accountAsset = Account.getAccountAsset(transaction.getSenderId(), attachment.getAssetId());
                long totalAssetQuantity = asset.getQuantityQNT();
                if (accountAsset == null || accountAsset.getQuantityQNT() < totalAssetQuantity || accountAsset.getUnconfirmedQuantityQNT() < totalAssetQuantity) {
                    throw new NxtException.NotCurrentlyValidException("Adding asset control requires the asset issuer to own all asset units");
                }
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            SetPhasingAssetControlAttachment attachment = (SetPhasingAssetControlAttachment) transaction.getAttachment();
            return TransactionType.isDuplicate(SET_PHASING_CONTROL, Long.toUnsignedString(attachment.getAssetId()), duplicates, true);
        }

        @Override
        protected boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        protected void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            SetPhasingAssetControlAttachment attachment = (SetPhasingAssetControlAttachment) transaction.getAttachment();
            AssetControl.PhasingOnly.set(attachment);
        }

        @Override
        protected void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {

        }

        @Override
        public long getAssetId(ChildTransaction transaction) {
            SetPhasingAssetControlAttachment attachment = (SetPhasingAssetControlAttachment) transaction.getAttachment();
            return attachment.getAssetId();
        }
    };

    private static abstract class AssetPropertyTransactionType<Att extends Attachment> extends AssetExchangeTransactionType<Att> {

        final void checkRecipient(Asset asset, long recipientId) throws NotValidException {
            if (recipientId != asset.getAccountId()) {
                throw new NxtException.NotValidException(String.format(
                        "Property transaction recipient must be asset issuer(%s), but was %s",
                        Long.toUnsignedString(asset.getAccountId()),
                        Long.toUnsignedString(recipientId)));
            }
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

        @Override
        protected final void checkLiquid(ChildTransaction transaction) {
        }

    }

    public static final TransactionType ASSET_PROPERTY_SET = new AssetPropertyTransactionType<AssetPropertyAttachment>() {

        @Override
        public long getAssetId(ChildTransaction transaction) {
            return ((AssetPropertyAttachment) transaction.getAttachment()).getAssetId();
        }

        private final Fee ASSET_PROPERTY_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 10, Constants.ONE_FXT / 10, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                AssetPropertyAttachment attachment = (AssetPropertyAttachment) transaction.getAttachment();
                return attachment.getValue().length();
            }
        };

        @Override
        public byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_PROPERTY_SET;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_PROPERTY_SET;
        }

        @Override
        public String getName() {
            return "AssetProperty";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return ASSET_PROPERTY_FEE;
        }

        @Override
        public AssetPropertyAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AssetPropertyAttachment(buffer);
        }

        @Override
        public AssetPropertyAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetPropertyAttachment(attachmentData);
        }

        @Override
        protected void validateAttachment(ChildTransactionImpl transaction, AssetPropertyAttachment attachment) throws ValidationException {
            if (Nxt.getBlockchain().getHeight() < Constants.ASSET_PROPERTIES_BLOCK) {
                throw new NxtException.NotYetEnabledException("Setting asset properties not yet enabled");
            }
            if (!AssetPropertyAttachment.PROPERTY_NAME_RW.validate(attachment.getProperty())
                    || attachment.getProperty().length() == 0
                    || !AssetPropertyAttachment.PROPERTY_VALUE_RW.validate(attachment.getValue())) {
                throw new NxtException.NotValidException("Invalid asset property: " + attachment.getJSONObject());
            }

            checkRecipient(Asset.getAsset(attachment.getAssetId()), transaction.getRecipientId());

            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Asset property transaction cannot be used to send money");
            }

        }

        @Override
        protected void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetPropertyAttachment attachment = (AssetPropertyAttachment) transaction.getAttachment();
            Asset.getAsset(attachment.getAssetId())
                    .setProperty(transaction.getId(), senderAccount, attachment.getProperty(), attachment.getValue());
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (Asset.getProperty(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate asset property id " + transaction.getStringId());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }
    };

    public static final TransactionType ASSET_PROPERTY_DELETE = new AssetPropertyTransactionType<AssetPropertyDeleteAttachment>() {

        @Override
        public long getAssetId(ChildTransaction transaction) {
            AssetPropertyDeleteAttachment attachment = (AssetPropertyDeleteAttachment) transaction.getAttachment();
            Asset.AssetProperty property = Asset.getProperty(attachment.getPropertyId());
            return property != null ? property.getAssetId() : 0;
        }

        @Override
        public byte getSubtype() {
            return SUBTYPE_ASSET_EXCHANGE_PROPERTY_DELETE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ASSET_PROPERTY_DELETE;
        }

        @Override
        public String getName() {
            return "AssetPropertyDelete";
        }

        @Override
        public AssetPropertyDeleteAttachment parseAttachment(ByteBuffer buffer) {
            return new AssetPropertyDeleteAttachment(buffer);
        }

        @Override
        public AssetPropertyDeleteAttachment parseAttachment(JSONObject attachmentData) {
            return new AssetPropertyDeleteAttachment(attachmentData);
        }

        @Override
        protected void validateAttachment(ChildTransactionImpl transaction, AssetPropertyDeleteAttachment attachment) throws ValidationException {
            if (Nxt.getBlockchain().getHeight() < Constants.ASSET_PROPERTIES_BLOCK) {
                throw new NxtException.NotYetEnabledException("Deleting asset properties not yet enabled");
            }
            Asset.AssetProperty property = Asset.getProperty(attachment.getPropertyId());
            if (property == null) {
                throw new NxtException.NotCurrentlyValidException("No such property " + Long.toUnsignedString(attachment.getPropertyId()));
            }
            Asset asset = Asset.getAsset(property.getAssetId());
            if (asset.getAccountId() != transaction.getSenderId() && property.getSetterId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Account " + Long.toUnsignedString(transaction.getSenderId())
                        + " cannot delete property " + Long.toUnsignedString(attachment.getPropertyId()));
            }

            checkRecipient(asset, transaction.getRecipientId());

            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Asset property transaction cannot be used to send money");
            }
        }

        @Override
        protected void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AssetPropertyDeleteAttachment attachment = (AssetPropertyDeleteAttachment) transaction.getAttachment();
            Asset.deleteProperty(attachment.getPropertyId());
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }
    };
}
