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

package nxtdesktop;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import nxt.addons.JO;
import nxt.crypto.Crypto;
import nxt.http.API;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.security.BlockchainPermission;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The class itself and methods in this class are invoked from JavaScript therefore has to be public
 */
@SuppressWarnings("WeakerAccess")
public class JavaScriptBridge {

    final DesktopApplication application;
    private Clipboard clipboard;

    public JavaScriptBridge(DesktopApplication application) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }
        this.application = application;
    }

    public void log(String message) {
        Logger.logInfoMessage(message);
    }

    @SuppressWarnings("unused")
    public void openBrowser(String account) {
        final String url = API.getWelcomePageUri().toString() + "?account=" + account;
        Platform.runLater(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                Logger.logInfoMessage("Cannot open " + API.getWelcomePageUri().toString() + " error " + e.getMessage());
            }
        });
    }

    @SuppressWarnings("unused")
    public String readContactsFile() {
        return readJsonFile("contacts.json");
    }

    @SuppressWarnings("unused")
    public String readApprovalModelsFile() {
        return readJsonFile("approval.models.json");
    }

    private String readJsonFile(String fileName) {
        try {
            Path folderPath = Paths.get(System.getProperty("user.home"), "downloads");
            return application.readTextfile(folderPath, fileName).orElse(null);
        } catch (IOException e) {
            Logger.logInfoMessage("Error reading " + fileName + ", error " + e.getMessage());
            JO response = new JO();
            response.put("error", e.getMessage());
            return response.toJSONString();
        }
    }

    public String getAdminPassword() {
        return API.getAdminPassword();
    }

    @SuppressWarnings("unused")
    public void popupHandlerURLChange(String newValue) {
        application.popupHandlerURLChange(newValue);
    }

    @SuppressWarnings("unused")
    public boolean copyText(String text) {
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
            if (clipboard == null) {
                return false;
            }
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        return clipboard.setContent(content);
    }

    @SuppressWarnings("unused")
    public void renderPaperWallet(String page) {
        API.setPaperWalletPage(page);
        byte[] hash = Crypto.sha256().digest(page.getBytes(StandardCharsets.UTF_8));
        Platform.runLater(() -> {
            try {
                URI uri = API.getPaperWalletUri();
                Desktop.getDesktop().browse(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), "hash=" + Convert.toHexString(hash), uri.getFragment()));
            } catch (Exception e) {
                Logger.logInfoMessage("Cannot open paper wallet " + e);
            }
        });
    }

    @SuppressWarnings("unused")
    public void downloadTextFile(String text, String filename) {
        application.downloadFile(text, filename);
    }

    @SuppressWarnings("unused")
    public boolean isFileReaderSupported() {
        String version = System.getProperty("javafx.version");
        return Integer.parseInt(version.substring(0, version.indexOf('.'))) >= 12;
    }
}