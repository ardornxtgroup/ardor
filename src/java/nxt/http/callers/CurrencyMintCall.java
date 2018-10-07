// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CurrencyMintCall extends APICall.Builder<CurrencyMintCall> {
    private CurrencyMintCall() {
        super("currencyMint");
    }

    public static CurrencyMintCall create(int chain) {
        CurrencyMintCall instance = new CurrencyMintCall();
        instance.param("chain", chain);
        return instance;
    }

    public CurrencyMintCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CurrencyMintCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CurrencyMintCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CurrencyMintCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyMintCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CurrencyMintCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CurrencyMintCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CurrencyMintCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CurrencyMintCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CurrencyMintCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CurrencyMintCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CurrencyMintCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyMintCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CurrencyMintCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyMintCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CurrencyMintCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CurrencyMintCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CurrencyMintCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CurrencyMintCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CurrencyMintCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyMintCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CurrencyMintCall nonce(String nonce) {
        return param("nonce", nonce);
    }

    public CurrencyMintCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CurrencyMintCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CurrencyMintCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CurrencyMintCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CurrencyMintCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CurrencyMintCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CurrencyMintCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CurrencyMintCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CurrencyMintCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CurrencyMintCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CurrencyMintCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CurrencyMintCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CurrencyMintCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CurrencyMintCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CurrencyMintCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CurrencyMintCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyMintCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyMintCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CurrencyMintCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CurrencyMintCall chain(String chain) {
        return param("chain", chain);
    }

    public CurrencyMintCall chain(int chain) {
        return param("chain", chain);
    }

    public CurrencyMintCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CurrencyMintCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CurrencyMintCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyMintCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CurrencyMintCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CurrencyMintCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CurrencyMintCall counter(String counter) {
        return param("counter", counter);
    }

    public CurrencyMintCall message(String message) {
        return param("message", message);
    }

    public CurrencyMintCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyMintCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CurrencyMintCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CurrencyMintCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CurrencyMintCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyMintCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CurrencyMintCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CurrencyMintCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CurrencyMintCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CurrencyMintCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
