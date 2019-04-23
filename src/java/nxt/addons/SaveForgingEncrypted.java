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

import nxt.Constants;
import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.http.APITag;
import nxt.http.ParameterException;
import nxt.http.ParameterParser;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SaveForgingEncrypted extends SaveEncrypted {

    @Override
    public String getAPIRequestType() {
        return "saveForgingEncrypted";
    }

    @Override
    protected APITag getAPITag() {
        return APITag.FORGING;
    }

    @Override
    protected String getDataParameter() {
        return "passphrases";
    }

    @Override
    protected List<String> getExtraParameters() {
        return Collections.singletonList("minEffectiveBalanceFXT");
    }

    @Override
    protected String getData(HttpServletRequest request) throws ParameterException {
        String passphrases = ParameterParser.getParameter(request, "passphrases");
        long minEffectiveBalanceFXT = ParameterParser.getLong(request, "minEffectiveBalanceFXT", 0, Constants.MAX_BALANCE_FXT, false);
        StringWriter stringWriter = new StringWriter();
        try (BufferedReader reader = new BufferedReader(new StringReader(passphrases));
             BufferedWriter writer = new BufferedWriter(stringWriter)) {
            Set<Long> accountIds = new HashSet<>();
            String passphrase;
            while ((passphrase = reader.readLine()) != null) {
                Account account = Account.getAccount(Crypto.getPublicKey(passphrase));
                if (account != null && account.getEffectiveBalanceFXT() >= minEffectiveBalanceFXT && accountIds.add(account.getId())) {
                    writer.write(passphrase);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return stringWriter.toString();
    }

}


