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

package nxt.addons;

import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;

/**
 * Only bundle my own transactions. Should still set minRateNQTPerFXT = 0 when
 * starting the bundler.
 */
public class PersonalBundler implements Bundler.Filter {

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        return bundler.getAccountId() == childTransaction.getSenderId();
    }

    @Override
    public String getName() {
        return "PersonalBundler";
    }

    @Override
    public String getDescription() {
        return "Only bundle the transactions of the bundler account";
    }
}
