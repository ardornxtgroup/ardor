// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CurrencyReserveIncreaseCall extends APICall.Builder<CurrencyReserveIncreaseCall> {
    private CurrencyReserveIncreaseCall() {
        super("currencyReserveIncrease");
    }

    public static CurrencyReserveIncreaseCall create(int chain) {
        CurrencyReserveIncreaseCall instance = new CurrencyReserveIncreaseCall();
        instance.param("chain", chain);
        return instance;
    }

    public CurrencyReserveIncreaseCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CurrencyReserveIncreaseCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CurrencyReserveIncreaseCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CurrencyReserveIncreaseCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyReserveIncreaseCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyReserveIncreaseCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CurrencyReserveIncreaseCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CurrencyReserveIncreaseCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CurrencyReserveIncreaseCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CurrencyReserveIncreaseCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CurrencyReserveIncreaseCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CurrencyReserveIncreaseCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyReserveIncreaseCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyReserveIncreaseCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CurrencyReserveIncreaseCall amountPerUnitNQT(long amountPerUnitNQT) {
        return param("amountPerUnitNQT", amountPerUnitNQT);
    }

    public CurrencyReserveIncreaseCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CurrencyReserveIncreaseCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CurrencyReserveIncreaseCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CurrencyReserveIncreaseCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CurrencyReserveIncreaseCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyReserveIncreaseCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyReserveIncreaseCall phasingLinkedTransaction(
            String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CurrencyReserveIncreaseCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CurrencyReserveIncreaseCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CurrencyReserveIncreaseCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CurrencyReserveIncreaseCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CurrencyReserveIncreaseCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CurrencyReserveIncreaseCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CurrencyReserveIncreaseCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CurrencyReserveIncreaseCall encryptedMessageIsPrunable(
            boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CurrencyReserveIncreaseCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CurrencyReserveIncreaseCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CurrencyReserveIncreaseCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CurrencyReserveIncreaseCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CurrencyReserveIncreaseCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CurrencyReserveIncreaseCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CurrencyReserveIncreaseCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyReserveIncreaseCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyReserveIncreaseCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CurrencyReserveIncreaseCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CurrencyReserveIncreaseCall chain(String chain) {
        return param("chain", chain);
    }

    public CurrencyReserveIncreaseCall chain(int chain) {
        return param("chain", chain);
    }

    public CurrencyReserveIncreaseCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CurrencyReserveIncreaseCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CurrencyReserveIncreaseCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyReserveIncreaseCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyReserveIncreaseCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CurrencyReserveIncreaseCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CurrencyReserveIncreaseCall message(String message) {
        return param("message", message);
    }

    public CurrencyReserveIncreaseCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyReserveIncreaseCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyReserveIncreaseCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CurrencyReserveIncreaseCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CurrencyReserveIncreaseCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyReserveIncreaseCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyReserveIncreaseCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CurrencyReserveIncreaseCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CurrencyReserveIncreaseCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CurrencyReserveIncreaseCall phasingSenderPropertyValue(
            String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
