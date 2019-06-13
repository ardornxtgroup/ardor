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

import nxt.NxtException;
import nxt.http.responses.TransactionResponse;

import java.util.List;

public interface Contract<InvocationData, ReturnedData> {
    JO processBlock(BlockContext context);
    JO processTransaction(TransactionContext context);
    JO processRequest(RequestContext context) throws NxtException;
    JO processVoucher(VoucherContext context);
    ReturnedData processInvocation(DelegatedContext context, InvocationData data);

    <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions);

    default String minProductVersion() {
        return "0.0.0";
    }
}
