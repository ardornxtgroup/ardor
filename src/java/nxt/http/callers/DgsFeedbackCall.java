// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DgsFeedbackCall extends APICall.Builder<DgsFeedbackCall> {
    private DgsFeedbackCall() {
        super("dgsFeedback");
    }

    public static DgsFeedbackCall create(int chain) {
        DgsFeedbackCall instance = new DgsFeedbackCall();
        instance.param("chain", chain);
        return instance;
    }

    public DgsFeedbackCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DgsFeedbackCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DgsFeedbackCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DgsFeedbackCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DgsFeedbackCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DgsFeedbackCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DgsFeedbackCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DgsFeedbackCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsFeedbackCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsFeedbackCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DgsFeedbackCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DgsFeedbackCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DgsFeedbackCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DgsFeedbackCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DgsFeedbackCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DgsFeedbackCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DgsFeedbackCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DgsFeedbackCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsFeedbackCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsFeedbackCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DgsFeedbackCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DgsFeedbackCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DgsFeedbackCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DgsFeedbackCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DgsFeedbackCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DgsFeedbackCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DgsFeedbackCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DgsFeedbackCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DgsFeedbackCall chain(String chain) {
        return param("chain", chain);
    }

    public DgsFeedbackCall chain(int chain) {
        return param("chain", chain);
    }

    public DgsFeedbackCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DgsFeedbackCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DgsFeedbackCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsFeedbackCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsFeedbackCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DgsFeedbackCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DgsFeedbackCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsFeedbackCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }

    public DgsFeedbackCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DgsFeedbackCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsFeedbackCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsFeedbackCall message(String message) {
        return param("message", message);
    }

    public DgsFeedbackCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsFeedbackCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsFeedbackCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DgsFeedbackCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DgsFeedbackCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsFeedbackCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsFeedbackCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DgsFeedbackCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DgsFeedbackCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DgsFeedbackCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DgsFeedbackCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DgsFeedbackCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DgsFeedbackCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DgsFeedbackCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DgsFeedbackCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DgsFeedbackCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
