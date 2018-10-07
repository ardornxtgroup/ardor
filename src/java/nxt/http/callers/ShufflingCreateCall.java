// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ShufflingCreateCall extends APICall.Builder<ShufflingCreateCall> {
    private ShufflingCreateCall() {
        super("shufflingCreate");
    }

    public static ShufflingCreateCall create(int chain) {
        ShufflingCreateCall instance = new ShufflingCreateCall();
        instance.param("chain", chain);
        return instance;
    }

    public ShufflingCreateCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ShufflingCreateCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ShufflingCreateCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ShufflingCreateCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingCreateCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingCreateCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ShufflingCreateCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ShufflingCreateCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ShufflingCreateCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ShufflingCreateCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ShufflingCreateCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ShufflingCreateCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingCreateCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingCreateCall participantCount(String participantCount) {
        return param("participantCount", participantCount);
    }

    public ShufflingCreateCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }

    public ShufflingCreateCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ShufflingCreateCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ShufflingCreateCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ShufflingCreateCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ShufflingCreateCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ShufflingCreateCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingCreateCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingCreateCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ShufflingCreateCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ShufflingCreateCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ShufflingCreateCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ShufflingCreateCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ShufflingCreateCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ShufflingCreateCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ShufflingCreateCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ShufflingCreateCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ShufflingCreateCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ShufflingCreateCall registrationPeriod(String registrationPeriod) {
        return param("registrationPeriod", registrationPeriod);
    }

    public ShufflingCreateCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ShufflingCreateCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ShufflingCreateCall holding(String holding) {
        return param("holding", holding);
    }

    public ShufflingCreateCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public ShufflingCreateCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ShufflingCreateCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ShufflingCreateCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ShufflingCreateCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ShufflingCreateCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ShufflingCreateCall amount(String amount) {
        return param("amount", amount);
    }

    public ShufflingCreateCall chain(String chain) {
        return param("chain", chain);
    }

    public ShufflingCreateCall chain(int chain) {
        return param("chain", chain);
    }

    public ShufflingCreateCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ShufflingCreateCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ShufflingCreateCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingCreateCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingCreateCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ShufflingCreateCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ShufflingCreateCall message(String message) {
        return param("message", message);
    }

    public ShufflingCreateCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingCreateCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingCreateCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ShufflingCreateCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ShufflingCreateCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingCreateCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingCreateCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ShufflingCreateCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ShufflingCreateCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ShufflingCreateCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
