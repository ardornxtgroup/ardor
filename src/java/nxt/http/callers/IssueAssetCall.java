// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class IssueAssetCall extends APICall.Builder<IssueAssetCall> {
    private IssueAssetCall() {
        super("issueAsset");
    }

    public static IssueAssetCall create(int chain) {
        IssueAssetCall instance = new IssueAssetCall();
        instance.param("chain", chain);
        return instance;
    }

    public IssueAssetCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public IssueAssetCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public IssueAssetCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public IssueAssetCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public IssueAssetCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public IssueAssetCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public IssueAssetCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public IssueAssetCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public IssueAssetCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public IssueAssetCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public IssueAssetCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public IssueAssetCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IssueAssetCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IssueAssetCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public IssueAssetCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public IssueAssetCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public IssueAssetCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public IssueAssetCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public IssueAssetCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public IssueAssetCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IssueAssetCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IssueAssetCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public IssueAssetCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public IssueAssetCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public IssueAssetCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public IssueAssetCall name(String name) {
        return param("name", name);
    }

    public IssueAssetCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public IssueAssetCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public IssueAssetCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public IssueAssetCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public IssueAssetCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public IssueAssetCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public IssueAssetCall description(String description) {
        return param("description", description);
    }

    public IssueAssetCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public IssueAssetCall phased(boolean phased) {
        return param("phased", phased);
    }

    public IssueAssetCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public IssueAssetCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public IssueAssetCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public IssueAssetCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public IssueAssetCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public IssueAssetCall chain(String chain) {
        return param("chain", chain);
    }

    public IssueAssetCall chain(int chain) {
        return param("chain", chain);
    }

    public IssueAssetCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public IssueAssetCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public IssueAssetCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IssueAssetCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IssueAssetCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public IssueAssetCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public IssueAssetCall message(String message) {
        return param("message", message);
    }

    public IssueAssetCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IssueAssetCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IssueAssetCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public IssueAssetCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public IssueAssetCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IssueAssetCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IssueAssetCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public IssueAssetCall decimals(String decimals) {
        return param("decimals", decimals);
    }

    public IssueAssetCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public IssueAssetCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public IssueAssetCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
