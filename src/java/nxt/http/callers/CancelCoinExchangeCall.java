// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CancelCoinExchangeCall extends APICall.Builder<CancelCoinExchangeCall> {
    private CancelCoinExchangeCall() {
        super("cancelCoinExchange");
    }

    public static CancelCoinExchangeCall create(int chain) {
        CancelCoinExchangeCall instance = new CancelCoinExchangeCall();
        instance.param("chain", chain);
        return instance;
    }

    public CancelCoinExchangeCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CancelCoinExchangeCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CancelCoinExchangeCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CancelCoinExchangeCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CancelCoinExchangeCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CancelCoinExchangeCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CancelCoinExchangeCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CancelCoinExchangeCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelCoinExchangeCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelCoinExchangeCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CancelCoinExchangeCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CancelCoinExchangeCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CancelCoinExchangeCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CancelCoinExchangeCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CancelCoinExchangeCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CancelCoinExchangeCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CancelCoinExchangeCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CancelCoinExchangeCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelCoinExchangeCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelCoinExchangeCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CancelCoinExchangeCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CancelCoinExchangeCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CancelCoinExchangeCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CancelCoinExchangeCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CancelCoinExchangeCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CancelCoinExchangeCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CancelCoinExchangeCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CancelCoinExchangeCall order(String order) {
        return param("order", order);
    }

    public CancelCoinExchangeCall order(long order) {
        return unsignedLongParam("order", order);
    }

    public CancelCoinExchangeCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CancelCoinExchangeCall chain(String chain) {
        return param("chain", chain);
    }

    public CancelCoinExchangeCall chain(int chain) {
        return param("chain", chain);
    }

    public CancelCoinExchangeCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CancelCoinExchangeCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CancelCoinExchangeCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelCoinExchangeCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelCoinExchangeCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CancelCoinExchangeCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CancelCoinExchangeCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CancelCoinExchangeCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelCoinExchangeCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelCoinExchangeCall message(String message) {
        return param("message", message);
    }

    public CancelCoinExchangeCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelCoinExchangeCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelCoinExchangeCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CancelCoinExchangeCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CancelCoinExchangeCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelCoinExchangeCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelCoinExchangeCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CancelCoinExchangeCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CancelCoinExchangeCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CancelCoinExchangeCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CancelCoinExchangeCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CancelCoinExchangeCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CancelCoinExchangeCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CancelCoinExchangeCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CancelCoinExchangeCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CancelCoinExchangeCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
