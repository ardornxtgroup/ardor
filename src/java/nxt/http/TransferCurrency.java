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
import nxt.ms.Currency;
import nxt.ms.CurrencyTransferAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NOT_ENOUGH_CURRENCY;

public final class TransferCurrency extends CreateTransaction {

    static final TransferCurrency instance = new TransferCurrency();

    private TransferCurrency() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "recipient", "currency", "unitsQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long recipient = ParameterParser.getAccountId(req, "recipient", true);

        Currency currency = ParameterParser.getCurrency(req);
        long unitsQNT = ParameterParser.getUnitsQNT(req);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new CurrencyTransferAttachment(currency.getId(), unitsQNT);
        try {
            return createTransaction(req, account, recipient, 0, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return NOT_ENOUGH_CURRENCY;
        }
    }

}
