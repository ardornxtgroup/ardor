/*
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

import nxt.account.AccountControlFxtTransactionType;
import nxt.account.AccountPropertyTransactionType;
import nxt.account.PaymentFxtTransactionType;
import nxt.account.PaymentTransactionType;
import nxt.ae.AssetExchangeTransactionType;
import nxt.aliases.AliasTransactionType;
import nxt.ce.CoinExchangeFxtTransactionType;
import nxt.ce.CoinExchangeTransactionType;
import nxt.dgs.DigitalGoodsTransactionType;
import nxt.lightcontracts.LightContractTransactionType;
import nxt.messaging.MessagingTransactionType;
import nxt.ms.MonetarySystemTransactionType;
import nxt.shuffling.ShufflingTransactionType;
import nxt.taggeddata.TaggedDataTransactionType;
import nxt.voting.AccountControlTransactionType;
import nxt.voting.VotingTransactionType;

/**
 * Important!
 * Whenever adding or changing a value run the main menu to generate the constants in the Constants inner class
 */
public enum TransactionTypeEnum {

    // Parent Transactions
    CHILD_BLOCK(ChildBlockFxtTransactionType.INSTANCE),
    PARENT_PAYMENT(PaymentFxtTransactionType.ORDINARY),
    LEASING(AccountControlFxtTransactionType.EFFECTIVE_BALANCE_LEASING),
    PARENT_ORDER_ISSUE(CoinExchangeFxtTransactionType.ORDER_ISSUE),
    PARENT_ORDER_CANCEL(CoinExchangeFxtTransactionType.ORDER_CANCEL),

    // Child Transactions
    CHILD_PAYMENT(PaymentTransactionType.ORDINARY),
    SEND_MESSAGE(MessagingTransactionType.ARBITRARY_MESSAGE),

    ASSET_ISSUANCE(AssetExchangeTransactionType.ASSET_ISSUANCE),
    ASSET_TRANSFER(AssetExchangeTransactionType.ASSET_TRANSFER),
    ASSET_ASK_ORDER_PLACEMENT(AssetExchangeTransactionType.ASK_ORDER_PLACEMENT),
    ASSET_BID_ORDER_PLACEMENT(AssetExchangeTransactionType.BID_ORDER_PLACEMENT),
    ASSET_ASK_ORDER_CANCELLATION(AssetExchangeTransactionType.ASK_ORDER_CANCELLATION),
    ASSET_BID_ORDER_CANCELLATION(AssetExchangeTransactionType.BID_ORDER_CANCELLATION),
    ASSET_DIVIDEND_PAYMENTreturn(AssetExchangeTransactionType.DIVIDEND_PAYMENT),
    ASSET_DELETE(AssetExchangeTransactionType.ASSET_DELETE),
    ASSET_INCREASE(AssetExchangeTransactionType.ASSET_INCREASE),
    ASSET_SET_PHASING_CONTROL(AssetExchangeTransactionType.SET_PHASING_CONTROL),
    ASSET_PROPERTY_SET(AssetExchangeTransactionType.ASSET_PROPERTY_SET),
    ASSET_PROPERTY_DELETE(AssetExchangeTransactionType.ASSET_PROPERTY_DELETE),

    DGS_LISTING(DigitalGoodsTransactionType.LISTING),
    DGS_DELISTING(DigitalGoodsTransactionType.DELISTING),
    DGS_PRICE_CHANGE(DigitalGoodsTransactionType.PRICE_CHANGE),
    DGS_QUANTITY_CHANGE(DigitalGoodsTransactionType.QUANTITY_CHANGE),
    DGS_PURCHASE(DigitalGoodsTransactionType.PURCHASE),
    DGS_DELIVERY(DigitalGoodsTransactionType.DELIVERY),
    DGS_FEEDBACK(DigitalGoodsTransactionType.FEEDBACK),
    DGS_REFUND(DigitalGoodsTransactionType.REFUND),

    SET_PHASING_ONLY(AccountControlTransactionType.SET_PHASING_ONLY),

    MS_CURRENCY_ISSUANCE(MonetarySystemTransactionType.CURRENCY_ISSUANCE),
    MS_RESERVE_INCREASE(MonetarySystemTransactionType.RESERVE_INCREASE),
    MS_RESERVE_CLAIM(MonetarySystemTransactionType.RESERVE_CLAIM),
    MS_CURRENCY_TRANSFER(MonetarySystemTransactionType.CURRENCY_TRANSFER),
    MS_PUBLISH_EXCHANGE_OFFER(MonetarySystemTransactionType.PUBLISH_EXCHANGE_OFFER),
    MS_EXCHANGE_BUY(MonetarySystemTransactionType.EXCHANGE_BUY),
    MS_EXCHANGE_SELL(MonetarySystemTransactionType.EXCHANGE_SELL),
    MS_CURRENCY_MINTING(MonetarySystemTransactionType.CURRENCY_MINTING),
    MS_CURRENCY_DELETION(MonetarySystemTransactionType.CURRENCY_DELETION),

    TAGGED_DATA_UPLOAD(TaggedDataTransactionType.TAGGED_DATA_UPLOAD),

    SHUFFLING_CREATION(ShufflingTransactionType.SHUFFLING_CREATION),
    SHUFFLING_REGISTRATION(ShufflingTransactionType.SHUFFLING_REGISTRATION),
    SHUFFLING_PROCESSING(ShufflingTransactionType.SHUFFLING_PROCESSING),
    SHUFFLING_RECIPIENTS(ShufflingTransactionType.SHUFFLING_RECIPIENTS),
    SHUFFLING_VERIFICATION(ShufflingTransactionType.SHUFFLING_VERIFICATION),
    SHUFFLING_CANCELLATION(ShufflingTransactionType.SHUFFLING_CANCELLATION),

    ALIAS_ASSIGNMENT(AliasTransactionType.ALIAS_ASSIGNMENT),
    ALIAS_BUY(AliasTransactionType.ALIAS_BUY),
    ALIAS_SELL(AliasTransactionType.ALIAS_SELL),
    ALIAS_DELETE(AliasTransactionType.ALIAS_DELETE),

    POLL_CREATION(VotingTransactionType.POLL_CREATION),
    VOTE_CASTING(VotingTransactionType.VOTE_CASTING),
    PHASING_VOTE_CASTING(VotingTransactionType.PHASING_VOTE_CASTING),

    ACCOUNT_INFO(AccountPropertyTransactionType.ACCOUNT_INFO),
    ACCOUNT_PROPERTY_SET(AccountPropertyTransactionType.ACCOUNT_PROPERTY_SET),
    ACCOUNT_PROPERTY_DELETE(AccountPropertyTransactionType.ACCOUNT_PROPERTY_DELETE),

    COIN_EXCHANGE_ORDER_ISSUE(CoinExchangeTransactionType.ORDER_ISSUE),
    COIN_EXCHANGE_ORDER_CANCEL(CoinExchangeTransactionType.ORDER_CANCEL),

    CONTRACT_REFERENCE_SET(LightContractTransactionType.CONTRACT_REFERENCE_SET),
    CONTRACT_REFERENCE_DELETE(LightContractTransactionType.CONTRACT_REFERENCE_DELETE);

    private final TransactionType transactionType;

    TransactionTypeEnum(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
}
