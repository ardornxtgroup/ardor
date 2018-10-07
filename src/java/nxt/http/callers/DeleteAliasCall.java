// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DeleteAliasCall extends APICall.Builder<DeleteAliasCall> {
    private DeleteAliasCall() {
        super("deleteAlias");
    }

    public static DeleteAliasCall create(int chain) {
        DeleteAliasCall instance = new DeleteAliasCall();
        instance.param("chain", chain);
        return instance;
    }

    public DeleteAliasCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DeleteAliasCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DeleteAliasCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DeleteAliasCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAliasCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAliasCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DeleteAliasCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DeleteAliasCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DeleteAliasCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DeleteAliasCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DeleteAliasCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DeleteAliasCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAliasCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAliasCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DeleteAliasCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DeleteAliasCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DeleteAliasCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DeleteAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public DeleteAliasCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DeleteAliasCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAliasCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAliasCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DeleteAliasCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DeleteAliasCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DeleteAliasCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DeleteAliasCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DeleteAliasCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DeleteAliasCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DeleteAliasCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DeleteAliasCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DeleteAliasCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DeleteAliasCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DeleteAliasCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DeleteAliasCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DeleteAliasCall alias(String alias) {
        return param("alias", alias);
    }

    public DeleteAliasCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DeleteAliasCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DeleteAliasCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DeleteAliasCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DeleteAliasCall chain(String chain) {
        return param("chain", chain);
    }

    public DeleteAliasCall chain(int chain) {
        return param("chain", chain);
    }

    public DeleteAliasCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DeleteAliasCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DeleteAliasCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAliasCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAliasCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DeleteAliasCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DeleteAliasCall message(String message) {
        return param("message", message);
    }

    public DeleteAliasCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAliasCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAliasCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DeleteAliasCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DeleteAliasCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAliasCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAliasCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DeleteAliasCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DeleteAliasCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DeleteAliasCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
