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

package nxt.installer;

import com.izforge.izpack.event.RegistryInstallerListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
final class ConfigHandler {
    private static final Logger logger = Logger.getLogger(RegistryInstallerListener.class.getName());
    private static final String FILE_HEADER =
            "# This file contains settings configured during installation.\n" +
                    "# Do not modify this file, you may override these settings inside the nxt.properties file.\n" +
                    "# See conf/nxt-default.properties for a full list of properties.\n\n";
    private static final String JAR = "ardor.jar";
    private static final String SERVER = "http://localhost";
    private static final int[] PORTS = {26876, 27876};   // try to detect both testnet and real servers
    private static final String VAR_PREFIX = "nxt.installer.";

    public static final String FILE_PATH = "conf/nxt-installer.properties";
    public static final String VAR_CLEAN_INSTALL_DIR = VAR_PREFIX + "cleanInstallDir";
    public static final String VAR_SHUTDOWN_SERVER = VAR_PREFIX + "shutdownServer";
    public static final String VAR_FILE_CONTENTS = "settings";

    public boolean isServerRunning() {
        for (int port : PORTS) {
            try {
                URL url = new URL(SERVER + ':' + port + "/test");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            } catch (IOException e) {
                // try next port
            }
        }
        return false;
    }

    public boolean shutdownServer() {
        // TODO this will only work in case the node did not define an admin password or nxt.apiServerHost
        // Ideally we should invoke GetStateCall here and check the needsAdminPassword attribute and only
        // enable the shutdown option in this case. This will introduce dependency between the custom panel
        // and the Nxt project which does not exist at the moment so could have further implications.
        boolean done = false;
        for (int port : PORTS) {
            try {
                String spec = SERVER + ':' + port + "/nxt?";
                logger.log(Level.INFO, "Shutdown url: " + spec);
                String urlParameters = "requestType=shutdown";
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                URL url = new URL(spec);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);
                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(postData);
                }
                done |= (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
                logger.log(Level.INFO, "Shutdown response: " + conn.getResponseCode());
            } catch (IOException e) {
                logger.log(Level.INFO, "Server is not listening");
                // try next port
            }
        }
        return done;
    }

    public boolean isNxtInstallDir(String path) {
        return path != null && Files.exists(Paths.get(path, JAR));
    }

    public boolean cleanNxtInstallDir(String installPath, boolean retry) {
        logger.log(Level.INFO, "Trying to clean installation dir " + installPath);
        if (isNxtInstallDir(installPath)) {
            if (rmdir(installPath)) {
                logger.log(Level.INFO, "Installation dir removed " + installPath);
                return true;
            }
            if (retry) {
                for (int tries = 3; tries > 0; tries--) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (rmdir(installPath)) {
                        logger.log(Level.INFO, "Installation dir " + installPath + " removed on " + (3 - tries) + " try");
                        return true;
                    }
                }
            }
        } else {
            logger.log(Level.INFO, installPath + " is not an Ardor installation dir");
        }
        return false;
    }

    private boolean rmdir(String path) {
        try {
            for (Path p : Files.walk(Paths.get(path)).sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
                Files.delete(p);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            return false;
        }
        return true;
    }

    public static class Setting {
        private final boolean defaultValue;
        private final String description;
        private final Map<String, String> properties = new HashMap<>();
        private final List<String> lines = new LinkedList<>();

        Setting(String description, boolean defaultValue) {
            this.description = description;
            this.defaultValue = defaultValue;
        }

        String getName() {
            return description.split("\\. ", 2)[0];
        }

        public boolean isDefault() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public List<String> getLines() {
            return lines;
        }
    }

    /**
     * The file settings.txt is generated by the build-installer.sh script
     * It lists all the sample properties files, each properties file represents a single setting
     * on the installer panel.
     * @return list of settings for the installer panel
     */
    public List<Setting> readSettings() {
        try (InputStream is = getClass().getResourceAsStream("resources/settings.txt");
             BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            return in.lines()
                    .map(this::readSetting)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * For each properties file the code below analyzes the first property comment
     * All the text up to the first dot is used as description. The full comment
     * is used as tooltip.
     * @return one installer setting composed of 1 or more properties
     */
    private Setting readSetting(String settingFile) {
        Setting setting = null;
        try (InputStream is = getClass().getResourceAsStream("resources/" + settingFile);
             BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line;
            do {
                //noinspection StatementWithEmptyBody
                while ((line = in.readLine()) != null && !line.startsWith("#")) ;

                List<String> lines = new LinkedList<>();
                StringBuilder description = new StringBuilder();
                boolean defaultValue = false;
                while (line != null && line.startsWith("#")) {
                    description.append(line.substring(1).trim()).append('\n');
                    if (line.equals("# Default:true")) {
                        defaultValue = true;
                    } else {
                        lines.add(line);
                    }
                    line = in.readLine();
                }
                if (line != null && line.contains("=")) {
                    lines.add(line);
                    String[] parts = line.split("=");
                    String name = parts[0];
                    StringBuilder value = new StringBuilder();
                    line = parts[1];
                    while (line != null) {
                        line = line.trim();
                        if (line.endsWith("\\")) {
                            value.append(line, 0, line.length() - 1);
                            line = in.readLine();
                            lines.add(line);
                        } else {
                            value.append(line);
                            break;
                        }
                    }
                    if (setting == null) {
                        setting = new Setting(description.toString().trim(), defaultValue);
                    }
                    setting.properties.put(name, value.toString());
                    setting.getLines().addAll(lines);
                    setting.getLines().add("");
                }
            } while (line != null);
        } catch (IOException e) {
            // this setting will be skipped
            return null;
        }
        return setting;
    }

    public String writeSettings(List<Setting> settings) {
        return settings.stream().flatMap(s -> s.getLines().stream()).collect(Collectors.joining("\n"));
    }

    public boolean writeSettingsFile(String content, String path) {
        Path propFile = Paths.get(path, FILE_PATH);
        try (BufferedWriter out = Files.newBufferedWriter(propFile)) {
            out.write("# This file is " + propFile +"\n");
            out.write(FILE_HEADER);
            out.write(content);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write " + propFile.toAbsolutePath() + ", " + e);
            return false;
        }
        return true;
    }
}
