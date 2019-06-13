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
import nxt.addons.ContractInvocationParameter;
import nxt.addons.ContractParametersProvider;
import nxt.addons.JO;
import nxt.addons.RandomnessSource;
import nxt.addons.RequestContext;

public class GetRandomNumber extends AbstractContract {

    @ContractParametersProvider
    public interface Params {

        @ContractInvocationParameter
        default int seed() {
            return 0;
        }
    }

    @Override
    public JO processRequest(RequestContext context) {
        Params params = context.getParams(Params.class);
        RandomnessSource r = context.initRandom(params.seed());
        JO response = new JO();
        response.put("random", r.nextInt(1000));
        return response;
    }
}
