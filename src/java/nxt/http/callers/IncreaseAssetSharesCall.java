// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class IncreaseAssetSharesCall extends APICall.Builder<IncreaseAssetSharesCall> {
    private IncreaseAssetSharesCall() {
        super("increaseAssetShares");
    }

    public static IncreaseAssetSharesCall create(int chain) {
        IncreaseAssetSharesCall instance = new IncreaseAssetSharesCall();
        instance.param("chain", chain);
        return instance;
    }

    public IncreaseAssetSharesCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public IncreaseAssetSharesCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public IncreaseAssetSharesCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public IncreaseAssetSharesCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public IncreaseAssetSharesCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public IncreaseAssetSharesCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public IncreaseAssetSharesCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public IncreaseAssetSharesCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public IncreaseAssetSharesCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public IncreaseAssetSharesCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public IncreaseAssetSharesCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public IncreaseAssetSharesCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IncreaseAssetSharesCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IncreaseAssetSharesCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public IncreaseAssetSharesCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public IncreaseAssetSharesCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public IncreaseAssetSharesCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public IncreaseAssetSharesCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public IncreaseAssetSharesCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public IncreaseAssetSharesCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IncreaseAssetSharesCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IncreaseAssetSharesCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public IncreaseAssetSharesCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public IncreaseAssetSharesCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public IncreaseAssetSharesCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public IncreaseAssetSharesCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public IncreaseAssetSharesCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public IncreaseAssetSharesCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public IncreaseAssetSharesCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public IncreaseAssetSharesCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public IncreaseAssetSharesCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public IncreaseAssetSharesCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public IncreaseAssetSharesCall phased(boolean phased) {
        return param("phased", phased);
    }

    public IncreaseAssetSharesCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public IncreaseAssetSharesCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public IncreaseAssetSharesCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public IncreaseAssetSharesCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public IncreaseAssetSharesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public IncreaseAssetSharesCall chain(String chain) {
        return param("chain", chain);
    }

    public IncreaseAssetSharesCall chain(int chain) {
        return param("chain", chain);
    }

    public IncreaseAssetSharesCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public IncreaseAssetSharesCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public IncreaseAssetSharesCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IncreaseAssetSharesCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IncreaseAssetSharesCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public IncreaseAssetSharesCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public IncreaseAssetSharesCall message(String message) {
        return param("message", message);
    }

    public IncreaseAssetSharesCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IncreaseAssetSharesCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IncreaseAssetSharesCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public IncreaseAssetSharesCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public IncreaseAssetSharesCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IncreaseAssetSharesCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IncreaseAssetSharesCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public IncreaseAssetSharesCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public IncreaseAssetSharesCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public IncreaseAssetSharesCall asset(String asset) {
        return param("asset", asset);
    }

    public IncreaseAssetSharesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public IncreaseAssetSharesCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
