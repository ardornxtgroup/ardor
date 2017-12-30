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
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.ChildChain;
import nxt.voting.PhasingParams;
import nxt.voting.SetPhasingOnlyAttachment;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Sets an account control that blocks transactions unless they are phased with certain parameters
 * 
 * <p>
 * Parameters
 * <ul>
 * <li>controlVotingModel - The expected voting model of the phasing. Possible values: 
 *  <ul>
 *  <li>NONE(-1) - the phasing control is removed</li>
 *  <li>ACCOUNT(0) - only by-account voting is allowed</li>
 *  <li>NQT(1) - only balance voting is allowed</li>
 *  <li>ASSET(2) - only asset voting is allowed</li>
 *  <li>CURRENCY(3) - only currency voting is allowed</li>
 *  </ul>
 * </li>
 * <li>controlQuorum - The expected quorum.</li>
 * <li>controlMinBalance - The expected minimum balance</li>
 * <li>controlMinBalanceModel - The expected minimum balance model. Possible values:
 * <ul>
 *  <li>NONE(0) No minimum balance restriction</li>
 *  <li>NQT(1) Nxt balance threshold</li>
 *  <li>ASSET(2) Asset balance threshold</li>
 *  <li>CURRENCY(3) Currency balance threshold</li>
 * </ul>
 * </li>
 * <li>controlHolding - The expected holding ID - asset ID or currency ID.</li>
 * <li>controlWhitelisted - multiple values - the expected whitelisted accounts</li>
 * <li>controlMaxFees - The maximum allowed accumulated total fees for not yet finished phased transactions, as ':' separated chainId:maxFee values.</li>
 * <li>controlMinDuration - The minimum phasing duration (finish height minus current height).</li>
 * <li>controlHolding - The maximum allowed phasing duration.</li>
 * </ul>
 *
 * 
 */
public final class SetPhasingOnlyControl extends CreateTransaction {

    static final SetPhasingOnlyControl instance = new SetPhasingOnlyControl();

    private SetPhasingOnlyControl() {
        super(new APITag[] {APITag.ACCOUNT_CONTROL, APITag.CREATE_TRANSACTION}, "controlVotingModel", "controlQuorum", "controlMinBalance",
                "controlMinBalanceModel", "controlHolding", "controlWhitelisted", "controlWhitelisted", "controlWhitelisted",
                "controlMaxFees", "controlMaxFees", "controlMaxFees",
                "controlMinDuration", "controlMaxDuration", "controlParams");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        Account account = ParameterParser.getSenderAccount(request);
        JSONObject controlParamsJson = ParameterParser.getJson(request, "controlParams");
        PhasingParams phasingParams;
        if (controlParamsJson != null) {
            phasingParams = new PhasingParams(controlParamsJson);
        } else {
            phasingParams = ParameterParser.parsePhasingParams(request, "control");
        }
        SortedMap<Integer,Long> maxFees = new TreeMap<>();
        String[] maxFeeValues = request.getParameterValues("controlMaxFees");
        if (maxFeeValues != null && maxFeeValues.length > 0) {
            for (String chainMaxFee : maxFeeValues) {
                String[] s = chainMaxFee.split(":");
                if (s.length != 2) {
                    return JSONResponses.incorrect("controlMaxFees");
                }
                int chainId = Integer.parseInt(s[0]);
                ChildChain chain = ChildChain.getChildChain(chainId);
                if (chain == null) {
                    return JSONResponses.UNKNOWN_CHAIN;
                }
                long maxFee = Long.parseLong(s[1]);
                if (maxFee <= 0 || maxFee > Constants.MAX_BALANCE_NQT) {
                    return JSONResponses.incorrect("controlMaxFees");
                }
                maxFees.put(chainId, maxFee);
            }
        }
        short minDuration = (short)ParameterParser.getInt(request, "controlMinDuration", 0, Constants.MAX_PHASING_DURATION - 1, false);
        short maxDuration = (short) ParameterParser.getInt(request, "controlMaxDuration", 0, Constants.MAX_PHASING_DURATION - 1, false);
        return createTransaction(request, account, new SetPhasingOnlyAttachment(phasingParams, maxFees, minDuration, maxDuration));
    }

    @Override
    public boolean isIgnisOnly() {
        return true;
    }

}
