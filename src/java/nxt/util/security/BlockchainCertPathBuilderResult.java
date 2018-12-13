package nxt.util.security;

import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderResult;

public class BlockchainCertPathBuilderResult implements CertPathBuilderResult {

    private CertPath certPath;

    public BlockchainCertPathBuilderResult(CertPath certPath) {
        this.certPath = certPath;
    }

    @Override
    public CertPath getCertPath() {
        return certPath;
    }

    @Override
    public Object clone() {
        return new BlockchainCertPathBuilderResult(certPath);
    }
}
