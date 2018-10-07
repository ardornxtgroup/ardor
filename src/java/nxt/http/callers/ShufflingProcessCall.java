// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ShufflingProcessCall extends APICall.Builder<ShufflingProcessCall> {
    private ShufflingProcessCall() {
        super("shufflingProcess");
    }

    public static ShufflingProcessCall create(int chain) {
        ShufflingProcessCall instance = new ShufflingProcessCall();
        instance.param("chain", chain);
        return instance;
    }

    public ShufflingProcessCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ShufflingProcessCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ShufflingProcessCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ShufflingProcessCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingProcessCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingProcessCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ShufflingProcessCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ShufflingProcessCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ShufflingProcessCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ShufflingProcessCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ShufflingProcessCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ShufflingProcessCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingProcessCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingProcessCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ShufflingProcessCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ShufflingProcessCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ShufflingProcessCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ShufflingProcessCall recipientSecretPhrase(String recipientSecretPhrase) {
        return param("recipientSecretPhrase", recipientSecretPhrase);
    }

    public ShufflingProcessCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ShufflingProcessCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingProcessCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingProcessCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ShufflingProcessCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ShufflingProcessCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ShufflingProcessCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ShufflingProcessCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ShufflingProcessCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ShufflingProcessCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingProcessCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingProcessCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ShufflingProcessCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ShufflingProcessCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ShufflingProcessCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ShufflingProcessCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ShufflingProcessCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ShufflingProcessCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ShufflingProcessCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ShufflingProcessCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ShufflingProcessCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ShufflingProcessCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ShufflingProcessCall chain(String chain) {
        return param("chain", chain);
    }

    public ShufflingProcessCall chain(int chain) {
        return param("chain", chain);
    }

    public ShufflingProcessCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ShufflingProcessCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ShufflingProcessCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ShufflingProcessCall recipientPublicKey(String... recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingProcessCall recipientPublicKey(byte[]... recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingProcessCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ShufflingProcessCall message(String message) {
        return param("message", message);
    }

    public ShufflingProcessCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingProcessCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingProcessCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ShufflingProcessCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ShufflingProcessCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingProcessCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingProcessCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ShufflingProcessCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ShufflingProcessCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ShufflingProcessCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
