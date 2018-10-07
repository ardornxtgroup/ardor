// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class UploadTaggedDataCall extends APICall.Builder<UploadTaggedDataCall> {
    private UploadTaggedDataCall() {
        super("uploadTaggedData");
    }

    public static UploadTaggedDataCall create(int chain) {
        UploadTaggedDataCall instance = new UploadTaggedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public UploadTaggedDataCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public UploadTaggedDataCall data(String data) {
        return param("data", data);
    }

    public UploadTaggedDataCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public UploadTaggedDataCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public UploadTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public UploadTaggedDataCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public UploadTaggedDataCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public UploadTaggedDataCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public UploadTaggedDataCall type(int type) {
        return param("type", type);
    }

    public UploadTaggedDataCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public UploadTaggedDataCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public UploadTaggedDataCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public UploadTaggedDataCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public UploadTaggedDataCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public UploadTaggedDataCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public UploadTaggedDataCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public UploadTaggedDataCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public UploadTaggedDataCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public UploadTaggedDataCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public UploadTaggedDataCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public UploadTaggedDataCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public UploadTaggedDataCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public UploadTaggedDataCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public UploadTaggedDataCall tags(String tags) {
        return param("tags", tags);
    }

    public UploadTaggedDataCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public UploadTaggedDataCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public UploadTaggedDataCall filename(String filename) {
        return param("filename", filename);
    }

    public UploadTaggedDataCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public UploadTaggedDataCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public UploadTaggedDataCall name(String name) {
        return param("name", name);
    }

    public UploadTaggedDataCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public UploadTaggedDataCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public UploadTaggedDataCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public UploadTaggedDataCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public UploadTaggedDataCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public UploadTaggedDataCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public UploadTaggedDataCall description(String description) {
        return param("description", description);
    }

    public UploadTaggedDataCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public UploadTaggedDataCall phased(boolean phased) {
        return param("phased", phased);
    }

    public UploadTaggedDataCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public UploadTaggedDataCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public UploadTaggedDataCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public UploadTaggedDataCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public UploadTaggedDataCall isText(boolean isText) {
        return param("isText", isText);
    }

    public UploadTaggedDataCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public UploadTaggedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public UploadTaggedDataCall chain(int chain) {
        return param("chain", chain);
    }

    public UploadTaggedDataCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public UploadTaggedDataCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public UploadTaggedDataCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public UploadTaggedDataCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public UploadTaggedDataCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public UploadTaggedDataCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public UploadTaggedDataCall message(String message) {
        return param("message", message);
    }

    public UploadTaggedDataCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public UploadTaggedDataCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public UploadTaggedDataCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public UploadTaggedDataCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public UploadTaggedDataCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public UploadTaggedDataCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public UploadTaggedDataCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public UploadTaggedDataCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public UploadTaggedDataCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public UploadTaggedDataCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public APICall.Builder file(byte[] b) {
        return parts("file", b);
    }
}
