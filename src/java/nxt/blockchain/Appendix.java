/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt.blockchain;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public interface Appendix {

    int getAppendixType();
    int getSize();
    int getFullSize();
    void putBytes(ByteBuffer buffer);
    JSONObject getJSONObject();
    byte getVersion();
    int getBaselineFeeHeight();
    Fee getBaselineFee(Transaction transaction);
    int getNextFeeHeight();
    Fee getNextFee(Transaction transaction);
    Fee getFee(Transaction transaction, int height);
    boolean isPhased(Transaction transaction);
    boolean isAllowed(Chain chain);

    interface Prunable {
        byte[] getHash();
        boolean hasPrunableData();
        void restorePrunableData(Transaction transaction, int blockTimestamp, int height);
        default boolean shouldLoadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
            return Nxt.getEpochTime() - transaction.getTimestamp() <
                    (includeExpiredPrunable && Constants.INCLUDE_EXPIRED_PRUNABLE ?
                            Constants.MAX_PRUNABLE_LIFETIME : Constants.MIN_PRUNABLE_LIFETIME);
        }
        void putMyPrunableBytes(ByteBuffer buffer);
        int getMyFullSize();
    }

    interface Encryptable {
        void encrypt(String secretPhrase);
    }

    interface Parser {
        AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException;
        AbstractAppendix parse(JSONObject attachmentData) throws NxtException.NotValidException;
    }

    abstract class AbstractAppendix implements Appendix {

        private final byte version;

        protected AbstractAppendix(JSONObject attachmentData) {
            version = ((Long) attachmentData.get("version." + getAppendixName())).byteValue();
        }

        protected AbstractAppendix(ByteBuffer buffer) {
            this.version = buffer.get();
        }

        protected AbstractAppendix(int version) {
            this.version = (byte) version;
        }

        protected AbstractAppendix() {
            this.version = 1;
        }

        public abstract String getAppendixName();

        @Override
        public final int getSize() {
            return getMySize() + (version > 0 ? 1 : 0);
        }

        @Override
        public final int getFullSize() {
            if (this instanceof Prunable && ((Prunable)this).hasPrunableData()) {
                return ((Prunable)this).getMyFullSize() + 1;
            } else {
                return getSize();
            }
        }

        protected abstract int getMySize();

        @Override
        public final void putBytes(ByteBuffer buffer) {
            if (version > 0) {
                buffer.put(version);
            }
            putMyBytes(buffer);
        }

        protected abstract void putMyBytes(ByteBuffer buffer);

        final void putPrunableBytes(ByteBuffer buffer) {
            if (this instanceof Prunable && ((Prunable)this).hasPrunableData()) {
                buffer.put(version);
                ((Prunable)this).putMyPrunableBytes(buffer);
            } else {
                putBytes(buffer);
            }
        }

        @Override
        public final JSONObject getJSONObject() {
            JSONObject json = new JSONObject();
            json.put("version." + getAppendixName(), version);
            putMyJSON(json);
            return json;
        }

        protected abstract void putMyJSON(JSONObject json);

        @Override
        public final byte getVersion() {
            return version;
        }

        public boolean verifyVersion() {
            return version == 1;
        }

        @Override
        public int getBaselineFeeHeight() {
            return 0;
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return Fee.NONE;
        }

        @Override
        public int getNextFeeHeight() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Fee getNextFee(Transaction transaction) {
            return getBaselineFee(transaction);
        }

        @Override
        public final Fee getFee(Transaction transaction, int height) {
            return height >= getNextFeeHeight() ? getNextFee(transaction) : getBaselineFee(transaction);
        }

        public abstract void validate(Transaction transaction) throws NxtException.ValidationException;

        public void validateId(Transaction transaction) throws NxtException.ValidationException {}

        public void validateAtFinish(Transaction transaction) throws NxtException.ValidationException {
            if (!isPhased(transaction)) {
                return;
            }
            validate(transaction);
        }

        public abstract void apply(Transaction transaction, Account senderAccount, Account recipientAccount);

        public final void loadPrunable(Transaction transaction) {
            loadPrunable(transaction, false);
        }

        public void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {}

        public abstract boolean isPhasable();

        @Override
        public final boolean isPhased(Transaction transaction) {
            return isPhasable() && transaction instanceof ChildTransaction && ((ChildTransaction)transaction).getPhasing() != null;
        }

    }

    static boolean hasAppendix(String appendixName, JSONObject attachmentData) {
        return attachmentData.get("version." + appendixName) != null;
    }

}
