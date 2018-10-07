// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SellAliasCall extends APICall.Builder<SellAliasCall> {
    private SellAliasCall() {
        super("sellAlias");
    }

    public static SellAliasCall create(int chain) {
        SellAliasCall instance = new SellAliasCall();
        instance.param("chain", chain);
        return instance;
    }

    public SellAliasCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SellAliasCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SellAliasCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SellAliasCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SellAliasCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SellAliasCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SellAliasCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SellAliasCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SellAliasCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SellAliasCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SellAliasCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SellAliasCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SellAliasCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SellAliasCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SellAliasCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SellAliasCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SellAliasCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SellAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public SellAliasCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SellAliasCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SellAliasCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SellAliasCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SellAliasCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SellAliasCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SellAliasCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SellAliasCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SellAliasCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SellAliasCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SellAliasCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SellAliasCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SellAliasCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SellAliasCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SellAliasCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SellAliasCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public SellAliasCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SellAliasCall alias(String alias) {
        return param("alias", alias);
    }

    public SellAliasCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SellAliasCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SellAliasCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SellAliasCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SellAliasCall chain(String chain) {
        return param("chain", chain);
    }

    public SellAliasCall chain(int chain) {
        return param("chain", chain);
    }

    public SellAliasCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SellAliasCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SellAliasCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SellAliasCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SellAliasCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SellAliasCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SellAliasCall message(String message) {
        return param("message", message);
    }

    public SellAliasCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SellAliasCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SellAliasCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SellAliasCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SellAliasCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SellAliasCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SellAliasCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SellAliasCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SellAliasCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public SellAliasCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SellAliasCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SellAliasCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
