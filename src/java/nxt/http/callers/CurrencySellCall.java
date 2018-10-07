// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CurrencySellCall extends APICall.Builder<CurrencySellCall> {
    private CurrencySellCall() {
        super("currencySell");
    }

    public static CurrencySellCall create(int chain) {
        CurrencySellCall instance = new CurrencySellCall();
        instance.param("chain", chain);
        return instance;
    }

    public CurrencySellCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CurrencySellCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CurrencySellCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CurrencySellCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencySellCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencySellCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CurrencySellCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CurrencySellCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CurrencySellCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CurrencySellCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CurrencySellCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CurrencySellCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencySellCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencySellCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencySellCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CurrencySellCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CurrencySellCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CurrencySellCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CurrencySellCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CurrencySellCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencySellCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencySellCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CurrencySellCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CurrencySellCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CurrencySellCall rateNQTPerUnit(String rateNQTPerUnit) {
        return param("rateNQTPerUnit", rateNQTPerUnit);
    }

    public CurrencySellCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CurrencySellCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CurrencySellCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CurrencySellCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CurrencySellCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CurrencySellCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CurrencySellCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CurrencySellCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CurrencySellCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CurrencySellCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CurrencySellCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CurrencySellCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CurrencySellCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencySellCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencySellCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CurrencySellCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CurrencySellCall chain(String chain) {
        return param("chain", chain);
    }

    public CurrencySellCall chain(int chain) {
        return param("chain", chain);
    }

    public CurrencySellCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CurrencySellCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CurrencySellCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencySellCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencySellCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CurrencySellCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CurrencySellCall message(String message) {
        return param("message", message);
    }

    public CurrencySellCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencySellCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencySellCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CurrencySellCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CurrencySellCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencySellCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencySellCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CurrencySellCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CurrencySellCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CurrencySellCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
