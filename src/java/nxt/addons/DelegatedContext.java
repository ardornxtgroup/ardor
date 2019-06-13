/*
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

package nxt.addons;

import nxt.http.responses.BlockResponse;

// TODO add more substance to this class
// We want to expose content of the original context but without compromising security
public class DelegatedContext extends AbstractContractContext {

    private final AbstractContractContext context;

    public DelegatedContext(AbstractContractContext context, String contractName, JO setupParameters) {
        super(context.getConfig(), contractName);
        setContractSetupParameters(setupParameters);
        this.context = context;
    }

    @Override
    public BlockResponse getBlock() {
        return context.getBlock();
    }

    @Override
    protected String getReferencedTransaction() {
        return context.getReferencedTransaction();
    }

    @Override
    public RandomnessSource getRandomnessSource() {
        return context.getRandomnessSource();
    }
}
