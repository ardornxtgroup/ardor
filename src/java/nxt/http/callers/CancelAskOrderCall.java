// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CancelAskOrderCall extends APICall.Builder<CancelAskOrderCall> {
    private CancelAskOrderCall() {
        super("cancelAskOrder");
    }

    public static CancelAskOrderCall create(int chain) {
        CancelAskOrderCall instance = new CancelAskOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public CancelAskOrderCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CancelAskOrderCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CancelAskOrderCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CancelAskOrderCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CancelAskOrderCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CancelAskOrderCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CancelAskOrderCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CancelAskOrderCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelAskOrderCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelAskOrderCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CancelAskOrderCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CancelAskOrderCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CancelAskOrderCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CancelAskOrderCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CancelAskOrderCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CancelAskOrderCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CancelAskOrderCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CancelAskOrderCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelAskOrderCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelAskOrderCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CancelAskOrderCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CancelAskOrderCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CancelAskOrderCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CancelAskOrderCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CancelAskOrderCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CancelAskOrderCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CancelAskOrderCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CancelAskOrderCall order(String order) {
        return param("order", order);
    }

    public CancelAskOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }

    public CancelAskOrderCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CancelAskOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public CancelAskOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public CancelAskOrderCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CancelAskOrderCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CancelAskOrderCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelAskOrderCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelAskOrderCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CancelAskOrderCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CancelAskOrderCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CancelAskOrderCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelAskOrderCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelAskOrderCall message(String message) {
        return param("message", message);
    }

    public CancelAskOrderCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelAskOrderCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelAskOrderCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CancelAskOrderCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CancelAskOrderCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelAskOrderCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelAskOrderCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CancelAskOrderCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CancelAskOrderCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CancelAskOrderCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CancelAskOrderCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CancelAskOrderCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CancelAskOrderCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CancelAskOrderCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CancelAskOrderCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CancelAskOrderCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
