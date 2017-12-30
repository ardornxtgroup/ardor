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

import nxt.blockchain.ChildChain;
import nxt.ms.ExchangeOfferHome;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAvailableToBuy extends APIServlet.APIRequestHandler {

    static final GetAvailableToBuy instance = new GetAvailableToBuy();

    private GetAvailableToBuy() {
        super(new APITag[] {APITag.MS}, "currency", "unitsQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long currencyId = ParameterParser.getUnsignedLong(req, "currency", true);
        long units = ParameterParser.getUnitsQNT(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        ExchangeOfferHome.AvailableOffers availableOffers = childChain.getExchangeOfferHome().getAvailableToBuy(currencyId, units);
        return JSONData.availableOffers(availableOffers);
    }

}
