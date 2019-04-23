// Auto generated code, do not modify
package nxt.http.callers;

public class IssueCurrencyCall extends CreateTransactionCallBuilder<IssueCurrencyCall> {
    private IssueCurrencyCall() {
        super("issueCurrency");
    }

    public static IssueCurrencyCall create(int chain) {
        return new IssueCurrencyCall().param("chain", chain);
    }

    public IssueCurrencyCall code(String code) {
        return param("code", code);
    }

    public IssueCurrencyCall minDifficulty(String minDifficulty) {
        return param("minDifficulty", minDifficulty);
    }

    public IssueCurrencyCall ruleset(String ruleset) {
        return param("ruleset", ruleset);
    }

    public IssueCurrencyCall minReservePerUnitNQT(long minReservePerUnitNQT) {
        return param("minReservePerUnitNQT", minReservePerUnitNQT);
    }

    public IssueCurrencyCall description(String description) {
        return param("description", description);
    }

    public IssueCurrencyCall initialSupplyQNT(long initialSupplyQNT) {
        return param("initialSupplyQNT", initialSupplyQNT);
    }

    public IssueCurrencyCall issuanceHeight(boolean issuanceHeight) {
        return param("issuanceHeight", issuanceHeight);
    }

    public IssueCurrencyCall type(int type) {
        return param("type", type);
    }

    public IssueCurrencyCall maxSupplyQNT(long maxSupplyQNT) {
        return param("maxSupplyQNT", maxSupplyQNT);
    }

    public IssueCurrencyCall maxDifficulty(String maxDifficulty) {
        return param("maxDifficulty", maxDifficulty);
    }

    public IssueCurrencyCall decimals(String decimals) {
        return param("decimals", decimals);
    }

    public IssueCurrencyCall reserveSupplyQNT(long reserveSupplyQNT) {
        return param("reserveSupplyQNT", reserveSupplyQNT);
    }

    public IssueCurrencyCall name(String name) {
        return param("name", name);
    }

    public IssueCurrencyCall algorithm(String algorithm) {
        return param("algorithm", algorithm);
    }
}
