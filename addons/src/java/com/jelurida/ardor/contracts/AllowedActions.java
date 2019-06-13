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

package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.BlockContext;
import nxt.addons.JO;
import nxt.http.callers.SendMessageCall;
import nxt.util.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contract for testing purposes which attempts actions allowed by the security policy and verifies that they indeed
 * succeed.
 */
public class AllowedActions extends AbstractContract {

    public interface Action {
        void attempt() throws Exception;
    }

    private static final String WORKED = "worked";
    private static final String BLOCKED = "blocked";

    @Override
    public JO processBlock(BlockContext context) {
        if (context.getHeight() != 2) {
            return context.generateInfoResponse("height must be 2");
        }
        attemptAction(context, "Write to temp folder", () -> {
            Path path = Paths.get(System.getProperty("java.io.tmpdir"), "writeToTempFolder.txt");
            Files.write(path, "dummy".getBytes());
            Files.readAllBytes(path);
            Files.delete(path);
        });

        attemptAction(context, "Create thread", () -> {
            Thread t = new Thread(() -> Logger.logInfoMessage("Thread started"));
            t.setDaemon(true);
            t.start();
            t.join();
        });
        return context.getResponse();
    }

    private void attemptAction(BlockContext context, String actionDescription, Action action) {
        JO response = new JO();
        response.put("type", actionDescription);
        try {
            action.attempt();
            response.put("status", WORKED);
            report(context, response);
        } catch (Exception e) {
            if (e instanceof SecurityException || e.getCause() instanceof SecurityException) {
                response.put("status", BLOCKED);
                report(context, response);
            } else {
                response.put("status", WORKED);
                report(context, response);
            }
        }
    }

    private void report(BlockContext context, JO response) {
        SendMessageCall sendMessageCall = SendMessageCall.create(2).
                recipient(context.getAccountRs()).
                message(response.toJSONString()).
                messageIsPrunable(true);
        context.createTransaction(sendMessageCall);
    }
}
