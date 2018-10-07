// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class BundleTransactionsCall extends APICall.Builder<BundleTransactionsCall> {
    private BundleTransactionsCall() {
        super("bundleTransactions");
    }

    public static BundleTransactionsCall create(int chain) {
        BundleTransactionsCall instance = new BundleTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public BundleTransactionsCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public BundleTransactionsCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public BundleTransactionsCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public BundleTransactionsCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public BundleTransactionsCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public BundleTransactionsCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public BundleTransactionsCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public BundleTransactionsCall transactionFullHash(String... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public BundleTransactionsCall transactionFullHash(byte[]... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public BundleTransactionsCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public BundleTransactionsCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public BundleTransactionsCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public BundleTransactionsCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public BundleTransactionsCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public BundleTransactionsCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public BundleTransactionsCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public BundleTransactionsCall phased(boolean phased) {
        return param("phased", phased);
    }

    public BundleTransactionsCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public BundleTransactionsCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public BundleTransactionsCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public BundleTransactionsCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public BundleTransactionsCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public BundleTransactionsCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public BundleTransactionsCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public BundleTransactionsCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public BundleTransactionsCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public BundleTransactionsCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public BundleTransactionsCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public BundleTransactionsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public BundleTransactionsCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public BundleTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public BundleTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public BundleTransactionsCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public BundleTransactionsCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public BundleTransactionsCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public BundleTransactionsCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public BundleTransactionsCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public BundleTransactionsCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public BundleTransactionsCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public BundleTransactionsCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public BundleTransactionsCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public BundleTransactionsCall message(String message) {
        return param("message", message);
    }

    public BundleTransactionsCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public BundleTransactionsCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public BundleTransactionsCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public BundleTransactionsCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public BundleTransactionsCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public BundleTransactionsCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public BundleTransactionsCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public BundleTransactionsCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public BundleTransactionsCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public BundleTransactionsCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public BundleTransactionsCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public BundleTransactionsCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public BundleTransactionsCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public BundleTransactionsCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public BundleTransactionsCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public BundleTransactionsCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
