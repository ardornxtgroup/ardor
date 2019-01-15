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

import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.ms.Currency;
import nxt.ms.ExchangeSellAttachment;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NO_COST_ORDER;

/**
 * Sell currency for coin
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id
 * <li>rateNQTPerUnit - exchange rate between coin amount and currency units
 * <li>unitsQNT - number of units to sell
 * </ul>
 *
 * <p>
 * The currency sell transaction attempts to match existing exchange offers. When a match is found, the minimum number of units
 * between the number of units offered and the units requested are exchanged at a rate matching the lowest buy offer<br>
 * A single transaction can match multiple buy offers or none.
 * Unlike asset ask order, currency sell is not saved. It's either executed immediately (fully or partially) or not executed
 * at all.
 * For every match between buyer and seller an exchange record is saved, exchange records can be retrieved using the {@link GetExchanges} API
 */
public final class CurrencySell extends CreateTransaction {

    static final CurrencySell instance = new CurrencySell();

    private CurrencySell() {
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

        Attachment attachment = new ExchangeSellAttachment(currency.getId(), rateNQT, unitsQNT);
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_CURRENCY;
        }
    }

    @Override
    String getDocsUrlPath() {
        return "Monetary_System#Currency_Buy_.2F_Sell";
    }
}
