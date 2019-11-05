// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBundlerRatesCall extends APICall.Builder<GetBundlerRatesCall> {
    private GetBundlerRatesCall() {
        super(ApiSpec.getBundlerRates);
    }

    public static GetBundlerRatesCall create() {
        return new GetBundlerRatesCall();
    }

    public GetBundlerRatesCall minBundlerFeeLimitFQT(long minBundlerFeeLimitFQT) {
        return param("minBundlerFeeLimitFQT", minBundlerFeeLimitFQT);
    }

    public GetBundlerRatesCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }
}
