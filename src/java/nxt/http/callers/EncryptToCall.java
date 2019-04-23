// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class EncryptToCall extends APICall.Builder<EncryptToCall> {
    private EncryptToCall() {
        super("encryptTo");
    }

    public static EncryptToCall create() {
        return new EncryptToCall();
    }

    public EncryptToCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public EncryptToCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public EncryptToCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }
}
