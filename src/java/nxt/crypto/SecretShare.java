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

package nxt.crypto;

import java.math.BigInteger;
import java.util.Objects;

public class SecretShare {
    private final int x;
    private final BigInteger share;

    public SecretShare(final int x, final BigInteger share) {
        this.x = x;
        this.share = share;
    }

    public int getX() {
        return x;
    }

    public BigInteger getShare() {
        return share;
    }

    @Override
    public String toString() {
        return "SecretShare [x=" + x + ", share=" + share + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecretShare that = (SecretShare) o;
        return x == that.x && Objects.equals(share, that.share);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, share);
    }
}
