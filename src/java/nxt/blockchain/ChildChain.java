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
import nxt.ae.AssetDividendHome;
import nxt.ae.OrderHome;
import nxt.ae.TradeHome;
import nxt.aliases.AliasHome;
import nxt.dgs.DigitalGoodsHome;
import nxt.dgs.DigitalGoodsTransactionType;
import nxt.http.APIEnum;
import nxt.http.APITag;
import nxt.ms.CurrencyFounderHome;
import nxt.ms.ExchangeHome;
import nxt.ms.ExchangeOfferHome;
import nxt.ms.ExchangeRequestHome;
import nxt.shuffling.ShufflingHome;
import nxt.shuffling.ShufflingParticipantHome;
import nxt.shuffling.ShufflingTransactionType;
import nxt.taggeddata.TaggedDataHome;
import nxt.voting.PhasingPollHome;
import nxt.voting.PhasingVoteHome;
import nxt.voting.PollHome;
import nxt.voting.VoteHome;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChildChain extends Chain {

    private static final Map<String, ChildChain> childChains = new HashMap<>();
    private static final Map<Integer, ChildChain> childChainsById = new HashMap<>();

    private static final Collection<ChildChain> allChildChains = Collections.unmodifiableCollection(childChains.values());

    public static final ChildChain IGNIS = new ChildChain(2, "IGNIS", 8, Constants.isTestnet ? 99972484729793502L : 99944969459860052L, (Constants.isTestnet ? 7 : 10) * 100000000,
            Collections.emptySet(), EnumSet.noneOf(APIEnum.class), EnumSet.noneOf(APITag.class));
    public static final ChildChain AEUR = new ChildChain(3, "AEUR", 4, Constants.isTestnet ? 9997248455672L : 100000000000L, 0, new LinkedHashSet<>(Arrays.asList(ShufflingTransactionType.SHUFFLING_CREATION)),
                    EnumSet.noneOf(APIEnum.class), EnumSet.of(APITag.SHUFFLING));
    public static final ChildChain BITSWIFT = new ChildChain(4, "BITSWIFT", 8, Constants.isTestnet ? 388463474549710L : 388463474539339L, 10 * 100000000L,
            new LinkedHashSet<>(Arrays.asList(DigitalGoodsTransactionType.LISTING)), EnumSet.noneOf(APIEnum.class), EnumSet.of(APITag.DGS));

    public static ChildChain getChildChain(String name) {
        return childChains.get(name);
    }

    public static ChildChain getChildChain(int id) {
        return childChainsById.get(id);
    }

    public static Collection<ChildChain> getAll() {
        return allChildChains;
    }

    public static void init() {}

    public final long SHUFFLING_DEPOSIT_NQT;

    private final AliasHome aliasHome;
    private final AssetDividendHome assetDividendHome;
    private final CurrencyFounderHome currencyFounderHome;
    private final DigitalGoodsHome digitalGoodsHome;
    private final ExchangeHome exchangeHome;
    private final ExchangeOfferHome exchangeOfferHome;
    private final ExchangeRequestHome exchangeRequestHome;
    private final OrderHome orderHome;
    private final PhasingPollHome phasingPollHome;
    private final PhasingVoteHome phasingVoteHome;
    private final PollHome pollHome;
    private final ShufflingHome shufflingHome;
    private final ShufflingParticipantHome shufflingParticipantHome;
    private final TaggedDataHome taggedDataHome;
    private final TradeHome tradeHome;
    private final VoteHome voteHome;
    private final Set<TransactionType> disabledTransactionTypes;

    private ChildChain(int id, String name, int decimals, long totalAmount, long shufflingDepositNQT, Set<TransactionType> disabledTransactionTypes,
                       EnumSet<APIEnum> disabledAPIs, EnumSet<APITag> disabledAPITags) {
        super(id, name, decimals, totalAmount, disabledAPIs, disabledAPITags);
        this.SHUFFLING_DEPOSIT_NQT = shufflingDepositNQT;
        this.aliasHome = AliasHome.forChain(this);
        this.assetDividendHome = AssetDividendHome.forChain(this);
        this.currencyFounderHome = CurrencyFounderHome.forChain(this);
        this.digitalGoodsHome = DigitalGoodsHome.forChain(this);
        this.exchangeHome = ExchangeHome.forChain(this);
        this.exchangeOfferHome = ExchangeOfferHome.forChain(this);
        this.exchangeRequestHome = ExchangeRequestHome.forChain(this);
        this.tradeHome = TradeHome.forChain(this);
        this.orderHome = OrderHome.forChain(this);
        this.phasingVoteHome = PhasingVoteHome.forChain(this);
        this.phasingPollHome = PhasingPollHome.forChain(this);
        this.pollHome = PollHome.forChain(this);
        this.shufflingHome = ShufflingHome.forChain(this);
        this.shufflingParticipantHome = ShufflingParticipantHome.forChain(this);
        this.taggedDataHome = TaggedDataHome.forChain(this);
        this.voteHome = VoteHome.forChain(this);
        this.disabledTransactionTypes = Collections.unmodifiableSet(disabledTransactionTypes);
        childChains.put(name, this);
        childChainsById.put(id, this);
    }

    public AliasHome getAliasHome() {
        return aliasHome;
    }

    public AssetDividendHome getAssetDividendHome() {
        return assetDividendHome;
    }

    public CurrencyFounderHome getCurrencyFounderHome() {
        return currencyFounderHome;
    }

    public DigitalGoodsHome getDigitalGoodsHome() {
        return digitalGoodsHome;
    }

    public ExchangeHome getExchangeHome() {
        return exchangeHome;
    }

    public ExchangeOfferHome getExchangeOfferHome() {
        return exchangeOfferHome;
    }

    public ExchangeRequestHome getExchangeRequestHome() {
        return exchangeRequestHome;
    }

    public OrderHome getOrderHome() {
        return orderHome;
    }

    public PhasingPollHome getPhasingPollHome() {
        return phasingPollHome;
    }

    public PhasingVoteHome getPhasingVoteHome() {
        return phasingVoteHome;
    }

    public PollHome getPollHome() {
        return pollHome;
    }

    public ShufflingHome getShufflingHome() {
        return shufflingHome;
    }

    public ShufflingParticipantHome getShufflingParticipantHome() {
        return shufflingParticipantHome;
    }

    public TaggedDataHome getTaggedDataHome() {
        return taggedDataHome;
    }

    public TradeHome getTradeHome() {
        return tradeHome;
    }

    public VoteHome getVoteHome() {
        return voteHome;
    }

    @Override
    public boolean isAllowed(TransactionType transactionType) {
        return transactionType.getType() >= 0 && !disabledTransactionTypes.contains(transactionType) && (this == IGNIS || !transactionType.isGlobal());
    }

    @Override
    public Set<TransactionType> getDisabledTransactionTypes() {
        return disabledTransactionTypes;
    }

    @Override
    public ChildTransactionImpl.BuilderImpl newTransactionBuilder(byte[] senderPublicKey, long amount, long fee, short deadline, Attachment attachment) throws NxtException.NotValidException {
        return ChildTransactionImpl.newTransactionBuilder(this.getId(), (byte)1, senderPublicKey, amount, fee, deadline, (Attachment.AbstractAttachment)attachment);
    }

    @Override
    ChildTransactionImpl.BuilderImpl newTransactionBuilder(byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                           List<Appendix.AbstractAppendix> appendages, JSONObject transactionData) throws NxtException.NotValidException {
        return ChildTransactionImpl.newTransactionBuilder(this.getId(), version, senderPublicKey, amount, fee, deadline,
                appendages, transactionData);
    }

    @Override
    ChildTransactionImpl.BuilderImpl newTransactionBuilder(byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                           List<Appendix.AbstractAppendix> appendages, ByteBuffer buffer) throws NxtException.NotValidException {
        return ChildTransactionImpl.newTransactionBuilder(this.getId(), version, senderPublicKey, amount, fee, deadline,
                appendages, buffer);
    }

    @Override
    ChildTransactionImpl.BuilderImpl newTransactionBuilder(byte version, long amount, long fee, short deadline,
                                                           List<Appendix.AbstractAppendix> appendages, ResultSet rs) throws NxtException.NotValidException {
        return ChildTransactionImpl.newTransactionBuilder(this.getId(), version, amount, fee, deadline, appendages, rs);
    }

    @Override
    UnconfirmedTransaction newUnconfirmedTransaction(ResultSet rs) throws SQLException, NxtException.NotValidException {
        return new UnconfirmedChildTransaction(rs);
    }

}
