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

package nxt.http;

import nxt.NxtException;
import nxt.blockchain.ChildChain;
import nxt.ms.Currency;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.MISSING_CURRENCY;
import static nxt.http.JSONResponses.UNKNOWN_CURRENCY;

public final class GetCurrency extends APIServlet.APIRequestHandler {

    static final GetCurrency instance = new GetCurrency();

    private GetCurrency() {
        super(new APITag[] {APITag.MS}, "currency", "code", "includeCounts", "includeDeleted");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        boolean includeDeleted = "true".equalsIgnoreCase(req.getParameter("includeDeleted"));
        long currencyId = ParameterParser.getUnsignedLong(req, "currency", false);
        Currency currency;
        if (currencyId == 0) {
            String currencyCode = Convert.emptyToNull(req.getParameter("code"));
            if (currencyCode == null || includeDeleted) {
                return MISSING_CURRENCY;
            }
            ChildChain childChain = ParameterParser.getChildChain(req);
            currency = Currency.getCurrencyByCode(childChain, currencyCode);
        } else {
            currency = Currency.getCurrency(currencyId, includeDeleted);
        }
        if (currency == null) {
            throw new ParameterException(UNKNOWN_CURRENCY);
        }
        return JSONData.currency(currency, includeCounts);
    }

}
