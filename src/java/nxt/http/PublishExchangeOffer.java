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
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Attachment;
import nxt.ms.Currency;
import nxt.ms.CurrencyType;
import nxt.ms.PublishExchangeOfferAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Publish exchange offer for {@link CurrencyType#EXCHANGEABLE} currency
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id of an active currency
 * <li>buyRateNQTPerUnit - Coin amount for buying a currency unit specified in NQT
 * <li>sellRateNQTPerUnit - Coin amount for selling a currency unit specified in NQT
 * <li>initialBuySupplyQNT - Initial number of currency units offered to buy by the publisher
 * <li>initialSellSupplyQNT - Initial number of currency units offered for sell by the publisher
 * <li>totalBuyLimitQNT - Total number of currency units which can be bought from the offer
 * <li>totalSellLimitQNT - Total number of currency units which can be sold from the offer
 * <li>expirationHeight - Blockchain height at which the offer is expired
 * </ul>
 *
 * <p>
 * Publishing an exchange offer internally creates a buy offer and a counter sell offer linked together.
 * Typically the buyRateNQTPerUnit specified would be less than the sellRateNQTPerUnit thus allowing the publisher to make profit
 *
 * <p>
 * Each {@link CurrencyBuy} transaction which matches this offer reduces the sell supply and increases the buy supply
 * Similarly, each {@link CurrencySell} transaction which matches this offer reduces the buy supply and increases the sell supply
 * Therefore the multiple buy/sell transaction can be issued against this offer during it's lifetime.
 * However, the total buy limit and sell limit stops exchanging based on this offer after the accumulated buy/sell limit is reached
 * after possibly multiple exchange operations.
 *
 * <p>
 * Only one exchange offer is allowed per account. Publishing a new exchange offer when another exchange offer exists
 * for the account, removes the existing exchange offer and publishes the new exchange offer
 */
public final class PublishExchangeOffer extends CreateTransaction {

    static final PublishExchangeOffer instance = new PublishExchangeOffer();

    private PublishExchangeOffer() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "buyRateNQTPerUnit", "sellRateNQTPerUnit",
                "totalBuyLimitQNT", "totalSellLimitQNT", "initialBuySupplyQNT", "initialSellSupplyQNT", "expirationHeight");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Currency currency = ParameterParser.getCurrency(req);
        long buyRateNQT = ParameterParser.getLong(req, "buyRateNQTPerUnit", 1, Constants.MAX_BALANCE_NQT, true);
        long sellRateNQT= ParameterParser.getLong(req, "sellRateNQTPerUnit", 1, Constants.MAX_BALANCE_NQT, true);
        long totalBuyLimit = ParameterParser.getLong(req, "totalBuyLimitQNT", 0, Constants.MAX_CURRENCY_TOTAL_SUPPLY, true);
        long totalSellLimit = ParameterParser.getLong(req, "totalSellLimitQNT", 0, Constants.MAX_CURRENCY_TOTAL_SUPPLY, true);
        long initialBuySupply = ParameterParser.getLong(req, "initialBuySupplyQNT", 0, Constants.MAX_CURRENCY_TOTAL_SUPPLY, true);
        long initialSellSupply = ParameterParser.getLong(req, "initialSellSupplyQNT", 0, Constants.MAX_CURRENCY_TOTAL_SUPPLY, true);
        int expirationHeight = ParameterParser.getInt(req, "expirationHeight", 0, Integer.MAX_VALUE, true);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new PublishExchangeOfferAttachment(currency.getId(), buyRateNQT, sellRateNQT,
                totalBuyLimit, totalSellLimit, initialBuySupply, initialSellSupply, expirationHeight);
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
    }

}
