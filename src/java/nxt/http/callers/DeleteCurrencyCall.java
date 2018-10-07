// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DeleteCurrencyCall extends APICall.Builder<DeleteCurrencyCall> {
    private DeleteCurrencyCall() {
        super("deleteCurrency");
    }

    public static DeleteCurrencyCall create(int chain) {
        DeleteCurrencyCall instance = new DeleteCurrencyCall();
        instance.param("chain", chain);
        return instance;
    }

    public DeleteCurrencyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DeleteCurrencyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DeleteCurrencyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DeleteCurrencyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DeleteCurrencyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DeleteCurrencyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DeleteCurrencyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DeleteCurrencyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteCurrencyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteCurrencyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DeleteCurrencyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DeleteCurrencyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DeleteCurrencyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DeleteCurrencyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DeleteCurrencyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DeleteCurrencyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DeleteCurrencyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DeleteCurrencyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteCurrencyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteCurrencyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DeleteCurrencyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DeleteCurrencyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DeleteCurrencyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DeleteCurrencyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DeleteCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public DeleteCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public DeleteCurrencyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DeleteCurrencyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DeleteCurrencyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DeleteCurrencyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DeleteCurrencyCall chain(String chain) {
        return param("chain", chain);
    }

    public DeleteCurrencyCall chain(int chain) {
        return param("chain", chain);
    }

    public DeleteCurrencyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DeleteCurrencyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DeleteCurrencyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteCurrencyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteCurrencyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DeleteCurrencyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DeleteCurrencyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DeleteCurrencyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteCurrencyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteCurrencyCall message(String message) {
        return param("message", message);
    }

    public DeleteCurrencyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteCurrencyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteCurrencyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DeleteCurrencyCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DeleteCurrencyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteCurrencyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteCurrencyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DeleteCurrencyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DeleteCurrencyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DeleteCurrencyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DeleteCurrencyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DeleteCurrencyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DeleteCurrencyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DeleteCurrencyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DeleteCurrencyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DeleteCurrencyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
