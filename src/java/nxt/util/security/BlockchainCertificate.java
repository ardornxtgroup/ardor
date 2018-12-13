package nxt.util.security;

import nxt.blockchain.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * Represent a transaction as certificate encapsulating the transaction sender public key
 */
public class BlockchainCertificate extends Certificate {

    Transaction transaction;
    private byte[] publicKey;
    private byte[] bytes;
    private byte[] signature;

    public BlockchainCertificate(byte[] publicKey) {
        super("BlockchainCertificate");
        this.publicKey = publicKey;
    }

    protected BlockchainCertificate(Transaction transaction) {
        this(transaction.getSenderPublicKey());
        bytes = transaction.getBytes();
        signature = transaction.getSignature();
        this.transaction = transaction;
    }

    @Override
    public byte[] getEncoded() {
        return publicKey;
    }

    @Override
    public void verify(PublicKey key) {
        Crypto.verify(signature, bytes, key.getEncoded());
    }

    @Override
    public void verify(PublicKey key, String sigProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "BlockchainCertificate{" +
                "publicKey=" + Convert.toHexString(publicKey) +
                ", bytes=" + (bytes == null ? "N/A" : Convert.toHexString(bytes)) +
                ", signature=" + (signature == null ? "N/A" : Convert.toHexString(signature)) +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        if (transaction != null) {
            return new BlockchainCertificate(transaction);
        } else {
            return new BlockchainCertificate(publicKey);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        return new BlockchainPublicKey(publicKey);
    }
}
