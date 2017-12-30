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

package nxt.http;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountRestrictions;
import nxt.ae.Asset;
import nxt.ae.AssetTransfer;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Generator;
import nxt.ms.Currency;
import nxt.ms.CurrencyTransfer;
import nxt.peer.NetworkHandler;
import nxt.peer.Peers;
import nxt.util.UPnP;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public final class GetState extends APIServlet.APIRequestHandler {

    static final GetState instance = new GetState();

    private GetState() {
        super(new APITag[] {APITag.INFO}, "includeCounts", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        JSONObject response = GetBlockchainStatus.instance.processRequest(req);

        if ("true".equalsIgnoreCase(req.getParameter("includeCounts")) && API.checkPassword(req)) {
            Chain chain = ParameterParser.getChain(req);
            response.put("numberOfTransactions", Nxt.getBlockchain().getTransactionCount(chain));
            response.put("numberOfAccounts", Account.getCount());
            response.put("numberOfAssets", Asset.getCount());
            response.put("numberOfTransfers", AssetTransfer.getCount());
            response.put("numberOfCurrencies", Currency.getCount());
            response.put("numberOfCurrencyTransfers", CurrencyTransfer.getCount());
            response.put("numberOfPrunableMessages", chain.getPrunableMessageHome().getCount());
            response.put("numberOfAccountLeases", Account.getAccountLeaseCount());
            response.put("numberOfActiveAccountLeases", Account.getActiveLeaseCount());
            response.put("numberOfPhasingOnlyAccounts", AccountRestrictions.PhasingOnly.getCount());
            if (chain instanceof ChildChain) {
                ChildChain childChain = (ChildChain) chain;
                int askCount = childChain.getOrderHome().getAskCount();
                int bidCount = childChain.getOrderHome().getBidCount();
                response.put("numberOfOrders", askCount + bidCount);
                response.put("numberOfAskOrders", askCount);
                response.put("numberOfBidOrders", bidCount);
                response.put("numberOfTrades", childChain.getTradeHome().getCount());
                response.put("numberOfBuyOffers", childChain.getExchangeOfferHome().getBuyOfferCount());
                response.put("numberOfSellOffers", childChain.getExchangeOfferHome().getSellOfferCount());
                response.put("numberOfExchangeRequests", childChain.getExchangeRequestHome().getCount());
                response.put("numberOfExchanges", childChain.getExchangeHome().getCount());
                response.put("numberOfAliases", childChain.getAliasHome().getCount());
                response.put("numberOfGoods", childChain.getDigitalGoodsHome().getGoodsCount());
                response.put("numberOfPurchases", childChain.getDigitalGoodsHome().getPurchaseCount());
                response.put("numberOfTags", childChain.getDigitalGoodsHome().getTagCount());
                response.put("numberOfPolls", childChain.getPollHome().getCount());
                response.put("numberOfVotes", childChain.getVoteHome().getCount());
                response.put("numberOfTaggedData", childChain.getTaggedDataHome().getCount());
                response.put("numberOfDataTags", childChain.getTaggedDataHome().getTagCount());
                response.put("numberOfShufflings", childChain.getShufflingHome().getCount());
                response.put("numberOfActiveShufflings", childChain.getShufflingHome().getActiveCount());
            }
        }
        response.put("numberOfPeers", Peers.getAllPeers().size());
        response.put("numberOfConnectedPeers", NetworkHandler.getConnectionCount());
        response.put("numberOfUnlockedAccounts", Generator.getAllGenerators().size());
        response.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        response.put("maxMemory", Runtime.getRuntime().maxMemory());
        response.put("totalMemory", Runtime.getRuntime().totalMemory());
        response.put("freeMemory", Runtime.getRuntime().freeMemory());
        response.put("peerPort", NetworkHandler.getDefaultPeerPort());
        response.put("isOffline", Constants.isOffline);
        response.put("needsAdminPassword", !API.disableAdminPassword);
        response.put("customLoginWarning", Constants.customLoginWarning);
        InetAddress externalAddress = UPnP.getExternalAddress();
        if (externalAddress != null) {
            response.put("upnpExternalAddress", externalAddress.getHostAddress());
        }
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}
