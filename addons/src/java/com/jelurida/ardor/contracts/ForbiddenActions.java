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

import nxt.Nxt;
import nxt.addons.AbstractContract;
import nxt.addons.BlockContext;
import nxt.addons.ContractLoader;
import nxt.addons.JO;
import nxt.blockchain.Bundler;
import nxt.blockchain.Generator;
import nxt.dbschema.Db;
import nxt.http.callers.SendMessageCall;
import nxt.util.security.BlockchainSecurityProvider;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Arrays;

/**
 * Contract for testing purposes which attempts actions not allowed by the security policy and verifies that they indeed
 * fail.
 */
public class ForbiddenActions extends AbstractContract {

    public interface Action {
        void execute() throws Exception;
    }

    private static final String WORKED = "worked";
    private static final String BLOCKED = "blocked";

    @Override
    public JO processBlock(BlockContext context) {
        if (context.getHeight() != 2) {
            return context.generateInfoResponse("Block height is not 2");
        }

        // Java permissions
        attemptAction(context, "Local file access", () -> Files.readAllLines(Paths.get("conf", "Read Sensitive Data")));
        attemptAction(context, "Make fields public using reflection", () ->  {
            Field[] declaredFields = getClass().getDeclaredFields(); // accessDeclaredMembers is allowed
            Arrays.stream(declaredFields).forEach(f -> f.setAccessible(true)); // suppressAccessChecks is forbidden
        });
        attemptAction(context, "Execute os command", () -> Runtime.getRuntime().exec("cmd.exe"));
        attemptAction(context, "Open server socket", () -> new ServerSocket(12345));
        attemptAction(context, "Create class loader", ContractLoader.CloudDataClassLoader::new);
        attemptAction(context, "Management functions", () -> ManagementFactory.getRuntimeMXBean().getSystemProperties());
        attemptAction(context, "Replace Security Manager", () -> System.setSecurityManager(new SecurityManager()));
        attemptAction(context, "Add security provider", () -> Security.addProvider(new BlockchainSecurityProvider()));
        attemptAction(context, "Load library", () -> Runtime.getRuntime().loadLibrary("SomethingBad"));
        attemptAction(context, "Read environment variables", () -> System.getenv("Something sensitive"));
        new Thread(() -> attemptAction(context, "Execute os command from a new thread", () -> Runtime.getRuntime().exec("cmd.exe"))).start();

        // Internal Blockchain permissions
        attemptAction(context, "Get blockchain", () -> Nxt.getBlockchain().getHeight());
        attemptAction(context, "Get blockchain processor", Nxt::getBlockchainProcessor);
        attemptAction(context, "Get transaction processor", Nxt::getTransactionProcessor);
        attemptAction(context, "New transaction builder", () -> Nxt.newTransactionBuilder((byte[])null));
        attemptAction(context, "Database access", () -> Db.db.getConnection("PUBLIC"));
        attemptAction(context, "Nxt properties", () -> Nxt.getStringProperty("sensitive info"));
        attemptAction(context, "Forging", () -> Generator.stopForging("..."));
        attemptAction(context, "Bundling", Bundler::stopAllBundlers);
        attemptAction(context, "ContractRunner passphrase", () -> context.getConfig().getSecretPhrase());
        attemptAction(context, "ContractRunner validator passphrase", () -> context.getConfig().getValidatorSecretPhrase());
        attemptAction(context, "System exit", () -> System.exit(-1));
        return context.getResponse();
    }

    private void attemptAction(BlockContext context, String actionDescription, Action action) {
        JO response = new JO();
        response.put("type", actionDescription);
        try {
            action.execute();
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
