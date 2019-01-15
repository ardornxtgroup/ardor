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

package nxt.blockchain;

import nxt.NxtException;
import nxt.account.AccountLedger;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Filter;
import org.json.simple.JSONObject;

import java.util.List;

public interface Transaction extends AccountLedger.LedgerEventId {

    interface Builder {

        Builder recipientId(long recipientId);

        Builder timestamp(int timestamp);

        Builder ecBlockHeight(int height);

        Builder ecBlockId(long blockId);

        Builder appendix(Appendix appendix);

        Transaction build() throws NxtException.NotValidException;

        Transaction build(String secretPhrase) throws NxtException.NotValidException;

        Transaction build(String secretPhrase, boolean isVoucher) throws NxtException.NotValidException;

    }

    Chain getChain();

    long getId();

    String getStringId();

    long getSenderId();

    byte[] getSenderPublicKey();

    long getRecipientId();

    int getHeight();

    long getBlockId();

    Block getBlock();

    short getIndex();

    int getTimestamp();

    int getBlockTimestamp();

    short getDeadline();

    int getExpiration();

    long getAmount();

    long getFee();

    long getMinimumFeeFQT();

    byte[] getSignature();

    byte[] getFullHash();

    TransactionType getType();

    Attachment getAttachment();

    boolean verifySignature();

    void validate() throws NxtException.ValidationException;

    byte[] getBytes();

    byte[] getUnsignedBytes();

    byte[] getPrunableBytes();

    JSONObject getJSONObject();

    JSONObject getPrunableAttachmentJSON();

    byte getVersion();

    int getFullSize();

    List<? extends Appendix> getAppendages();

    List<? extends Appendix> getAppendages(boolean includeExpiredPrunable);

    List<? extends Appendix> getAppendages(Filter<Appendix> filter, boolean includeExpiredPrunable);

    int getECBlockHeight();

    long getECBlockId();

    boolean isPhased();

    PrunablePlainMessageAppendix getPrunablePlainMessage();

    PrunableEncryptedMessageAppendix getPrunableEncryptedMessage();

}
