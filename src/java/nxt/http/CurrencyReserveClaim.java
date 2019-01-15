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
import nxt.ms.Currency;
import nxt.ms.CurrencyType;
import nxt.ms.ReserveClaimAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Claim currency units and receive back coin invested into this currency before it became active
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id
 * <li>unitsQNT - the number of currency units claimed<br>
 * This value is multiplied by current currency rate and the result is added to the sender coin account balance.
 * </ul>
 * <p>
 * Constraints
 * <p>This transaction is allowed only when the currency is {@link CurrencyType#CLAIMABLE} and is already active.<br>
 */
public final class CurrencyReserveClaim extends CreateTransaction {

    static final CurrencyReserveClaim instance = new CurrencyReserveClaim();

    private CurrencyReserveClaim() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "unitsQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Currency currency = ParameterParser.getCurrency(req);
        long units = ParameterParser.getUnitsQNT(req);
        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = new ReserveClaimAttachment(currency.getId(), units);
        return createTransaction(req, account, attachment);

    }

}
