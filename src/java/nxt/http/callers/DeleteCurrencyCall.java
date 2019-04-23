// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteCurrencyCall extends CreateTransactionCallBuilder<DeleteCurrencyCall> {
    private DeleteCurrencyCall() {
        super("deleteCurrency");
    }

    public static DeleteCurrencyCall create(int chain) {
        return new DeleteCurrencyCall().param("chain", chain);
    }

    public DeleteCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public DeleteCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
