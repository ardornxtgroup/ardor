// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DgsPurchaseCall extends APICall.Builder<DgsPurchaseCall> {
    private DgsPurchaseCall() {
        super("dgsPurchase");
    }

    public static DgsPurchaseCall create(int chain) {
        DgsPurchaseCall instance = new DgsPurchaseCall();
        instance.param("chain", chain);
        return instance;
    }

    public DgsPurchaseCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DgsPurchaseCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DgsPurchaseCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DgsPurchaseCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsPurchaseCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public DgsPurchaseCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsPurchaseCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsPurchaseCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DgsPurchaseCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DgsPurchaseCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DgsPurchaseCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DgsPurchaseCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DgsPurchaseCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DgsPurchaseCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsPurchaseCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsPurchaseCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DgsPurchaseCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DgsPurchaseCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DgsPurchaseCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DgsPurchaseCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DgsPurchaseCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsPurchaseCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsPurchaseCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DgsPurchaseCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DgsPurchaseCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DgsPurchaseCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DgsPurchaseCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DgsPurchaseCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DgsPurchaseCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DgsPurchaseCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DgsPurchaseCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DgsPurchaseCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DgsPurchaseCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DgsPurchaseCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DgsPurchaseCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public DgsPurchaseCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DgsPurchaseCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DgsPurchaseCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DgsPurchaseCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DgsPurchaseCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DgsPurchaseCall chain(String chain) {
        return param("chain", chain);
    }

    public DgsPurchaseCall chain(int chain) {
        return param("chain", chain);
    }

    public DgsPurchaseCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DgsPurchaseCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DgsPurchaseCall quantity(String quantity) {
        return param("quantity", quantity);
    }

    public DgsPurchaseCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsPurchaseCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsPurchaseCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DgsPurchaseCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DgsPurchaseCall message(String message) {
        return param("message", message);
    }

    public DgsPurchaseCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsPurchaseCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsPurchaseCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DgsPurchaseCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DgsPurchaseCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsPurchaseCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsPurchaseCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DgsPurchaseCall deliveryDeadlineTimestamp(String deliveryDeadlineTimestamp) {
        return param("deliveryDeadlineTimestamp", deliveryDeadlineTimestamp);
    }

    public DgsPurchaseCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DgsPurchaseCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DgsPurchaseCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
