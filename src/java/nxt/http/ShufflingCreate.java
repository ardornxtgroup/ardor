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
import nxt.account.HoldingType;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.shuffling.ShufflingCreationAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ShufflingCreate extends CreateTransaction {

    static final ShufflingCreate instance = new ShufflingCreate();

    private ShufflingCreate() {
        super(new APITag[] {APITag.SHUFFLING, APITag.CREATE_TRANSACTION},
                "holding", "holdingType", "amount", "participantCount", "registrationPeriod");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        HoldingType holdingType = ParameterParser.getHoldingType(req);
        ChildChain childChain = ParameterParser.getChildChain(req);
        long holdingId = holdingType != HoldingType.COIN ? ParameterParser.getHoldingId(req) : childChain.getId();
        long amount = ParameterParser.getLong(req, "amount", 0L, Long.MAX_VALUE, true);
        if (holdingType == HoldingType.COIN && amount < childChain.SHUFFLING_DEPOSIT_NQT) {
            return JSONResponses.incorrect("amount", String.format("Minimum shuffling amount is %f %s",
                    ((double) childChain.SHUFFLING_DEPOSIT_NQT) / childChain.ONE_COIN, childChain.getName()));
        }
        byte participantCount = ParameterParser.getByte(req, "participantCount", Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS,
                Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS, true);
        short registrationPeriod = (short)ParameterParser.getInt(req, "registrationPeriod", 0, Constants.MAX_SHUFFLING_REGISTRATION_PERIOD, true);
        Attachment attachment = new ShufflingCreationAttachment(holdingId, holdingType, amount, participantCount, registrationPeriod);
        Account account = ParameterParser.getSenderAccount(req);
        if (account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return JSONResponses.error("Accounts under phasing only control cannot start a shuffling");
        }
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return JSONResponses.notEnoughHolding(holdingType);
        }
    }

}
