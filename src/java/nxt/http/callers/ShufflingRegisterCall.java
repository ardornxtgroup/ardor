// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ShufflingRegisterCall extends APICall.Builder<ShufflingRegisterCall> {
    private ShufflingRegisterCall() {
        super("shufflingRegister");
    }

    public static ShufflingRegisterCall create(int chain) {
        ShufflingRegisterCall instance = new ShufflingRegisterCall();
        instance.param("chain", chain);
        return instance;
    }

    public ShufflingRegisterCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ShufflingRegisterCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ShufflingRegisterCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ShufflingRegisterCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ShufflingRegisterCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ShufflingRegisterCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ShufflingRegisterCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ShufflingRegisterCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingRegisterCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingRegisterCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ShufflingRegisterCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ShufflingRegisterCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ShufflingRegisterCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ShufflingRegisterCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ShufflingRegisterCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ShufflingRegisterCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ShufflingRegisterCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ShufflingRegisterCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingRegisterCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingRegisterCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ShufflingRegisterCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ShufflingRegisterCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ShufflingRegisterCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ShufflingRegisterCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ShufflingRegisterCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ShufflingRegisterCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ShufflingRegisterCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ShufflingRegisterCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ShufflingRegisterCall chain(String chain) {
        return param("chain", chain);
    }

    public ShufflingRegisterCall chain(int chain) {
        return param("chain", chain);
    }

    public ShufflingRegisterCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ShufflingRegisterCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ShufflingRegisterCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingRegisterCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingRegisterCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ShufflingRegisterCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ShufflingRegisterCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ShufflingRegisterCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingRegisterCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingRegisterCall message(String message) {
        return param("message", message);
    }

    public ShufflingRegisterCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingRegisterCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingRegisterCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ShufflingRegisterCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ShufflingRegisterCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingRegisterCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingRegisterCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ShufflingRegisterCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ShufflingRegisterCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ShufflingRegisterCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ShufflingRegisterCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ShufflingRegisterCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ShufflingRegisterCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ShufflingRegisterCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ShufflingRegisterCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ShufflingRegisterCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public ShufflingRegisterCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingRegisterCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
