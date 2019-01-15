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

package nxt.env.service;

import nxt.Nxt;
import nxt.env.LookAndFeel;
import nxt.util.security.BlockchainPermission;

import javax.swing.*;

@SuppressWarnings("UnusedDeclaration")
public class ArdorService_ServiceManagement {

    public static boolean serviceInit() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("windowsService"));
        }
        LookAndFeel.init();
        new Thread(() -> {
            String[] args = {};
            Nxt.main(args);
        }).start();
        return true;
    }

    // Invoked when registering the service
    public static String[] serviceGetInfo() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("windowsService"));
        }
        return new String[]{
                "Ardor Server", // Long name
                "Manages the Ardor cryptographic currency protocol", // Description
                "true", // IsAutomatic
                "true", // IsAcceptStop
                "", // failure exe
                "", // args failure
                "", // dependencies
                "NONE/NONE/NONE", // ACTION = NONE | REBOOT | RESTART | RUN
                "0/0/0", // ActionDelay in seconds
                "-1", // Reset time in seconds
                "", // Boot Message
                "false" // IsAutomatic Delayed
        };
    }

    public static boolean serviceIsCreate() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("windowsService"));
        }
        return JOptionPane.showConfirmDialog(null, "Do you want to install the Ardor service ?", "Create Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceIsLaunch() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("windowsService"));
        }
        return true;
    }

    public static boolean serviceIsDelete() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("windowsService"));
        }
        return JOptionPane.showConfirmDialog(null, "This Ardor service is already installed. Do you want to delete it ?", "Delete Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceControl_Pause() {
        return false;
    }

    public static boolean serviceControl_Continue() {
        return false;
    }

    public static boolean serviceControl_Stop() {
        return true;
    }

    public static boolean serviceControl_Shutdown() {
        return true;
    }

    public static void serviceFinish() {
        System.exit(0);
    }

}
