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
package nxt.peer;

import nxt.Constants;
import nxt.Nxt;
import nxt.crypto.Crypto;
import nxt.util.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Peer message handler
 */
class MessageHandler implements Runnable {

    /** Message queue */
    private static final LinkedBlockingQueue<QueueEntry> messageQueue = new LinkedBlockingQueue<>();

    /** Shutdown started */
    private static volatile boolean messageShutdown = false;

    /**
     * Construct a message handler
     */
    MessageHandler() {
    }

    /**
     * Process a message
     *
     * @param   peer                    Peer
     * @param   bytes                   Message bytes
     */
    static void processMessage(PeerImpl peer, ByteBuffer bytes) {
        bytes.position(bytes.position() - 4);
        int msgLength = bytes.getInt();
        messageQueue.offer(new QueueEntry(peer, bytes, (msgLength & 0x80000000) != 0));
    }

    /**
     * Shutdown the message handlers
     */
    static void shutdown() {
        if (!messageShutdown) {
            messageShutdown = true;
            messageQueue.offer(new QueueEntry(null, null, false));
        }
    }

    /**
     * Message handling thread
     */
    @Override
    public void run() {
        Logger.logDebugMessage(Thread.currentThread().getName() + " started");
        try {
            while (true) {
                QueueEntry entry = messageQueue.take();
                //
                // During shutdown, discard all pending messages until we reach the shutdown entry.
                // Requeue the shutdown entry to wake up the next message handler so that
                // it can then shutdown.
                //
                if (messageShutdown) {
                    if (entry.getPeer() == null) {
                        messageQueue.offer(entry);
                        break;
                    }
                    continue;
                }
                //
                // Process the message
                //
                PeerImpl peer = entry.getPeer();
                if (peer.getState() != Peer.State.CONNECTED) {
                    continue;
                }
                if (peer.isHandshakePending() && entry.isEncrypted()) {
                    peer.queueInputMessage(entry.getBytes());
                    continue;
                }
                NetworkMessage message = null;
                NetworkMessage response;
                try {
                    ByteBuffer buffer = entry.getBytes();
                    if (entry.isEncrypted()) {
                        byte[] sessionKey = peer.getSessionKey();
                        if (sessionKey == null) {
                            throw new IllegalStateException("Encrypted message received without a session key");
                        }
                        byte[] encryptedBytes = new byte[buffer.limit() - buffer.position()];
                        buffer.get(encryptedBytes);
                        byte[] msgBytes = Crypto.aesGCMDecrypt(encryptedBytes, sessionKey);
                        buffer = ByteBuffer.wrap(msgBytes);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                    }
                    message = NetworkMessage.getMessage(buffer);
                    if (Peers.isLogLevelEnabled(Peers.LOG_LEVEL_NAMES)) {
                        Logger.logDebugMessage(String.format("%s[%d] message received from %s",
                                message.getMessageName(), message.getMessageId(), peer.getHost()));
                    }
                    if (message.isResponse()) {
                        if (message.getMessageId() == 0) {
                            Logger.logErrorMessage(message.getMessageName()
                                    + " response message does not have a message identifier");
                        } else {
                            peer.completeRequest(message);
                        }
                    } else {
                        if (message.downloadNotAllowed()) {
                            if (Nxt.getBlockchainProcessor().isDownloading()) {
                                throw new IllegalStateException(Errors.DOWNLOADING);
                            }
                            if (Constants.isLightClient) {
                                throw new IllegalStateException(Errors.LIGHT_CLIENT);
                            }
                        }
                        response = message.processMessage(peer);
                        if (message.requiresResponse()) {
                            if (response == null) {
                                Logger.logErrorMessage("No response for " + message.getMessageName() + " message");
                            } else {
                                peer.sendMessage(response);
                            }
                        }
                    }
                } catch (NetworkProtocolException exc) {
                    Logger.logDebugMessage("Unable to process message from " + peer.getHost() + ": " + exc.getMessage());
                    peer.blacklist(exc);
                } catch (Exception exc) {
                    String errorMessage = (Peers.hideErrorDetails ? exc.getClass().getName() :
                            (exc.getMessage() != null ? exc.getMessage() : exc.toString()));
                    boolean severeError;
                    if (exc instanceof IllegalStateException) {
                        severeError = false;
                    } else {
                        severeError = true;
                        Logger.logDebugMessage("Unable to process message from " + peer.getHost() + ": " + errorMessage, exc);
                    }
                    if (message != null && message.requiresResponse()) {
                        response = new NetworkMessage.ErrorMessage(message.getMessageId(),
                                severeError, message.getMessageName(), errorMessage);
                        peer.sendMessage(response);
                    }
                }
                //
                // Restart reads from the peer if the pending messages have been cleared
                //
                if (peer.getState() == Peer.State.CONNECTED) {
                    int count = peer.decrementInputCount();
                    if (count == 0) {
                        try {
                            NetworkHandler.KeyEvent event = peer.getKeyEvent();
                            if (event != null && (event.getKey().interestOps() & SelectionKey.OP_READ) == 0) {
                                event.update(SelectionKey.OP_READ, 0);
                            }
                        } catch (IllegalStateException exc) {
                            Logger.logErrorMessage("Unable to update network selection key", exc);
                        }
                    }
                }
            }
        } catch (Throwable exc) {
            Logger.logErrorMessage("Message handler abnormally terminated", exc);
        }
        Logger.logDebugMessage(Thread.currentThread().getName() +  " stopped");
    }

    /**
     * Message queue entry
     */
    private static class QueueEntry {

        /** Peer */
        private final PeerImpl peer;

        /** Message buffer */
        private final ByteBuffer bytes;

        /** Message is encrypted */
        private final boolean isEncrypted;

        /**
         * Construct a queue entry
         *
         * @param   peer                Peer
         * @param   bytes               Message bytes
         * @param   isEncrypted         TRUE if message is encrypted
         */
        private QueueEntry(PeerImpl peer, ByteBuffer bytes, boolean isEncrypted) {
            this.peer = peer;
            this.bytes = bytes;
            this.isEncrypted = isEncrypted;
        }

        /**
         * Get the peer
         *
         * @return                      Peer
         */
        private PeerImpl getPeer() {
            return peer;
        }

        /**
         * Get the message bytes
         *
         * @return                      Message buffer
         */
        private ByteBuffer getBytes() {
            return bytes;
        }

        /**
         * Check if the message is encrypted
         *
         * @return                      TRUE if the message is encrypted
         */
        private boolean isEncrypted() {
            return isEncrypted;
        }
    }
}
