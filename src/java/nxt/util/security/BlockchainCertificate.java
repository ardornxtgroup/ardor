/*
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

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
