// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class VerifyPrunableMessageCall extends APICall.Builder<VerifyPrunableMessageCall> {
    private VerifyPrunableMessageCall() {
        super(ApiSpec.verifyPrunableMessage);
    }

    public static VerifyPrunableMessageCall create(int chain) {
        return new VerifyPrunableMessageCall().param("chain", chain);
    }

    public VerifyPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public VerifyPrunableMessageCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public VerifyPrunableMessageCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public VerifyPrunableMessageCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public VerifyPrunableMessageCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public VerifyPrunableMessageCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public VerifyPrunableMessageCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public VerifyPrunableMessageCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public VerifyPrunableMessageCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public VerifyPrunableMessageCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public VerifyPrunableMessageCall message(String message) {
        return param("message", message);
    }

    public VerifyPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
