// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DividendPaymentCall extends APICall.Builder<DividendPaymentCall> {
    private DividendPaymentCall() {
        super("dividendPayment");
    }

    public static DividendPaymentCall create(int chain) {
        DividendPaymentCall instance = new DividendPaymentCall();
        instance.param("chain", chain);
        return instance;
    }

    public DividendPaymentCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DividendPaymentCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DividendPaymentCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DividendPaymentCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DividendPaymentCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DividendPaymentCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DividendPaymentCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DividendPaymentCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DividendPaymentCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DividendPaymentCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DividendPaymentCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DividendPaymentCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DividendPaymentCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DividendPaymentCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }

    public DividendPaymentCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DividendPaymentCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DividendPaymentCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DividendPaymentCall height(int height) {
        return param("height", height);
    }

    public DividendPaymentCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DividendPaymentCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DividendPaymentCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DividendPaymentCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DividendPaymentCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DividendPaymentCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DividendPaymentCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DividendPaymentCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DividendPaymentCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DividendPaymentCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DividendPaymentCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DividendPaymentCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DividendPaymentCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DividendPaymentCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DividendPaymentCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DividendPaymentCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DividendPaymentCall holding(String holding) {
        return param("holding", holding);
    }

    public DividendPaymentCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public DividendPaymentCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DividendPaymentCall amountNQTPerShare(String amountNQTPerShare) {
        return param("amountNQTPerShare", amountNQTPerShare);
    }

    public DividendPaymentCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DividendPaymentCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DividendPaymentCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DividendPaymentCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DividendPaymentCall chain(String chain) {
        return param("chain", chain);
    }

    public DividendPaymentCall chain(int chain) {
        return param("chain", chain);
    }

    public DividendPaymentCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DividendPaymentCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DividendPaymentCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DividendPaymentCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DividendPaymentCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DividendPaymentCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DividendPaymentCall message(String message) {
        return param("message", message);
    }

    public DividendPaymentCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DividendPaymentCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DividendPaymentCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DividendPaymentCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DividendPaymentCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DividendPaymentCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DividendPaymentCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DividendPaymentCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DividendPaymentCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DividendPaymentCall asset(String asset) {
        return param("asset", asset);
    }

    public DividendPaymentCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public DividendPaymentCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
