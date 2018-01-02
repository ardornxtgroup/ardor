/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
import nxt.NxtException;
import nxt.http.APIEnum;
import nxt.http.APITag;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class FxtChain extends Chain {

    public static final String FXT_NAME = "ARDR";

    public static final FxtChain FXT = new FxtChain();

    public static void init() {}

    private FxtChain() {
        super(1, FXT_NAME, 8, Constants.isTestnet ? 99949858899030000L : 99846623125660000L, EnumSet.of(APIEnum.SEND_MESSAGE), EnumSet.of(APITag.ALIASES, APITag.AE, APITag.DGS,
                APITag.DATA, APITag.MS, APITag.SHUFFLING, APITag.VS));
    }

    @Override
    public String getDbSchema() {
        return "PUBLIC";
    }

    @Override
    public boolean isAllowed(TransactionType transactionType) {
        return transactionType.getType() < 0;
    }

    @Override
    public Set<TransactionType> getDisabledTransactionTypes() {
        return Collections.emptySet();
    }

    @Override
    public FxtTransactionImpl.BuilderImpl newTransactionBuilder(byte[] senderPublicKey, long amount, long fee, short deadline, Attachment attachment) throws NxtException.NotValidException {
        return FxtTransactionImpl.newTransactionBuilder((byte)1, senderPublicKey, amount, fee, deadline, (Attachment.AbstractAttachment)attachment);
    }

    @Override
    FxtTransactionImpl.BuilderImpl newTransactionBuilder(byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                         List<Appendix.AbstractAppendix> appendages, JSONObject transactionData) throws NxtException.NotValidException {
        return FxtTransactionImpl.newTransactionBuilder(version, senderPublicKey, amount, fee, deadline,
                appendages, transactionData);
    }

    @Override
    FxtTransactionImpl.BuilderImpl newTransactionBuilder(byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                         List<Appendix.AbstractAppendix> appendages, ByteBuffer buffer) throws NxtException.NotValidException {
        return FxtTransactionImpl.newTransactionBuilder(version, senderPublicKey, amount, fee, deadline,
                appendages, buffer);
    }

    @Override
    FxtTransactionImpl.BuilderImpl newTransactionBuilder(byte version, long amount, long fee, short deadline,
                                                         List<Appendix.AbstractAppendix> appendages, ResultSet rs) throws NxtException.NotValidException {
        return FxtTransactionImpl.newTransactionBuilder(version, amount, fee, deadline, appendages, rs);
    }

    @Override
    UnconfirmedTransaction newUnconfirmedTransaction(ResultSet rs) throws SQLException, NxtException.NotValidException {
        return new UnconfirmedFxtTransaction(rs);
    }
}
