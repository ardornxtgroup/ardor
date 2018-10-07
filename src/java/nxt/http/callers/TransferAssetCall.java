// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class TransferAssetCall extends APICall.Builder<TransferAssetCall> {
    private TransferAssetCall() {
        super("transferAsset");
    }

    public static TransferAssetCall create(int chain) {
        TransferAssetCall instance = new TransferAssetCall();
        instance.param("chain", chain);
        return instance;
    }

    public TransferAssetCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public TransferAssetCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public TransferAssetCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public TransferAssetCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public TransferAssetCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public TransferAssetCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public TransferAssetCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public TransferAssetCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public TransferAssetCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public TransferAssetCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public TransferAssetCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public TransferAssetCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public TransferAssetCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public TransferAssetCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public TransferAssetCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public TransferAssetCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public TransferAssetCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public TransferAssetCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public TransferAssetCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public TransferAssetCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public TransferAssetCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public TransferAssetCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public TransferAssetCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public TransferAssetCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public TransferAssetCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public TransferAssetCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public TransferAssetCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public TransferAssetCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public TransferAssetCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public TransferAssetCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public TransferAssetCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public TransferAssetCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public TransferAssetCall phased(boolean phased) {
        return param("phased", phased);
    }

    public TransferAssetCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public TransferAssetCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public TransferAssetCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public TransferAssetCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public TransferAssetCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public TransferAssetCall chain(String chain) {
        return param("chain", chain);
    }

    public TransferAssetCall chain(int chain) {
        return param("chain", chain);
    }

    public TransferAssetCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public TransferAssetCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public TransferAssetCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public TransferAssetCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public TransferAssetCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public TransferAssetCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public TransferAssetCall message(String message) {
        return param("message", message);
    }

    public TransferAssetCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public TransferAssetCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public TransferAssetCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public TransferAssetCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public TransferAssetCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public TransferAssetCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public TransferAssetCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public TransferAssetCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public TransferAssetCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public TransferAssetCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public TransferAssetCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public TransferAssetCall asset(String asset) {
        return param("asset", asset);
    }

    public TransferAssetCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public TransferAssetCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
