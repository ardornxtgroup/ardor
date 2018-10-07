package nxt.http.responses;

import nxt.addons.JO;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

public interface TransactionResponse {

    static TransactionResponse create(Object object) {
        if (object instanceof JSONObject) {
            return new TransactionResponseImpl((JSONObject) object);
        } else {
            return new TransactionResponseImpl((JO) object);
        }
    }

    TransactionType getTransactionType();

    int getChainId();

    long getSenderId();

    String getSender();

    String getSenderRs();

    byte[] getSenderPublicKey();

    long getRecipientId();

    String getRecipient();

    String getRecipientRs();

    int getHeight();

    long getBlockId();

    short getIndex();

    int getTimestamp();

    int getBlockTimestamp();

    short getDeadline();

    int getExpiration();

    long getAmount();

    long getFee();

    byte[] getSignature();

    byte[] getFullHash();

    byte getType();

    byte getSubType();

    byte getVersion();

    int getECBlockHeight();

    long getECBlockId();

    boolean isPhased();

    long getFxtTransaction();

    long getTransactionId();

    String getUnsignedLongTransactionId();

    int getConfirmations();

    int getBlockTimeStamp();

    ChainTransactionId getReferencedTransaction();

    byte[] getSignatureHash();

    int getEcBlockHeight();

    long getEcBlockId();

    boolean isBundled();

    long getRandomSeed(String secretPhrase);

    JO getAttachmentJson();

    JO getJson();
}
