// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllBundlerRatesCall extends APICall.Builder<GetAllBundlerRatesCall> {
    private GetAllBundlerRatesCall() {
        super(ApiSpec.getAllBundlerRates);
    }

    public static GetAllBundlerRatesCall create() {
        return new GetAllBundlerRatesCall();
    }

    public GetAllBundlerRatesCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }
}
