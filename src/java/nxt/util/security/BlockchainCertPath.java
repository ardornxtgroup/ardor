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

import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockchainCertPath extends CertPath {

    private final List<Certificate> certificates = new ArrayList<>();

    public BlockchainCertPath(Certificate certificate) {
        super("Blockchain");
        certificates.add(certificate);
    }

    @Override
    public Iterator<String> getEncodings() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getEncoded(String encoding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Certificate> getCertificates() {
        return certificates;
    }
}
