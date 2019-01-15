/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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
package nxt.peer;

import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.util.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nxt.Constants;

public final class BundlerRate {

    /** Empty public key */
    private static final byte[] emptyPublicKey = new byte[32];

    /** Empty signature */
    private static final byte[] emptySignature = new byte[64];

    /**
     * Process a BundlerRate message (there is no response message)
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.BundlerRateMessage request) {
        List<BundlerRate> rates = request.getRates();
        //
        // Verify the bundler accounts
        //
        List<BundlerRate> validRates = new ArrayList<>();
        long currentAccountId = 0;
        byte[] publicKey = null;
        long balance = 0;
        for (BundlerRate rate : rates) {
            long accountId = rate.getAccountId();
            if (currentAccountId != accountId) {
                Account account = Account.getAccount(accountId);
                if (account == null) {
                    Logger.logDebugMessage("Bundler account "
                            + Long.toUnsignedString(accountId) + " does not exist");
                    publicKey = null;
                } else {
                    publicKey = Account.getPublicKey(accountId);
                    if (publicKey == null) {
                        Logger.logDebugMessage("Bundler account "
                                + Long.toUnsignedString(accountId) + " does not have a public key");
                    } else {
                        balance = account.getEffectiveBalanceFXT();
                    }
                }
                currentAccountId = accountId;
            }
            if (publicKey == null || Peers.isBundlerBlacklisted(accountId)) {
                continue;
            }
            if (!Crypto.verify(rate.getSignature(), rate.getUnsignedBytes(), rate.getPublicKey()) ||
                        !Arrays.equals(rate.getPublicKey(), publicKey)) {
                Logger.logDebugMessage("Bundler rate for account "
                        + Long.toUnsignedString(accountId) + " failed signature verification");
            } else if (balance >= Peers.minBundlerBalanceFXT &&
                    rate.getFeeLimit() >= Peers.minBundlerFeeLimitFXT * Constants.ONE_FXT) {
                rate.setBalance(balance);
                validRates.add(rate);
            }
        }
        //
        // Update the rates and relay the message
        //
        Peers.updateBundlerRates(peer, request, validRates);
        return null;
    }

    /** Bundler chain */
    private final ChildChain chain;

    /** Bundler rate */
    private final long rate;

    /** Current fee limit */
    private final long feeLimit;

    /** Bundler account */
    private final long accountId;

    /** Bundler public key */
    private final byte[] publicKey;

    /** Timestamp */
    private final int timestamp;

    /** Signature */
    private final byte[] signature;

    /** Account FXT balance (not sent to peers) */
    private long accountBalance;

    /**
     * Create an unsigned bundler rate
     *
     * @param   chain                   Child chain
     * @param   accountId               Account identifier
     * @param   rate                    Bundler rate
     * @param   feeLimit                Current fee limit
     */
    public BundlerRate(ChildChain chain, long accountId, long rate, long feeLimit) {
        this.chain = chain;
        this.publicKey = emptyPublicKey;
        this.accountId = accountId;
        this.rate = rate;
        this.feeLimit = feeLimit;
        this.timestamp = 0;
        this.signature = emptySignature;
    }

    /**
     * Create a signed bundler rate
     *
     * @param   chain                   Child chain
     * @param   rate                    Bundler rate
     * @param   feeLimit                Current fee limit
     * @param   secretPhrase            Bundler account secret phrase
     */
    public BundlerRate(ChildChain chain, long rate, long feeLimit, String secretPhrase) {
        this.chain = chain;
        this.publicKey = Crypto.getPublicKey(secretPhrase);
        this.accountId = Account.getId(publicKey);
        this.rate = rate;
        this.feeLimit = feeLimit;
        //rounded to 10 minutes for privacy reasons
        this.timestamp = (Nxt.getEpochTime() / 600) * 600;
        this.signature = Crypto.sign(getUnsignedBytes(), secretPhrase);
    }

    /**
     * Create a signed bundler rate
     *
     * @param   buffer                      Encoded data
     * @throws  BufferUnderflowException    Encoded data is too short
     * @throws  NetworkException            Encoded data is not valid
     */
    public BundlerRate(ByteBuffer buffer) throws BufferUnderflowException, NetworkException {
        int chainId = buffer.getInt();
        this.chain = ChildChain.getChildChain(chainId);
        if (this.chain == null) {
            throw new NetworkException("Child chain '" + chainId + "' is not valid");
        }
        this.publicKey = new byte[32];
        buffer.get(this.publicKey);
        this.accountId = Account.getId(this.publicKey);
        this.rate = buffer.getLong();
        this.feeLimit = buffer.getLong();
        this.timestamp = buffer.getInt();
        this.signature = new byte[64];
        buffer.get(this.signature);
    }

    /**
     * Get the encoded length
     *
     * @return                          Encoded length
     */
    public int getLength() {
        return 4 + 32 + 8 + 8 + 4 + 64;
    }

    /**
     * Get our bytes
     *
     * @param   buffer                      Byte buffer
     * @throws  BufferOverflowException     Allocated buffer is too small
     */
    public void getBytes(ByteBuffer buffer) {
        buffer.putInt(chain.getId())
              .put(publicKey)
              .putLong(rate)
              .putLong(feeLimit)
              .putInt(timestamp)
              .put(signature);
    }

    /**
     * Get the unsigned bytes
     *
     * @return                              Rate bytes
     */
    public byte[] getUnsignedBytes() {
        byte[] bytes = new byte[getLength() - 64];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(chain.getId())
              .put(publicKey)
              .putLong(rate)
              .putLong(feeLimit)
              .putInt(timestamp);
        return bytes;
    }

    /**
     * Get the child chain
     *
     * @return                          Child chain
     */
    public ChildChain getChain() {
        return chain;
    }

    /**
     * Get the account identifier
     *
     * @return                          Account identifier
     */
    public long getAccountId() {
        return accountId;
    }

    /**
     * Get the account public key
     *
     * @return                          Account public key
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Get the bundler rate
     *
     * @return                          Rate
     */
    public long getRate() {
        return rate;
    }

    /**
     * Get the fee limit
     *
     * @return                          Fee limit
     */
    public long getFeeLimit() {
        return feeLimit;
    }

    /**
     * Get the timestamp
     *
     * @return                          Timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Get the signature
     *
     * @return                          Signature
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Get the account balance
     *
     * @return                          Account balance
     */
    public long getBalance() {
        return accountBalance;
    }

    /**
     * Set the account balance
     *
     * @param   balance                 Account balance
     */
    public void setBalance(long balance) {
        accountBalance = balance;
    }

    /**
     * Get the hash code
     *
     * @return                          Hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(chain.getId()) ^ Long.hashCode(accountId) ^ Long.hashCode(rate) ^
                    Integer.hashCode(timestamp);
    }

    /**
     * Check if two bundler rates are equal
     *
     * @param   obj                     Bundler rate to compare
     * @return                          TRUE if the rates are equal
     */
    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof BundlerRate) &&
                chain == ((BundlerRate)obj).chain &&
                accountId == ((BundlerRate)obj).accountId &&
                rate == ((BundlerRate)obj).rate &&
                timestamp == ((BundlerRate)obj).timestamp);
    }
}
