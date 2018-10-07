// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DgsDeliveryCall extends APICall.Builder<DgsDeliveryCall> {
    private DgsDeliveryCall() {
        super("dgsDelivery");
    }

    public static DgsDeliveryCall create(int chain) {
        DgsDeliveryCall instance = new DgsDeliveryCall();
        instance.param("chain", chain);
        return instance;
    }

    public DgsDeliveryCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DgsDeliveryCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DgsDeliveryCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DgsDeliveryCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsDeliveryCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsDeliveryCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DgsDeliveryCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DgsDeliveryCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DgsDeliveryCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DgsDeliveryCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DgsDeliveryCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DgsDeliveryCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsDeliveryCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsDeliveryCall goodsIsText(boolean goodsIsText) {
        return param("goodsIsText", goodsIsText);
    }

    public DgsDeliveryCall discountNQT(long discountNQT) {
        return param("discountNQT", discountNQT);
    }

    public DgsDeliveryCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DgsDeliveryCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DgsDeliveryCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DgsDeliveryCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DgsDeliveryCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DgsDeliveryCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsDeliveryCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsDeliveryCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DgsDeliveryCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DgsDeliveryCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DgsDeliveryCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DgsDeliveryCall goodsData(String goodsData) {
        return param("goodsData", goodsData);
    }

    public DgsDeliveryCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DgsDeliveryCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DgsDeliveryCall goodsNonce(String goodsNonce) {
        return param("goodsNonce", goodsNonce);
    }

    public DgsDeliveryCall goodsNonce(byte[] goodsNonce) {
        return param("goodsNonce", goodsNonce);
    }

    public DgsDeliveryCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DgsDeliveryCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DgsDeliveryCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DgsDeliveryCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DgsDeliveryCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DgsDeliveryCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DgsDeliveryCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DgsDeliveryCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DgsDeliveryCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DgsDeliveryCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DgsDeliveryCall goodsToEncrypt(String goodsToEncrypt) {
        return param("goodsToEncrypt", goodsToEncrypt);
    }

    public DgsDeliveryCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DgsDeliveryCall chain(String chain) {
        return param("chain", chain);
    }

    public DgsDeliveryCall chain(int chain) {
        return param("chain", chain);
    }

    public DgsDeliveryCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DgsDeliveryCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DgsDeliveryCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsDeliveryCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsDeliveryCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DgsDeliveryCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DgsDeliveryCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsDeliveryCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }

    public DgsDeliveryCall message(String message) {
        return param("message", message);
    }

    public DgsDeliveryCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsDeliveryCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsDeliveryCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DgsDeliveryCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DgsDeliveryCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsDeliveryCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsDeliveryCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DgsDeliveryCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DgsDeliveryCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DgsDeliveryCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
