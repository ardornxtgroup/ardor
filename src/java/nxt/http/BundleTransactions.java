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

import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.ChildBlockAttachment;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.UnconfirmedTransaction;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BundleTransactions extends CreateTransaction {

    static final BundleTransactions instance = new BundleTransactions();

    private BundleTransactions() {
        super(new APITag[] {APITag.FORGING, APITag.CREATE_TRANSACTION}, "transactionFullHash", "transactionFullHash", "transactionFullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Account account = ParameterParser.getSenderAccount(req);
        List<ChildTransaction> childTransactions = new ArrayList<>();
        String[] transactionFullHashesValues = req.getParameterValues("transactionFullHash");
        if (transactionFullHashesValues == null || transactionFullHashesValues.length == 0) {
            return JSONResponses.missing("transactionFullHash");
        }
        for (String s : transactionFullHashesValues) {
            byte[] hash = Convert.parseHexString(s);
            UnconfirmedTransaction unconfirmedTransaction = Nxt.getTransactionProcessor().getUnconfirmedTransaction(Convert.fullHashToId(hash));
            if (unconfirmedTransaction == null || !Arrays.equals(hash, unconfirmedTransaction.getFullHash())) {
                return JSONResponses.UNKNOWN_TRANSACTION_FULL_HASH;
            }
            if (! (unconfirmedTransaction.getTransaction() instanceof ChildTransaction)) {
                return JSONResponses.INCORRECT_TRANSACTION;
            }
            childTransactions.add((ChildTransaction)unconfirmedTransaction.getTransaction());
        }
        ChildBlockAttachment attachment = new ChildBlockAttachment(childTransactions);
        return createTransaction(req, account, attachment);
    }

}
