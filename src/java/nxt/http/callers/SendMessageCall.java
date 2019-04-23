// Auto generated code, do not modify
package nxt.http.callers;

public class SendMessageCall extends CreateTransactionCallBuilder<SendMessageCall> {
    private SendMessageCall() {
        super("sendMessage");
    }

    public static SendMessageCall create(int chain) {
        return new SendMessageCall().param("chain", chain);
    }
}
