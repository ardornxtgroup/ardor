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
package nxt.peer;

import nxt.Constants;
import nxt.Nxt;
import nxt.crypto.Crypto;
import nxt.http.API;
import nxt.http.APIEnum;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ThreadPool;
import nxt.util.UPnP;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The network handler creates outbound connections and adds them to the
 * network selector.  A new outbound connection will be created whenever
 * the number of outbound connections is less than the maximum number of
 * outbound connections.
 *
 * The network handler opens a local port and listens for incoming connections.
 * When a connection is received, it creates a socket channel and accepts the
 * connection as long as the maximum number of inbound connections has not been reached.
 * The socket is then added to the network selector.
 *
 * When a message is received from a peer node, it is processed by a message
 * handler executing on a separate thread.  The message handler processes the
 * message and then creates a response message to be returned to the originating node.
 *
 * The network handler terminates when its shutdown() method is called.
 */
public final class NetworkHandler implements Runnable {

    /** Default peer port */
    static final int DEFAULT_PEER_PORT = (Constants.isPermissioned ? 27873 : 27874);

    /** Testnet peer port */
    static final int TESTNET_PEER_PORT = (Constants.isPermissioned ? 26873 : 26874);

    /** Maximum number of pending messages for a single peer */
    static final int MAX_PENDING_MESSAGES = 25;

    /** Message header magic bytes */
    private static final byte[] MESSAGE_HEADER_MAGIC = new byte[] {(byte)0x03, (byte)0x2c, (byte)0x05, (byte)0xc2};

    /** Message header length */
    private static final int MESSAGE_HEADER_LENGTH = MESSAGE_HEADER_MAGIC.length + 4;

    /** Maximum message size */
    static final int MAX_MESSAGE_SIZE = 1024 * 1024;

    /** Server port */
    private static final int serverPort = Constants.isTestnet ? TESTNET_PEER_PORT :
            Nxt.getIntProperty("nxt.peerServerPort", DEFAULT_PEER_PORT);

    /** Enable UPnP */
    private static final boolean enablePeerUPnP = Nxt.getBooleanProperty("nxt.enablePeerUPnP");

    /** Share my address */
    private static final boolean shareAddress = Nxt.getBooleanProperty("nxt.shareMyAddress");

    /** Maximum number of outbound connections */
    private static final int maxOutbound = Nxt.getIntProperty("nxt.maxNumberOfOutboundConnections", 8);

    /** Maximum number of inbound connections */
    private static final int maxInbound = Nxt.getIntProperty("nxt.maxNumberOfInboundConnections", 64);

    /** Connect timeout (seconds) */
    static final int peerConnectTimeout = Nxt.getIntProperty("nxt.peerConnectTimeout", 10);

    /** Peer read timeout (seconds) */
    static final int peerReadTimeout = Nxt.getIntProperty("nxt.peerReadTimeout", 10);

    /** Listen address */
    private static final String listenAddress = Nxt.getStringProperty("nxt.peerServerHost", "0.0.0.0");

    /** GetInfo message which is sent each time an outbound connection is created */
    private static final NetworkMessage.GetInfoMessage getInfoMessage;

    /** My address */
    static String myAddress;

    /** My host name */
    static String myHost;

    /** My port */
    static int myPort = -1;

    /** Announced address */
    static String announcedAddress;

    static {
        try {
            myAddress = Convert.emptyToNull(Nxt.getStringProperty("nxt.myAddress"));
            if (myAddress != null) {
                myAddress = myAddress.toLowerCase().trim();
                URI uri = new URI("http://" + myAddress);
                myHost = uri.getHost();
                myPort = uri.getPort();
                if (myHost == null) {
                    throw new RuntimeException("nxt.myAddress is not a valid host address");
                }
                if (myPort == TESTNET_PEER_PORT && !Constants.isTestnet) {
                    throw new RuntimeException("Port " + TESTNET_PEER_PORT + " should only be used for testnet");
                }
                if (Constants.isTestnet) {
                    announcedAddress = myHost;
                } else if (myPort == -1 && serverPort != DEFAULT_PEER_PORT) {
                    announcedAddress = myHost + ":" + serverPort;
                } else if (myPort == DEFAULT_PEER_PORT) {
                    announcedAddress = myHost;
                } else {
                    announcedAddress = myAddress;
                }
            }
        } catch (URISyntaxException e) {
            Logger.logWarningMessage("Your announced address is not valid: " + e.toString());
            myAddress = null;
        }
    }

