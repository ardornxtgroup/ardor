// Auto generated code, do not modify
package nxt.http.callers;

public class SetAccountPropertyCall extends CreateTransactionCallBuilder<SetAccountPropertyCall> {
    private SetAccountPropertyCall() {
        super(ApiSpec.setAccountProperty);
    }

    public static SetAccountPropertyCall create(int chain) {
        return new SetAccountPropertyCall().param("chain", chain);
    }

    public SetAccountPropertyCall property(String property) {
        return param("property", property);
    }

    public SetAccountPropertyCall value(String value) {
        return param("value", value);
    }
}
