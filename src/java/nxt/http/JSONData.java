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

package nxt.http;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountLedger.LedgerEntry;
import nxt.account.AccountRestrictions;
import nxt.account.BalanceHome;
import nxt.account.FundingMonitor;
import nxt.account.HoldingType;
import nxt.account.Token;
import nxt.addons.*;
import nxt.addons.ContractRunner.INVOCATION_TYPE;
import nxt.ae.Asset;
import nxt.ae.AssetDeleteAttachment;
import nxt.ae.AssetDividendHome;
import nxt.ae.AssetHistory;
import nxt.ae.AssetTransfer;
import nxt.ae.AssetTransferAttachment;
import nxt.ae.OrderCancellationAttachment;
import nxt.ae.OrderHome;
import nxt.ae.OrderPlacementAttachment;
import nxt.ae.TradeHome;
import nxt.aliases.AliasHome;
import nxt.blockchain.Appendix;
import nxt.blockchain.Block;
import nxt.blockchain.Bundler;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.blockchain.Generator;
import nxt.blockchain.Transaction;
import nxt.blockchain.UnconfirmedTransaction;
import nxt.ce.CoinExchange;
import nxt.ce.OrderCancelAttachment;
import nxt.ce.OrderIssueAttachment;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.db.DbIterator;
import nxt.dgs.DigitalGoodsHome;
import nxt.lightcontracts.ContractReference;
import nxt.messaging.PrunableMessageHome;
import nxt.ms.Currency;
import nxt.ms.CurrencyFounderHome;
import nxt.ms.CurrencyTransfer;
import nxt.ms.CurrencyTransferAttachment;
import nxt.ms.CurrencyType;
import nxt.ms.ExchangeAttachment;
import nxt.ms.ExchangeHome;
import nxt.ms.ExchangeOfferHome;
import nxt.ms.ExchangeRequestHome;
import nxt.ms.MonetarySystemTransactionType;
import nxt.ms.PublishExchangeOfferAttachment;
import nxt.peer.BundlerRate;
import nxt.peer.Peer;
import nxt.shuffling.Shuffler;
import nxt.shuffling.ShufflingHome;
import nxt.shuffling.ShufflingParticipantHome;
import nxt.taggeddata.TaggedDataHome;
import nxt.util.Convert;
import nxt.util.Filter;
import nxt.util.Logger;
import nxt.voting.PhasingPollHome;
import nxt.voting.PhasingVoteHome;
import nxt.voting.PollHome;
import nxt.voting.VoteHome;
import nxt.voting.VoteWeighting;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JSONData {

    static JSONObject alias(AliasHome.Alias alias) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", alias.getAccountId());
        json.put("aliasName", alias.getAliasName());
        json.put("aliasURI", alias.getAliasURI());
        json.put("timestamp", alias.getTimestamp());
        json.put("alias", Long.toUnsignedString(alias.getId()));
        AliasHome.Offer offer = alias.getOffer();
        if (offer != null) {
            json.put("priceNQT", String.valueOf(offer.getPriceNQT()));
            if (offer.getBuyerId() != 0) {
                json.put("buyer", Long.toUnsignedString(offer.getBuyerId()));
            }
        }
        return json;
    }

    static JSONObject balance(Chain chain, long accountId, int height) {
        JSONObject json = new JSONObject();
        BalanceHome.Balance balance = chain.getBalanceHome().getBalance(accountId, height);
        if (balance == null) {
            json.put("balanceNQT", "0");
            json.put("unconfirmedBalanceNQT", "0");
        } else {
            json.put("balanceNQT", String.valueOf(balance.getBalance()));
            json.put("unconfirmedBalanceNQT", String.valueOf(balance.getUnconfirmedBalance()));
        }
        return json;
    }

    static JSONObject lessor(Account account, boolean includeEffectiveBalance) {
        JSONObject json = new JSONObject();
        Account.AccountLease accountLease = account.getAccountLease();
        if (accountLease.getCurrentLesseeId() != 0) {
            putAccount(json, "currentLessee", accountLease.getCurrentLesseeId());
            json.put("currentHeightFrom", String.valueOf(accountLease.getCurrentLeasingHeightFrom()));
            json.put("currentHeightTo", String.valueOf(accountLease.getCurrentLeasingHeightTo()));
            if (includeEffectiveBalance) {
                json.put("effectiveBalanceFXT", String.valueOf(account.getGuaranteedBalanceFQT() / Constants.ONE_FXT));
            }
        }
        if (accountLease.getNextLesseeId() != 0) {
            putAccount(json, "nextLessee", accountLease.getNextLesseeId());
            json.put("nextHeightFrom", String.valueOf(accountLease.getNextLeasingHeightFrom()));
            json.put("nextHeightTo", String.valueOf(accountLease.getNextLeasingHeightTo()));
        }
        return json;
    }

    static JSONObject asset(Asset asset, boolean includeCounts) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", asset.getAccountId());
        json.put("name", asset.getName());
        json.put("description", asset.getDescription());
        json.put("decimals", asset.getDecimals());
        json.put("quantityQNT", String.valueOf(asset.getQuantityQNT()));
        json.put("asset", Long.toUnsignedString(asset.getId()));
        json.put("hasPhasingAssetControl", asset.hasPhasingControl());
        if (includeCounts) {
            json.put("numberOfTransfers", AssetTransfer.getTransferCount(asset.getId()));
            json.put("numberOfAccounts", Account.getAssetAccountCount(asset.getId()));
        }
        return json;
    }

    static JSONObject currency(Currency currency, boolean includeCounts) {
        JSONObject json = new JSONObject();
        json.put("currency", Long.toUnsignedString(currency.getId()));
        putAccount(json, "account", currency.getAccountId());
        json.put("name", currency.getName());
        json.put("code", currency.getCode());
        json.put("description", currency.getDescription());
        json.put("type", currency.getType());
        json.put("chain", currency.getChildChain().getId());
        json.put("initialSupplyQNT", String.valueOf(currency.getInitialSupplyQNT()));
        json.put("currentSupplyQNT", String.valueOf(currency.getCurrentSupplyQNT()));
        json.put("reserveSupplyQNT", String.valueOf(currency.getReserveSupplyQNT()));
        json.put("maxSupplyQNT", String.valueOf(currency.getMaxSupplyQNT()));
        json.put("creationHeight", currency.getCreationHeight());
        json.put("issuanceHeight", currency.getIssuanceHeight());
        json.put("minReservePerUnitNQT", String.valueOf(currency.getMinReservePerUnitNQT()));
        json.put("currentReservePerUnitNQT", String.valueOf(currency.getCurrentReservePerUnitNQT()));
        json.put("minDifficulty", currency.getMinDifficulty());
        json.put("maxDifficulty", currency.getMaxDifficulty());
        json.put("algorithm", currency.getAlgorithm());
        json.put("decimals", currency.getDecimals());
        if (includeCounts) {
            json.put("numberOfTransfers", CurrencyTransfer.getTransferCount(currency.getId()));
        }
        JSONArray types = new JSONArray();
        for (CurrencyType type : CurrencyType.values()) {
            if (currency.is(type)) {
                types.add(type.toString());
            }
        }
        json.put("types", types);
        return json;
    }

    static JSONObject currencyFounder(CurrencyFounderHome.CurrencyFounder founder) {
        JSONObject json = new JSONObject();
        json.put("currency", Long.toUnsignedString(founder.getCurrencyId()));
        putAccount(json, "account", founder.getAccountId());
        json.put("amountPerUnitNQT", String.valueOf(founder.getAmountPerUnitNQT()));
        return json;
    }

    static JSONObject accountAsset(Account.AccountAsset accountAsset, boolean includeAccount, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        if (includeAccount) {
            putAccount(json, "account", accountAsset.getAccountId());
        }
        json.put("asset", Long.toUnsignedString(accountAsset.getAssetId()));
        json.put("quantityQNT", String.valueOf(accountAsset.getQuantityQNT()));
        json.put("unconfirmedQuantityQNT", String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
        if (includeAssetInfo) {
            putAssetInfo(json, accountAsset.getAssetId());
        }
        return json;
    }

    static JSONObject accountCurrency(Account.AccountCurrency accountCurrency, boolean includeAccount, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        if (includeAccount) {
            putAccount(json, "account", accountCurrency.getAccountId());
        }
        json.put("currency", Long.toUnsignedString(accountCurrency.getCurrencyId()));
        json.put("unitsQNT", String.valueOf(accountCurrency.getUnits()));
        json.put("unconfirmedUnitsQNT", String.valueOf(accountCurrency.getUnconfirmedUnits()));
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, accountCurrency.getCurrencyId());
        }
        return json;
    }

    static JSONObject accountProperty(Account.AccountProperty accountProperty, boolean includeAccount, boolean includeSetter) {
        JSONObject json = new JSONObject();
        if (includeAccount) {
            putAccount(json, "recipient", accountProperty.getRecipientId());
        }
        if (includeSetter) {
            putAccount(json, "setter", accountProperty.getSetterId());
        }
        json.put("property", accountProperty.getProperty());
        json.put("value", accountProperty.getValue());
        return json;
    }

    static JSONObject assetProperty(Asset.AssetProperty property, boolean includeAsset, boolean includeSetter) {
        JSONObject json = new JSONObject();
        if (includeAsset) {
            json.put("asset", Long.toUnsignedString(property.getAssetId()));
        }
        if (includeSetter) {
            putAccount(json, "setter", property.getSetterId());
        }
        json.put("property", property.getProperty());
        json.put("value", property.getValue());
        return json;
    }

    static JSONObject askOrder(OrderHome.Ask order) {
        JSONObject json = order(order);
        json.put("type", "ask");
        return json;
    }

    static JSONObject bidOrder(OrderHome.Bid order) {
        JSONObject json = order(order);
        json.put("type", "bid");
        return json;
    }

    private static JSONObject order(OrderHome.Order order) {
        JSONObject json = new JSONObject();
        json.put("order", Long.toUnsignedString(order.getId()));
        json.put("orderFullHash", Convert.toHexString(order.getFullHash()));
        json.put("asset", Long.toUnsignedString(order.getAssetId()));
        putAccount(json, "account", order.getAccountId());
        json.put("quantityQNT", String.valueOf(order.getQuantityQNT()));
        json.put("priceNQTPerShare", String.valueOf(order.getPriceNQT()));
        json.put("height", order.getHeight());
        json.put("transactionIndex", order.getTransactionIndex());
        json.put("transactionHeight", order.getTransactionHeight());
        return json;
    }

    static JSONObject expectedAskOrder(Transaction transaction) {
        JSONObject json = expectedOrder(transaction);
        json.put("type", "ask");
        return json;
    }

    static JSONObject expectedBidOrder(Transaction transaction) {
        JSONObject json = expectedOrder(transaction);
        json.put("type", "bid");
        return json;
    }

    private static JSONObject expectedOrder(Transaction transaction) {
        JSONObject json = new JSONObject();
        OrderPlacementAttachment attachment = (OrderPlacementAttachment)transaction.getAttachment();
        json.put("order", Long.toUnsignedString(transaction.getId()));
        json.put("orderFullHash", Convert.toHexString(transaction.getFullHash()));
        json.put("asset", Long.toUnsignedString(attachment.getAssetId()));
        putAccount(json, "account", transaction.getSenderId());
        json.put("quantityQNT", String.valueOf(attachment.getQuantityQNT()));
        json.put("priceNQTPerShare", String.valueOf(attachment.getPriceNQT()));
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject expectedOrderCancellation(Transaction transaction) {
        JSONObject json = new JSONObject();
        OrderCancellationAttachment attachment = (OrderCancellationAttachment)transaction.getAttachment();
        json.put("order", Long.toUnsignedString(attachment.getOrderId()));
        putAccount(json, "account", transaction.getSenderId());
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject offer(ExchangeOfferHome.ExchangeOffer offer) {
        JSONObject json = new JSONObject();
        json.put("offer", Long.toUnsignedString(offer.getId()));
        json.put("offerFullHash", Convert.toHexString(offer.getFullHash()));
        putAccount(json, "account", offer.getAccountId());
        json.put("height", offer.getHeight());
        json.put("expirationHeight", offer.getExpirationHeight());
        json.put("currency", Long.toUnsignedString(offer.getCurrencyId()));
        json.put("rateNQTPerUnit", String.valueOf(offer.getRateNQT()));
        json.put("limitQNT", String.valueOf(offer.getLimitQNT()));
        json.put("supplyQNT", String.valueOf(offer.getSupplyQNT()));
        return json;
    }

    static JSONObject expectedBuyOffer(Transaction transaction) {
        JSONObject json = expectedOffer(transaction);
        PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment)transaction.getAttachment();
        json.put("rateNQTPerUnit", String.valueOf(attachment.getBuyRateNQT()));
        json.put("limitQNT", String.valueOf(attachment.getTotalBuyLimitQNT()));
        json.put("supplyQNT", String.valueOf(attachment.getInitialBuySupplyQNT()));
        return json;
    }

    static JSONObject expectedSellOffer(Transaction transaction) {
        JSONObject json = expectedOffer(transaction);
        PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment)transaction.getAttachment();
        json.put("rateNQTPerUnit", String.valueOf(attachment.getSellRateNQT()));
        json.put("limitQNT", String.valueOf(attachment.getTotalSellLimitQNT()));
        json.put("supplyQNT", String.valueOf(attachment.getInitialSellSupplyQNT()));
        return json;
    }

    private static JSONObject expectedOffer(Transaction transaction) {
        PublishExchangeOfferAttachment attachment = (PublishExchangeOfferAttachment)transaction.getAttachment();
        JSONObject json = new JSONObject();
        json.put("offer", Long.toUnsignedString(transaction.getId()));
        json.put("offerFullHash", Convert.toHexString(transaction.getFullHash()));
        putAccount(json, "account", transaction.getSenderId());
        json.put("expirationHeight", attachment.getExpirationHeight());
        json.put("currency", Long.toUnsignedString(attachment.getCurrencyId()));
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject availableOffers(ExchangeOfferHome.AvailableOffers availableOffers) {
        JSONObject json = new JSONObject();
        json.put("rateNQTPerUnit", String.valueOf(availableOffers.getRateNQT()));
        json.put("unitsQNT", String.valueOf(availableOffers.getUnitsQNT()));
        json.put("amountNQT", String.valueOf(availableOffers.getAmountNQT()));
        return json;
    }

    static JSONObject coinExchangeOrder(CoinExchange.Order order) {
        JSONObject json = new JSONObject();
        Chain chain = Chain.getChain(order.getChainId());
        Chain exchangeChain = Chain.getChain(order.getExchangeId());
        long quantityQNT = order.getQuantityQNT();
        long bidPriceNQT = order.getBidPriceNQT();
        json.put("order", Long.toUnsignedString(order.getId()));
        json.put("orderFullHash", Convert.toHexString(order.getFullHash()));
        json.put("chain", chain.getId());
        json.put("exchange", exchangeChain.getId());
        putAccount(json, "account", order.getAccountId());
        json.put("quantityQNT", String.valueOf(quantityQNT));
        json.put("bidNQTPerCoin", String.valueOf(bidPriceNQT));
        json.put("askNQTPerCoin", String.valueOf(order.getAskPriceNQT()));
        long exchangeQNT = Convert.unitRateToAmount(quantityQNT, exchangeChain.getDecimals(),
                                        bidPriceNQT, chain.getDecimals());
        json.put("exchangeQNT", String.valueOf(exchangeQNT));
        return json;
    }

    static JSONObject coinExchangeTrade(CoinExchange.Trade trade) {
        JSONObject json = new JSONObject();
        json.put("orderFullHash", Convert.toHexString(trade.getOrderFullHash()));
        json.put("matchFullHash", Convert.toHexString(trade.getMatchFullHash()));
        json.put("chain", trade.getChainId());
        json.put("exchange", trade.getExchangeId());
        putAccount(json, "account", trade.getAccountId());
        json.put("quantityQNT", String.valueOf(trade.getExchangeQuantityQNT()));
        json.put("priceNQTPerCoin", String.valueOf(trade.getExchangePriceNQT()));
        json.put("exchangeRate", trade.getExchangePrice().toPlainString());
        json.put("block", Long.toUnsignedString(trade.getBlockId()));
        json.put("height", trade.getHeight());
        json.put("timestamp", trade.getTimestamp());
        return json;
    }

    static JSONObject expectedCoinExchangeOrder(Transaction transaction) {
        JSONObject json = new JSONObject();
        json.put("order", Long.toUnsignedString(transaction.getId()));
        json.put("orderFullHash", Convert.toHexString(transaction.getFullHash()));
        putAccount(json, "account", transaction.getSenderId());
        OrderIssueAttachment orderIssueAttachment = (OrderIssueAttachment)transaction.getAttachment();
        Chain chain = orderIssueAttachment.getChain();
        Chain exchangeChain = orderIssueAttachment.getExchangeChain();
        long priceNQT = orderIssueAttachment.getPriceNQT();
        long quantityQNT = orderIssueAttachment.getQuantityQNT();
        json.put("chain", chain.getId());
        json.put("exchange", exchangeChain.getId());
        json.put("quantityQNT", String.valueOf(quantityQNT));
        json.put("bidNQTPerCoin", String.valueOf(priceNQT));
        BigDecimal[] ask = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(priceNQT, chain.getDecimals()), MathContext.DECIMAL128)
                    .movePointRight(exchangeChain.getDecimals())
                    .divideAndRemainder(BigDecimal.ONE, MathContext.DECIMAL128);
        long askNQT = ask[0].longValue() + (ask[1].signum() != 0 ? 1 : 0);
        json.put("askNQTPerCoin", String.valueOf(askNQT));
        long exchangeQNT = Convert.unitRateToAmount(quantityQNT, exchangeChain.getDecimals(),
                                        priceNQT, chain.getDecimals());
        json.put("exchangeQNT", String.valueOf(exchangeQNT));
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject expectedCoinExchangeOrderCancellation(Transaction transaction) {
        JSONObject json = new JSONObject();
        OrderCancelAttachment attachment = (OrderCancelAttachment)transaction.getAttachment();
        json.put("order", Long.toUnsignedString(attachment.getOrderId()));
        putAccount(json, "account", transaction.getSenderId());
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject shuffling(ShufflingHome.Shuffling shuffling, boolean includeHoldingInfo) {
        JSONObject json = new JSONObject();
        putAccount(json, "issuer", shuffling.getIssuerId());
        json.put("holding", Long.toUnsignedString(shuffling.getHoldingId()));
        HoldingType holdingType = shuffling.getHoldingType();
        json.put("holdingType", holdingType.getCode());
        if (shuffling.getAssigneeAccountId() != 0) {
            putAccount(json, "assignee", shuffling.getAssigneeAccountId());
        }
        json.put("amount", String.valueOf(shuffling.getAmount()));
        json.put("blocksRemaining", shuffling.getBlocksRemaining());
        json.put("participantCount", shuffling.getParticipantCount());
        json.put("registrantCount", shuffling.getRegistrantCount());
        json.put("stage", shuffling.getStage().getCode());
        json.put("shufflingStateHash", Convert.toHexString(shuffling.getStateHash()));
        json.put("shufflingFullHash", Convert.toHexString(shuffling.getFullHash()));
        JSONArray recipientPublicKeys = new JSONArray();
        for (byte[] recipientPublicKey : shuffling.getRecipientPublicKeys()) {
            recipientPublicKeys.add(Convert.toHexString(recipientPublicKey));
        }
        if (recipientPublicKeys.size() > 0) {
            json.put("recipientPublicKeys", recipientPublicKeys);
        }
        if (includeHoldingInfo) {
            json.put("holdingInfo", holdingInfoJson(holdingType, shuffling.getHoldingId()));
        }
        return json;
    }

    private static JSONObject holdingInfoJson(HoldingType holdingType, long holdingId) {
        JSONObject holdingJson = new JSONObject();
        if (holdingType == HoldingType.COIN) {
            putChainInfo(holdingJson, holdingId);
        } else if (holdingType == HoldingType.ASSET) {
            putAssetInfo(holdingJson, holdingId);
        } else if (holdingType == HoldingType.CURRENCY) {
            putCurrencyInfo(holdingJson, holdingId);
        }
        return holdingJson;
    }

    static JSONObject participant(ShufflingParticipantHome.ShufflingParticipant participant) {
        JSONObject json = new JSONObject();
        json.put("shufflingFullHash", Convert.toHexString(participant.getShufflingFullHash()));
        putAccount(json, "account", participant.getAccountId());
        putAccount(json, "nextAccount", participant.getNextAccountId());
        json.put("state", participant.getState().getCode());
        return json;
    }

    static JSONObject shuffler(Shuffler shuffler, boolean includeParticipantState) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", shuffler.getAccountId());
        putAccount(json, "recipient", Account.getId(shuffler.getRecipientPublicKey()));
        json.put("shufflingFullHash", Convert.toHexString(shuffler.getShufflingFullHash()));
        json.put("feeRateNQTPerFXT", Long.toUnsignedString(shuffler.getFeeRateNQTPerFXT()));
        if (shuffler.getFailedTransaction() != null) {
            json.put("failedTransaction", unconfirmedTransaction(shuffler.getFailedTransaction()));
            json.put("failureCause", shuffler.getFailureCause().getMessage());
        }
        if (includeParticipantState) {
            ShufflingParticipantHome.ShufflingParticipant participant = shuffler.getChildChain().getShufflingParticipantHome()
                    .getParticipant(shuffler.getShufflingFullHash(), shuffler.getAccountId());
            if (participant != null) {
                json.put("participantState", participant.getState().getCode());
            }
        }
        json.put("chain", shuffler.getChildChain().getId());
        return json;
    }

    static JSONObject block(Block block, boolean includeTransactions, boolean includeExecutedPhased) {
        JSONObject json = new JSONObject();
        json.put("block", block.getStringId());
        json.put("height", block.getHeight());
        putAccount(json, "generator", block.getGeneratorId());
        json.put("generatorPublicKey", Convert.toHexString(block.getGeneratorPublicKey()));
        json.put("timestamp", block.getTimestamp());
        json.put("numberOfTransactions", block.getFxtTransactions().size());
        json.put("totalFeeFQT", String.valueOf(block.getTotalFeeFQT()));
        json.put("version", block.getVersion());
        json.put("baseTarget", Long.toUnsignedString(block.getBaseTarget()));
        json.put("cumulativeDifficulty", block.getCumulativeDifficulty().toString());
        if (block.getPreviousBlockId() != 0) {
            json.put("previousBlock", Long.toUnsignedString(block.getPreviousBlockId()));
        }
        if (block.getNextBlockId() != 0) {
            json.put("nextBlock", Long.toUnsignedString(block.getNextBlockId()));
        }
        json.put("payloadHash", Convert.toHexString(block.getPayloadHash()));
        json.put("generationSignature", Convert.toHexString(block.getGenerationSignature()));
        json.put("previousBlockHash", Convert.toHexString(block.getPreviousBlockHash()));
        json.put("blockSignature", Convert.toHexString(block.getBlockSignature()));
        JSONArray transactions = new JSONArray();
        if (includeTransactions) {
            block.getFxtTransactions().forEach(transaction -> transactions.add(transaction(transaction)));
        } else {
            block.getFxtTransactions().forEach(transaction -> transactions.add(Convert.toHexString(transaction.getFullHash())));
        }
        json.put("transactions", transactions);
        if (includeExecutedPhased) {
            JSONArray phasedTransactions = new JSONArray();
            try (DbIterator<PhasingPollHome.PhasingPollResult> phasingPollResults = PhasingPollHome.getApproved(block.getHeight())) {
                for (PhasingPollHome.PhasingPollResult phasingPollResult : phasingPollResults) {
                    if (includeTransactions) {
                        phasedTransactions.add(transaction(Nxt.getBlockchain().getTransaction(phasingPollResult.getChildChain(), phasingPollResult.getFullHash())));
                    } else {
                        ChainTransactionId phasedTransactionId = new ChainTransactionId(phasingPollResult.getChildChain().getId(), phasingPollResult.getFullHash());
                        phasedTransactions.add(phasedTransactionId.getJSON());
                    }
                }
            }
            json.put("executedPhasedTransactions", phasedTransactions);
        }
        return json;
    }

    static JSONObject encryptedData(EncryptedData encryptedData) {
        JSONObject json = new JSONObject();
        json.put("data", Convert.toHexString(encryptedData.getData()));
        json.put("nonce", Convert.toHexString(encryptedData.getNonce()));
        return json;
    }

    static JSONObject goods(DigitalGoodsHome.Goods goods, boolean includeCounts) {
        JSONObject json = new JSONObject();
        json.put("goods", Long.toUnsignedString(goods.getId()));
        json.put("goodsFullHash", Convert.toHexString(goods.getFullHash()));
        json.put("name", goods.getName());
        json.put("description", goods.getDescription());
        json.put("quantity", goods.getQuantity());
        json.put("priceNQT", String.valueOf(goods.getPriceNQT()));
        putAccount(json, "seller", goods.getSellerId());
        json.put("tags", goods.getTags());
        JSONArray tagsJSON = new JSONArray();
        Collections.addAll(tagsJSON, goods.getParsedTags());
        json.put("parsedTags", tagsJSON);
        json.put("delisted", goods.isDelisted());
        json.put("timestamp", goods.getTimestamp());
        json.put("hasImage", goods.hasImage());
        if (includeCounts) {
            json.put("numberOfPurchases", goods.getDGSHome().getGoodsPurchaseCount(goods.getId(), false, true));
            json.put("numberOfPublicFeedbacks", goods.getDGSHome().getGoodsPurchaseCount(goods.getId(), true, true));
        }
        return json;
    }

    static JSONObject tag(DigitalGoodsHome.Tag tag) {
        JSONObject json = new JSONObject();
        json.put("tag", tag.getTag());
        json.put("inStockCount", tag.getInStockCount());
        json.put("totalCount", tag.getTotalCount());
        return json;
    }

    static JSONObject token(Token token) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", Account.getId(token.getPublicKey()));
        json.put("timestamp", token.getTimestamp());
        json.put("valid", token.isValid());
        return json;
    }

    static JSONObject peer(Peer peer) {
        JSONObject json = new JSONObject();
        json.put("address", peer.getHost());
        json.put("port", peer.getPort());
        json.put("state", peer.getState().ordinal());
        json.put("announcedAddress", peer.getAnnouncedAddress());
        json.put("shareAddress", peer.shareAddress());
        json.put("downloadedVolume", peer.getDownloadedVolume());
        json.put("uploadedVolume", peer.getUploadedVolume());
        json.put("application", peer.getApplication());
        json.put("version", peer.getVersion());
        json.put("platform", peer.getPlatform());
        if (peer.getApiPort() != 0) {
            json.put("apiPort", peer.getApiPort());
        }
        if (peer.getApiSSLPort() != 0) {
            json.put("apiSSLPort", peer.getApiSSLPort());
        }
        json.put("blacklisted", peer.isBlacklisted());
        json.put("lastUpdated", peer.getLastUpdated());
        json.put("lastConnectAttempt", peer.getLastConnectAttempt());
        json.put("inbound", peer.isInbound());
        if (peer.isBlacklisted()) {
            json.put("blacklistingCause", peer.getBlacklistingCause());
        }
        JSONArray servicesArray = new JSONArray();
        for (Peer.Service service : Peer.Service.values()) {
            if (peer.providesService(service)) {
                servicesArray.add(service.name());
            }
        }
        json.put("services", servicesArray);
        json.put("blockchainState", peer.getBlockchainState());
        return json;
    }

    static JSONObject poll(PollHome.Poll poll) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", poll.getAccountId());
        json.put("poll", Long.toUnsignedString(poll.getId()));
        json.put("name", poll.getName());
        json.put("description", poll.getDescription());
        JSONArray options = new JSONArray();
        Collections.addAll(options, poll.getOptions());
        json.put("options", options);
        json.put("finishHeight", poll.getFinishHeight());
        json.put("minNumberOfOptions", poll.getMinNumberOfOptions());
        json.put("maxNumberOfOptions", poll.getMaxNumberOfOptions());
        json.put("minRangeValue", poll.getMinRangeValue());
        json.put("maxRangeValue", poll.getMaxRangeValue());
        putVoteWeighting(json, poll.getVoteWeighting());
        json.put("finished", poll.isFinished());
        json.put("timestamp", poll.getTimestamp());
        return json;
    }

    static JSONObject pollResults(PollHome.Poll poll, List<PollHome.OptionResult> results, VoteWeighting voteWeighting) {
        JSONObject json = new JSONObject();
        json.put("poll", Long.toUnsignedString(poll.getId()));
        if (voteWeighting.getMinBalanceModel() == VoteWeighting.MinBalanceModel.ASSET) {
            json.put("decimals", Asset.getAsset(voteWeighting.getHoldingId()).getDecimals());
        } else if(voteWeighting.getMinBalanceModel() == VoteWeighting.MinBalanceModel.CURRENCY) {
            Currency currency = Currency.getCurrency(voteWeighting.getHoldingId(), true);
            json.put("decimals", currency.getDecimals());
        }
        putVoteWeighting(json, voteWeighting);
        json.put("finished", poll.isFinished());
        JSONArray options = new JSONArray();
        Collections.addAll(options, poll.getOptions());
        json.put("options", options);

        JSONArray resultsJson = new JSONArray();
        for (PollHome.OptionResult option : results) {
            JSONObject optionJSON = new JSONObject();
            if (option != null) {
                optionJSON.put("result", String.valueOf(option.getResult()));
                optionJSON.put("weight", String.valueOf(option.getWeight()));
            } else {
                optionJSON.put("result", "");
                optionJSON.put("weight", "0");
            }
            resultsJson.add(optionJSON);
        }
        json.put("results", resultsJson);
        return json;
    }

    interface VoteWeighter {
        long calcWeight(long voterId);
    }
    static JSONObject vote(VoteHome.Vote vote, VoteWeighter weighter) {
        JSONObject json = new JSONObject();
        putAccount(json, "voter", vote.getVoterId());
        json.put("transactionFullHash", Convert.toHexString(vote.getFullHash()));
        JSONArray votesJson = new JSONArray();
        for (byte v : vote.getVoteBytes()) {
            if (v == Constants.NO_VOTE_VALUE) {
                votesJson.add("");
            } else {
                votesJson.add(Byte.toString(v));
            }
        }
        json.put("votes", votesJson);
        if (weighter != null) {
            json.put("weight", String.valueOf(weighter.calcWeight(vote.getVoterId())));
        }
        return json;
    }

    static JSONObject phasingPoll(PhasingPollHome.PhasingPoll poll, boolean countVotes) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(poll.getFullHash()));
        json.put("finishHeight", poll.getFinishHeight());
        json.put("quorum", String.valueOf(poll.getQuorum()));
        putAccount(json, "account", poll.getAccountId());
        JSONArray whitelistJson = new JSONArray();
        for (long accountId : poll.getWhitelist()) {
            JSONObject whitelisted = new JSONObject();
            putAccount(whitelisted, "whitelisted", accountId);
            whitelistJson.add(whitelisted);
        }
        json.put("whitelist", whitelistJson);
        List<ChainTransactionId> linkedTransactions = poll.getLinkedTransactions();
        if (linkedTransactions.size() > 0) {
            JSONArray linkedTransactionsJSON = new JSONArray();
            for (ChainTransactionId linkedTransaction : linkedTransactions) {
                linkedTransactionsJSON.add(linkedTransaction.getJSON());
            }
            json.put("linkedTransactions", linkedTransactionsJSON);
        }
        if (poll.getHashedSecret() != null) {
            json.put("hashedSecret", Convert.toHexString(poll.getHashedSecret()));
            json.put("hashedSecretAlgorithm", poll.getAlgorithm());
        }
        putVoteWeighting(json, poll.getVoteWeighting());
        PhasingPollHome.PhasingPollResult phasingPollResult = PhasingPollHome.getResult(poll.getFullHash());
        json.put("finished", phasingPollResult != null);
        if (phasingPollResult != null) {
            json.put("approved", phasingPollResult.isApproved());
            json.put("result", String.valueOf(phasingPollResult.getResult()));
            json.put("executionHeight", phasingPollResult.getHeight());
        } else if (countVotes) {
            json.put("result", String.valueOf(poll.countVotes()));
        }
        json.put("canFinishEarly", poll.getParams().allowEarlyFinish());
        return json;
    }

    static JSONObject phasingPollResult(PhasingPollHome.PhasingPollResult phasingPollResult) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(phasingPollResult.getFullHash()));
        json.put("approved", phasingPollResult.isApproved());
        json.put("result", String.valueOf(phasingPollResult.getResult()));
        json.put("executionHeight", phasingPollResult.getHeight());
        return json;
    }

    static JSONObject phasingPollVote(PhasingVoteHome.PhasingVote vote) {
        JSONObject json = new JSONObject();
        JSONData.putAccount(json, "voter", vote.getVoterId());
        json.put("transactionFullHash", Convert.toHexString(vote.getVoteFullHash()));
        return json;
    }

    private static void putVoteWeighting(JSONObject json, VoteWeighting voteWeighting) {
        json.put("votingModel", voteWeighting.getVotingModel().getCode());
        json.put("minBalance", String.valueOf(voteWeighting.getMinBalance()));
        json.put("minBalanceModel", voteWeighting.getMinBalanceModel().getCode());
        if (voteWeighting.getHoldingId() != 0) {
            json.put("holding", Long.toUnsignedString(voteWeighting.getHoldingId()));
        }
    }

    public static JSONObject phasingOnly(AccountRestrictions.PhasingOnly phasingOnly) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", phasingOnly.getAccountId());

        JSONObject paramsJson = new JSONObject();
        phasingOnly.getPhasingParams().putMyJSON(paramsJson);
        json.put("controlParams", paramsJson);

        Map<Integer,Long> maxFees = phasingOnly.getMaxFees();
        JSONObject maxFeesJSON = new JSONObject();
        maxFees.entrySet().forEach(entry -> maxFeesJSON.put(entry.getKey(), String.valueOf(entry.getValue())));
        json.put("maxFees", maxFeesJSON);
        json.put("minDuration", phasingOnly.getMinDuration());
        json.put("maxDuration", phasingOnly.getMaxDuration());
        json.put("canFinishEarly", phasingOnly.getPhasingParams().allowEarlyFinish());
        return json;
    }

    static JSONObject purchase(DigitalGoodsHome.Purchase purchase) {
        JSONObject json = new JSONObject();
        json.put("purchase", Long.toUnsignedString(purchase.getId()));
        json.put("goods", Long.toUnsignedString(purchase.getGoodsId()));
        DigitalGoodsHome.Goods goods = purchase.getDGSHome().getGoods(purchase.getGoodsId());
        json.put("name", goods.getName());
        json.put("hasImage", goods.hasImage());
        json.put("goodsFullHash", Convert.toHexString(goods.getFullHash()));
        putAccount(json, "seller", purchase.getSellerId());
        json.put("priceNQT", String.valueOf(purchase.getPriceNQT()));
        json.put("quantity", purchase.getQuantity());
        putAccount(json, "buyer", purchase.getBuyerId());
        json.put("timestamp", purchase.getTimestamp());
        json.put("deliveryDeadlineTimestamp", purchase.getDeliveryDeadlineTimestamp());
        if (purchase.getNote() != null) {
            json.put("note", encryptedData(purchase.getNote()));
        }
        json.put("pending", purchase.isPending());
        if (purchase.getEncryptedGoods() != null) {
            json.put("goodsData", encryptedData(purchase.getEncryptedGoods()));
            json.put("goodsIsText", purchase.goodsIsText());
        }
        if (purchase.getFeedbackNotes() != null) {
            JSONArray feedbacks = new JSONArray();
            for (EncryptedData encryptedData : purchase.getFeedbackNotes()) {
                feedbacks.add(0, encryptedData(encryptedData));
            }
            json.put("feedbackNotes", feedbacks);
        }
        if (purchase.getPublicFeedbacks() != null) {
            JSONArray publicFeedbacks = new JSONArray();
            for (String publicFeedback : purchase.getPublicFeedbacks()) {
                publicFeedbacks.add(0, publicFeedback);
            }
            json.put("publicFeedbacks", publicFeedbacks);
        }
        if (purchase.getRefundNote() != null) {
            json.put("refundNote", encryptedData(purchase.getRefundNote()));
        }
        if (purchase.getDiscountNQT() > 0) {
            json.put("discountNQT", String.valueOf(purchase.getDiscountNQT()));
        }
        if (purchase.getRefundNQT() > 0) {
            json.put("refundNQT", String.valueOf(purchase.getRefundNQT()));
        }
        return json;
    }

    static JSONObject trade(TradeHome.Trade trade, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        json.put("timestamp", trade.getTimestamp());
        json.put("quantityQNT", String.valueOf(trade.getQuantityQNT()));
        json.put("priceNQTPerShare", String.valueOf(trade.getPriceNQT()));
        json.put("asset", Long.toUnsignedString(trade.getAssetId()));
        json.put("askOrderFullHash", Convert.toHexString(trade.getAskOrderFullHash()));
        json.put("bidOrderFullHash", Convert.toHexString(trade.getBidOrderFullHash()));
        json.put("askOrderHeight", trade.getAskOrderHeight());
        json.put("bidOrderHeight", trade.getBidOrderHeight());
        putAccount(json, "seller", trade.getSellerId());
        putAccount(json, "buyer", trade.getBuyerId());
        json.put("block", Long.toUnsignedString(trade.getBlockId()));
        json.put("height", trade.getHeight());
        json.put("tradeType", trade.isBuy() ? "buy" : "sell");
        if (includeAssetInfo) {
            putAssetInfo(json, trade.getAssetId());
        }
        return json;
    }

    static JSONObject assetTransfer(AssetTransfer assetTransfer, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        json.put("assetTransferFullHash", Convert.toHexString(assetTransfer.getFullHash()));
        json.put("chain", assetTransfer.getChainId());
        json.put("asset", Long.toUnsignedString(assetTransfer.getAssetId()));
        putAccount(json, "sender", assetTransfer.getSenderId());
        putAccount(json, "recipient", assetTransfer.getRecipientId());
        json.put("quantityQNT", String.valueOf(assetTransfer.getQuantityQNT()));
        json.put("height", assetTransfer.getHeight());
        json.put("timestamp", assetTransfer.getTimestamp());
        if (includeAssetInfo) {
            putAssetInfo(json, assetTransfer.getAssetId());
        }
        return json;
    }

    static JSONObject expectedAssetTransfer(Transaction transaction, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        AssetTransferAttachment attachment = (AssetTransferAttachment)transaction.getAttachment();
        json.put("assetTransferFullHash", Convert.toHexString(transaction.getFullHash()));
        json.put("chain", transaction.getChain().getId());
        json.put("asset", Long.toUnsignedString(attachment.getAssetId()));
        putAccount(json, "sender", transaction.getSenderId());
        putAccount(json, "recipient", transaction.getRecipientId());
        json.put("quantityQNT", String.valueOf(attachment.getQuantityQNT()));
        if (includeAssetInfo) {
            putAssetInfo(json, attachment.getAssetId());
        }
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject assetHistory(AssetHistory assetHistory, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        json.put("assetHistoryFullHash", Convert.toHexString(assetHistory.getFullHash()));
        json.put("chain", assetHistory.getChainId());
        json.put("asset", Long.toUnsignedString(assetHistory.getAssetId()));
        putAccount(json, "account", assetHistory.getAccountId());
        json.put("quantityQNT", String.valueOf(assetHistory.getQuantityQNT()));
        json.put("height", assetHistory.getHeight());
        json.put("timestamp", assetHistory.getTimestamp());
        if (includeAssetInfo) {
            putAssetInfo(json, assetHistory.getAssetId());
        }
        return json;
    }

    static JSONObject expectedAssetDelete(Transaction transaction, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        AssetDeleteAttachment attachment = (AssetDeleteAttachment)transaction.getAttachment();
        json.put("assetDeleteFullHash", Convert.toHexString(transaction.getFullHash()));
        json.put("chain", transaction.getChain().getId());
        json.put("asset", Long.toUnsignedString(attachment.getAssetId()));
        putAccount(json, "account", transaction.getSenderId());
        json.put("quantityQNT", String.valueOf(attachment.getQuantityQNT()));
        if (includeAssetInfo) {
            putAssetInfo(json, attachment.getAssetId());
        }
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject assetDividend(AssetDividendHome.AssetDividend assetDividend, boolean includeHoldingInfo) {
        JSONObject json = new JSONObject();
        json.put("assetDividendFullHash", Convert.toHexString(assetDividend.getFullHash()));
        json.put("asset", Long.toUnsignedString(assetDividend.getAssetId()));
        json.put("amountNQTPerShare", String.valueOf(assetDividend.getAmountNQT()));
        json.put("totalDividend", String.valueOf(assetDividend.getTotalDividend()));
        json.put("dividendHeight", assetDividend.getDividendHeight());
        json.put("numberOfAccounts", assetDividend.getNumAccounts());
        json.put("height", assetDividend.getHeight());
        json.put("timestamp", assetDividend.getTimestamp());
        json.put("holding", Long.toUnsignedString(assetDividend.getHoldingId()));
        HoldingType holdingType = assetDividend.getHoldingType();
        json.put("holdingType", holdingType.getCode());
        if (includeHoldingInfo) {
            json.put("holdingInfo", holdingInfoJson(holdingType, assetDividend.getHoldingId()));
        }
        return json;
    }

    static JSONObject currencyTransfer(CurrencyTransfer transfer, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transferFullHash", Convert.toHexString(transfer.getFullHash()));
        json.put("chain", transfer.getChainId());
        json.put("currency", Long.toUnsignedString(transfer.getCurrencyId()));
        putAccount(json, "sender", transfer.getSenderId());
        putAccount(json, "recipient", transfer.getRecipientId());
        json.put("unitsQNT", String.valueOf(transfer.getUnitsQNT()));
        json.put("height", transfer.getHeight());
        json.put("timestamp", transfer.getTimestamp());
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, transfer.getCurrencyId());
        }
        return json;
    }

    static JSONObject expectedCurrencyTransfer(Transaction transaction, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        CurrencyTransferAttachment attachment = (CurrencyTransferAttachment)transaction.getAttachment();
        json.put("transferFullHash", Convert.toHexString(transaction.getFullHash()));
        json.put("chain", transaction.getChain().getId());
        json.put("currency", Long.toUnsignedString(attachment.getCurrencyId()));
        putAccount(json, "sender", transaction.getSenderId());
        putAccount(json, "recipient", transaction.getRecipientId());
        json.put("unitsQNT", String.valueOf(attachment.getUnitsQNT()));
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, attachment.getCurrencyId());
        }
        putExpectedTransaction(json, transaction);
        return json;
    }

    static JSONObject exchange(ExchangeHome.Exchange exchange, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(exchange.getTransactionFullHash()));
        json.put("timestamp", exchange.getTimestamp());
        json.put("unitsQNT", String.valueOf(exchange.getUnitsQNT()));
        json.put("rateNQTPerUnit", String.valueOf(exchange.getRateNQT()));
        json.put("currency", Long.toUnsignedString(exchange.getCurrencyId()));
        json.put("offer", Long.toUnsignedString(exchange.getOfferId()));
        json.put("offerFullHash", Convert.toHexString(exchange.getOfferFullHash()));
        putAccount(json, "seller", exchange.getSellerId());
        putAccount(json, "buyer", exchange.getBuyerId());
        json.put("block", Long.toUnsignedString(exchange.getBlockId()));
        json.put("height", exchange.getHeight());
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, exchange.getCurrencyId());
        }
        return json;
    }

    static JSONObject exchangeRequest(ExchangeRequestHome.ExchangeRequest exchangeRequest, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(exchangeRequest.getFullHash()));
        json.put("subtype", exchangeRequest.isBuy() ? MonetarySystemTransactionType.EXCHANGE_BUY.getSubtype() : MonetarySystemTransactionType.EXCHANGE_SELL.getSubtype());
        json.put("timestamp", exchangeRequest.getTimestamp());
        json.put("unitsQNT", String.valueOf(exchangeRequest.getUnitsQNT()));
        json.put("rateNQTPerUnit", String.valueOf(exchangeRequest.getRateNQT()));
        json.put("height", exchangeRequest.getHeight());
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, exchangeRequest.getCurrencyId());
        }
        return json;
    }

    static JSONObject expectedExchangeRequest(Transaction transaction, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(transaction.getFullHash()));
        json.put("subtype", transaction.getType().getSubtype());
        ExchangeAttachment attachment = (ExchangeAttachment)transaction.getAttachment();
        json.put("unitsQNT", String.valueOf(attachment.getUnitsQNT()));
        json.put("rateNQTPerUnit", String.valueOf(attachment.getRateNQT()));
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, attachment.getCurrencyId());
        }
        putExpectedTransaction(json, transaction);
        return json;
    }

    public static JSONObject unconfirmedTransaction(Transaction transaction) {
        return unconfirmedTransaction(transaction, null);
    }

    static JSONObject unconfirmedTransaction(Transaction transaction, Filter<Appendix> filter) {
        JSONObject json = new JSONObject();
        json.put("type", transaction.getType().getType());
        json.put("subtype", transaction.getType().getSubtype());
        json.put("chain", transaction.getChain().getId());
        json.put("phased", transaction.isPhased());
        json.put("timestamp", transaction.getTimestamp());
        json.put("deadline", transaction.getDeadline());
        json.put("senderPublicKey", Convert.toHexString(transaction.getSenderPublicKey()));
        if (transaction.getRecipientId() != 0) {
            putAccount(json, "recipient", transaction.getRecipientId());
        }
        json.put("amountNQT", String.valueOf(transaction.getAmount()));
        json.put("feeNQT", String.valueOf(transaction.getFee()));
        if (transaction instanceof ChildTransaction) {
            ChainTransactionId referencedTransactionId = ((ChildTransaction)transaction).getReferencedTransactionId();
            if (referencedTransactionId != null) {
                json.put("referencedTransaction", referencedTransactionId.getJSON());
            }
            json.put("fxtTransaction", Long.toUnsignedString(((ChildTransaction) transaction).getFxtTransactionId()));
            if (transaction instanceof UnconfirmedTransaction) {
                json.put("isBundled", ((UnconfirmedTransaction)transaction).isBundled());
            }
        }
        byte[] signature = Convert.emptyToNull(transaction.getSignature());
        if (signature != null) {
            json.put("signature", Convert.toHexString(signature));
            json.put("signatureHash", Convert.toHexString(Crypto.sha256().digest(signature)));
            json.put("fullHash", Convert.toHexString(transaction.getFullHash()));
            if (transaction instanceof FxtTransaction) {
                json.put("transaction", Long.toUnsignedString(transaction.getId()));
            }
        }
        JSONObject attachmentJSON = new JSONObject();
        if (filter == null) {
            for (Appendix appendage : transaction.getAppendages(true)) {
                attachmentJSON.putAll(appendage.getJSONObject());
            }
        } else {
            for (Appendix appendage : transaction.getAppendages(filter, true)) {
                attachmentJSON.putAll(appendage.getJSONObject());
            }
        }
        if (! attachmentJSON.isEmpty()) {
            for (Map.Entry entry : (Iterable<Map.Entry>) attachmentJSON.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    entry.setValue(String.valueOf(entry.getValue()));
                }
            }
            json.put("attachment", attachmentJSON);
        }
        putAccount(json, "sender", transaction.getSenderId());
        json.put("height", transaction.getHeight());
        json.put("version", transaction.getVersion());
        json.put("ecBlockId", Long.toUnsignedString(transaction.getECBlockId()));
        json.put("ecBlockHeight", transaction.getECBlockHeight());

        return json;
    }

    public static JSONObject transaction(Transaction transaction) {
        return transaction(transaction, false);
    }

    static JSONObject transaction(Transaction transaction, boolean includePhasingResult) {
        JSONObject json = transaction(transaction, null);
        if (includePhasingResult && transaction.isPhased()) {
            PhasingPollHome.PhasingPollResult phasingPollResult = PhasingPollHome.getResult(transaction);
            if (phasingPollResult != null) {
                json.put("approved", phasingPollResult.isApproved());
                json.put("result", String.valueOf(phasingPollResult.getResult()));
                json.put("executionHeight", phasingPollResult.getHeight());
            }
        }
        return json;
    }

    static JSONObject transaction(Transaction transaction, Filter<Appendix> filter) {
        JSONObject json = unconfirmedTransaction(transaction, filter);
        json.put("block", Long.toUnsignedString(transaction.getBlockId()));
        json.put("confirmations", Nxt.getBlockchain().getHeight() - transaction.getHeight());
        json.put("blockTimestamp", transaction.getBlockTimestamp());
        json.put("transactionIndex", transaction.getIndex());
        return json;
    }

    static JSONObject fxtTransaction(FxtTransaction fxtTransaction, boolean includeChildTransactions) {
        JSONObject json = transaction(fxtTransaction);
        if (includeChildTransactions) {
            JSONArray childTransactions = new JSONArray();
            fxtTransaction.getChildTransactions().forEach(childTransaction -> {
                childTransactions.add(transaction(childTransaction));
            });
            json.put("childTransactions", childTransactions);
        }
        return json;
    }

    static JSONObject unconfirmedFxtTransaction(FxtTransaction fxtTransaction, boolean includeChildTransactions) {
        JSONObject json = unconfirmedTransaction(fxtTransaction);
        if (includeChildTransactions) {
            JSONArray childTransactions = new JSONArray();
            fxtTransaction.getChildTransactions().forEach(childTransaction -> {
                childTransactions.add(unconfirmedTransaction(childTransaction));
            });
            json.put("childTransactions", childTransactions);
        }
        return json;
    }

    static JSONObject generator(Generator generator, int elapsedTime) {
        JSONObject response = new JSONObject();
        long deadline = generator.getDeadline();
        putAccount(response, "account", generator.getAccountId());
        response.put("deadline", deadline);
        response.put("hitTime", generator.getHitTime());
        response.put("remaining", Math.max(deadline - elapsedTime, 0));
        return response;
    }

    public static JSONObject accountMonitor(FundingMonitor monitor, boolean includeMonitoredAccounts, boolean includeHoldingInfo) {
        JSONObject json = new JSONObject();
        json.put("chain", monitor.getChain().getId());
        json.put("holdingType", monitor.getHoldingType().getCode());
        json.put("account", Long.toUnsignedString(monitor.getAccountId()));
        json.put("accountRS", monitor.getAccountName());
        json.put("holding", Long.toUnsignedString(monitor.getHoldingId()));
        json.put("property", monitor.getProperty());
        json.put("amount", String.valueOf(monitor.getAmount()));
        json.put("threshold", String.valueOf(monitor.getThreshold()));
        json.put("interval", monitor.getInterval());
        json.put("feeRateNQTPerFXT", String.valueOf(monitor.getFeeRateNQTPerFXT()));
        if (includeMonitoredAccounts) {
            JSONArray jsonAccounts = new JSONArray();
            List<FundingMonitor.MonitoredAccount> accountList = FundingMonitor.getMonitoredAccounts(monitor);
            accountList.forEach(account -> jsonAccounts.add(JSONData.monitoredAccount(account)));
            json.put("monitoredAccounts", jsonAccounts);
        }
        if (includeHoldingInfo) {
            json.put("holdingInfo", holdingInfoJson(monitor.getHoldingType(), monitor.getHoldingId()));
        }
        return json;
    }

    public static JSONObject monitoredAccount(FundingMonitor.MonitoredAccount account) {
        JSONObject json = new JSONObject();
        json.put("account", Long.toUnsignedString(account.getAccountId()));
        json.put("accountRS", account.getAccountName());
        json.put("amount", String.valueOf(account.getAmount()));
        json.put("threshold", String.valueOf(account.getThreshold()));
        json.put("interval", account.getInterval());
        return json;
    }

    static JSONObject prunableMessage(PrunableMessageHome.PrunableMessage prunableMessage, String secretPhrase, byte[] sharedKey) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(prunableMessage.getFullHash()));
        if (prunableMessage.getMessage() == null || prunableMessage.getEncryptedData() == null) {
            json.put("isText", prunableMessage.getMessage() != null ? prunableMessage.messageIsText() : prunableMessage.encryptedMessageIsText());
        }
        putAccount(json, "sender", prunableMessage.getSenderId());
        if (prunableMessage.getRecipientId() != 0) {
            putAccount(json, "recipient", prunableMessage.getRecipientId());
        }
        json.put("transactionTimestamp", prunableMessage.getTransactionTimestamp());
        json.put("blockTimestamp", prunableMessage.getBlockTimestamp());
        EncryptedData encryptedData = prunableMessage.getEncryptedData();
        if (encryptedData != null) {
            json.put("encryptedMessage", encryptedData(prunableMessage.getEncryptedData()));
            json.put("encryptedMessageIsText", prunableMessage.encryptedMessageIsText());
            byte[] decrypted = null;
            try {
                if (secretPhrase != null) {
                    decrypted = prunableMessage.decrypt(secretPhrase);
                } else if (sharedKey != null && sharedKey.length > 0) {
                    decrypted = prunableMessage.decrypt(sharedKey);
                }
                if (decrypted != null) {
                    json.put("decryptedMessage", Convert.toString(decrypted, prunableMessage.encryptedMessageIsText()));
                }
            } catch (RuntimeException e) {
                putException(json, e, "Decryption failed");
            }
            json.put("isCompressed", prunableMessage.isCompressed());
        }
        if (prunableMessage.getMessage() != null) {
            json.put("message", Convert.toString(prunableMessage.getMessage(), prunableMessage.messageIsText()));
            json.put("messageIsText", prunableMessage.messageIsText());
        }
        return json;
    }

    static JSONObject taggedData(TaggedDataHome.TaggedData taggedData, boolean includeData) {
        JSONObject json = new JSONObject();
        json.put("transactionFullHash", Convert.toHexString(taggedData.getTransactionFullHash()));
        putAccount(json, "account", taggedData.getAccountId());
        json.put("name", taggedData.getName());
        json.put("description", taggedData.getDescription());
        json.put("tags", taggedData.getTags());
        JSONArray tagsJSON = new JSONArray();
        Collections.addAll(tagsJSON, taggedData.getParsedTags());
        json.put("parsedTags", tagsJSON);
        json.put("type", taggedData.getType());
        json.put("channel", taggedData.getChannel());
        json.put("filename", taggedData.getFilename());
        json.put("isText", taggedData.isText());
        if (includeData) {
            json.put("data", taggedData.isText() ? Convert.toString(taggedData.getData()) : Convert.toHexString(taggedData.getData()));
        }
        json.put("transactionTimestamp", taggedData.getTransactionTimestamp());
        json.put("blockTimestamp", taggedData.getBlockTimestamp());
        return json;
	}

    static JSONObject dataTag(TaggedDataHome.Tag tag) {
        JSONObject json = new JSONObject();
        json.put("tag", tag.getTag());
        json.put("count", tag.getCount());
        return json;
    }

    static JSONObject apiRequestHandler(APIServlet.APIRequestHandler handler) {
        JSONObject json = new JSONObject();
        json.put("allowRequiredBlockParameters", handler.allowRequiredBlockParameters());
        if (handler.getFileParameter() != null) {
            json.put("fileParameter", handler.getFileParameter());
        }
        json.put("requireBlockchain", handler.requireBlockchain());
        json.put("requirePost", handler.requirePost());
        json.put("requirePassword", handler.requirePassword());
        json.put("requireFullClient", handler.requireFullClient());
        return json;
    }

    public static JSONObject bundler(Bundler bundler) {
        JSONObject json = new JSONObject();
        putAccount(json, "bundler", bundler.getAccountId());
        json.put("chain", bundler.getChildChain().getId());
        json.put("totalFeesLimitFQT", String.valueOf(bundler.getTotalFeesLimitFQT()));
        json.put("currentTotalFeesFQT", String.valueOf(bundler.getCurrentTotalFeesFQT()));
        List<Bundler.Rule> bundlingRules = bundler.getBundlingRules();
        if (bundlingRules.size() == 1) {
            //return for backward compatibility
            json.put("minRateNQTPerFXT", String.valueOf(bundlingRules.get(0).getMinRateNQTPerFXT()));
            json.put("overpayFQTPerFXT", String.valueOf(bundlingRules.get(0).getOverpayFQTPerFXT()));
        }
        BundlerRate announcedRate = bundler.getBundlerRate();
        if (announcedRate != null) {
            json.put("announcedMinRateNQTPerFXT", String.valueOf(announcedRate.getRate()));
        }
        JSONArray rulesArray = new JSONArray();
        for (Bundler.Rule rule : bundlingRules) {
            rulesArray.add(bundlingRule(rule));
        }
        json.put("bundlingRules", rulesArray);
        return json;
    }

    static JSONObject bundlingRule(Bundler.Rule rule) {
        JSONObject json = new JSONObject();
        json.put("minRateNQTPerFXT", String.valueOf(rule.getMinRateNQTPerFXT()));
        json.put("feeCalculatorName", rule.getFeeCalculator().getName());
        if (rule.getOverpayFQTPerFXT() != 0) {
            json.put("overpayFQTPerFXT", String.valueOf(rule.getOverpayFQTPerFXT()));
        }
        List<Bundler.Filter> filters = rule.getFilters();
        if (!filters.isEmpty()) {
            JSONArray filtersJson = new JSONArray();
            filters.forEach(filter -> filtersJson.add(bundlingFilter(filter)));
            json.put("filters", filtersJson);
        }
        return json;
    }

    static JSONObject bundlingFilter(Bundler.Filter filter) {
        JSONObject json = new JSONObject();
        json.put("name", filter.getName());
        if (filter.getParameter() != null) {
            json.put("parameter", filter.getParameter());
        }
        return json;
    }

    public static JSONObject contractReference(ContractReference contractReference, boolean includeContract) {
        JSONObject json = new JSONObject();
        json.put("name", contractReference.getContractName());
        if (contractReference.getContractParams() != null) {
            json.put("setupParameters", contractReference.getContractParams());
        } else {
            json.put("setupParameters", "");
        }
        if (includeContract) {
            ContractAndSetupParameters contractAndSetupParameters = ContractLoader.loadContractAndSetupParameters(contractReference);
            json.put("contract", contract(contractAndSetupParameters.getContract()));
        } else {
            json.put("contract", contractReference.getContractId().getJSON());
        }
        json.put("id", Long.toUnsignedString(contractReference.getId()));
        return json;
    }

    public static JSONObject contract(Contract contract) {
        JSONObject json = new JSONObject();
        json.put("contractClass", contract.getClass().getCanonicalName());
        JA invocationTypes = new JA();
        Arrays.stream(contract.getClass().getDeclaredMethods())
                .flatMap(method -> INVOCATION_TYPE.getByMethodName(method.getName()).map(Stream::of).orElseGet(Stream::empty))
                .forEach(invocationType -> {
                    JO param = new JO();
                    param.put("type", invocationType.name());
                    JO stat = new JO();
                    JSONObject normal = stat(invocationType.getStatNormal(contract.getClass().getCanonicalName()));
                    stat.put("normal", normal);
                    JSONObject error = stat(invocationType.getStatErr(contract.getClass().getCanonicalName()));
                    stat.put("error", error);
                    param.put("stat", stat);
                    invocationTypes.add(param);
                });
        json.put("invocationTypes", invocationTypes);
        JA invocationParams = new JA();
        Class<?> parametersProvider = ContractLoader.getParametersProvider(contract);
        if (parametersProvider != null) {
            Method[] parameterMethods = parametersProvider.getDeclaredMethods();
            invocationParams.addAllJO(
                    Arrays.stream(parameterMethods)
                            .filter(method -> method.getDeclaredAnnotation(ContractInvocationParameter.class) != null)
                            .map(method -> {
                                JO param = new JO();
                                param.put("name", method.getName());
                                param.put("type", method.getReturnType().getCanonicalName());
                                return param;
                            })
                            .collect(Collectors.toList()));
        }
        json.put("supportedInvocationParams", invocationParams);
        JA validationAnnotations = new JA();
        for (Method m : contract.getClass().getDeclaredMethods()) {
            for (Annotation a : m.getDeclaredAnnotations()) {
                for (Annotation meta : a.annotationType().getDeclaredAnnotations()) {
                    if (meta.annotationType().equals(ValidationAnnotation.class)) {
                        JO annotationData = getAnnotationData(a);
                        annotationData.put("forMethod", m.getName());
                        validationAnnotations.add(annotationData);
                        break;
                    }
                }
            }
        }
        json.put("validityChecks", validationAnnotations);
        return json;
    }

    private static JSONObject stat(StatisticalSummary stat) {
        JO json = new JO();
        json.put("max", String.format(Locale.ROOT, "%.3f", stat.getMax() / 1e9));
        json.put("min", String.format(Locale.ROOT, "%.3f", stat.getMin()  / 1e9));
        json.put("average", String.format(Locale.ROOT, "%.3f", stat.getMean() / 1e9));
        json.put("count", stat.getN());
        json.put("std", String.format(Locale.ROOT, "%.3f", stat.getStandardDeviation() / 1e9));
        json.put("sum", String.format(Locale.ROOT, "%.3f", stat.getSum() / 1e9));
        json.put("variance", String.format(Locale.ROOT, "%.3f", stat.getVariance() / 1e18));
        return json.toJSONObject();
    }

    private static JO getAnnotationData(Annotation a) {
        JO annotationData = new JO();
        annotationData.put("name", a.annotationType().getSimpleName());
        if (a.annotationType() == ValidateTransactionType.class) {
            ValidateTransactionType validateTransactionType = (ValidateTransactionType) a;
            try {
                annotationData.put("accept", Arrays.stream(validateTransactionType.accept()).map(Enum::name).collect(Collectors.joining(",")));
                annotationData.put("reject", Arrays.stream(validateTransactionType.reject()).map(Enum::name).collect(Collectors.joining(",")));
            } catch (Exception e) {
                Logger.logInfoMessage("Cannot parse validation transaction type for old contract");
            }
        } else if (a.annotationType() == ValidateChain.class) {
            ValidateChain validateChain = (ValidateChain) a;
            annotationData.put("accept", Arrays.toString(validateChain.accept()));
            annotationData.put("reject", Arrays.toString(validateChain.reject()));
        }
        return annotationData;
    }

    static void putPrunableAttachment(JSONObject json, Transaction transaction) {
        JSONObject prunableAttachment = transaction.getPrunableAttachmentJSON();
        if (prunableAttachment != null) {
            json.put("prunableAttachmentJSON", prunableAttachment);
        }
    }

    static void putException(JSONObject json, Exception e) {
        putException(json, e, "");
    }

    static void putException(JSONObject json, Exception e, String error) {
        json.put("errorCode", 4);
        if (error.length() > 0) {
            error += ": ";
        }
        json.put("error", e.toString());
        json.put("errorDescription", error + e.getMessage());
    }

    public static void putAccount(JSONObject json, String name, long accountId) {
        json.put(name, Long.toUnsignedString(accountId));
        json.put(name + "RS", Convert.rsAccount(accountId));
    }

    private static void putCurrencyInfo(JSONObject json, long currencyId) {
        Currency currency = Currency.getCurrency(currencyId, true);
        json.put("name", currency.getName());
        json.put("code", currency.getCode());
        json.put("type", currency.getType());
        json.put("decimals", currency.getDecimals());
        json.put("issuanceHeight", currency.getIssuanceHeight());
        json.put("chain", currency.getChildChain().getId());
        putAccount(json, "issuerAccount", currency.getAccountId());
    }

    private static void putAssetInfo(JSONObject json, long assetId) {
        Asset asset = Asset.getAsset(assetId);
        json.put("name", asset.getName());
        json.put("decimals", asset.getDecimals());
    }

    private static void putChainInfo(JSONObject json, long chainId) {
        Chain chain = Chain.getChain((int)chainId);
        json.put("name", chain.getName());
        json.put("decimals", chain.getDecimals());
    }

    private static void putExpectedTransaction(JSONObject json, Transaction transaction) {
        json.put("height", Nxt.getBlockchain().getHeight() + 1);
        json.put("phased", transaction.isPhased());
        if (transaction.getBlockId() != 0) { // those values may be wrong for unconfirmed transactions
            json.put("transactionHeight", transaction.getHeight());
            json.put("confirmations", Nxt.getBlockchain().getHeight() - transaction.getHeight());
        }
        if (!json.containsKey("chain")) {
            json.put("chain", transaction.getChain().getId());
        }
    }

    static void ledgerEntry(JSONObject json, LedgerEntry entry, boolean includeTransactions, boolean includeHoldingInfo) {
        putAccount(json, "account", entry.getAccountId());
        json.put("ledgerId", Long.toUnsignedString(entry.getLedgerId()));
        json.put("block", Long.toUnsignedString(entry.getBlockId()));
        json.put("height", entry.getHeight());
        json.put("timestamp", entry.getTimestamp());
        json.put("eventType", entry.getEvent().name());
        json.put("event", Long.toUnsignedString(entry.getEventId()));
        byte[] eventHash = entry.getEventHash();
        if (eventHash != null) {
            json.put("eventHash", Convert.toHexString(eventHash));
        }
        json.put("chain", entry.getChainId());
        json.put("isTransactionEvent", entry.getEvent().isTransaction());
        json.put("change", String.valueOf(entry.getChange()));
        json.put("balance", String.valueOf(entry.getBalance()));
        AccountLedger.LedgerHolding ledgerHolding = entry.getHolding();
        json.put("holdingType", ledgerHolding.name());
        json.put("holdingTypeCode", ledgerHolding.getCode());
        json.put("holdingTypeIsUnconfirmed", ledgerHolding.isUnconfirmed());
        json.put("holding", Long.toUnsignedString(entry.getHoldingId()));
        if (includeHoldingInfo) {
            JSONObject holdingJson = null;
            if (ledgerHolding == AccountLedger.LedgerHolding.ASSET_BALANCE
                    || ledgerHolding == AccountLedger.LedgerHolding.UNCONFIRMED_ASSET_BALANCE) {
                holdingJson = new JSONObject();
                putAssetInfo(holdingJson, entry.getHoldingId());
            } else if (ledgerHolding == AccountLedger.LedgerHolding.CURRENCY_BALANCE
                    || ledgerHolding == AccountLedger.LedgerHolding.UNCONFIRMED_CURRENCY_BALANCE) {
                holdingJson = new JSONObject();
                putCurrencyInfo(holdingJson, entry.getHoldingId());
            }
            if (holdingJson != null) {
                json.put("holdingInfo", holdingJson);
            }
        }
        if (includeTransactions && entry.getEvent().isTransaction()) {
            Chain chain = Chain.getChain(entry.getChainId());
            Transaction transaction = Nxt.getBlockchain().getTransaction(chain, entry.getEventHash());
            json.put("transaction", JSONData.transaction(transaction));
        }
    }

    public static JSONObject standbyShuffler(StandbyShuffler standbyShuffler, boolean includeHoldingInfo) {
        JSONObject json = new JSONObject();
        json.put("account", Long.toUnsignedString(standbyShuffler.getAccountId()));
        json.put("accountRS", Convert.rsAccount(standbyShuffler.getAccountId()));
        json.put("chain", standbyShuffler.getChain().getId());
        HoldingType holdingType = standbyShuffler.getHoldingType();
        json.put("holdingType", holdingType.getCode());
        json.put("holding", Long.toUnsignedString(standbyShuffler.getHoldingId()));
        json.put("minAmount", String.valueOf(standbyShuffler.getMinAmount()));
        json.put("maxAmount", String.valueOf(standbyShuffler.getMaxAmount()));
        json.put("minParticipants", standbyShuffler.getMinParticipants());
        json.put("feeRateNQTPerFXT", standbyShuffler.getFeeRateNQTPerFXT());
        JSONArray publicKeys = new JSONArray();
        standbyShuffler.getRecipientPublicKeys().forEach(publicKey -> publicKeys.add(Convert.toHexString(publicKey)));
        json.put("recipientPublicKeys", publicKeys);
        if (includeHoldingInfo) {
            json.put("holdingInfo", holdingInfoJson(holdingType, standbyShuffler.getHoldingId()));
        }
        json.put("reservedPublicKeysCount", standbyShuffler.getReservedPublicKeysCount());
        return json;
    }

    private JSONData() {} // never

}
