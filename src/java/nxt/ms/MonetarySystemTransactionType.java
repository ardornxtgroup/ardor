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

package nxt.ms;

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountLedger.LedgerEvent;
import nxt.account.BalanceHome;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

public abstract class MonetarySystemTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE = 0;
    private static final byte SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE = 1;
    private static final byte SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM = 2;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER = 3;
    private static final byte SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER = 4;
    private static final byte SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY = 5;
    private static final byte SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL = 6;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING = 7;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION = 8;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE:
                return MonetarySystemTransactionType.CURRENCY_ISSUANCE;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE:
                return MonetarySystemTransactionType.RESERVE_INCREASE;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM:
                return MonetarySystemTransactionType.RESERVE_CLAIM;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER:
                return MonetarySystemTransactionType.CURRENCY_TRANSFER;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER:
                return MonetarySystemTransactionType.PUBLISH_EXCHANGE_OFFER;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY:
                return MonetarySystemTransactionType.EXCHANGE_BUY;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL:
                return MonetarySystemTransactionType.EXCHANGE_SELL;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING:
                return MonetarySystemTransactionType.CURRENCY_MINTING;
            case MonetarySystemTransactionType.SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION:
                return MonetarySystemTransactionType.CURRENCY_DELETION;
            default:
                return null;
        }
    }

    private MonetarySystemTransactionType() {}

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_MONETARY_SYSTEM;
    }

    @Override
    public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        MonetarySystemAttachment attachment = (MonetarySystemAttachment) transaction.getAttachment();
        Currency currency = Currency.getCurrency(attachment.getCurrencyId());
        String nameLower = currency.getName().toLowerCase(Locale.ROOT);
        String codeLower = currency.getCode().toLowerCase(Locale.ROOT);
        boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, false);
        if (! nameLower.equals(codeLower)) {
            isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, false);
        }
        return isDuplicate;
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    public static final TransactionType CURRENCY_ISSUANCE = new MonetarySystemTransactionType() {

        private final Fee FIVE_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(4 * Constants.ONE_FXT);
        private final Fee FOUR_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(100 * Constants.ONE_FXT);
        private final Fee THREE_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(2500 * Constants.ONE_FXT);

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_ISSUANCE;
        }

        @Override
        public String getName() {
            return "CurrencyIssuance";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            ChildChain childChain = (ChildChain)transaction.getChain();
            CurrencyIssuanceAttachment attachment = (CurrencyIssuanceAttachment) transaction.getAttachment();
            int minLength = Math.min(attachment.getCode().length(), attachment.getName().length());
            Currency oldCurrency;
            int oldMinLength = Integer.MAX_VALUE;
            if ((oldCurrency = Currency.getCurrencyByCode(childChain, attachment.getCode())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByCode(childChain, attachment.getName())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByName(childChain, attachment.getName())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByName(childChain, attachment.getCode())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if (minLength >= oldMinLength) {
                return FIVE_LETTER_CURRENCY_ISSUANCE_FEE;
            }
            switch (minLength) {
                case 3:
                    return THREE_LETTER_CURRENCY_ISSUANCE_FEE;
                case 4:
                    return FOUR_LETTER_CURRENCY_ISSUANCE_FEE;
                case 5:
                    return FIVE_LETTER_CURRENCY_ISSUANCE_FEE;
                default:
                    // never, invalid code length will be checked and caught later
                    return THREE_LETTER_CURRENCY_ISSUANCE_FEE;
            }
        }

        @Override
        public CurrencyIssuanceAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new CurrencyIssuanceAttachment(buffer);
        }

        @Override
        public CurrencyIssuanceAttachment parseAttachment(JSONObject attachmentData) {
            return new CurrencyIssuanceAttachment(attachmentData);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            CurrencyIssuanceAttachment attachment = (CurrencyIssuanceAttachment) transaction.getAttachment();
            String nameLower = attachment.getName().toLowerCase(Locale.ROOT);
            String codeLower = attachment.getCode().toLowerCase(Locale.ROOT);
            boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, true);
            if (! nameLower.equals(codeLower)) {
                isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, true);
            }
            return isDuplicate;
        }

        @Override
        public boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return isDuplicate(CURRENCY_ISSUANCE, getName(), duplicates, true);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            CurrencyIssuanceAttachment attachment = (CurrencyIssuanceAttachment) transaction.getAttachment();
            if (attachment.getMaxSupplyQNT() > Constants.MAX_CURRENCY_TOTAL_SUPPLY
                    || attachment.getMaxSupplyQNT() <= 0
                    || attachment.getInitialSupplyQNT() < 0
                    || attachment.getInitialSupplyQNT() > attachment.getMaxSupplyQNT()
                    || attachment.getReserveSupplyQNT() < 0
                    || attachment.getReserveSupplyQNT() > attachment.getMaxSupplyQNT()
                    || attachment.getIssuanceHeight() < 0
                    || attachment.getMinReservePerUnitNQT() < 0
                    || attachment.getDecimals() < 0 || attachment.getDecimals() > 8
                    || attachment.getRuleset() != 0) {
                throw new NxtException.NotValidException("Invalid currency issuance: " + attachment.getJSONObject());
            }
            int t = 1;
            for (int i = 0; i < 32; i++) {
                if ((t & attachment.getType()) != 0 && CurrencyType.get(t) == null) {
                    throw new NxtException.NotValidException("Invalid currency type: " + attachment.getType());
                }
                t <<= 1;
            }
            CurrencyType.validate(attachment.getType(), transaction);
            CurrencyType.validateCurrencyNaming(transaction, attachment);
        }


        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            CurrencyIssuanceAttachment attachment = (CurrencyIssuanceAttachment) transaction.getAttachment();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            Currency.addCurrency(getLedgerEvent(), eventId, transaction, senderAccount, attachment);
            senderAccount.addToCurrencyAndUnconfirmedCurrencyUnits(getLedgerEvent(), eventId, transaction.getId(),
                    attachment.getInitialSupplyQNT());
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (Currency.getCurrency(transaction.getId(), true) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate currency id " + transaction.getStringId());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType RESERVE_INCREASE = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_RESERVE_INCREASE;
        }

        @Override
        public String getName() {
            return "ReserveIncrease";
        }

        @Override
        public ReserveIncreaseAttachment parseAttachment(ByteBuffer buffer) {
            return new ReserveIncreaseAttachment(buffer);
        }

        @Override
        public ReserveIncreaseAttachment parseAttachment(JSONObject attachmentData) {
            return new ReserveIncreaseAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ReserveIncreaseAttachment attachment = (ReserveIncreaseAttachment) transaction.getAttachment();
            if (attachment.getAmountPerUnitNQT() <= 0) {
                throw new NxtException.NotValidException("Reserve increase coin amount must be positive: " + attachment.getAmountPerUnitNQT());
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ReserveIncreaseAttachment attachment = (ReserveIncreaseAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            ChildChain childChain = transaction.getChain();
            long amount = Convert.unitRateToAmount(currency.getReserveSupplyQNT(), currency.getDecimals(),
                                    attachment.getAmountPerUnitNQT(), childChain.getDecimals());
            BalanceHome.Balance balance = childChain.getBalanceHome().getBalance(senderAccount.getId());
            if (balance.getUnconfirmedBalance() < amount) {
                return false;
            }
            balance.addToUnconfirmedBalance(getLedgerEvent(), AccountLedger.newEventId(transaction), -amount);
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ReserveIncreaseAttachment attachment = (ReserveIncreaseAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId(), true);
            if (currency == null) {
                return;
            }
            ChildChain childChain = transaction.getChain();
            long amount = Convert.unitRateToAmount(currency.getReserveSupplyQNT(), currency.getDecimals(),
                                    attachment.getAmountPerUnitNQT(), childChain.getDecimals());
            senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(),
                    AccountLedger.newEventId(transaction), amount);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ReserveIncreaseAttachment attachment = (ReserveIncreaseAttachment) transaction.getAttachment();
            transaction.getChain().getCurrencyFounderHome().increaseReserve(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    senderAccount, attachment.getCurrencyId(), attachment.getAmountPerUnitNQT());
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType RESERVE_CLAIM = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_RESERVE_CLAIM;
        }

        @Override
        public String getName() {
            return "ReserveClaim";
        }

        @Override
        public ReserveClaimAttachment parseAttachment(ByteBuffer buffer) {
            return new ReserveClaimAttachment(buffer);
        }

        @Override
        public ReserveClaimAttachment parseAttachment(JSONObject attachmentData) {
            return new ReserveClaimAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ReserveClaimAttachment attachment = (ReserveClaimAttachment) transaction.getAttachment();
            if (attachment.getUnitsQNT() <= 0) {
                throw new NxtException.NotValidException("Reserve claim number of units must be positive: " + attachment.getUnitsQNT());
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ReserveClaimAttachment attachment = (ReserveClaimAttachment) transaction.getAttachment();
            if (senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId()) >= attachment.getUnitsQNT()) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getCurrencyId(), -attachment.getUnitsQNT());
                return true;
            }
            return false;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ReserveClaimAttachment attachment = (ReserveClaimAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getCurrencyId(), attachment.getUnitsQNT());
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ReserveClaimAttachment attachment = (ReserveClaimAttachment) transaction.getAttachment();
            Currency.claimReserve(transaction.getChain(), getLedgerEvent(), AccountLedger.newEventId(transaction),
                    senderAccount, attachment.getCurrencyId(), attachment.getUnitsQNT());
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType CURRENCY_TRANSFER = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_TRANSFER;
        }

        @Override
        public String getName() {
            return "CurrencyTransfer";
        }

        @Override
        public CurrencyTransferAttachment parseAttachment(ByteBuffer buffer) {
            return new CurrencyTransferAttachment(buffer);
        }

        @Override
        public CurrencyTransferAttachment parseAttachment(JSONObject attachmentData) {
            return new CurrencyTransferAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            CurrencyTransferAttachment attachment = (CurrencyTransferAttachment) transaction.getAttachment();
            if (attachment.getUnitsQNT() <= 0) {
                throw new NxtException.NotValidException("Invalid currency transfer: " + attachment.getJSONObject());
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active: " + attachment.getJSONObject());
            }
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            CurrencyTransferAttachment attachment = (CurrencyTransferAttachment) transaction.getAttachment();
            if (attachment.getUnitsQNT() > senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId())) {
                return false;
            }
            senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                    attachment.getCurrencyId(), -attachment.getUnitsQNT());
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            CurrencyTransferAttachment attachment = (CurrencyTransferAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getCurrencyId(), attachment.getUnitsQNT());
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            CurrencyTransferAttachment attachment = (CurrencyTransferAttachment) transaction.getAttachment();
            Currency.transferCurrency(getLedgerEvent(), AccountLedger.newEventId(transaction), senderAccount, recipientAccount,
                    attachment.getCurrencyId(), attachment.getUnitsQNT());
            CurrencyTransfer.addTransfer(transaction, attachment);
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public final boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType PUBLISH_EXCHANGE_OFFER = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_PUBLISH_EXCHANGE_OFFER;
        }

        @Override
        public String getName() {
            return "PublishExchangeOffer";
        }

        @Override
        public PublishExchangeOfferAttachment parseAttachment(ByteBuffer buffer) {
            return new PublishExchangeOfferAttachment(buffer);
        }

        @Override
        public PublishExchangeOfferAttachment parseAttachment(JSONObject attachmentData) {
            return new PublishExchangeOfferAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment) transaction.getAttachment();
            if (attachment.getBuyRateNQT() <= 0
                    || attachment.getSellRateNQT() <= 0
                    || attachment.getBuyRateNQT() > attachment.getSellRateNQT()) {
                throw new NxtException.NotValidException(String.format("Invalid exchange offer, buy rate %d and sell rate %d has to be larger than 0, buy rate cannot be larger than sell rate",
                        attachment.getBuyRateNQT(), attachment.getSellRateNQT()));
            }
            if (attachment.getTotalBuyLimitQNT() < 0
                    || attachment.getTotalSellLimitQNT() < 0
                    || attachment.getInitialBuySupplyQNT() < 0
                    || attachment.getInitialSellSupplyQNT() < 0
                    || attachment.getExpirationHeight() < 0) {
                throw new NxtException.NotValidException("Invalid exchange offer, units and height cannot be negative: " + attachment.getJSONObject());
            }
            if (attachment.getTotalBuyLimitQNT() < attachment.getInitialBuySupplyQNT()
                    || attachment.getTotalSellLimitQNT() < attachment.getInitialSellSupplyQNT()) {
                throw new NxtException.NotValidException("Initial supplies must not exceed total limits");
            }
            if (attachment.getTotalBuyLimitQNT() == 0 && attachment.getTotalSellLimitQNT() == 0) {
                throw new NxtException.NotValidException("Total buy and sell limits cannot be both 0");
            }
            if (attachment.getInitialBuySupplyQNT() == 0 && attachment.getInitialSellSupplyQNT() == 0) {
                throw new NxtException.NotValidException("Initial buy and sell supply cannot be both 0");
            }
            if (attachment.getExpirationHeight() <= attachment.getFinishValidationHeight(transaction)) {
                throw new NxtException.NotCurrentlyValidException("Expiration height must be after transaction execution height");
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active: " + attachment.getJSONObject());
            }
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment) transaction.getAttachment();
            long currencyId = attachment.getCurrencyId();
            Currency currency = Currency.getCurrency(currencyId);
            ChildChain childChain = transaction.getChain();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            if (attachment.getInitialBuySupplyQNT() > 0) {
                long amountNQT = Convert.unitRateToAmount(attachment.getInitialBuySupplyQNT(), currency.getDecimals(),
                                            attachment.getBuyRateNQT(), childChain.getDecimals());
                BalanceHome.Balance balance = childChain.getBalanceHome().getBalance(senderAccount.getId());
                if (balance.getUnconfirmedBalance() < amountNQT) {
                    return false;
                }
                balance.addToUnconfirmedBalance(getLedgerEvent(), eventId, -amountNQT);
            }
            if (senderAccount.getUnconfirmedCurrencyUnits(currencyId) < attachment.getInitialSellSupplyQNT()) {
                return false;
            }
            senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), eventId,
                        currencyId, -attachment.getInitialSellSupplyQNT());
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment) transaction.getAttachment();
            long currencyId = attachment.getCurrencyId();
            Currency currency = Currency.getCurrency(currencyId, true);
            if (currency == null) {
                return;
            }
            ChildChain childChain = transaction.getChain();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            if (attachment.getInitialBuySupplyQNT() > 0) {
                long amountNQT = Convert.unitRateToAmount(attachment.getInitialBuySupplyQNT(), currency.getDecimals(),
                                            attachment.getBuyRateNQT(), childChain.getDecimals());
                senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, amountNQT);
            }
            if (!currency.isDeleted()) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), eventId,
                        currencyId, attachment.getInitialSellSupplyQNT());
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment) transaction.getAttachment();
            transaction.getChain().getExchangeOfferHome().publishOffer(transaction, attachment);
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getExchangeOfferHome().getBuyOffer(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate exchange offer id " + transaction.getStringId());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return true;
        }

    };

    abstract static class MonetarySystemExchange extends MonetarySystemTransactionType {

        @Override
        public final void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ExchangeAttachment attachment = (ExchangeAttachment) transaction.getAttachment();
            if (attachment.getRateNQT() <= 0 || attachment.getUnitsQNT() == 0) {
                throw new NxtException.NotValidException("Invalid exchange: " + attachment.getJSONObject());
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not active: " + attachment.getJSONObject());
            }
            long amount = Convert.unitRateToAmount(attachment.getUnitsQNT(), currency.getDecimals(), attachment.getRateNQT(),
                    transaction.getChain().getDecimals());
            if (amount == 0) {
                throw new NxtException.NotValidException("Currency exchange request has zero value: " + attachment.getJSONObject());
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

    }

    public static final TransactionType EXCHANGE_BUY = new MonetarySystemExchange() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_EXCHANGE_BUY;
        }

        @Override
        public String getName() {
            return "ExchangeBuy";
        }

        @Override
        public ExchangeBuyAttachment parseAttachment(ByteBuffer buffer) {
            return new ExchangeBuyAttachment(buffer);
        }

        @Override
        public ExchangeBuyAttachment parseAttachment(JSONObject attachmentData) {
            return new ExchangeBuyAttachment(attachmentData);
        }


        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ExchangeBuyAttachment attachment = (ExchangeBuyAttachment) transaction.getAttachment();
            long currencyId = attachment.getCurrencyId();
            Currency currency = Currency.getCurrency(currencyId);
            ChildChain childChain = transaction.getChain();
            long amount = Convert.unitRateToAmount(attachment.getUnitsQNT(), currency.getDecimals(),
                                    attachment.getRateNQT(), childChain.getDecimals());
            BalanceHome.Balance balance = childChain.getBalanceHome().getBalance(senderAccount.getId());
            if (balance.getUnconfirmedBalance() < amount) {
                return false;
            }
            balance.addToUnconfirmedBalance(getLedgerEvent(), AccountLedger.newEventId(transaction), -amount);
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ExchangeBuyAttachment attachment = (ExchangeBuyAttachment) transaction.getAttachment();
            long currencyId = attachment.getCurrencyId();
            Currency currency = Currency.getCurrency(currencyId, true);
            if (currency == null) {
                return;
            }
            ChildChain childChain = transaction.getChain();
            long amount = Convert.unitRateToAmount(attachment.getUnitsQNT(), currency.getDecimals(),
                                    attachment.getRateNQT(), childChain.getDecimals());
            senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(),
                    AccountLedger.newEventId(transaction), amount);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ExchangeBuyAttachment attachment = (ExchangeBuyAttachment) transaction.getAttachment();
            transaction.getChain().getExchangeRequestHome().addExchangeRequest(transaction, attachment);
            transaction.getChain().getExchangeOfferHome().exchangeNXTForCurrency(transaction, senderAccount, attachment.getCurrencyId(),
                    attachment.getRateNQT(), attachment.getUnitsQNT());
        }

    };

    public static final TransactionType EXCHANGE_SELL = new MonetarySystemExchange() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_EXCHANGE_SELL;
        }

        @Override
        public String getName() {
            return "ExchangeSell";
        }

        @Override
        public ExchangeSellAttachment parseAttachment(ByteBuffer buffer) {
            return new ExchangeSellAttachment(buffer);
        }

        @Override
        public ExchangeSellAttachment parseAttachment(JSONObject attachmentData) {
            return new ExchangeSellAttachment(attachmentData);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ExchangeSellAttachment attachment = (ExchangeSellAttachment) transaction.getAttachment();
            if (senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId()) >= attachment.getUnitsQNT()) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getCurrencyId(), -attachment.getUnitsQNT());
                return true;
            }
            return false;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ExchangeSellAttachment attachment = (ExchangeSellAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), AccountLedger.newEventId(transaction),
                        attachment.getCurrencyId(), attachment.getUnitsQNT());
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ExchangeSellAttachment attachment = (ExchangeSellAttachment) transaction.getAttachment();
            transaction.getChain().getExchangeRequestHome().addExchangeRequest(transaction, attachment);
            transaction.getChain().getExchangeOfferHome().exchangeCurrencyForNXT(transaction, senderAccount, attachment.getCurrencyId(),
                    attachment.getRateNQT(), attachment.getUnitsQNT());
        }

    };

    public static final TransactionType CURRENCY_MINTING = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_MINTING;
        }

        @Override
        public String getName() {
            return "CurrencyMinting";
        }

        @Override
        public CurrencyMintingAttachment parseAttachment(ByteBuffer buffer) {
            return new CurrencyMintingAttachment(buffer);
        }

        @Override
        public CurrencyMintingAttachment parseAttachment(JSONObject attachmentData) {
            return new CurrencyMintingAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            CurrencyMintingAttachment attachment = (CurrencyMintingAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (attachment.getUnitsQNT() <= 0) {
                throw new NxtException.NotValidException("Invalid number of units: " + attachment.getUnitsQNT());
            }
            if (attachment.getUnitsQNT() > (currency.getMaxSupplyQNT() - currency.getReserveSupplyQNT()) / Constants.MAX_MINTING_RATIO) {
                throw new NxtException.NotValidException(String.format("Cannot mint more than 1/%d of the total units supply in a single request", Constants.MAX_MINTING_RATIO));
            }
            if (!currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active " + attachment.getJSONObject());
            }
            long counter = CurrencyMint.getCounter(attachment.getCurrencyId(), transaction.getSenderId());
            if (attachment.getCounter() <= counter) {
                throw new NxtException.NotCurrentlyValidException(String.format("Counter %d has to be bigger than %d", attachment.getCounter(), counter));
            }
            if (!CurrencyMinting.meetsTarget(transaction.getSenderId(), currency, attachment)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Hash doesn't meet target %s", attachment.getJSONObject()));
            }
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            CurrencyMintingAttachment attachment = (CurrencyMintingAttachment) transaction.getAttachment();
            CurrencyMint.mintCurrency(getLedgerEvent(), AccountLedger.newEventId(transaction), senderAccount, attachment);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            CurrencyMintingAttachment attachment = (CurrencyMintingAttachment) transaction.getAttachment();
            return TransactionType.isDuplicate(CURRENCY_MINTING, attachment.getCurrencyId() + ":" + transaction.getSenderId(), duplicates, true)
                    || super.isDuplicate(transaction, duplicates);
        }

        @Override
        public boolean isUnconfirmedDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            CurrencyMintingAttachment attachment = (CurrencyMintingAttachment) transaction.getAttachment();
            return TransactionType.isDuplicate(CURRENCY_MINTING, attachment.getCurrencyId() + ":" + transaction.getSenderId(), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType CURRENCY_DELETION = new MonetarySystemTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_DELETION;
        }

        @Override
        public String getName() {
            return "CurrencyDeletion";
        }

        @Override
        public CurrencyDeletionAttachment parseAttachment(ByteBuffer buffer) {
            return new CurrencyDeletionAttachment(buffer);
        }

        @Override
        public CurrencyDeletionAttachment parseAttachment(JSONObject attachmentData) {
            return new CurrencyDeletionAttachment(attachmentData);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            CurrencyDeletionAttachment attachment = (CurrencyDeletionAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            String nameLower = currency.getName().toLowerCase(Locale.ROOT);
            String codeLower = currency.getCode().toLowerCase(Locale.ROOT);
            boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, true);
            if (! nameLower.equals(codeLower)) {
                isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, true);
            }
            return isDuplicate;
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            CurrencyDeletionAttachment attachment = (CurrencyDeletionAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (!currency.canBeDeletedBy(transaction.getSenderId())) {
                throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(currency.getId()) + " cannot be deleted by account " +
                        Long.toUnsignedString(transaction.getSenderId()));
            }
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            CurrencyDeletionAttachment attachment = (CurrencyDeletionAttachment) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            currency.delete(getLedgerEvent(), AccountLedger.newEventId(transaction), senderAccount);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public final boolean isPhasingSafe() {
            return false;
        }

    };

}
