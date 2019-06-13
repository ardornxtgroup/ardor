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

package nxt.addons;

import nxt.http.responses.TransactionResponse;

import java.util.List;

public abstract class AbstractContract<InvocationData, ReturnedData> implements Contract<InvocationData, ReturnedData> {
    @Override
    public JO processBlock(BlockContext context) { throw new UnsupportedOperationException(); }

    @Override
    public JO processTransaction(TransactionContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JO processRequest(RequestContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JO processVoucher(VoucherContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReturnedData processInvocation(DelegatedContext context, InvocationData data) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processInvocation operation", getClass().getCanonicalName()));
        context.generateResponse(jo);
        return null;
    }

    @Override
    public <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions) {
        for (TransactionResponse transactionResponse : existingUnconfirmedTransactions) {
            // Check for byte to byte equality
            if (transactionResponse.equals(myTransaction)) {
                return true;
            }
        }
        return false;
    }
}
