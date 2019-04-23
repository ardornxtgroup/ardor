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
import nxt.account.Account;
import nxt.account.FundingMonitor;
import nxt.account.HoldingType;
import nxt.ae.Asset;
import nxt.blockchain.Chain;
import nxt.blockchain.FxtChain;
import nxt.crypto.Crypto;
import nxt.ms.Currency;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.MONITOR_ALREADY_STARTED;
import static nxt.http.JSONResponses.UNKNOWN_ACCOUNT;

/**
 * Start a funding monitor
 * <p>
 * A funding monitor will transfer COIN, ASSET or CURRENCY from the funding account
 * to a recipient account when the amount held by the recipient account drops below
 * the threshold.  The transfer will not be done until the current block
 * height is greater than equal to the block height of the last transfer plus the
 * interval. Holding type codes are listed in getConstants. The asset or currency is
 * specified by the holding identifier.
 * <p>
 * The funding account is identified by the secret phrase.  The secret phrase must
 * be specified since the funding monitor needs to sign the transactions that it submits.
 * <p>
 * The recipient accounts are identified by the specified account property.  Each account
 * that has this property set by the funding account will be monitored for changes.
 * The property value can be omitted or it can consist of a JSON string containing one or more
 * values in the format: {"amount":long,"threshold":long,"interval":integer}
 * <p>
 * The long values can be specified as numeric values or as strings.
 * <p>
 * For example, {"amount":25,"threshold":10,"interval":1440}.  The specified values will
 * override the default values specified when the account monitor is started.
 * <p>
 * Coin, Asset and Currency decimal places are determined by the chain, asset or currency definition.
 */
public final class StartFundingMonitor extends APIServlet.APIRequestHandler {

    static final StartFundingMonitor instance = new StartFundingMonitor();

    private StartFundingMonitor() {
        super(new APITag[] {APITag.ACCOUNTS}, "holdingType", "holding", "property", "amount", "threshold",
                "interval", "secretPhrase", "feeRateNQTPerFXT");
    }

    /**
     * Process the request
     *
     * @param   req                 Client request
     * @return                      Client response
     * @throws  NxtException        Unable to process request
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Chain chain = ParameterParser.getChain(req);
        HoldingType holdingType = ParameterParser.getHoldingType(req);
        long holdingId = ParameterParser.getHoldingId(req);
        String property = ParameterParser.getAccountProperty(req, true);
        long amount = ParameterParser.getLong(req, "amount", 0, Long.MAX_VALUE, true);
        if (amount < FundingMonitor.MIN_FUND_AMOUNT) {
            return JSONResponses.incorrect("amount", "Minimum funding amount is " + FundingMonitor.MIN_FUND_AMOUNT);
        }
        long threshold = ParameterParser.getLong(req, "threshold", 0, Long.MAX_VALUE, true);
        if (threshold < FundingMonitor.MIN_FUND_THRESHOLD) {
            return JSONResponses.incorrect("threshold", "Minimum funding threshold is " + FundingMonitor.MIN_FUND_THRESHOLD);
        }
        int interval = ParameterParser.getInt(req, "interval", FundingMonitor.MIN_FUND_INTERVAL, Integer.MAX_VALUE, true);
        long feeRateNQTPerFXT = ParameterParser.getLong(req, "feeRateNQTPerFXT", 0, Constants.MAX_BALANCE_NQT, true);
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        switch (holdingType) {
            case ASSET:
                if (chain == FxtChain.FXT) {
                    return JSONResponses.INCORRECT_CHAIN;
                }
                Asset asset = Asset.getAsset(holdingId);
                if (asset == null) {
                    return JSONResponses.UNKNOWN_ASSET;
                }
                break;
            case CURRENCY:
                if (chain == FxtChain.FXT) {
                    return JSONResponses.INCORRECT_CHAIN;
                }
                Currency currency = Currency.getCurrency(holdingId);
                if (currency == null) {
                    return JSONResponses.UNKNOWN_CURRENCY;
                }
                break;
            case COIN:
                if (holdingId != chain.getId()) {
                    return JSONResponses.INCORRECT_CHAIN;
                }
        }
        Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
        if (account == null) {
            return UNKNOWN_ACCOUNT;
        }
        FundingMonitor monitor = FundingMonitor.startMonitor(chain, holdingType, holdingId, property, amount, threshold, interval, secretPhrase, feeRateNQTPerFXT);
        if (monitor != null) {
            JSONObject response = new JSONObject();
            response.put("started", true);
            response.put("monitor", JSONData.accountMonitor(monitor, false));
            return response;
        } else {
            return MONITOR_ALREADY_STARTED;
        }
    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
