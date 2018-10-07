// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class BuyAliasCall extends APICall.Builder<BuyAliasCall> {
    private BuyAliasCall() {
        super("buyAlias");
    }

    public static BuyAliasCall create(int chain) {
        BuyAliasCall instance = new BuyAliasCall();
        instance.param("chain", chain);
        return instance;
    }

    public BuyAliasCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public BuyAliasCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public BuyAliasCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public BuyAliasCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public BuyAliasCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public BuyAliasCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public BuyAliasCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public BuyAliasCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public BuyAliasCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public BuyAliasCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public BuyAliasCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public BuyAliasCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public BuyAliasCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public BuyAliasCall amountNQT(long amountNQT) {
        return param("amountNQT", amountNQT);
    }

    public BuyAliasCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public BuyAliasCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public BuyAliasCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public BuyAliasCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public BuyAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public BuyAliasCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public BuyAliasCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public BuyAliasCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public BuyAliasCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public BuyAliasCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public BuyAliasCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public BuyAliasCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public BuyAliasCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public BuyAliasCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public BuyAliasCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public BuyAliasCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public BuyAliasCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public BuyAliasCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public BuyAliasCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public BuyAliasCall phased(boolean phased) {
        return param("phased", phased);
    }

    public BuyAliasCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public BuyAliasCall alias(String alias) {
        return param("alias", alias);
    }

    public BuyAliasCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public BuyAliasCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public BuyAliasCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public BuyAliasCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public BuyAliasCall chain(String chain) {
        return param("chain", chain);
    }

    public BuyAliasCall chain(int chain) {
        return param("chain", chain);
    }

    public BuyAliasCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public BuyAliasCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public BuyAliasCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public BuyAliasCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public BuyAliasCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public BuyAliasCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public BuyAliasCall message(String message) {
        return param("message", message);
    }

    public BuyAliasCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public BuyAliasCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public BuyAliasCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public BuyAliasCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public BuyAliasCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public BuyAliasCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public BuyAliasCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public BuyAliasCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public BuyAliasCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public BuyAliasCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
