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

package nxt.voting;

import nxt.NxtException.AccountControlException;
import nxt.blockchain.ChildTransaction;
import nxt.util.BooleanExpression;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import static nxt.voting.VoteWeighting.VotingModel.COMPOSITE;

public abstract class PhasingControl {

    protected PhasingParams params;


    protected PhasingControl() {

    }

    protected PhasingControl(PhasingParams params) {
        this.params = params;
    }

    protected void init(ResultSet rs, Supplier<List<? extends SubPoll>> subPollsSupplier) throws SQLException {
        VoteWeighting voteWeighting = AbstractPoll.readVoteWeighting(rs);

        long[] whitelistArr = PhasingParams.readWhitelist(rs);

        PhasingParams.CompositeVoting compositeVoting = null;
        if (voteWeighting.getVotingModel() == COMPOSITE) {
            List<? extends SubPoll> phasingOnlySubPolls = subPollsSupplier.get();
            SortedMap<String, PhasingParams> subPolls = new TreeMap<>();
            phasingOnlySubPolls.forEach(subPoll -> subPolls.put(subPoll.getVariableName(), subPoll.getParams()));
            compositeVoting = new PhasingParams.CompositeVoting(rs.getString("expression"), subPolls);
        }

        PhasingParams.PropertyVoting senderPropertyVoting = PhasingParams.readPropertyVoting(rs, "sender_", voteWeighting);
        PhasingParams.PropertyVoting recipientPropertyVoting = PhasingParams.readPropertyVoting(rs, "recipient_", voteWeighting);

        params = new PhasingParams(
                voteWeighting,
                rs.getLong("quorum"),
                whitelistArr,
                Collections.emptyList(), null, compositeVoting, senderPropertyVoting, recipientPropertyVoting);
    }

    protected final void checkPhasing(PhasingAppendix phasingAppendix) throws AccountControlException {
        if (phasingAppendix == null) {
            throw new AccountControlException("Non-phased transaction when phasing " + getControlType() + " control is enabled");
        }

        PhasingParams transactionParams = phasingAppendix.getParams();

        if (transactionParams.getVoteWeighting().getVotingModel() == COMPOSITE) {
            BooleanExpression transactionExpression;
            BooleanExpression controlExpression;
            Map<String, PhasingParams> transactionSubPolls = transactionParams.getSubPolls();
            Map<String, PhasingParams> controlSubPolls;

            transactionExpression = transactionParams.getExpression();
            if (params.getVoteWeighting().getVotingModel() == COMPOSITE) {
                controlExpression = params.getExpression();
                controlSubPolls = params.getSubPolls();
            } else {
                //automatically created expression with single literal
                controlExpression = new BooleanExpression(getDefaultControlVariable());
                controlSubPolls = Collections.singletonMap(getDefaultControlVariable(), params);
            }

            Set<String> commonVariables = new HashSet<>(controlSubPolls.keySet());
            commonVariables.retainAll(transactionSubPolls.keySet());
            for (String k : commonVariables) {
                PhasingParams transactionSubPoll = transactionSubPolls.get(k);
                PhasingParams controlSubPoll = controlSubPolls.get(k);
                if (!transactionSubPoll.equals(controlSubPoll)) {
                    throw new AccountControlException("Sub-poll for variable \"" + k + "\" does not match the sub-poll " +
                            "for the same variable in " + getControlType() + " control. Expected: " + controlSubPoll.toString() +
                            ". Actual: " + transactionSubPoll.toString());
                }
            }
            try {
                if (!BooleanExpression.fastImplicationCheck(transactionExpression, controlExpression)) {
                    throw new AccountControlException("Phasing expression does not imply the " + getControlType() + " control expression, " +
                            "i.e. it is less restrictive. Phasing expression: \"" + transactionParams.getExpressionStr() +
                            "\". Control expression: \"" + params.getExpressionStr() + "\"");
                }
            } catch (BooleanExpression.BooleanExpressionException e) {
                throw new AccountControlException("Failed to parse boolean expression \"" + e.getMessage() + "\"", e);
            }
        } else if (!params.equals(transactionParams)) {
            throw new AccountControlException("Phasing parameters do not match phasing " + getControlType() + " control. Expected: " +
                    params.toString() + " . Actual: " + transactionParams.toString());
        }
    }

    public final PhasingParams getPhasingParams() {
        return params;
    }

    public abstract String getControlType();

    public abstract String getDefaultControlVariable();

    protected abstract void checkTransaction(ChildTransaction childTransaction) throws AccountControlException;

    public abstract static class SubPoll {
        protected final String variableName;
        protected final PhasingParams params;

        protected SubPoll(String variableName, PhasingParams params) {
            this.variableName = variableName;
            this.params = params;
        }

        protected SubPoll(ResultSet rs) throws SQLException {
            this.variableName = rs.getString("name");

            VoteWeighting voteWeighting = AbstractPoll.readVoteWeighting(rs);

            long[] whitelistArr = PhasingParams.readWhitelist(rs);

            PhasingParams.PropertyVoting senderPropertyVoting = PhasingParams.readPropertyVoting(rs, "sender_", voteWeighting);
            PhasingParams.PropertyVoting recipientPropertyVoting = PhasingParams.readPropertyVoting(rs, "recipient_", voteWeighting);

            params = new PhasingParams(voteWeighting, rs.getLong("quorum"),
                    whitelistArr, null, null, null, senderPropertyVoting, recipientPropertyVoting);
        }

        public String getVariableName() {
            return variableName;
        }

        public PhasingParams getParams() {
            return params;
        }
    }
}
