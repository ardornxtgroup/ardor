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
