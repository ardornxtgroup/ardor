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

package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.RequestContext;

public class ContractWithInnerInterface extends AbstractContract {

    public interface Inner {
        String addPrefix(String text);
    }

    public class InnerImpl implements Inner {
        @Override
        public String addPrefix(String text) {
            return "prefix_" + text;
        }
    }

    @Override
    public JO processRequest(RequestContext context) {
        Inner inner = new InnerImpl();
        JO resp = new JO();
        resp.put("text", inner.addPrefix("myMessage"));
        return context.generateResponse(resp);
    }
}