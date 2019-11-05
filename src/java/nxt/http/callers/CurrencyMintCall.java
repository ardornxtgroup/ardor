// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyMintCall extends CreateTransactionCallBuilder<CurrencyMintCall> {
    private CurrencyMintCall() {
        super(ApiSpec.currencyMint);
    }

    public static CurrencyMintCall create(int chain) {
        return new CurrencyMintCall().param("chain", chain);
    }

    public CurrencyMintCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyMintCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyMintCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyMintCall counter(String counter) {
        return param("counter", counter);
    }

    public CurrencyMintCall nonce(String nonce) {
        return param("nonce", nonce);
    }
}
