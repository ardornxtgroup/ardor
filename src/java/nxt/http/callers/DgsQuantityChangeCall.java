// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DgsQuantityChangeCall extends APICall.Builder<DgsQuantityChangeCall> {
    private DgsQuantityChangeCall() {
        super("dgsQuantityChange");
    }

    public static DgsQuantityChangeCall create(int chain) {
        DgsQuantityChangeCall instance = new DgsQuantityChangeCall();
        instance.param("chain", chain);
        return instance;
    }

    public DgsQuantityChangeCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DgsQuantityChangeCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DgsQuantityChangeCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DgsQuantityChangeCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsQuantityChangeCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public DgsQuantityChangeCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsQuantityChangeCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsQuantityChangeCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DgsQuantityChangeCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DgsQuantityChangeCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DgsQuantityChangeCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DgsQuantityChangeCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DgsQuantityChangeCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DgsQuantityChangeCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsQuantityChangeCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsQuantityChangeCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DgsQuantityChangeCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DgsQuantityChangeCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DgsQuantityChangeCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DgsQuantityChangeCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DgsQuantityChangeCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsQuantityChangeCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsQuantityChangeCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DgsQuantityChangeCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DgsQuantityChangeCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DgsQuantityChangeCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DgsQuantityChangeCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DgsQuantityChangeCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DgsQuantityChangeCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DgsQuantityChangeCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DgsQuantityChangeCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DgsQuantityChangeCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DgsQuantityChangeCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DgsQuantityChangeCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DgsQuantityChangeCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DgsQuantityChangeCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DgsQuantityChangeCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DgsQuantityChangeCall deltaQuantity(String deltaQuantity) {
        return param("deltaQuantity", deltaQuantity);
    }

    public DgsQuantityChangeCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DgsQuantityChangeCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DgsQuantityChangeCall chain(String chain) {
        return param("chain", chain);
    }

    public DgsQuantityChangeCall chain(int chain) {
        return param("chain", chain);
    }

    public DgsQuantityChangeCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DgsQuantityChangeCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DgsQuantityChangeCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsQuantityChangeCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsQuantityChangeCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DgsQuantityChangeCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DgsQuantityChangeCall message(String message) {
        return param("message", message);
    }

    public DgsQuantityChangeCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsQuantityChangeCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsQuantityChangeCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DgsQuantityChangeCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DgsQuantityChangeCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsQuantityChangeCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsQuantityChangeCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DgsQuantityChangeCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DgsQuantityChangeCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DgsQuantityChangeCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
