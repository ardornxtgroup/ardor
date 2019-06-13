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

package nxt.http;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.FxtChain;
import nxt.blockchain.Transaction;
import nxt.peer.Peers;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;

public final class CalculateFee extends APIServlet.APIRequestHandler {

    static final CalculateFee instance = new CalculateFee();

    private CalculateFee() {
        super(new APITag[]{APITag.TRANSACTIONS}, "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String transactionJSON = Convert.emptyToNull(req.getParameter("transactionJSON"));
        String transactionBytes = Convert.emptyToNull(req.getParameter("transactionBytes"));
        String prunableAttachmentJSON = Convert.emptyToNull(req.getParameter("prunableAttachmentJSON"));
        long minBundlerBalanceFXT = ParameterParser.getLong(req, "minBundlerBalanceFXT", 0, Constants.MAX_BALANCE_FXT, Constants.minBundlerBalanceFXT);
        long minBundlerFeeLimitFQT = ParameterParser.getLong(req, "minBundlerFeeLimitFQT", 0, Constants.MAX_BALANCE_FXT * Constants.ONE_FXT, Constants.minBundlerFeeLimitFXT * Constants.ONE_FXT);

        JSONObject response = new JSONObject();
        try {
            Transaction.Builder builder = ParameterParser.parseTransaction(transactionJSON, transactionBytes, prunableAttachmentJSON);
            Transaction transaction = builder.build();
            long minFeeFQT = transaction.getMinimumFeeFQT();
            response.put("minimumFeeFQT", String.valueOf(minFeeFQT));
            if (transaction.getChain() == FxtChain.FXT) {
                response.put("feeNQT", String.valueOf(minFeeFQT));
            } else {
                long feeRateNQTPerFXT = Peers.getBestBundlerRate(transaction.getChain(), minBundlerBalanceFXT, Math.max(minFeeFQT, minBundlerFeeLimitFQT), Peers.getBestBundlerRateWhitelist());
                BigInteger[] fee = BigInteger.valueOf(minFeeFQT).multiply(BigInteger.valueOf(feeRateNQTPerFXT))
                        .divideAndRemainder(Constants.ONE_FXT_BIG_INTEGER);
                long feeNQT = fee[0].longValueExact() + (fee[1].equals(BigInteger.ZERO) ? 0 : 1);
                response.put("feeNQT", String.valueOf(feeNQT));
            }
        } catch (NxtException.NotValidException e) {
            JSONData.putException(response, e, "Incorrect transaction json or bytes");
        }
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
