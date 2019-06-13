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
