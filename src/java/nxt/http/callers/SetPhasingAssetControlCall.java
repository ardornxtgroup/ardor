// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetPhasingAssetControlCall extends APICall.Builder<SetPhasingAssetControlCall> {
    private SetPhasingAssetControlCall() {
        super("setPhasingAssetControl");
    }

    public static SetPhasingAssetControlCall create(int chain) {
        SetPhasingAssetControlCall instance = new SetPhasingAssetControlCall();
        instance.param("chain", chain);
        return instance;
    }

    public SetPhasingAssetControlCall controlRecipientPropertyValue(
            String controlRecipientPropertyValue) {
        return param("controlRecipientPropertyValue", controlRecipientPropertyValue);
    }

    public SetPhasingAssetControlCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SetPhasingAssetControlCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SetPhasingAssetControlCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SetPhasingAssetControlCall controlExpression(String controlExpression) {
        return param("controlExpression", controlExpression);
    }

    public SetPhasingAssetControlCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SetPhasingAssetControlCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SetPhasingAssetControlCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SetPhasingAssetControlCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SetPhasingAssetControlCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SetPhasingAssetControlCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SetPhasingAssetControlCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SetPhasingAssetControlCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SetPhasingAssetControlCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetPhasingAssetControlCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetPhasingAssetControlCall controlHolding(String controlHolding) {
        return param("controlHolding", controlHolding);
    }

    public SetPhasingAssetControlCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SetPhasingAssetControlCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SetPhasingAssetControlCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SetPhasingAssetControlCall controlMinBalance(String controlMinBalance) {
        return param("controlMinBalance", controlMinBalance);
    }

    public SetPhasingAssetControlCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SetPhasingAssetControlCall controlMinBalanceModel(String controlMinBalanceModel) {
        return param("controlMinBalanceModel", controlMinBalanceModel);
    }

    public SetPhasingAssetControlCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SetPhasingAssetControlCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetPhasingAssetControlCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetPhasingAssetControlCall controlSenderPropertyName(String controlSenderPropertyName) {
        return param("controlSenderPropertyName", controlSenderPropertyName);
    }

    public SetPhasingAssetControlCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SetPhasingAssetControlCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SetPhasingAssetControlCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SetPhasingAssetControlCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SetPhasingAssetControlCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SetPhasingAssetControlCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SetPhasingAssetControlCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SetPhasingAssetControlCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SetPhasingAssetControlCall encryptedMessageIsPrunable(
            boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SetPhasingAssetControlCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SetPhasingAssetControlCall controlQuorum(String controlQuorum) {
        return param("controlQuorum", controlQuorum);
    }

    public SetPhasingAssetControlCall controlParams(String controlParams) {
        return param("controlParams", controlParams);
    }

    public SetPhasingAssetControlCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SetPhasingAssetControlCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SetPhasingAssetControlCall controlRecipientPropertyName(
            String controlRecipientPropertyName) {
        return param("controlRecipientPropertyName", controlRecipientPropertyName);
    }

    public SetPhasingAssetControlCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SetPhasingAssetControlCall controlSenderPropertySetter(
            String controlSenderPropertySetter) {
        return param("controlSenderPropertySetter", controlSenderPropertySetter);
    }

    public SetPhasingAssetControlCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SetPhasingAssetControlCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SetPhasingAssetControlCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SetPhasingAssetControlCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SetPhasingAssetControlCall chain(String chain) {
        return param("chain", chain);
    }

    public SetPhasingAssetControlCall chain(int chain) {
        return param("chain", chain);
    }

    public SetPhasingAssetControlCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SetPhasingAssetControlCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SetPhasingAssetControlCall controlRecipientPropertySetter(
            String controlRecipientPropertySetter) {
        return param("controlRecipientPropertySetter", controlRecipientPropertySetter);
    }

    public SetPhasingAssetControlCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetPhasingAssetControlCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetPhasingAssetControlCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SetPhasingAssetControlCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SetPhasingAssetControlCall message(String message) {
        return param("message", message);
    }

    public SetPhasingAssetControlCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetPhasingAssetControlCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetPhasingAssetControlCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SetPhasingAssetControlCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SetPhasingAssetControlCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetPhasingAssetControlCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetPhasingAssetControlCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SetPhasingAssetControlCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SetPhasingAssetControlCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SetPhasingAssetControlCall controlVotingModel(String controlVotingModel) {
        return param("controlVotingModel", controlVotingModel);
    }

    public SetPhasingAssetControlCall asset(String asset) {
        return param("asset", asset);
    }

    public SetPhasingAssetControlCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public SetPhasingAssetControlCall controlWhitelisted(String... controlWhitelisted) {
        return param("controlWhitelisted", controlWhitelisted);
    }

    public SetPhasingAssetControlCall phasingSenderPropertyValue(
            String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public SetPhasingAssetControlCall controlSenderPropertyValue(
            String controlSenderPropertyValue) {
        return param("controlSenderPropertyValue", controlSenderPropertyValue);
    }
}
