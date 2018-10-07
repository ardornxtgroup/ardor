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

package nxt.addons;

import nxt.account.Account;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;
import nxt.db.DbIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Only bundle transactions sent by accounts which has the "bundling" property set on it by the bundler
 */
public class AccountPropertyBundler implements Bundler.Filter {

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        try (DbIterator<Account.AccountProperty> iterator = Account.getProperties(childTransaction.getSenderId(), bundler.getAccountId(), "bundling", 0, Integer.MAX_VALUE)) {
            return iterator.hasNext();
        }
    }

    @Override
    public String getName() {
        return "PropertyBundler";
    }

    @Override
    public String getDescription() {
        return "Only bundle transactions sent by accounts which have the \"bundling\" property set on them by the bundler account";
    }
}
