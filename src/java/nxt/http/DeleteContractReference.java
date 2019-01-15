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

import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Attachment;
import nxt.lightcontracts.ContractReference;
import nxt.lightcontracts.ContractReferenceDeleteAttachment;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class DeleteContractReference extends CreateTransaction {

    static final DeleteContractReference instance = new DeleteContractReference();

    private DeleteContractReference() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.CREATE_TRANSACTION}, "contractName");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account senderAccount = ParameterParser.getSenderAccount(req);
        String contractName = Convert.nullToEmpty(req.getParameter("contractName")).trim();
        if (contractName.isEmpty()) {
            return JSONResponses.missing("contractName");
        }
        ContractReference contractReference = ContractReference.getContractReference(senderAccount.getId(), contractName);
        if (contractReference == null) {
            return JSONResponses.unknown("contractName");
        }
        if (contractReference.getAccountId() != senderAccount.getId()) {
            return JSONResponses.incorrect("contractName");
        }
        Attachment attachment = new ContractReferenceDeleteAttachment(contractReference.getId());
        return createTransaction(req, senderAccount, attachment);

    }

    @Override
    public boolean isIgnisOnly() {
        return true;
    }

}
