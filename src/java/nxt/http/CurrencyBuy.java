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

package nxt.http;

import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.ms.Currency;
import nxt.ms.ExchangeBuyAttachment;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NO_COST_ORDER;

/**
 * Buy currency for coin
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id
 * <li>rateNQTPerUnit - exchange rate between coin amount and currency units
 * <li>unitsQNT - number of units to buy
 * </ul>
 *
 * <p>
 * The currency buy transaction attempts to match existing exchange offers. When a match is found, the minimum number of units
 * between the number of units offered and the units requested are exchanged at a rate matching the highest sell offer<br>
 * A single transaction can match multiple sell offers or none.
 * Unlike asset bid order, currency buy is not saved. It's either executed immediately (fully or partially) or not executed
 * at all.
 * For every match between buyer and seller an exchange record is saved, exchange records can be retrieved using the {@link GetExchanges} API
 */
public final class CurrencyBuy extends CreateTransaction {

    static final CurrencyBuy instance = new CurrencyBuy();

    private CurrencyBuy() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "rateNQTPerUnit", "unitsQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Currency currency = ParameterParser.getCurrency(req);
        long rateNQT = ParameterParser.getRateNQTPerUnit(req);
        long unitsQNT = ParameterParser.getUnitsQNT(req);
        Account account = ParameterParser.getSenderAccount(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        long amount = Convert.unitRateToAmount(unitsQNT, currency.getDecimals(), rateNQT, childChain.getDecimals());
        if (amount == 0) {
            return NO_COST_ORDER;
        }

        Attachment attachment = new ExchangeBuyAttachment(currency.getId(), rateNQT, unitsQNT);
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
    }

}
