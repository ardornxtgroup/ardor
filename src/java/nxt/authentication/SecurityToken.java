/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
package nxt.authentication;

import java.nio.ByteBuffer;

/**
 * Security token
 */
public interface SecurityToken {

    /**
     * Get peer account identifier
     *
     * @return                  Peer account identifier
     */
    long getPeerAccountId();

    /**
     * Get peer public key
     *
     * @return                  Peer public key
     */
    byte[] getPeerPublicKey();

    /**
     * Get the session key
     *
     * @param   secretPhrase    Server credentials secret phrase
     * @param   peerPublicKey   Peer public key
     * @return                  Session key or null if there is no session key
     */
    byte[] getSessionKey(String secretPhrase, byte[] peerPublicKey);

    /**
     * Set the session key
     *
     * @param   secretPhrase    Server credentials secret phrase
     * @param   peerPublicKey   Peer public key
     * @param   sessionKey      Session key
     */
    void setSessionKey(String secretPhrase, byte[] peerPublicKey, byte[] sessionKey);

    /**
     * Get the serialized token length
     *
     * @return                  Serialized token length
     */
    int getLength();

    /**
     * Get the serialized token
     *
     * @return                  Serialized token
     */
    byte[] getBytes();

    /**
     * Add the serialized token to a buffer
     *
     * @param   buffer          Byte buffer
     * @return                  Byte buffer
     */
    ByteBuffer getBytes(ByteBuffer buffer);
}
