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

package nxt.env;

public enum ServerStatus {
    NOT_INITIALIZED(false, false, "Waiting for Initialization"), BEFORE_DATABASE(false, false, "Loading Database"), AFTER_DATABASE(true, false, "Loading Resources"), STARTED(true, true, "Online");

    private final boolean isDatabaseReady;
    private final boolean isApiPortReady;
    private final String message;

    ServerStatus(boolean isDatabaseReady, boolean isApiPortReady, String message) {
        this.isDatabaseReady = isDatabaseReady;
        this.isApiPortReady = isApiPortReady;
        this.message = message;
    }

    public boolean isDatabaseReady() {
        return isDatabaseReady;
    }

    public boolean isApiPortReady() {
        return isApiPortReady;
    }

    public String getMessage() {
        return message;
    }
}
