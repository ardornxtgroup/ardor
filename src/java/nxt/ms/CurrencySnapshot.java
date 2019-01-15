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

package nxt.ms;

import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.db.DbIterator;
import nxt.migration.HoldingSnapshot;

import java.util.Map;
import java.util.stream.Collectors;

class CurrencySnapshot extends HoldingSnapshot {
    CurrencySnapshot() {
        super(HoldingType.CURRENCY);
    }

    @Override
    protected Map<String, Long> takeSnapshot(long holdingId) {
        try (DbIterator<Account.AccountCurrency> accounts = Account.getCurrencyAccounts(holdingId, -1, -1)) {
            return accounts.stream()
                    .collect(Collectors.toMap(
                            account -> Long.toUnsignedString(account.getAccountId()),
                            Account.AccountCurrency::getUnits));
        }
    }
}
