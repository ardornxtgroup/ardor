// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAccountPropertyCall extends CreateTransactionCallBuilder<DeleteAccountPropertyCall> {
    private DeleteAccountPropertyCall() {
        super("deleteAccountProperty");
    }

    public static DeleteAccountPropertyCall create(int chain) {
        return new DeleteAccountPropertyCall().param("chain", chain);
    }

    public DeleteAccountPropertyCall property(String property) {
        return param("property", property);
    }

    public DeleteAccountPropertyCall setter(String setter) {
        return param("setter", setter);
    }

    public DeleteAccountPropertyCall setter(long setter) {
        return unsignedLongParam("setter", setter);
    }
}
