// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DeleteAssetPropertyCall extends APICall.Builder<DeleteAssetPropertyCall> {
    private DeleteAssetPropertyCall() {
        super("deleteAssetProperty");
    }

    public static DeleteAssetPropertyCall create(int chain) {
        DeleteAssetPropertyCall instance = new DeleteAssetPropertyCall();
        instance.param("chain", chain);
        return instance;
    }

    public DeleteAssetPropertyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DeleteAssetPropertyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DeleteAssetPropertyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DeleteAssetPropertyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAssetPropertyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAssetPropertyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DeleteAssetPropertyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DeleteAssetPropertyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DeleteAssetPropertyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DeleteAssetPropertyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DeleteAssetPropertyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DeleteAssetPropertyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAssetPropertyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAssetPropertyCall property(String property) {
        return param("property", property);
    }

    public DeleteAssetPropertyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DeleteAssetPropertyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DeleteAssetPropertyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DeleteAssetPropertyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DeleteAssetPropertyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DeleteAssetPropertyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAssetPropertyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAssetPropertyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DeleteAssetPropertyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DeleteAssetPropertyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DeleteAssetPropertyCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DeleteAssetPropertyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DeleteAssetPropertyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DeleteAssetPropertyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DeleteAssetPropertyCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DeleteAssetPropertyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DeleteAssetPropertyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DeleteAssetPropertyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DeleteAssetPropertyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DeleteAssetPropertyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DeleteAssetPropertyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DeleteAssetPropertyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DeleteAssetPropertyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DeleteAssetPropertyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DeleteAssetPropertyCall chain(String chain) {
        return param("chain", chain);
    }

    public DeleteAssetPropertyCall chain(int chain) {
        return param("chain", chain);
    }

    public DeleteAssetPropertyCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DeleteAssetPropertyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DeleteAssetPropertyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAssetPropertyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAssetPropertyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DeleteAssetPropertyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DeleteAssetPropertyCall message(String message) {
        return param("message", message);
    }

    public DeleteAssetPropertyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAssetPropertyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAssetPropertyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DeleteAssetPropertyCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DeleteAssetPropertyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAssetPropertyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAssetPropertyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DeleteAssetPropertyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DeleteAssetPropertyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DeleteAssetPropertyCall setter(String setter) {
        return param("setter", setter);
    }

    public DeleteAssetPropertyCall asset(String asset) {
        return param("asset", asset);
    }

    public DeleteAssetPropertyCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public DeleteAssetPropertyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
