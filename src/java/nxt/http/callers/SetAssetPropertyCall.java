// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetAssetPropertyCall extends APICall.Builder<SetAssetPropertyCall> {
    private SetAssetPropertyCall() {
        super("setAssetProperty");
    }

    public static SetAssetPropertyCall create(int chain) {
        SetAssetPropertyCall instance = new SetAssetPropertyCall();
        instance.param("chain", chain);
        return instance;
    }

    public SetAssetPropertyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SetAssetPropertyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SetAssetPropertyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SetAssetPropertyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SetAssetPropertyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SetAssetPropertyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SetAssetPropertyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SetAssetPropertyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SetAssetPropertyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SetAssetPropertyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SetAssetPropertyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SetAssetPropertyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetAssetPropertyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetAssetPropertyCall property(String property) {
        return param("property", property);
    }

    public SetAssetPropertyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SetAssetPropertyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SetAssetPropertyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SetAssetPropertyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SetAssetPropertyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SetAssetPropertyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetAssetPropertyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetAssetPropertyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SetAssetPropertyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SetAssetPropertyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SetAssetPropertyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SetAssetPropertyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SetAssetPropertyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SetAssetPropertyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SetAssetPropertyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SetAssetPropertyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SetAssetPropertyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SetAssetPropertyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SetAssetPropertyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SetAssetPropertyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SetAssetPropertyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SetAssetPropertyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SetAssetPropertyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SetAssetPropertyCall value(String value) {
        return param("value", value);
    }

    public SetAssetPropertyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SetAssetPropertyCall chain(String chain) {
        return param("chain", chain);
    }

    public SetAssetPropertyCall chain(int chain) {
        return param("chain", chain);
    }

    public SetAssetPropertyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SetAssetPropertyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SetAssetPropertyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetAssetPropertyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetAssetPropertyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SetAssetPropertyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SetAssetPropertyCall message(String message) {
        return param("message", message);
    }

    public SetAssetPropertyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetAssetPropertyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetAssetPropertyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SetAssetPropertyCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SetAssetPropertyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetAssetPropertyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetAssetPropertyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SetAssetPropertyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SetAssetPropertyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SetAssetPropertyCall asset(String asset) {
        return param("asset", asset);
    }

    public SetAssetPropertyCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public SetAssetPropertyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
