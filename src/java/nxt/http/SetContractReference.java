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

package nxt.http;

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChainTransactionId;
import nxt.lightcontracts.ContractReferenceAttachment;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_CONTRACT_NAME_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_CONTRACT_PARAMS_LENGTH;

public final class SetContractReference extends CreateTransaction {

    static final SetContractReference instance = new SetContractReference();

    private SetContractReference() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.CREATE_TRANSACTION}, "contractName", "contractParams", "contract");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account senderAccount = ParameterParser.getSenderAccount(req);
        String contractName = Convert.nullToEmpty(req.getParameter("contractName")).trim();
        String contractParams = Convert.nullToEmpty(req.getParameter("contractParams")).trim();

        if (contractName.length() > Constants.MAX_CONTRACT_NAME_LENGTH || contractName.length() == 0) {
            return INCORRECT_CONTRACT_NAME_LENGTH;
        }

        if (contractParams.length() > Constants.MAX_CONTRACT_PARAMS_LENGTH) {
            return INCORRECT_CONTRACT_PARAMS_LENGTH;
        }

        ChainTransactionId contractId = ParameterParser.getChainTransactionId(req, "contract");

        Attachment attachment = new ContractReferenceAttachment(contractName, contractParams, contractId);
        return createTransaction(req, senderAccount, attachment);

    }

    @Override
    public boolean isIgnisOnly() {
        return true;
    }

}