    /** Network listener instance */
    private static final NetworkHandler listener = new NetworkHandler();

    /** Network listener thread */
    private static Thread listenerThread;

    /** Current number of inbound connections */
    private static final AtomicInteger inboundCount = new AtomicInteger(0);

    /** Current number of outbound connections */
    private static final AtomicInteger outboundCount = new AtomicInteger(0);

    /** Listen channel */
    private static ServerSocketChannel listenChannel;

    /** Network selector */
    private static Selector networkSelector;

    /** Channel register queue */
    private static final ConcurrentLinkedQueue<KeyEvent> keyEventQueue = new ConcurrentLinkedQueue<>();

    /** Connection map */
    static final ConcurrentHashMap<InetAddress, PeerImpl> connectionMap = new ConcurrentHashMap<>();

    /** Network started */
    private static volatile boolean networkStarted = false;

    /** Network shutdown */
    private static volatile boolean networkShutdown = false;

    /**
     * Construct a network handler
     */
    private NetworkHandler() { }

    /**
     * Initialize the network handler
     */
    public static void init() {}

    static {
        //
        // Don't start the network handler if we are offline
        //
        if (! Constants.isOffline) {
            //
            // Create the GetInfo message which is sent when an outbound connection is
            // completed.  The remote peer will send its GetInfo message in response.
            //
            if (serverPort == TESTNET_PEER_PORT && !Constants.isTestnet) {
                throw new RuntimeException("Port " + TESTNET_PEER_PORT + " should only be used for testnet");
            }
            String platform = Nxt.getStringProperty("nxt.myPlatform",
                    System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            if (platform.length() > Peers.MAX_PLATFORM_LENGTH) {
                platform = platform.substring(0, Peers.MAX_PLATFORM_LENGTH);
            }
            if (myAddress != null) {
                try {
                    InetAddress[] myAddrs = InetAddress.getAllByName(myHost);
                    boolean addrValid = false;
                    Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
                    chkAddr:
                    while (intfs.hasMoreElements()) {
                        NetworkInterface intf = intfs.nextElement();
                        List<InterfaceAddress> intfAddrs = intf.getInterfaceAddresses();
                        for (InterfaceAddress intfAddr : intfAddrs) {
                            InetAddress extAddr = intfAddr.getAddress();
                            for (InetAddress myAddr : myAddrs) {
                                if (extAddr.equals(myAddr)) {
                                    addrValid = true;
                                    break chkAddr;
                                }
                            }
                        }
                    }
                    if (!addrValid) {
                        InetAddress extAddr = UPnP.getExternalAddress();
                        if (extAddr != null) {
                            for (InetAddress myAddr : myAddrs) {
                                if (extAddr.equals(myAddr)) {
                                    addrValid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!addrValid) {
                        Logger.logWarningMessage("Your announced address does not match your external address");
                    }
                } catch (SocketException e) {
                    Logger.logErrorMessage("Unable to enumerate the network interfaces: " + e.toString());
                } catch (UnknownHostException e) {
                    Logger.logWarningMessage("Your announced address is not valid: " + e.toString());
                }
            }
            long services = 0;
            for (Peer.Service service : Peers.myServices) {
                services |= service.getCode();
            }
            String disabledAPIs = null;
            if ((API.isOpenAPI) && !Constants.isLightClient) {
                EnumSet<APIEnum> disabledAPISet = EnumSet.noneOf(APIEnum.class);

                API.disabledAPIs.forEach(apiName -> {
                    APIEnum api = APIEnum.fromName(apiName);
                    if (api != null) {
                        disabledAPISet.add(api);
                    }
                });
                API.disabledAPITags.forEach(apiTag -> {
                    for (APIEnum api : APIEnum.values()) {
                        if (api.getHandler() != null && api.getHandler().getAPITags().contains(apiTag)) {
                            disabledAPISet.add(api);
                        }
                    }
                });
                disabledAPIs = APIEnum.enumSetToBase64String(disabledAPISet);
            }
            if (Constants.isPermissioned && Peers.peerSecretPhrase == null) {
                networkShutdown = true;
                throw new RuntimeException("Peer credentials not specified for permissioned blockchain");
            }
            getInfoMessage = new NetworkMessage.GetInfoMessage(Nxt.APPLICATION, Nxt.VERSION, platform,
                    shareAddress, announcedAddress, API.openAPIPort, API.openAPISSLPort, services,
                    disabledAPIs, API.apiServerIdleTimeout,
                    (Constants.isPermissioned ? Crypto.getPublicKey(Peers.peerSecretPhrase) : null));
            try {
                //
                // Create the selector for listening for network events
                //
                networkSelector = Selector.open();
                //
                // Create the listen channel
                //
                listenChannel = ServerSocketChannel.open();
                listenChannel.configureBlocking(false);
                listenChannel.bind(new InetSocketAddress(listenAddress, serverPort), 10);
                listenChannel.register(networkSelector, SelectionKey.OP_ACCEPT);
            } catch (IOException exc) {
                networkShutdown = true;
                throw new RuntimeException("Unable to create network listener", exc);
            }
            //
            // Start the network handler after server initialization has completed
            //
            ThreadPool.runAfterStart(() -> {
                if (enablePeerUPnP) {
                    UPnP.addPort(serverPort);
                }
                //
                // Start the network listener
                //
                listenerThread = new Thread(listener, "Network Listener");
                listenerThread.setDaemon(true);
                listenerThread.start();
                //
                // Start the message handlers
                //
                for (int i = 1; i <= 4; i++) {
                    MessageHandler handler = new MessageHandler();
                    Thread handlerThread = new Thread(handler, "Message Handler " + i);
                    handlerThread.setDaemon(true);
                    handlerThread.start();
                }
            });
        } else {
            networkShutdown = true;
            getInfoMessage = null;
            Logger.logInfoMessage("Network handler is offline");
        }
    }

    /**
     * Shutdown the network handler
     */
    public static void shutdown() {
        if (!networkShutdown) {
            networkShutdown = true;
            MessageHandler.shutdown();
            if (enablePeerUPnP) {
                UPnP.deletePort(serverPort);
            }
            if (networkSelector != null) {
                wakeup();
            }
        }
    }

    /**
     * Wakes up the network listener
     */
    static void wakeup() {
        if (Thread.currentThread() != listenerThread) {
            networkSelector.wakeup();
        }
    }

    /**
     * Network listener
     */
    @Override
    public void run() {
        try {
            Logger.logDebugMessage("Network listener started");
            networkStarted = true;
            //
            // Process network events
            //
            while (!networkShutdown) {
                processEvents();
            }
        } catch (RejectedExecutionException exc) {
            Logger.logInfoMessage("Server shutdown started, Network listener stopping");
        } catch (Throwable exc) {
            Logger.logErrorMessage("Network listener abnormally terminated", exc);
            networkShutdown = true;
        }
        networkStarted = false;
        Logger.logDebugMessage("Network listener stopped");
    }

    /**
     * Process network events
     */
    private void processEvents() {
        int count;
        try {
            //
            // Process pending selection key events
            //
            KeyEvent keyEvent;
            while ((keyEvent = keyEventQueue.poll()) != null) {
                keyEvent.process();
            }
            //
            // Process selectable events
            //
            // Note that you need to remove the key from the selected key
            // set.  Otherwise, the selector will return immediately since
            // it thinks there are still unprocessed events.  Also, accessing
            // a key after the channel is closed will cause an exception to be
            // thrown, so it is best to test for just one event at a time for
            // each selection key.
            //
            count = networkSelector.select();
            if (count > 0 && !networkShutdown) {
                Set<SelectionKey> selectedKeys = networkSelector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext() && !networkShutdown) {
                    SelectionKey key = keyIterator.next();
                    SelectableChannel channel = key.channel();
                    if (channel.isOpen() && key.isValid()) {
                        if (key.isAcceptable())
                            processAccept(key);
                        else if (key.isConnectable())
                            processConnect(key);
                        else if (key.isReadable())
                            processRead(key);
                        else if (key.isWritable())
                            processWrite(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (CancelledKeyException exc) {
            Logger.logDebugMessage("Network selector key cancelled - retrying", exc);
        } catch (ClosedSelectorException exc) {
            Logger.logErrorMessage("Network selector closed unexpectedly", exc);
            networkShutdown = true;
        } catch (IOException exc) {
            Logger.logErrorMessage("I/O error while processing selection event", exc);
        }
    }

    /**
     * We need to register channels and modify selection keys on the listener thread to
     * avoid deadlocks in the network selector
     */
    static class KeyEvent {

        /** Peer */
        private final PeerImpl peer;

        /** Socket channel */
        private final SocketChannel channel;

        /** Interest ops to add */
        private int addOps;

        /** Interest ops to remove */
        private int removeOps;

        /** Selection key */
        private SelectionKey key = null;

        /** Cyclic barrier used to wait for event completion */
        private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        /**
         * Construct a KeyEvent
         *
         * @param   peer                Peer
         * @param   channel             Channel to register
         * @param   initialOps          Initial interest operations
         */
        private KeyEvent(PeerImpl peer, SocketChannel channel, int initialOps) {
            this.peer = peer;
            this.channel = channel;
            this.addOps = initialOps;
        }

        /**
         * Register the channel and wait for completion (called by the thread creating the channel)
         *
         * @return                      Selection key assigned to the channel
         */
        private SelectionKey register() {
            if (Thread.currentThread() == listenerThread) {
                registerChannel();
                return key;
            }
            keyEventQueue.add(this);
            networkSelector.wakeup();
            try {
                cyclicBarrier.await(5, TimeUnit.SECONDS);
            } catch (BrokenBarrierException | InterruptedException | TimeoutException exc) {
                throw new IllegalStateException("Thread interrupted while waiting for key event completion");
            }
            cyclicBarrier.reset();
            return key;
        }

        /**
         * Update the interest operations for the selection key
         *
         * @param   addOps              Operations to be added
         * @param   removeOps           Operations to be removed
         */
        void update(int addOps, int removeOps) {
            if (peer.isDisconnectPending()) {
                return;
            }
            if (Thread.currentThread() == listenerThread) {
                if (key.isValid()) {
                    key.interestOps((key.interestOps() | addOps) & (~removeOps));
                }
            } else {
                synchronized(this) {
                    cyclicBarrier.reset();
                    this.addOps = addOps;
                    this.removeOps = removeOps;
                    keyEventQueue.add(this);
                    networkSelector.wakeup();
                    try {
                        cyclicBarrier.await(5, TimeUnit.SECONDS);
                    } catch (BrokenBarrierException | InterruptedException | TimeoutException exc) {
                        throw new IllegalStateException("Thread interrupted while waiting for key event completion");
                    }
                }
            }
        }

        /**
         * Process the key event (called on the listener thread)
         */
        private void process() {
            try {
                if (key == null) {
                    registerChannel();
                } else if (key.isValid()) {
                    key.interestOps((key.interestOps() | addOps) & (~removeOps));
                }
                cyclicBarrier.await(100, TimeUnit.MILLISECONDS);
            } catch (BrokenBarrierException | InterruptedException | TimeoutException exc) {
                Logger.logErrorMessage("Listener thread interrupted while waiting for key event completion");
            }
        }

        /**
         * Register the channel
         */
        private void registerChannel() {
            try {
                key = channel.register(networkSelector, addOps);
                key.attach(peer);
                peer.setKeyEvent(this);
            } catch (IOException exc) {
                // Ignore - the channel has been closed
            }
        }

        /**
         * Get the selection key
         *
         * @return                      Selection key
         */
        SelectionKey getKey() {
            return key;
        }
    }

    /**
     * Create a new outbound connection
     *
     * @param   peer                    Target peer
     */
    static void createConnection(PeerImpl peer) {
        try {
            InetAddress address = InetAddress.getByName(peer.getHost());
            InetSocketAddress remoteAddress = new InetSocketAddress(address, peer.getPort());
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(null);
            channel.connect(remoteAddress);
            peer.setConnectionAddress(remoteAddress);
            peer.setChannel(channel);
            connectionMap.put(address, peer);
            outboundCount.incrementAndGet();
            KeyEvent event = new KeyEvent(peer, channel, SelectionKey.OP_CONNECT);
            SelectionKey key = event.register();
            if (key == null) {
                Logger.logErrorMessage("Unable to register socket channel for " + peer.getHost());
            }
        } catch (BindException exc) {
            Logger.logErrorMessage("Unable to bind local port: " +
                    (exc.getMessage() != null ? exc.getMessage() : exc.toString()));
        } catch (UnknownHostException exc) {
            Logger.logErrorMessage("Unable to resolve host " + peer.getHost() + ": " +
                    (exc.getMessage() != null ? exc.getMessage() : exc.toString()));
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to open connection to " + peer.getHost() + ": " +
                    (exc.getMessage() != null ? exc.getMessage() : exc.toString()));
        }
    }

    /**
     * Process OP_CONNECT event (outbound connect completed)
     *
     * @param   connectKey              Selection key
     */
    private void processConnect(SelectionKey connectKey) {
        PeerImpl peer = (PeerImpl)connectKey.attachment();
        if (peer == null || peer.getChannel() == null) {
            return;                     // Channel has been closed
        }
        SocketChannel channel = peer.getChannel();
        try {
            channel.finishConnect();
            if (peer.getState() != Peer.State.CONNECTED) {
                KeyEvent keyEvent = peer.getKeyEvent();
                if (keyEvent != null) {
                    keyEvent.update(SelectionKey.OP_READ, SelectionKey.OP_CONNECT);
                }
                Peers.peersService.execute(() -> {
                    peer.connectComplete(true);
                    sendGetInfoMessage(peer);
                });
            }
        } catch (IOException exc) {
            Peers.peersService.execute(() -> peer.connectComplete(false));
        }
    }

    /**
     * Process OP_ACCEPT event (inbound connect received)
     *
     * @param   acceptKey               Selection key
     */
    private void processAccept(SelectionKey acceptKey) {
        try {
            SocketChannel channel = listenChannel.accept();
            if (channel != null) {
                InetSocketAddress remoteAddress = (InetSocketAddress)channel.getRemoteAddress();
                String hostAddress = remoteAddress.getAddress().getHostAddress();
                PeerImpl peer = Peers.findOrCreatePeer(remoteAddress.getAddress());
                if (peer == null) {
                    channel.close();
                    Logger.logDebugMessage("Peer not accepted: Connection rejected from " + hostAddress);
                } else if (!Peers.shouldGetMorePeers()) {
                    channel.close();
                    Logger.logDebugMessage("New peers are not accepted: Connection rejected from " + hostAddress);
                } else if (inboundCount.get() >= maxInbound) {
                    channel.close();
                    Logger.logDebugMessage("Max inbound connections reached: Connection rejected from " + hostAddress);
                } else if (peer.isBlacklisted()) {
                    channel.close();
                    Logger.logDebugMessage("Peer is blacklisted: Connection rejected from " + hostAddress);
                } else if (connectionMap.get(remoteAddress.getAddress()) != null) {
                    channel.close();
                    Logger.logDebugMessage("Connection already established with " + hostAddress + ", disconnecting");
                    peer.setDisconnectPending();
                    Peers.peersService.execute(peer::disconnectPeer);
                } else {
                    channel.configureBlocking(false);
                    peer.setConnectionAddress(remoteAddress);
                    peer.setChannel(channel);
                    peer.setLastUpdated(Nxt.getEpochTime());
                    connectionMap.put(remoteAddress.getAddress(), peer);
                    inboundCount.incrementAndGet();
                    Peers.addPeer(peer);
                    KeyEvent event = new KeyEvent(peer, channel, 0);
                    SelectionKey key = event.register();
                    if (key == null) {
                        Logger.logErrorMessage("Unable to register socket channel for " + peer.getHost());
                    } else {
                        Peers.peersService.execute(peer::setInbound);
                    }
                }
            }
        } catch (IOException exc) {
            Logger.logErrorMessage("Unable to accept connection", exc);
            networkShutdown = true;
        }
    }

    /**
     * Process OP_READ event (ready to read data)
     *
     * @param   readKey                 Network selection key
     */
    private void processRead(SelectionKey readKey) {
        PeerImpl peer = (PeerImpl)readKey.attachment();
        SocketChannel channel = peer.getChannel();
        ByteBuffer buffer = peer.getInputBuffer();
        peer.setLastUpdated(Nxt.getEpochTime());
        try {
            int count;
            //
            // Read data until we have a complete message or no more data is available
            //
            while (true) {
                //
                // Allocate a header buffer if no read is in progress
                //   4-byte magic bytes
                //   4-byte message length (High-order bit set if message is encrypted)
                //
                if (buffer == null) {
                    buffer = ByteBuffer.wrap(new byte[MESSAGE_HEADER_LENGTH]);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    peer.setInputBuffer(buffer);
                }
                //
                // Read until buffer is full or there is no more data available
                //
                if (buffer.position() < buffer.limit()) {
                    count = channel.read(buffer);
                    if (count <= 0) {
                        if (count < 0) {
                            Logger.logDebugMessage("Connection with " + peer.getHost() + " closed by peer");
                            KeyEvent keyEvent = peer.getKeyEvent();
                            if (keyEvent != null) {
                                keyEvent.update(0, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            }
                            peer.setDisconnectPending();
                            Peers.peersService.execute(peer::disconnectPeer);
                        }
                        break;
                    }
                    peer.updateDownloadedVolume(count);
                }
                //
                // Process the message header and allocate a new buffer to hold the complete message
                //
                if (buffer.position() == buffer.limit() && buffer.limit() == MESSAGE_HEADER_LENGTH) {
                    buffer.position(0);
                    byte[] hdrBytes = new byte[MESSAGE_HEADER_MAGIC.length];
                    buffer.get(hdrBytes);
                    int msgLength = buffer.getInt();
                    int length = msgLength & 0x7fffffff;
                    if (!Arrays.equals(hdrBytes, MESSAGE_HEADER_MAGIC)) {
                        Logger.logDebugMessage("Incorrect message header received from " + peer.getHost());
                        Logger.logDebugMessage("  " + Arrays.toString(hdrBytes));
                        KeyEvent keyEvent = peer.getKeyEvent();
                        if (keyEvent != null) {
                            keyEvent.update(0, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                        peer.setDisconnectPending();
                        Peers.peersService.execute(peer::disconnectPeer);
                        break;
                    }
                    if (length < 1 || length > MAX_MESSAGE_SIZE + 32) {
                        Logger.logDebugMessage("Message length " + length + " for message from " + peer.getHost()
                                + " is not valid");
                        KeyEvent keyEvent = peer.getKeyEvent();
                        if (keyEvent != null) {
                            keyEvent.update(0, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                        peer.setDisconnectPending();
                        Peers.peersService.execute(peer::disconnectPeer);
                        break;
                    }
                    byte[] msgBytes = new byte[MESSAGE_HEADER_LENGTH + length];
                    buffer = ByteBuffer.wrap(msgBytes);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffer.put(hdrBytes);
                    buffer.putInt(msgLength);
                    peer.setInputBuffer(buffer);
                }
                //
                // Queue the message for the message handler if the buffer is full
                //
                // We will disable read operations for this peer if it has too many
                // pending messages.  Read operations will be re-enabled once
                // all of the pending messages have been processed.  We do this to keep
                // one peer from flooding us with messages.
                //
                if (buffer.position() == buffer.limit()) {
                    peer.setInputBuffer(null);
                    buffer.position(MESSAGE_HEADER_LENGTH);
                    int inputCount = peer.incrementInputCount();
                    if (inputCount >= MAX_PENDING_MESSAGES) {
                        KeyEvent keyEvent = peer.getKeyEvent();
                        if (keyEvent != null) {
                            keyEvent.update(0, SelectionKey.OP_READ);
                        }
                    }
                    MessageHandler.processMessage(peer, buffer);
                    break;
                }
            }
        } catch (IOException exc) {
            Logger.logDebugMessage(String.format("%s: Peer %s", exc.getMessage(), peer.getHost()));
            KeyEvent keyEvent = peer.getKeyEvent();
            if (keyEvent != null) {
                keyEvent.update(0, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            peer.setDisconnectPending();
            Peers.peersService.execute(peer::disconnectPeer);
        }
    }

    /**
     * Get the message bytes
     *
     * @param   peer                    Peer
     * @param   message                 Network message
     * @return                          Serialized message
     */
    static ByteBuffer getMessageBytes(PeerImpl peer, NetworkMessage message) {
        ByteBuffer buffer;
        byte[] sessionKey = peer.getSessionKey();
        int length = message.getLength();
        if (sessionKey != null) {
            buffer = ByteBuffer.allocate(MESSAGE_HEADER_LENGTH + length + 32);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            message.getBytes(buffer);
            int byteLength = buffer.position();
            byte[] msgBytes = new byte[byteLength];
            buffer.position(0);
            buffer.get(msgBytes);
            byte[] encryptedBytes = Crypto.aesGCMEncrypt(msgBytes, sessionKey);
            buffer.position(0);
            buffer.put(MESSAGE_HEADER_MAGIC);
            buffer.putInt(encryptedBytes.length | 0x80000000);
            buffer.put(encryptedBytes);
        } else {
            buffer = ByteBuffer.allocate(MESSAGE_HEADER_LENGTH + length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(MESSAGE_HEADER_MAGIC);
            buffer.putInt(length);
            message.getBytes(buffer);
        }
        buffer.flip();
        return buffer;
    }

    /**
     * Process OP_WRITE event (ready to write data)
     *
     * @param   writeKey                Network selection key
     */
    private void processWrite(SelectionKey writeKey) {
        PeerImpl peer = (PeerImpl)writeKey.attachment();
        SocketChannel channel = peer.getChannel();
        ByteBuffer buffer = peer.getOutputBuffer();
        try {
            //
            // Write data until all pending messages have been sent or the socket buffer is full
            //
            while (true) {
                //
                // Get the next message if no write is in progress.  Disable write events
                // if there are no more messages to write.
                //
                if (buffer == null) {
                    buffer = peer.getQueuedMessage();
                    if (buffer == null) {
                        KeyEvent keyEvent = peer.getKeyEvent();
                        if (keyEvent != null) {
                            keyEvent.update(0, SelectionKey.OP_WRITE);
                        }
                        break;
                    }
                    peer.setOutputBuffer(buffer);
                }
                //
                // Write the current buffer to the channel
                //
                int count = channel.write(buffer);
                if (count > 0) {
                    peer.updateUploadedVolume(count);
                }
                if (buffer.position() < buffer.limit()) {
                    break;
                }
                buffer = null;
                peer.setOutputBuffer(null);
            }
        } catch (IOException exc) {
            Logger.logDebugMessage(String.format("%s: Peer %s", exc.getMessage(), peer.getHost()));
            KeyEvent keyEvent = peer.getKeyEvent();
            if (keyEvent != null) {
                keyEvent.update(0, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            peer.setDisconnectPending();
            Peers.peersService.execute(peer::disconnectPeer);
        }
    }

    /**
     * Close a connection
     *
     * @param   peer                    Peer connection to close
     */
    static void closeConnection(PeerImpl peer) {
        SocketChannel channel = peer.getChannel();
        if (channel == null) {
            return;
        }
        try {
            if (peer.isInbound()) {
                inboundCount.decrementAndGet();
            } else {
                outboundCount.decrementAndGet();
            }
            connectionMap.remove(peer.getConnectionAddress().getAddress());
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException exc) {
            // Ignore since the channel is closed in any event
        }
    }

    /**
     * Send our GetInfo message
     *
     * We will send the default GetInfo message for a non-permissioned blockchain
     *
     * @param   peer                    Target peer
     */
    static void sendGetInfoMessage(PeerImpl peer) {
        getInfoMessage.setBlockchainState(Peers.getMyBlockchainState());
        peer.sendMessage(getInfoMessage);
    }

    /**
     * Send our GetInfo message
     *
     * We will construct a GetInfo message containing the appropriate security
     * token for the target peer
     *
     * @param   peer                    Target peer
     * @param   peerPublicKey           Peer public key
     * @param   sessionKey              Session key
     */
    static void sendGetInfoMessage(PeerImpl peer, byte[] peerPublicKey, byte[] sessionKey) {
        if (!Constants.isPermissioned) {
            throw new IllegalStateException("Session key not supported");
        }
        NetworkMessage.GetInfoMessage message = new NetworkMessage.GetInfoMessage(
                Nxt.APPLICATION, Nxt.VERSION, getInfoMessage.getApplicationPlatform(),
                getInfoMessage.getShareAddress(), getInfoMessage.getAnnouncedAddress(),
                getInfoMessage.getApiPort(), getInfoMessage.getSslPort(), getInfoMessage.getServices(),
                getInfoMessage.getDisabledAPIs(), getInfoMessage.getApiServerIdleTimeout(),
                getInfoMessage.getSecurityToken().getPeerPublicKey());
        message.getSecurityToken().setSessionKey(Peers.peerSecretPhrase, peerPublicKey, sessionKey);
        peer.sendMessage(message);
    }

    /**
     * Check if the network has finished initialization
     *
     * @return                          TRUE if the network is available
     */
    public static boolean isNetworkStarted() {
        return networkStarted;
    }

    /**
     * Broadcast a message to all connected peers
     *
     * @param   message                 Message to send
     */
    public static void broadcastMessage(NetworkMessage message) {
        broadcastMessage(null, message);
    }

    /**
     * Broadcast a message to all connected peers
     *
     * @param   sender                  Message sender or null if our message
     * @param   message                 Message to send
     */
    public static void broadcastMessage(Peer sender, NetworkMessage message) {
        if (Constants.isOffline) {
            return;
        }
        connectionMap.values().forEach(peer -> {
            if (peer.getState() == Peer.State.CONNECTED &&
                    peer != sender &&
                    (peer.getBlockchainState() != Peer.BlockchainState.LIGHT_CLIENT ||
                     message.sendToLightClient())) {
                peer.sendMessage(message);
            }
        });
        wakeup();
    }

    /**
     * Get the default peer port
     *
     * @return                          Default peer port
     */
    public static int getDefaultPeerPort() {
        return Constants.isTestnet ? TESTNET_PEER_PORT : DEFAULT_PEER_PORT;
    }

    /**
     * Get the connected peer count
     *
     * @return                          Connected peer count
     */
    public static int getConnectionCount() {
        return inboundCount.get() + outboundCount.get();
    }

    /**
     * Get the number of inbound connections
     *
     * @return                          Number of inbound connections
     */
    public static int getInboundCount() {
        return inboundCount.get();
    }

    /**
     * Return the maximum number of inbound connections
     *
     * @return                          Number of inbound connections
     */
    public static int getMaxInboundConnections() {
        return maxInbound;
    }

    /**
     * Get the number of outbound connections
     *
     * @return                          Number of outbound connections
     */
    public static int getOutboundCount() {
        return outboundCount.get();
    }

    /**
     * Return the maximum number of outbound connections
     *
     * @return                          Number of outbound connections
     */
    public static int getMaxOutboundConnections() {
        return maxOutbound;
    }
}
