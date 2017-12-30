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

package nxt.dgs;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Appendix;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Search;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;

public abstract class DigitalGoodsTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_DIGITAL_GOODS_LISTING = 0;
    private static final byte SUBTYPE_DIGITAL_GOODS_DELISTING = 1;
    private static final byte SUBTYPE_DIGITAL_GOODS_PRICE_CHANGE = 2;
    private static final byte SUBTYPE_DIGITAL_GOODS_QUANTITY_CHANGE = 3;
    private static final byte SUBTYPE_DIGITAL_GOODS_PURCHASE = 4;
    private static final byte SUBTYPE_DIGITAL_GOODS_DELIVERY = 5;
    private static final byte SUBTYPE_DIGITAL_GOODS_FEEDBACK = 6;
    private static final byte SUBTYPE_DIGITAL_GOODS_REFUND = 7;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_DIGITAL_GOODS_LISTING:
                return DigitalGoodsTransactionType.LISTING;
            case SUBTYPE_DIGITAL_GOODS_DELISTING:
                return DigitalGoodsTransactionType.DELISTING;
            case SUBTYPE_DIGITAL_GOODS_PRICE_CHANGE:
                return DigitalGoodsTransactionType.PRICE_CHANGE;
            case SUBTYPE_DIGITAL_GOODS_QUANTITY_CHANGE:
                return DigitalGoodsTransactionType.QUANTITY_CHANGE;
            case SUBTYPE_DIGITAL_GOODS_PURCHASE:
                return DigitalGoodsTransactionType.PURCHASE;
            case SUBTYPE_DIGITAL_GOODS_DELIVERY:
                return DigitalGoodsTransactionType.DELIVERY;
            case SUBTYPE_DIGITAL_GOODS_FEEDBACK:
                return DigitalGoodsTransactionType.FEEDBACK;
            case SUBTYPE_DIGITAL_GOODS_REFUND:
                return DigitalGoodsTransactionType.REFUND;
            default:
                return null;
        }
    }

    private DigitalGoodsTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_DIGITAL_GOODS;
    }

    @Override
    public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
    }

    @Override
    public final void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
        if (transaction.getAmount() != 0) {
            throw new NxtException.NotValidException("Invalid digital goods transaction");
        }
        doValidateAttachment(transaction);
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    abstract void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException;


    public static final TransactionType LISTING = new DigitalGoodsTransactionType() {

        private final Fee DGS_LISTING_FEE = new Fee.SizeBasedFee(2 * Constants.ONE_FXT / 10, 2 * Constants.ONE_FXT / 10, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                ListingAttachment attachment = (ListingAttachment) transaction.getAttachment();
                return attachment.getName().length() + attachment.getDescription().length();
            }
        };

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_LISTING;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_LISTING;
        }

        @Override
        public String getName() {
            return "DigitalGoodsListing";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return DGS_LISTING_FEE;
        }

        @Override
        public ListingAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ListingAttachment(buffer);
        }

        @Override
        public ListingAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new ListingAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ListingAttachment attachment = (ListingAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().listGoods(transaction, attachment);
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ListingAttachment attachment = (ListingAttachment) transaction.getAttachment();
            if (attachment.getName().length() == 0
                    || attachment.getName().length() > Constants.MAX_DGS_LISTING_NAME_LENGTH
                    || attachment.getDescription().length() > Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH
                    || attachment.getTags().length() > Constants.MAX_DGS_LISTING_TAGS_LENGTH
                    || attachment.getQuantity() < 0 || attachment.getQuantity() > Constants.MAX_DGS_LISTING_QUANTITY
                    || attachment.getPriceNQT() <= 0 || attachment.getPriceNQT() > Constants.MAX_BALANCE_NQT) {
                throw new NxtException.NotValidException("Invalid digital goods listing: " + attachment.getJSONObject());
            }
            PrunablePlainMessageAppendix prunablePlainMessage = transaction.getPrunablePlainMessage();
            if (prunablePlainMessage != null) {
                byte[] image = prunablePlainMessage.getMessage();
                if (image != null) {
                    String mediaType = Search.detectMimeType(image);
                    if (mediaType == null || !mediaType.startsWith("image/")) {
                        throw new NxtException.NotValidException("Only image attachments allowed for DGS listing, media type is " + mediaType);
                    }
                }
            }
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getDigitalGoodsHome().getGoods(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate goods id " + transaction.getStringId());
            }
        }

        @Override
        public boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return isDuplicate(DigitalGoodsTransactionType.LISTING, getName(), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType DELISTING = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_DELISTING;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_DELISTING;
        }

        @Override
        public String getName() {
            return "DigitalGoodsDelisting";
        }

        @Override
        public DelistingAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new DelistingAttachment(buffer);
        }

        @Override
        public DelistingAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new DelistingAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            DelistingAttachment attachment = (DelistingAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().delistGoods(attachment.getGoodsId());
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            DelistingAttachment attachment = (DelistingAttachment) transaction.getAttachment();
            DigitalGoodsHome.Goods goods = transaction.getChain().getDigitalGoodsHome().getGoods(attachment.getGoodsId());
            if (goods != null && transaction.getSenderId() != goods.getSellerId()) {
                throw new NxtException.NotValidException("Invalid digital goods delisting - seller is different: " + attachment.getJSONObject());
            }
            if (goods == null || goods.isDelisted()) {
                throw new NxtException.NotCurrentlyValidException("Goods " + Long.toUnsignedString(attachment.getGoodsId()) +
                        "not yet listed or already delisted");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            DelistingAttachment attachment = (DelistingAttachment) transaction.getAttachment();
            return isDuplicate(DigitalGoodsTransactionType.DELISTING, Long.toUnsignedString(attachment.getGoodsId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType PRICE_CHANGE = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_PRICE_CHANGE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_PRICE_CHANGE;
        }

        @Override
        public String getName() {
            return "DigitalGoodsPriceChange";
        }

        @Override
        public PriceChangeAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new PriceChangeAttachment(buffer);
        }

        @Override
        public PriceChangeAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new PriceChangeAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            PriceChangeAttachment attachment = (PriceChangeAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().changePrice(attachment.getGoodsId(), attachment.getPriceNQT());
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            PriceChangeAttachment attachment = (PriceChangeAttachment) transaction.getAttachment();
            DigitalGoodsHome.Goods goods = transaction.getChain().getDigitalGoodsHome().getGoods(attachment.getGoodsId());
            if (attachment.getPriceNQT() <= 0 || attachment.getPriceNQT() > Constants.MAX_BALANCE_NQT
                    || (goods != null && transaction.getSenderId() != goods.getSellerId())) {
                throw new NxtException.NotValidException("Invalid digital goods price change: " + attachment.getJSONObject());
            }
            if (goods == null || goods.isDelisted()) {
                throw new NxtException.NotCurrentlyValidException("Goods " + Long.toUnsignedString(attachment.getGoodsId()) +
                        "not yet listed or already delisted");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            PriceChangeAttachment attachment = (PriceChangeAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on DigitalGoods.DELISTING
            return isDuplicate(DigitalGoodsTransactionType.DELISTING, Long.toUnsignedString(attachment.getGoodsId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType QUANTITY_CHANGE = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_QUANTITY_CHANGE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_QUANTITY_CHANGE;
        }

        @Override
        public String getName() {
            return "DigitalGoodsQuantityChange";
        }

        @Override
        public QuantityChangeAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new QuantityChangeAttachment(buffer);
        }

        @Override
        public QuantityChangeAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new QuantityChangeAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            QuantityChangeAttachment attachment = (QuantityChangeAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().changeQuantity(attachment.getGoodsId(), attachment.getDeltaQuantity());
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            QuantityChangeAttachment attachment = (QuantityChangeAttachment) transaction.getAttachment();
            DigitalGoodsHome.Goods goods = transaction.getChain().getDigitalGoodsHome().getGoods(attachment.getGoodsId());
            if (attachment.getDeltaQuantity() < -Constants.MAX_DGS_LISTING_QUANTITY
                    || attachment.getDeltaQuantity() > Constants.MAX_DGS_LISTING_QUANTITY
                    || (goods != null && transaction.getSenderId() != goods.getSellerId())) {
                throw new NxtException.NotValidException("Invalid digital goods quantity change: " + attachment.getJSONObject());
            }
            if (goods == null || goods.isDelisted()) {
                throw new NxtException.NotCurrentlyValidException("Goods " + Long.toUnsignedString(attachment.getGoodsId()) +
                        "not yet listed or already delisted");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            QuantityChangeAttachment attachment = (QuantityChangeAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on DigitalGoods.DELISTING
            return isDuplicate(DigitalGoodsTransactionType.DELISTING, Long.toUnsignedString(attachment.getGoodsId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType PURCHASE = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_PURCHASE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_PURCHASE;
        }

        @Override
        public String getName() {
            return "DigitalGoodsPurchase";
        }

        @Override
        public PurchaseAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new PurchaseAttachment(buffer);
        }

        @Override
        public PurchaseAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new PurchaseAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            PurchaseAttachment attachment = (PurchaseAttachment) transaction.getAttachment();
            if (transaction.getChain().getBalanceHome().getBalance(senderAccount.getId()).getUnconfirmedBalance() >= Math.multiplyExact((long) attachment.getQuantity(), attachment.getPriceNQT())) {
                senderAccount.addToUnconfirmedBalance(transaction.getChain(), getLedgerEvent(), AccountLedger.newEventId(transaction),
                        -Math.multiplyExact((long) attachment.getQuantity(), attachment.getPriceNQT()));
                return true;
            }
            return false;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            PurchaseAttachment attachment = (PurchaseAttachment) transaction.getAttachment();
            senderAccount.addToUnconfirmedBalance(transaction.getChain(), getLedgerEvent(), AccountLedger.newEventId(transaction),
                    Math.multiplyExact((long) attachment.getQuantity(), attachment.getPriceNQT()));
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            PurchaseAttachment attachment = (PurchaseAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().purchase(transaction, attachment);
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            PurchaseAttachment attachment = (PurchaseAttachment) transaction.getAttachment();
            DigitalGoodsHome.Goods goods = transaction.getChain().getDigitalGoodsHome().getGoods(attachment.getGoodsId());
            if (attachment.getQuantity() <= 0 || attachment.getQuantity() > Constants.MAX_DGS_LISTING_QUANTITY
                    || attachment.getPriceNQT() <= 0 || attachment.getPriceNQT() > Constants.MAX_BALANCE_NQT
                    || (goods != null && goods.getSellerId() != transaction.getRecipientId())) {
                throw new NxtException.NotValidException("Invalid digital goods purchase: " + attachment.getJSONObject());
            }
            if (transaction.getEncryptedMessage() != null && ! transaction.getEncryptedMessage().isText()) {
                throw new NxtException.NotValidException("Only text encrypted messages allowed");
            }
            if (goods == null || goods.isDelisted()) {
                throw new NxtException.NotCurrentlyValidException("Goods " + Long.toUnsignedString(attachment.getGoodsId()) +
                        "not yet listed or already delisted");
            }
            if (attachment.getQuantity() > goods.getQuantity() || attachment.getPriceNQT() != goods.getPriceNQT()) {
                throw new NxtException.NotCurrentlyValidException("Goods price or quantity changed: " + attachment.getJSONObject());
            }
            if (attachment.getDeliveryDeadlineTimestamp() <= Nxt.getBlockchain().getLastBlockTimestamp()) {
                throw new NxtException.NotCurrentlyValidException("Delivery deadline has already expired: " + attachment.getDeliveryDeadlineTimestamp());
            }
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getDigitalGoodsHome().getPurchase(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate purchase id " + transaction.getStringId());
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            PurchaseAttachment attachment = (PurchaseAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on DigitalGoods.DELISTING
            return isDuplicate(DigitalGoodsTransactionType.DELISTING, Long.toUnsignedString(attachment.getGoodsId()), duplicates, false);
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType DELIVERY = new DigitalGoodsTransactionType() {

        private final Fee DGS_DELIVERY_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 10, 2 * Constants.ONE_FXT / 10, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                DeliveryAttachment attachment = (DeliveryAttachment) transaction.getAttachment();
                return attachment.getGoodsDataLength() - 16;
            }
        };

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_DELIVERY;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_DELIVERY;
        }

        @Override
        public String getName() {
            return "DigitalGoodsDelivery";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return DGS_DELIVERY_FEE;
        }

        @Override
        public DeliveryAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new DeliveryAttachment(buffer);
        }

        @Override
        public DeliveryAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            if (attachmentData.get("goodsData") == null) {
                return new UnencryptedDeliveryAttachment(attachmentData);
            }
            return new DeliveryAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            DeliveryAttachment attachment = (DeliveryAttachment)transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().deliver(transaction, attachment);
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            DeliveryAttachment attachment = (DeliveryAttachment) transaction.getAttachment();
            DigitalGoodsHome.Purchase purchase = transaction.getChain().getDigitalGoodsHome().getPendingPurchase(attachment.getPurchaseId());
            if (attachment.getGoodsDataLength() > Constants.MAX_DGS_GOODS_LENGTH) {
                throw new NxtException.NotValidException("Invalid digital goods delivery data length: " + attachment.getGoodsDataLength());
            }
            if (attachment.getGoods() != null) {
                if (attachment.getGoods().getData().length == 0 || attachment.getGoods().getNonce().length != 32) {
                    throw new NxtException.NotValidException("Invalid digital goods delivery: " + attachment.getJSONObject());
                }
            }
            if (attachment.getDiscountNQT() < 0 || attachment.getDiscountNQT() > Constants.MAX_BALANCE_NQT
                    || (purchase != null &&
                    (purchase.getBuyerId() != transaction.getRecipientId()
                            || transaction.getSenderId() != purchase.getSellerId()
                            || attachment.getDiscountNQT() > Math.multiplyExact(purchase.getPriceNQT(), (long) purchase.getQuantity())))) {
                throw new NxtException.NotValidException("Invalid digital goods delivery: " + attachment.getJSONObject());
            }
            if (purchase == null || purchase.getEncryptedGoods() != null) {
                throw new NxtException.NotCurrentlyValidException("Purchase does not exist yet, or already delivered: "
                        + attachment.getJSONObject());
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            DeliveryAttachment attachment = (DeliveryAttachment) transaction.getAttachment();
            return isDuplicate(DigitalGoodsTransactionType.DELIVERY, Long.toUnsignedString(attachment.getPurchaseId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType FEEDBACK = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_FEEDBACK;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_FEEDBACK;
        }

        @Override
        public String getName() {
            return "DigitalGoodsFeedback";
        }

        @Override
        public FeedbackAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new FeedbackAttachment(buffer);
        }

        @Override
        public FeedbackAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new FeedbackAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            FeedbackAttachment attachment = (FeedbackAttachment)transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().feedback(attachment.getPurchaseId(),
                    transaction.getEncryptedMessage(), transaction.getMessage());
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            FeedbackAttachment attachment = (FeedbackAttachment) transaction.getAttachment();
            DigitalGoodsHome.Purchase purchase = transaction.getChain().getDigitalGoodsHome().getPurchase(attachment.getPurchaseId());
            if (purchase != null &&
                    (purchase.getSellerId() != transaction.getRecipientId()
                            || transaction.getSenderId() != purchase.getBuyerId())) {
                throw new NxtException.NotValidException("Invalid digital goods feedback: " + attachment.getJSONObject());
            }
            if (transaction.getEncryptedMessage() == null && transaction.getMessage() == null) {
                throw new NxtException.NotValidException("Missing feedback message");
            }
            if (transaction.getEncryptedMessage() != null && ! transaction.getEncryptedMessage().isText()) {
                throw new NxtException.NotValidException("Only text encrypted messages allowed");
            }
            if (transaction.getMessage() != null && ! transaction.getMessage().isText()) {
                throw new NxtException.NotValidException("Only text public messages allowed");
            }
            if (purchase == null || purchase.getEncryptedGoods() == null) {
                throw new NxtException.NotCurrentlyValidException("Purchase does not exist yet or not yet delivered");
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType REFUND = new DigitalGoodsTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_DIGITAL_GOODS_REFUND;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.DIGITAL_GOODS_REFUND;
        }

        @Override
        public String getName() {
            return "DigitalGoodsRefund";
        }

        @Override
        public RefundAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new RefundAttachment(buffer);
        }

        @Override
        public RefundAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new RefundAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            RefundAttachment attachment = (RefundAttachment) transaction.getAttachment();
            if (transaction.getChain().getBalanceHome().getBalance(senderAccount.getId()).getUnconfirmedBalance() >= attachment.getRefundNQT()) {
                senderAccount.addToUnconfirmedBalance(transaction.getChain(), getLedgerEvent(),
                        AccountLedger.newEventId(transaction), -attachment.getRefundNQT());
                return true;
            }
            return false;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            RefundAttachment attachment = (RefundAttachment) transaction.getAttachment();
            senderAccount.addToUnconfirmedBalance(transaction.getChain(), getLedgerEvent(),
                    AccountLedger.newEventId(transaction), attachment.getRefundNQT());
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            RefundAttachment attachment = (RefundAttachment) transaction.getAttachment();
            transaction.getChain().getDigitalGoodsHome().refund(getLedgerEvent(), AccountLedger.newEventId(transaction), transaction.getSenderId(),
                    attachment.getPurchaseId(), attachment.getRefundNQT(), transaction.getEncryptedMessage());
        }

        @Override
        void doValidateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            RefundAttachment attachment = (RefundAttachment) transaction.getAttachment();
            DigitalGoodsHome.Purchase purchase = transaction.getChain().getDigitalGoodsHome().getPurchase(attachment.getPurchaseId());
            if (attachment.getRefundNQT() < 0 || attachment.getRefundNQT() > Constants.MAX_BALANCE_NQT
                    || (purchase != null &&
                    (purchase.getBuyerId() != transaction.getRecipientId()
                            || transaction.getSenderId() != purchase.getSellerId()))) {
                throw new NxtException.NotValidException("Invalid digital goods refund: " + attachment.getJSONObject());
            }
            if (transaction.getEncryptedMessage() != null && ! transaction.getEncryptedMessage().isText()) {
                throw new NxtException.NotValidException("Only text encrypted messages allowed");
            }
            if (purchase == null || purchase.getEncryptedGoods() == null || purchase.getRefundNQT() != 0) {
                throw new NxtException.NotCurrentlyValidException("Purchase does not exist or is not delivered or is already refunded");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            RefundAttachment attachment = (RefundAttachment) transaction.getAttachment();
            return isDuplicate(DigitalGoodsTransactionType.REFUND, Long.toUnsignedString(attachment.getPurchaseId()), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

}
