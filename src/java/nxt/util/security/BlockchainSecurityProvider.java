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
