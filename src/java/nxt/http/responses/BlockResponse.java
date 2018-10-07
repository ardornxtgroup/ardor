package nxt.http.responses;

import nxt.addons.JO;
import nxt.blockchain.ChainTransactionId;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.List;

public interface BlockResponse {

    static BlockResponse create(Object object) {
        if (object instanceof JSONObject) {
            return new BlockResponseImpl((JSONObject) object);
        } else {
            return new BlockResponseImpl((JO) object);
        }
    }

    long getBlockId();

    String getBlock();

    int getHeight();

    long getGeneratorId();

    String getGenerator();

    String getGeneratorRs();

    byte[] getGeneratorPublicKey();

    int getTimestamp();

    int getNumberOfTransactions();

    long getTotalFeeFQT();

    byte getVersion();

    long getBaseTarget();

    BigInteger getCumulativeDifficulty();

    long getPreviousBlockId();

    String getPreviousBlock();

    long getNextBlockId();

    String getNextBlock();

    byte[] getPayloadHash();

    byte[] getGenerationSignature();

    byte[] getPreviousBlockHash();

    byte[] getBlockSignature();

    List<byte[]> getParentTransactionFullHashes();

    List<TransactionResponse> getParentTransactions();

    List<ChainTransactionId> getExecutedPhasedTransactionIds();

    List<TransactionResponse> getExecutedPhasedTransactions();
}
