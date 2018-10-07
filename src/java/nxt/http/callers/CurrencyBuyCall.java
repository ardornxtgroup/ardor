// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CurrencyBuyCall extends APICall.Builder<CurrencyBuyCall> {
    private CurrencyBuyCall() {
        super("currencyBuy");
    }

    public static CurrencyBuyCall create(int chain) {
        CurrencyBuyCall instance = new CurrencyBuyCall();
        instance.param("chain", chain);
        return instance;
    }

    public CurrencyBuyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CurrencyBuyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CurrencyBuyCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CurrencyBuyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyBuyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyBuyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CurrencyBuyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CurrencyBuyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CurrencyBuyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CurrencyBuyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CurrencyBuyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CurrencyBuyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyBuyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyBuyCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyBuyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CurrencyBuyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CurrencyBuyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CurrencyBuyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CurrencyBuyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CurrencyBuyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyBuyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyBuyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CurrencyBuyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CurrencyBuyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CurrencyBuyCall rateNQTPerUnit(String rateNQTPerUnit) {
        return param("rateNQTPerUnit", rateNQTPerUnit);
    }

    public CurrencyBuyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CurrencyBuyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CurrencyBuyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CurrencyBuyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CurrencyBuyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CurrencyBuyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CurrencyBuyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CurrencyBuyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CurrencyBuyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CurrencyBuyCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CurrencyBuyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CurrencyBuyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CurrencyBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyBuyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CurrencyBuyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CurrencyBuyCall chain(String chain) {
        return param("chain", chain);
    }

    public CurrencyBuyCall chain(int chain) {
        return param("chain", chain);
    }

    public CurrencyBuyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CurrencyBuyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CurrencyBuyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyBuyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyBuyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CurrencyBuyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CurrencyBuyCall message(String message) {
        return param("message", message);
    }

    public CurrencyBuyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyBuyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyBuyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CurrencyBuyCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CurrencyBuyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyBuyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyBuyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CurrencyBuyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CurrencyBuyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CurrencyBuyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
