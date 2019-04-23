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

package nxt;

import nxt.blockchain.Block;
import nxt.blockchain.BlockImpl;
import nxt.blockchain.Blockchain;
import nxt.blockchain.BlockchainImpl;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.BlockchainProcessorImpl;
import nxt.blockchain.FxtTransaction;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionProcessor;
import nxt.blockchain.TransactionProcessorImpl;
import nxt.configuration.Setup;
import nxt.configuration.SubSystem;
import nxt.env.DirProvider;
import nxt.env.RuntimeEnvironment;
import nxt.env.RuntimeMode;
import nxt.env.ServerStatus;
import nxt.http.API;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.Time;
import nxt.util.security.BlockchainPermission;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class Nxt {

    public static final String VERSION = "2.2.3";
    public static final String APPLICATION = "Ardor";

    private static volatile Time time = new Time.EpochTime();
    private static volatile ServerStatus serverStatus = ServerStatus.NOT_INITIALIZED;

    public static final String NXT_DEFAULT_PROPERTIES = "nxt-default.properties";
    public static final String NXT_PROPERTIES = "nxt.properties";
    public static final String NXT_INSTALLER_PROPERTIES = "nxt-installer.properties";
    public static final String CONFIG_DIR = "conf";

    private static final RuntimeMode runtimeMode;
    private static final DirProvider dirProvider;
    private static Setup setup = Setup.NOT_INITIALIZED;

    private static final Properties defaultProperties = new Properties();
    static {
        redirectSystemStreams("out");
        redirectSystemStreams("err");
        System.out.println("Initializing Nxt server version " + Nxt.VERSION);
        printCommandLineArguments();
        String installerConfiguredMode = getInstallerConfiguredRuntimeMode();
        runtimeMode = RuntimeEnvironment.getRuntimeMode(installerConfiguredMode);
        System.out.printf("Runtime mode %s\n", runtimeMode.getClass().getName());
        dirProvider = RuntimeEnvironment.getDirProvider(installerConfiguredMode);
        System.out.println("User home folder " + dirProvider.getUserHomeDir());
        loadProperties(defaultProperties, NXT_DEFAULT_PROPERTIES, true);
        if (!VERSION.equals(Nxt.defaultProperties.getProperty("nxt.version"))) {
            throw new RuntimeException("Using an nxt-default.properties file from a version other than " + VERSION + " is not supported!!!");
        }
    }

    /**
     * In previous versions the runtime mode was configured only by a Java VM flag.
     * But this means that every command line tool has to set it manually.
     * From now the installer can also set the runtime mode.
     * The challenge is that for unit tests we don't like to set desktop
     * runtime mode.
     * Therefore we rely on the fact that unit tests enable assertions (-ea) which are normally disabled.
     * We turn off runtime mode when assertions are enabled.
     * @return the runtime mode
     */
    private static String getInstallerConfiguredRuntimeMode() {
        Properties tempInstallerProperties = new Properties();
        loadProperties(tempInstallerProperties, NXT_INSTALLER_PROPERTIES, true);
        boolean isAssertOn = false;
        //noinspection ConstantConditions,AssertWithSideEffects
        assert isAssertOn = true;
        //noinspection ConstantConditions
        return isAssertOn || tempInstallerProperties.getProperty("nxt.runtime.mode") == null ? "" : tempInstallerProperties.getProperty("nxt.runtime.mode");
    }

    private static void redirectSystemStreams(String streamName) {
        String isStandardRedirect = System.getProperty("nxt.redirect.system." + streamName);
        Path path = null;
        if (isStandardRedirect != null) {
            try {
                path = Files.createTempFile("nxt.system." + streamName + ".", ".log");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            String explicitFileName = System.getProperty("nxt.system." + streamName);
            if (explicitFileName != null) {
                path = Paths.get(explicitFileName);
            }
        }
        if (path != null) {
            try {
                PrintStream stream = new PrintStream(Files.newOutputStream(path));
                if (streamName.equals("out")) {
                    System.setOut(new PrintStream(stream));
                } else {
                    System.setErr(new PrintStream(stream));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Properties properties = new Properties(defaultProperties);

    static {
        loadProperties(properties, NXT_INSTALLER_PROPERTIES, true);
        loadProperties(properties, NXT_PROPERTIES, false);
    }

    public static void loadProperties(Properties properties, String propertiesFile, boolean isDefault) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("properties"));
        }
        try {
            // Load properties from location specified as command line parameter
            String configFile = System.getProperty(propertiesFile);
            if (configFile != null) {
                System.out.printf("Loading %s from %s\n", propertiesFile, configFile);
                try (InputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                } catch (IOException e) {
                    throw new IllegalArgumentException(String.format("Error loading %s from %s", propertiesFile, configFile));
                }
            } else {
                try (InputStream is = ClassLoader.getSystemResourceAsStream(propertiesFile)) {
                    // When running nxt.exe from a Windows installation we always have nxt.properties in the classpath but this is not the nxt properties file
                    // Therefore we first load it from the classpath and then look for the real nxt.properties in the user folder.
                    if (is != null) {
                        System.out.printf("Loading %s from classpath\n", propertiesFile);
                        properties.load(is);
                        if (isDefault) {
                            return;
                        }
                    }
                    // load non-default properties files from the user folder
                    if (dirProvider == null || !dirProvider.isLoadPropertyFileFromUserDir()) {
                        return;
                    }
                    String homeDir = dirProvider.getUserHomeDir();
                    if (!Files.isReadable(Paths.get(homeDir))) {
                        System.out.printf("Creating dir %s\n", homeDir);
                        try {
                            Files.createDirectory(Paths.get(homeDir));
                        } catch(Exception e) {
                            if (!(e instanceof NoSuchFileException)) {
                                throw e;
                            }
                            // Fix for WinXP and 2003 which does have a roaming sub folder
                            Files.createDirectory(Paths.get(homeDir).getParent());
                            Files.createDirectory(Paths.get(homeDir));
                        }
                    }
                    Path confDir = Paths.get(homeDir, CONFIG_DIR);
                    if (!Files.isReadable(confDir)) {
                        System.out.printf("Creating dir %s\n", confDir);
                        Files.createDirectory(confDir);
                    }
                    Path propPath = Paths.get(confDir.toString()).resolve(Paths.get(propertiesFile));
                    if (Files.isReadable(propPath)) {
                        System.out.printf("Loading %s from dir %s\n", propertiesFile, confDir);
                        properties.load(Files.newInputStream(propPath));
                    } else {
                        System.out.printf("Creating property file %s\n", propPath);
                        Files.createFile(propPath);
                        Files.write(propPath, Convert.toBytes("# use this file for workstation specific " + propertiesFile));
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("Error loading " + propertiesFile, e);
                }
            }
        } catch(IllegalArgumentException e) {
            e.printStackTrace(); // make sure we log this exception
            throw e;
        }
    }

    private static void printCommandLineArguments() {
        try {
            List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            if (inputArguments != null && inputArguments.size() > 0) {
                System.out.println("Command line arguments");
            } else {
                return;
            }
            inputArguments.forEach(System.out::println);
        } catch (AccessControlException e) {
            System.out.println("Cannot read input arguments " + e.getMessage());
        }
    }

    public static int getIntProperty(String name) {
        return getIntProperty(name, 0);
    }

    public static int getIntProperty(String name, int defaultValue) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("properties"));
        }
        try {
            int result = Integer.parseInt(properties.getProperty(name));
            Logger.logMessage(name + " = \"" + result + "\"");
            return result;
        } catch (NumberFormatException e) {
            Logger.logMessage(name + " not defined or not numeric, using default value " + defaultValue);
            return defaultValue;
        }
    }

    public static String getStringProperty(String name) {
        return getStringProperty(name, null, false);
    }

    public static String getStringProperty(String name, String defaultValue) {
        return getStringProperty(name, defaultValue, false);
    }

    public static String getStringProperty(String name, String defaultValue, boolean doNotLog) {
        return getStringProperty(name, defaultValue, doNotLog, null);
    }

    public static String getStringProperty(String name, String defaultValue, boolean doNotLog, String encoding) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("properties"));
        }
        String value = properties.getProperty(name);
        if (value != null && ! "".equals(value)) {
            Logger.logMessage(name + " = \"" + (doNotLog ? "{not logged}" : value) + "\"");
        } else {
            Logger.logMessage(name + " not defined");
            value = defaultValue;
        }
        if (encoding == null || value == null) {
            return value;
        }
        try {
            return new String(value.getBytes(StandardCharsets.ISO_8859_1), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getStringListProperty(String name) {
        String value = getStringProperty(name);
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String s : value.split(";")) {
            s = s.trim();
            if (s.length() > 0) {
                result.add(s);
            }
        }
        return result;
    }

    public static boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("properties"));
        }
        String value = properties.getProperty(name);
        if (Boolean.TRUE.toString().equals(value)) {
            Logger.logMessage(name + " = \"true\"");
            return true;
        } else if (Boolean.FALSE.toString().equals(value)) {
            Logger.logMessage(name + " = \"false\"");
            return false;
        }
        Logger.logMessage(name + " not defined, using default " + defaultValue);
        return defaultValue;
    }

    public static Blockchain getBlockchain() {
        return BlockchainImpl.getInstance();
    }

    public static BlockchainProcessor getBlockchainProcessor() {
        return BlockchainProcessorImpl.getInstance();
    }

    public static TransactionProcessor getTransactionProcessor() {
        return TransactionProcessorImpl.getInstance();
    }

    public static Block parseBlock(byte[] blockBytes, List<? extends FxtTransaction> blockTransactions) throws NxtException.NotValidException {
        return BlockImpl.parseBlock(blockBytes, blockTransactions);
    }

    public static Transaction parseTransaction(byte[] transactionBytes) throws NxtException.NotValidException {
        return TransactionImpl.parseTransaction(transactionBytes);
    }

    public static Transaction.Builder newTransactionBuilder(byte[] transactionBytes) throws NxtException.NotValidException {
        return TransactionImpl.newTransactionBuilder(transactionBytes);
    }

    public static Transaction.Builder newTransactionBuilder(JSONObject transactionJSON) throws NxtException.NotValidException {
        return TransactionImpl.newTransactionBuilder(transactionJSON);
    }

    public static Transaction.Builder newTransactionBuilder(byte[] transactionBytes, JSONObject prunableAttachments) throws NxtException.NotValidException {
        return TransactionImpl.newTransactionBuilder(transactionBytes, prunableAttachments);
    }

    public static int getEpochTime() {
        return time.getTime();
    }
    
    public static void setTime(Time time) {
        Nxt.time = time;
    }

    public static void main(String[] args) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("lifecycle"));
        }
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(Nxt::shutdown));
            init();
        } catch (Throwable t) {
            System.out.println("Fatal error: " + t.toString());
            t.printStackTrace();
        }
    }

    public static void init(Setup setup, Properties customProperties) {
        properties.putAll(customProperties);
        init(setup);
    }

    public static void init() {
        init(Setup.FULL_NODE);
    }

    public static void init(Setup setup) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("lifecycle"));
        }
        Nxt.setup = setup;
        Init.init(setup);
    }

    /**
     * Shutdown the application.
     */
    public static void shutdown() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("lifecycle"));
        }
        Logger.logShutdownMessage("Shutting down...");
        setup.shutdownSequence().forEach(SubSystem::shutdown);
    }

    private static class Init {

        private static volatile boolean initialized = false;

        private static void init(Setup setup) {
            if (initialized) {
                throw new RuntimeException("Nxt.init has already been called");
            }
            try {
                long startTime = System.currentTimeMillis();
                setup.initSequence().forEach(SubSystem::init);
                logInitMessages(startTime);
            } catch (Exception e) {
                Logger.logErrorMessage(e.getMessage(), e);
                runtimeMode.alert(e.getMessage() + "\n" +
                        "See additional information in " + Paths.get(dirProvider.getLogFileDir().getPath(), "nxt.log").toAbsolutePath());
                System.exit(1);
            }
            initialized = true;
        }

        private static void logInitMessages(long startTime) {
            long currentTime = System.currentTimeMillis();
            Logger.logMessage("Initialization took " + (currentTime - startTime) / 1000 + " seconds");
            Logger.logMessage("Ardor server " + VERSION + " started successfully.");
            Logger.logMessage("Copyright © 2013-2016 The Nxt Core Developers.");
            Logger.logMessage("Copyright © 2016-2019 Jelurida IP B.V.");
            Logger.logMessage("Distributed under the Jelurida Public License version 1.1 for the Ardor Public Blockchain Platform, with ABSOLUTELY NO WARRANTY.");
            if (API.getWelcomePageUri() != null) {
                Logger.logMessage("Client UI is at " + API.getWelcomePageUri());
            }
            if (Constants.isTestnet) {
                Logger.logMessage("RUNNING ON TESTNET - DO NOT USE REAL ACCOUNTS!");
            }
            setServerStatus(ServerStatus.STARTED, API.getWelcomePageUri());
        }

        private Init() {} // never

    }

    public static String getProcessId() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("sensitiveInfo"));
        }
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        if (runtimeName == null) {
            return "";
        }
        String[] tokens = runtimeName.split("@");
        if (tokens.length == 2) {
            return tokens[0];
        }
        return "";
    }

    public static String getDbDir(String dbDir) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("dirProvider"));
        }
        return dirProvider.getDbDir(dbDir);
    }

    public static void updateLogFileHandler(Properties loggingProperties) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("dirProvider"));
        }
        dirProvider.updateLogFileHandler(loggingProperties);
    }

    public static String getUserHomeDir() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("dirProvider"));
        }
        return dirProvider.getUserHomeDir();
    }

    public static File getConfDir() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("dirProvider"));
        }
        return dirProvider.getConfDir();
    }

    public static void setServerStatus(ServerStatus status, URI wallet) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("lifecycle"));
        }
        serverStatus = status;
        runtimeMode.setServerStatus(status, wallet, dirProvider.getLogFileDir());
    }

    public static ServerStatus getServerStatus() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("sensitiveInfo"));
        }
        return serverStatus;
    }

    public static RuntimeMode getRuntimeMode() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("sensitiveInfo"));
        }
        return runtimeMode;
    }

    public static boolean isEnabled(SubSystem subSystem) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("sensitiveInfo"));
        }
        return setup.initSequence().contains(subSystem);
    }

    private Nxt() {} // never

}
