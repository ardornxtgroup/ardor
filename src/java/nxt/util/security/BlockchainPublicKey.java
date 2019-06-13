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

import nxt.util.Convert;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * Represent an account public key
 */
public class BlockchainPublicKey implements PublicKey {

    private final byte[] bytes;

    public BlockchainPublicKey(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String getAlgorithm() {
        return "Blockchain";
    }

    @Override
    public String getFormat() {
        return "BlockchainKey";
    }

    @Override
    public byte[] getEncoded() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockchainPublicKey that = (BlockchainPublicKey) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "publicKey:" + Convert.toHexString(bytes);
    }
}
