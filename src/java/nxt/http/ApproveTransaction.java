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
import nxt.util.Convert;
import nxt.voting.PhasingVoteCastingAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static nxt.http.JSONResponses.MISSING_PHASED_TRANSACTION;
import static nxt.http.JSONResponses.TOO_MANY_PHASING_VOTES;

public class ApproveTransaction extends CreateTransaction {
    static final ApproveTransaction instance = new ApproveTransaction();

    private ApproveTransaction() {
        super(new APITag[]{APITag.CREATE_TRANSACTION, APITag.PHASING}, "phasedTransaction", "phasedTransaction", "phasedTransaction",
                "revealedSecret", "revealedSecret", "revealedSecret", "revealedSecretIsText", "revealedSecretText");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        List<ChainTransactionId> phasedTransactionIds = ParameterParser.getChainTransactionIds(req, "phasedTransaction");
        if (phasedTransactionIds.isEmpty()) {
            return MISSING_PHASED_TRANSACTION;
        }
        if (phasedTransactionIds.size() > Constants.MAX_PHASING_VOTE_TRANSACTIONS) {
            return TOO_MANY_PHASING_VOTES;
        }

        List<byte[]> secrets = new ArrayList<>(Constants.MAX_PHASING_REVEALED_SECRETS_COUNT);

        String[] revealedSecrets = req.getParameterValues("revealedSecret");

        if (revealedSecrets != null) {
            boolean isText = "true".equalsIgnoreCase(req.getParameter("revealedSecretIsText"));
            for (String secretValue : revealedSecrets) {
                secretValue = Convert.emptyToNull(secretValue);
                if (secretValue != null) {
                    secrets.add(isText ? Convert.toBytes(secretValue) : Convert.parseHexString(secretValue));
                }
            }
        } else {
            revealedSecrets = req.getParameterValues("revealedSecretText");
            if (revealedSecrets != null) {
                for (String secretValue : revealedSecrets) {
                    secretValue = Convert.emptyToNull(secretValue);
                    if (secretValue != null) {
                        secrets.add(Convert.toBytes(secretValue));
                    }
                }
            }
        }
        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = new PhasingVoteCastingAttachment(phasedTransactionIds, secrets);
        return createTransaction(req, account, attachment);
    }
}
