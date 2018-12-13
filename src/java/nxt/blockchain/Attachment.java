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

import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public interface Attachment extends Appendix {

    TransactionType getTransactionType();

    abstract class AbstractAttachment extends Appendix.AbstractAppendix implements Attachment {

        protected AbstractAttachment(ByteBuffer buffer) {
            super(buffer);
        }

        protected AbstractAttachment(JSONObject attachmentData) {
            super(attachmentData);
        }

        protected AbstractAttachment(int version) {
            super(version);
        }

        protected AbstractAttachment() {}

        @Override
        public final int getAppendixType() {
            return 0;
        }

        @Override
        public final String getAppendixName() {
            return getTransactionType().getName();
        }

        @Override
        public final void validate(Transaction transaction) throws NxtException.ValidationException {
            getTransactionType().validateAttachment(transaction);
        }

        @Override
        public final void validateId(Transaction transaction) throws NxtException.ValidationException {
            getTransactionType().validateId(transaction);
        }

        @Override
        public final void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            getTransactionType().apply((TransactionImpl) transaction, senderAccount, recipientAccount);
        }

        @Override
        public final Fee getBaselineFee(Transaction transaction) {
            return getTransactionType().getBaselineFee(transaction);
        }

        @Override
        public final Fee getNextFee(Transaction transaction) {
            return getTransactionType().getNextFee(transaction);
        }

        @Override
        public final int getBaselineFeeHeight() {
            return getTransactionType().getBaselineFeeHeight();
        }

        @Override
        public final int getNextFeeHeight() {
            return getTransactionType().getNextFeeHeight();
        }

        @Override
        public final boolean isPhasable() {
            return !(this instanceof Prunable) && getTransactionType().isPhasable();
        }

        @Override
        public final boolean isAllowed(Chain chain) {
            return chain.isAllowed(getTransactionType());
        }

        public final int getFinishValidationHeight(Transaction transaction) {
            return isPhased(transaction) ? ((ChildTransaction)transaction).getPhasing().getFinishHeight() - 1 : Nxt.getBlockchain().getHeight();
        }

    }

    abstract class EmptyAttachment extends AbstractAttachment {

        protected EmptyAttachment() {
            super(0);
        }

        @Override
        protected final int getMySize() {
            return 0;
        }

        @Override
        protected final void putMyBytes(ByteBuffer buffer) {
        }

        @Override
        protected final void putMyJSON(JSONObject json) {
        }

        @Override
        public final boolean verifyVersion() {
            return getVersion() == 0;
        }

    }

    abstract class PropertyDeleteAttachment extends AbstractAttachment {

        private final long propertyId;

        protected PropertyDeleteAttachment(ByteBuffer buffer) {
            super(buffer);
            this.propertyId = buffer.getLong();
        }

        protected PropertyDeleteAttachment(JSONObject attachmentData) {
            super(attachmentData);
            this.propertyId = Convert.parseUnsignedLong((String)attachmentData.get("property"));
        }

        public PropertyDeleteAttachment(long propertyId) {
            this.propertyId = propertyId;
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(propertyId);
        }

        @Override
        protected void putMyJSON(JSONObject attachment) {
            attachment.put("property", Long.toUnsignedString(propertyId));
        }

        public long getPropertyId() {
            return propertyId;
        }
    }
}
