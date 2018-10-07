// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CastVoteCall extends APICall.Builder<CastVoteCall> {
    private CastVoteCall() {
        super("castVote");
    }

    public static CastVoteCall create(int chain) {
        CastVoteCall instance = new CastVoteCall();
        instance.param("chain", chain);
        return instance;
    }

    public CastVoteCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CastVoteCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CastVoteCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CastVoteCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CastVoteCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CastVoteCall poll(String poll) {
        return param("poll", poll);
    }

    public CastVoteCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public CastVoteCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CastVoteCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CastVoteCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CastVoteCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CastVoteCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CastVoteCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CastVoteCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CastVoteCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CastVoteCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CastVoteCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CastVoteCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CastVoteCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CastVoteCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CastVoteCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CastVoteCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CastVoteCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CastVoteCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CastVoteCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CastVoteCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CastVoteCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CastVoteCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CastVoteCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CastVoteCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CastVoteCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CastVoteCall vote02(String vote02) {
        return param("vote02", vote02);
    }

    public CastVoteCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CastVoteCall vote00(String vote00) {
        return param("vote00", vote00);
    }

    public CastVoteCall vote01(String vote01) {
        return param("vote01", vote01);
    }

    public CastVoteCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CastVoteCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CastVoteCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CastVoteCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CastVoteCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CastVoteCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CastVoteCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CastVoteCall chain(String chain) {
        return param("chain", chain);
    }

    public CastVoteCall chain(int chain) {
        return param("chain", chain);
    }

    public CastVoteCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CastVoteCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CastVoteCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CastVoteCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CastVoteCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CastVoteCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CastVoteCall message(String message) {
        return param("message", message);
    }

    public CastVoteCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CastVoteCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CastVoteCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CastVoteCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CastVoteCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CastVoteCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CastVoteCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CastVoteCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CastVoteCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CastVoteCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
