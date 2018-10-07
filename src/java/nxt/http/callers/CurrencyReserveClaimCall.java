// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CurrencyReserveClaimCall extends APICall.Builder<CurrencyReserveClaimCall> {
    private CurrencyReserveClaimCall() {
        super("currencyReserveClaim");
    }

    public static CurrencyReserveClaimCall create(int chain) {
        CurrencyReserveClaimCall instance = new CurrencyReserveClaimCall();
        instance.param("chain", chain);
        return instance;
    }

    public CurrencyReserveClaimCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CurrencyReserveClaimCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CurrencyReserveClaimCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CurrencyReserveClaimCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyReserveClaimCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyReserveClaimCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CurrencyReserveClaimCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CurrencyReserveClaimCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CurrencyReserveClaimCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CurrencyReserveClaimCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CurrencyReserveClaimCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CurrencyReserveClaimCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyReserveClaimCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyReserveClaimCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyReserveClaimCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CurrencyReserveClaimCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CurrencyReserveClaimCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CurrencyReserveClaimCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CurrencyReserveClaimCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CurrencyReserveClaimCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyReserveClaimCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyReserveClaimCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CurrencyReserveClaimCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CurrencyReserveClaimCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CurrencyReserveClaimCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CurrencyReserveClaimCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CurrencyReserveClaimCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CurrencyReserveClaimCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CurrencyReserveClaimCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CurrencyReserveClaimCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CurrencyReserveClaimCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CurrencyReserveClaimCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CurrencyReserveClaimCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CurrencyReserveClaimCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CurrencyReserveClaimCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CurrencyReserveClaimCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CurrencyReserveClaimCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyReserveClaimCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyReserveClaimCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CurrencyReserveClaimCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CurrencyReserveClaimCall chain(String chain) {
        return param("chain", chain);
    }

    public CurrencyReserveClaimCall chain(int chain) {
        return param("chain", chain);
    }

    public CurrencyReserveClaimCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CurrencyReserveClaimCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CurrencyReserveClaimCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyReserveClaimCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyReserveClaimCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CurrencyReserveClaimCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CurrencyReserveClaimCall message(String message) {
        return param("message", message);
    }

    public CurrencyReserveClaimCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyReserveClaimCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyReserveClaimCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CurrencyReserveClaimCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CurrencyReserveClaimCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyReserveClaimCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyReserveClaimCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CurrencyReserveClaimCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CurrencyReserveClaimCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CurrencyReserveClaimCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
