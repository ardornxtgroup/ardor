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

package nxt.tools;

import nxt.Nxt;
import nxt.configuration.Setup;
import nxt.http.GetConstants;
import nxt.util.JSON;
import nxt.util.ThreadPool;
import nxt.util.security.BlockchainPermission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ConstantsExporter {

    private static final Object sync = new Object();

    public static void main(String[] args) throws InterruptedException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        if (args.length != 1) {
            System.out.println("Usage: ConstantsExporter <destination constants.js file>");
            System.exit(1);
        }

        // Don't use runBeforeStart since this causes deadlock on the class instantiation lock
        ThreadPool.runAfterStart(() -> {
            Writer writer;
            try {
                writer = new FileWriter(new File(args[0]));
                writer.write("if (!NRS) {\n" +
                        "    var NRS = {};\n" +
                        "    NRS.constants = {};\n" +
                        "}\n\n");
                writer.write("NRS.constants.SERVER = ");
                JSON.writeJSONString(GetConstants.getConstants(), writer);
                writer.write("\n\n" +
                        "if (isNode) {\n" +
                        "    module.exports = NRS.constants.SERVER;\n" +
                        "}\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Nxt.shutdown();

            // Let the program end
            synchronized (sync) {
                sync.notify();
            }
        });

        Nxt.init(Setup.COMMAND_LINE_TOOL);

        // Wait for the getConstants thread to finish
        synchronized (sync) {
            sync.wait();
        }
    }
}
