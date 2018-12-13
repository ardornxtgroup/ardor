/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxtdesktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;
import nxt.Nxt;
import nxt.blockchain.Block;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionProcessor;
import nxt.http.API;
import nxt.messaging.PrunableMessageHome;
import nxt.taggeddata.TaggedDataHome;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.TrustAllSSLProvider;
import nxt.util.security.BlockchainPermission;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DesktopApplication extends Application {

    private static final Set DOWNLOAD_REQUEST_TYPES = new HashSet<>(Arrays.asList("downloadTaggedData", "downloadPrunableMessage"));
    private static final boolean ENABLE_JAVASCRIPT_DEBUGGER = false;
    private static volatile boolean isLaunched;
    private static volatile Stage stage;
    private static volatile WebEngine webEngine;
    private JSObject nrs;
    private volatile long updateTime;
    private JavaScriptBridge javaScriptBridge;

    public static void launch() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }

        if (!isLaunched) {
            isLaunched = true;
            Application.launch(DesktopApplication.class);
            return;
        }
        if (stage != null) {
            Platform.runLater(() -> showStage(false));
        }
    }

    @SuppressWarnings("unused")
    public static void refresh() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }

        Platform.runLater(() -> showStage(true));
    }

    private static void showStage(boolean isRefresh) {
        if (isRefresh) {
            webEngine.load(getUrl());
        }
        if (!stage.isShowing()) {
            stage.show();
        } else if (stage.isIconified()) {
            stage.setIconified(false);
        } else {
            stage.toFront();
        }
    }

    public static void shutdown() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }

        System.out.println("shutting down JavaFX platform");
        Platform.exit();
        if (ENABLE_JAVASCRIPT_DEBUGGER) {
            try {
                Class<?> aClass = Class.forName("com.mohamnag.fxwebview_debugger.DevToolsDebuggerServer");
                aClass.getMethod("stopDebugServer").invoke(null);
            } catch (Exception e) {
                Logger.logInfoMessage("Error shutting down webview debugger", e);
            }
        }
        System.out.println("JavaFX platform shutdown complete");
    }

    @Override
    public void start(Stage stage) {
        DesktopApplication.stage = stage;
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        WebView browser = new WebView();
        WebView invisible = new WebView();

        int height = (int) Math.min(primaryScreenBounds.getMaxY() - 100, 1000);
        int width = (int) Math.min(primaryScreenBounds.getMaxX() - 100, 1618);
        browser.setMinHeight(height);
        browser.setMinWidth(width);
        webEngine = browser.getEngine();
        webEngine.setUserDataDirectory(Nxt.getConfDir());

        Worker<Void> loadWorker = webEngine.getLoadWorker();
        loadWorker.stateProperty().addListener(
                (ov, oldState, newState) -> {
                    Logger.logDebugMessage("loadWorker old state " + oldState + " new state " + newState);
                    if (newState != Worker.State.SUCCEEDED) {
                        Logger.logDebugMessage("loadWorker state change ignored");
                        return;
                    }
                    JSObject window = (JSObject)webEngine.executeScript("window");
                    javaScriptBridge = new JavaScriptBridge(this); // Must be a member variable to prevent gc
                    window.setMember("java", javaScriptBridge);
                    Locale locale = Locale.getDefault();
                    String language = locale.getLanguage().toLowerCase() + "-" + locale.getCountry().toUpperCase();
                    window.setMember("javaFxLanguage", language);
                    webEngine.executeScript("console.log = function(msg) { java.log(msg); };");
                    stage.setTitle("Ardor Desktop - " + webEngine.getLocation());
                    nrs = (JSObject) webEngine.executeScript("NRS");
                    updateClientState("Desktop Wallet started");
                    BlockchainProcessor blockchainProcessor = Nxt.getBlockchainProcessor();
                    blockchainProcessor.addListener(this::updateClientState, BlockchainProcessor.Event.BLOCK_PUSHED);
                    Nxt.getTransactionProcessor().addListener(transaction ->
                            updateClientState(TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS, transaction), TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
                    Nxt.getTransactionProcessor().addListener(transaction ->
                            updateClientState(TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS, transaction), TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS);

                    if (ENABLE_JAVASCRIPT_DEBUGGER) {
                        try {
                            // Add the javafx_webview_debugger and websocket-* test libs to the classpath
                            // For more details, check https://github.com/mohamnag/javafx_webview_debugger
                            Class<?> aClass = Class.forName("com.mohamnag.fxwebview_debugger.DevToolsDebuggerServer");
                            Class webEngineClazz = WebEngine.class;
                            Field debuggerField = webEngineClazz.getDeclaredField("debugger");
                            debuggerField.setAccessible(true);
                            Object debugger = debuggerField.get(webEngine);
                            //noinspection JavaReflectionMemberAccess
                            Method startDebugServer = aClass.getMethod("startDebugServer", debugger.getClass(), int.class);
                            startDebugServer.invoke(null, debugger, 51742);
                        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            Logger.logInfoMessage("Cannot start JavaFx debugger", e);
                        }
                    }
               });

        // Invoked by the webEngine popup handler
        // The invisible webView does not show the link, instead it opens a browser window
        invisible.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> popupHandlerURLChange(newValue));

        // Invoked when changing the document.location property, when issuing a download request
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> webViewURLChange(newValue));

        // Invoked when clicking a link to external site like Help or API console
        webEngine.setCreatePopupHandler(
            config -> {
                Logger.logInfoMessage("popup request from webEngine");
                return invisible.getEngine();
            });

        webEngine.load(getUrl());

        Scene scene = new Scene(browser);
        String address = API.getServerRootUri().toString();
        stage.getIcons().add(new Image(address + "/img/nxt-icon-32x32.png"));
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        Platform.setImplicitExit(false); // So that we can reopen the application in case the user closed it
    }

    private void updateClientState(Block block) {
        if (Nxt.getBlockchainProcessor().isDownloading()) {
            if (System.currentTimeMillis() - updateTime < 10000L) {
                return;
            }
        }
        String msg = BlockchainProcessor.Event.BLOCK_PUSHED.toString() + " id " + block.getStringId() + " height " + block.getHeight();
        updateClientState(msg);
    }

    private void updateClientState(TransactionProcessor.Event transactionEvent, List<? extends Transaction> transactions) {
        if (System.currentTimeMillis() - updateTime > 3000L) {
            String msg = transactionEvent.toString() + " ids " + transactions.stream().map(Transaction::getStringId).collect(Collectors.joining(","));
            updateClientState(msg);
        }
    }

    private void updateClientState(String msg) {
        updateTime = System.currentTimeMillis();
        Platform.runLater(() -> webEngine.executeScript("NRS.getState(null, '" + msg + "')"));
    }

    private static String getUrl() {
        String url = API.getWelcomePageUri().toString();
        if (url.startsWith("https")) {
            HttpsURLConnection.setDefaultSSLSocketFactory(TrustAllSSLProvider.getSslSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllSSLProvider.getHostNameVerifier());
        }
        String defaultAccount = Nxt.getStringProperty("nxt.defaultDesktopAccount");
        if (defaultAccount != null && !defaultAccount.equals("")) {
            url += "?account=" + defaultAccount;
        }
        return url;
    }

    @SuppressWarnings("WeakerAccess")
    public void popupHandlerURLChange(String newValue) {
        Logger.logInfoMessage("popup request for " + newValue);
        Platform.runLater(() -> {
            try {
                Desktop.getDesktop().browse(new URI(newValue));
            } catch (Exception e) {
                Logger.logInfoMessage("Cannot open " + newValue + " error " + e.getMessage());
            }
        });
    }

    private void webViewURLChange(String newValue) {
        Logger.logInfoMessage("webview address changed to " + newValue);
        URL url;
        try {
            url = new URL(newValue);
        } catch (MalformedURLException e) {
            Logger.logInfoMessage("Malformed URL " + newValue, e);
            return;
        }
        String query = url.getQuery();
        if (query == null) {
            return;
        }
        String[] paramPairs = query.split("&");
        Map<String, String> params = new HashMap<>();
        for (String paramPair : paramPairs) {
            String[] keyValuePair = paramPair.split("=");
            if (keyValuePair.length == 2) {
                params.put(keyValuePair[0], keyValuePair[1]);
            }
        }
        String requestType = params.get("requestType");
        if (DOWNLOAD_REQUEST_TYPES.contains(requestType)) {
            download(requestType, params);
        } else {
            Logger.logInfoMessage(String.format("requestType %s is not a download request", requestType));
        }
    }

    private void download(String requestType, Map<String, String> params) {
        String chainName = params.get("chain");
        Chain chain = Chain.getChain(chainName);
        if (chain == null) {
            chain = Chain.getChain(Integer.valueOf(chainName));
        }
        boolean retrieve = "true".equals(params.get("retrieve"));
        byte[] transactionFullHash = Convert.parseHexString(params.get("transactionFullHash"));
        if (requestType.equals("downloadTaggedData")) {
            ChildChain childChain = (ChildChain)chain;
            TaggedDataHome.TaggedData taggedData = childChain.getTaggedDataHome().getData(transactionFullHash);
            if (taggedData == null && retrieve) {
                try {
                    if (Nxt.getBlockchainProcessor().restorePrunedTransaction(childChain, transactionFullHash) == null) {
                        growl("Pruned transaction data not currently available from any peer");
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    growl("Pruned transaction data cannot be restored using desktop wallet without full blockchain. Use Web Wallet instead");
                    return;
                }
                taggedData = childChain.getTaggedDataHome().getData(transactionFullHash);
            }
            if (taggedData == null) {
                growl("Tagged data not found");
                return;
            }
            byte[] data = taggedData.getData();
            String filename = taggedData.getFilename();
            if (filename == null || filename.trim().isEmpty()) {
                filename = taggedData.getName().trim();
            }
            downloadFile(data, filename);
        } else if (requestType.equals("downloadPrunableMessage")) {
            PrunableMessageHome.PrunableMessage prunableMessage = chain.getPrunableMessageHome().getPrunableMessage(transactionFullHash);
            if (prunableMessage == null && retrieve) {
                try {
                    if (Nxt.getBlockchainProcessor().restorePrunedTransaction(chain, transactionFullHash) == null) {
                        growl("Pruned message not currently available from any peer");
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    growl("Pruned message cannot be restored using desktop wallet without full blockchain. Use Web Wallet instead");
                    return;
                }
                prunableMessage = chain.getPrunableMessageHome().getPrunableMessage(transactionFullHash);
            }
            String secretPhrase = params.get("secretPhrase");
            byte[] sharedKey = Convert.parseHexString(params.get("sharedKey"));
            if (sharedKey == null) {
                sharedKey = Convert.EMPTY_BYTE;
            }
            if (sharedKey.length != 0 && secretPhrase != null) {
                growl("Do not specify both secret phrase and shared key");
                return;
            }
            byte[] data = null;
            if (prunableMessage != null) {
                try {
                    if (secretPhrase != null) {
                        data = prunableMessage.decrypt(secretPhrase);
                    } else if (sharedKey.length > 0) {
                        data = prunableMessage.decrypt(sharedKey);
                    } else {
                        data = prunableMessage.getMessage();
                    }
                } catch (RuntimeException e) {
                    Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                    growl("Wrong secretPhrase or sharedKey");
                    return;
                }
            }
            if (data == null) {
                data = Convert.EMPTY_BYTE;
            }
            downloadFile(data, Long.toUnsignedString(Convert.fullHashToId(transactionFullHash)));
        }
    }

    private void downloadFile(byte[] data, String filename) {
        Path folderPath = Paths.get(System.getProperty("user.home"), "downloads");
        Path path = Paths.get(folderPath.toString(), filename);
        Logger.logInfoMessage("Downloading data to " + path.toAbsolutePath());
        try {
            OutputStream outputStream = Files.newOutputStream(path);
            outputStream.write(data);
            outputStream.close();
            growl(String.format("File %s saved to folder %s", filename, folderPath));
        } catch (IOException e) {
            growl("Download failed " + e.getMessage(), e);
        }
    }

    public void stop() {
        System.out.println("DesktopApplication stopped"); // Should never happen
    }

    private void growl(String msg) {
        growl(msg, null);
    }

    private void growl(String msg, Exception e) {
        if (e == null) {
            Logger.logInfoMessage(msg);
        } else {
            Logger.logInfoMessage(msg, e);
        }
        nrs.call("growl", msg);
    }

}
