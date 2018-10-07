// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ShufflingVerifyCall extends APICall.Builder<ShufflingVerifyCall> {
    private ShufflingVerifyCall() {
        super("shufflingVerify");
    }

    public static ShufflingVerifyCall create(int chain) {
        ShufflingVerifyCall instance = new ShufflingVerifyCall();
        instance.param("chain", chain);
        return instance;
    }

    public ShufflingVerifyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ShufflingVerifyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ShufflingVerifyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ShufflingVerifyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingVerifyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingVerifyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ShufflingVerifyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ShufflingVerifyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ShufflingVerifyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ShufflingVerifyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ShufflingVerifyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ShufflingVerifyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingVerifyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingVerifyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ShufflingVerifyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ShufflingVerifyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ShufflingVerifyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ShufflingVerifyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ShufflingVerifyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingVerifyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingVerifyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ShufflingVerifyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ShufflingVerifyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ShufflingVerifyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ShufflingVerifyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ShufflingVerifyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ShufflingVerifyCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingVerifyCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingVerifyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ShufflingVerifyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ShufflingVerifyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ShufflingVerifyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ShufflingVerifyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ShufflingVerifyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ShufflingVerifyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ShufflingVerifyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ShufflingVerifyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ShufflingVerifyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ShufflingVerifyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ShufflingVerifyCall chain(String chain) {
        return param("chain", chain);
    }

    public ShufflingVerifyCall chain(int chain) {
        return param("chain", chain);
    }

    public ShufflingVerifyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ShufflingVerifyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ShufflingVerifyCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingVerifyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingVerifyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingVerifyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ShufflingVerifyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ShufflingVerifyCall message(String message) {
        return param("message", message);
    }

    public ShufflingVerifyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingVerifyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingVerifyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ShufflingVerifyCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ShufflingVerifyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingVerifyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingVerifyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ShufflingVerifyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ShufflingVerifyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ShufflingVerifyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
