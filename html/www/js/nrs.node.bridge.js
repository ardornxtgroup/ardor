/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

const options = {
    url: "http://localhost:27876", // URL of NXT remote node
    secretPhrase: "", // Secret phrase of the current account
    isTestNet: false, // Select testnet or mainnet
    chain: "2", // Default to IGNIS chain
    adminPassword: "" // Node admin password
};

exports.init = function(params) {
    if (!params) {
        return this;
    }
    options.url = params.url;
    options.secretPhrase = params.secretPhrase;
    options.isTestNet = params.isTestNet;
    options.chain = params.chain;
    options.adminPassword = params.adminPassword;
    return this;
};

exports.load = function(callback) {
    // jsdom is necessary to define the window object on which jquery relies
    require("jsdom").env("", function(err, window) {
        try {
            if (err) {
                console.error(err);
                return;
            }
            console.log("Started");

            // Load the necessary node modules and assign them to the global scope
            // the NXT client wasn't designed with modularity in mind therefore we need
            // to include every 3rd party library function in the global scope
            global.jQuery = require("jquery")(window);
            jQuery.growl = function(msg) { console.log("growl: " + msg)}; // disable growl messages
            jQuery.t = function(text) { return text; }; // Disable the translation functionality
            global.$ = global.jQuery; // Needed by extensions.js
            // noinspection NodeJsCodingAssistanceForCoreModules
            global.crypto = require("crypto");
            global.CryptoJS = require("crypto-js");
            global.async = require("async");
            global.pako = require("pako");
            var jsbn = require('jsbn');
            global.BigInteger = jsbn.BigInteger;
            global.window = window;
            window.console = console;

            // Mock other objects on which the client depends
            global.document = {};
            global.isNode = true; // for code which has to execute differently by node compared to browser
            global.navigator = {};
            navigator.userAgent = "";

            // Now load some NXT specific libraries into the global scope
            global.NxtAddress = require('./util/nxtaddress');
            global.curve25519 = require('./crypto/curve25519');
            global.curve25519_ = require('./crypto/curve25519_');
            require('./util/extensions');
            global.converters = require('./util/converters');

            // Now start loading the client itself
            // The main challenge is that in node every JavaScript file is a module with it's own scope
            // however the NXT client relies on a global browser scope which defines the NRS object
            // The idea here is to gradually compose the NRS object by adding functions from each
            // JavaScript file into the existing global.client scope
            global.client = {};
            global.client.isTestNet = options.isTestNet;
            global.client.mobileSettings = {};
            global.client.mobileSettings.chain = options.chain;
            global.client = Object.assign(client, require('./nrs.encryption'));
            global.client = Object.assign(client, require('./nrs.feature.detection'));
            global.client = Object.assign(client, require('./nrs.transactions.types'));
            global.client = Object.assign(client, require('./nrs.constants'));
            global.client = Object.assign(client, require('./nrs.console'));
            global.client = Object.assign(client, require('./nrs.util'));
            global.client = Object.assign(client, require('./nrs.numeric'));
            client.getModuleConfig = function() {
                return options;
            };
            global.client = Object.assign(client, require('./nrs'));
            global.client = Object.assign(client, require('./nrs.server'));
            // Now load the constants locally since we cannot trust the remote node to
            // return the correct constants.

            var constants;
            if (options.isTestNet) {
                constants = require('./data/constants.testnet');
            } else {
                constants = require('./data/constants.mainnet');
            }
            global.client.processConstants(constants);
            setCurrentAccount(options.secretPhrase);
            callback(global.client);
        } catch (e) {
            console.log(e.message);
            if (e.stack) {
                console.log(e.stack);
            }
            throw e;
        }
    });
};

function setCurrentAccount(secretPhrase) {
    client.account = client.getAccountId(secretPhrase);
    client.accountRS = client.convertNumericToRSAccountFormat(client.account);
    client.accountInfo = {}; // Do not cache the public key
    client.resetEncryptionState();
}
exports.setCurrentAccount = setCurrentAccount;