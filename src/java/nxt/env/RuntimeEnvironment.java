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

package nxt.env;

import nxt.Nxt;
import nxt.util.Logger;
import nxt.util.security.BlockchainPermission;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RuntimeEnvironment {

    public static final String RUNTIME_MODE_ARG = "nxt.runtime.mode";
    public static final String DIRPROVIDER_ARG = "nxt.runtime.dirProvider";

    private static final String osname = System.getProperty("os.name").toLowerCase();
    private static final String javaSpecVendor = System.getProperty("java.specification.vendor");
    private static final boolean isHeadless;
    private static final boolean hasJavaFX;
    static {
        boolean b;
        try {
            // Load by reflection to prevent exception in case java.awt does not exist
            Class graphicsEnvironmentClass = Class.forName("java.awt.GraphicsEnvironment");
            Method isHeadlessMethod = graphicsEnvironmentClass.getMethod("isHeadless");
            b = (Boolean)isHeadlessMethod.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            b = true;
        }
        isHeadless = b;
        try {
            Class.forName("javafx.application.Application");
            b = true;
        } catch (ClassNotFoundException e) {
            System.out.println("javafx not supported");
            b = false;
        }
        hasJavaFX = b;
    }

    private static boolean isWindowsRuntime() {
        return osname.startsWith("windows");
    }

    private static boolean isUnixRuntime() {
        return osname.contains("nux") || osname.contains("nix") || osname.contains("aix") || osname.contains("bsd") || osname.contains("sunos");
    }

    private static boolean isMacRuntime() {
        return osname.contains("mac");
    }

    public static boolean isAndroidRuntime() {
        return javaSpecVendor.equals("The Android Project");
    }

    private static boolean isWindowsService() {
        return "service".equalsIgnoreCase(System.getProperty(RUNTIME_MODE_ARG)) && isWindowsRuntime();
    }

    private static boolean isHeadless() {
        return isHeadless;
    }

    private static boolean isDesktopEnabled(String configuredMode) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }
        boolean isDesktopModeConfigured;
        if (configuredMode == null) {
            isDesktopModeConfigured = "desktop".equalsIgnoreCase(Nxt.getStringProperty(RUNTIME_MODE_ARG));
        } else {
            isDesktopModeConfigured = "desktop".equalsIgnoreCase(configuredMode);
        }
        return ("desktop".equalsIgnoreCase(System.getProperty(RUNTIME_MODE_ARG)) || isDesktopModeConfigured)
                && !isHeadless();
    }

    public static boolean isDesktopApplicationEnabled() {
        boolean isDesktopEnabled = isDesktopEnabled(null);
        boolean isLaunchDesktopApplication = Nxt.getBooleanProperty("nxt.launchDesktopApplication");
        Logger.logInfoMessage("Desktop application isDesktopEnabled:" + isDesktopEnabled + ", isLaunchDesktopApplication:" + isLaunchDesktopApplication + ", hasJavaFX:" + hasJavaFX);
        return isDesktopEnabled && isLaunchDesktopApplication && hasJavaFX;
    }

    public static RuntimeMode getRuntimeMode(String configuredMode) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("desktop"));
        }
        System.out.println("isHeadless=" + isHeadless());
        if (isDesktopEnabled(configuredMode)) {
            return new DesktopMode();
        } else if (isWindowsService()) {
            return new WindowsServiceMode();
        } else if (isAndroidRuntime()) {
            try {
                return (RuntimeMode) Class.forName("nxt.env.AndroidServiceMode").newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to instantiate nxt.env.AndroidServiceMode", e);
            }
        } else {
            return new CommandLineMode();
        }
    }

    public static DirProvider getDirProvider(String configuredMode) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("sensitiveInfo"));
        }
        String dirProvider = System.getProperty(DIRPROVIDER_ARG);
        if (dirProvider != null) {
            try {
                return (DirProvider)Class.forName(dirProvider).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                System.out.println("Failed to instantiate dirProvider " + dirProvider);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        if (isAndroidRuntime()) {
            return new AndroidDirProvider();
        }
        if (isDesktopEnabled(configuredMode)) {
            if (isWindowsRuntime()) {
                return new WindowsUserDirProvider();
            }
            if (isUnixRuntime()) {
                return new UnixUserDirProvider();
            }
            if (isMacRuntime()) {
                return new MacUserDirProvider();
            }
        }
        return new DefaultDirProvider();
    }
}
