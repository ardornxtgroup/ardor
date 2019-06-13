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

package nxt.util.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

public class BlockchainSecurityProvider extends Provider {

    public BlockchainSecurityProvider() {
        super("Jelurida", 1.0, "Jelurida Security Provider, bridging the gap between Java security and the blockchain");
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("securityProvider"));
        }
        AccessController.doPrivileged((PrivilegedAction) () -> {
            put("CertificateFactory.Blockchain", "nxt.util.security.BlockchainCertificateFactorySpi");
            put("CertPathBuilder.Blockchain", "nxt.util.security.BlockchainCertPathBuilderSpi");
            put("KeyStore.Blockchain", "nxt.util.security.BlockchainKeyStoreSpi");
            return null;
        });
    }
}
