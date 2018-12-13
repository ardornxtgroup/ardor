package nxt.util.security;

import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;

public class BlockchainCertPathBuilderSpi extends CertPathBuilderSpi {

    @Override
    public CertPathBuilderResult engineBuild(CertPathParameters params) {
        BlockchainCertificate certificate = ((BlockchainCertPathParameters) params).getCertificate();
        BlockchainCertPath blockchainCertPath = new BlockchainCertPath(certificate);
        return new BlockchainCertPathBuilderResult(blockchainCertPath);
    }
}
