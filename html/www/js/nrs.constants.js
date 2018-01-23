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

/**
 * @depends {nrs.js}
 */
var NRS = (function (NRS, $) {
    NRS.constants = {
        'DB_VERSION': 2,

        'PLUGIN_VERSION': 1,
        'MAX_SHORT_JAVA': 32767,
        'MAX_UNSIGNED_SHORT_JAVA': 65535,
        'MAX_INT_JAVA': 2147483647,
        'MAX_LONG_JAVA': "9223372036854775807",
        'DISABLED_API_ERROR_CODE': 16,
        'MAX_ONE_COIN': 10000000000000000,

        //Plugin launch status numbers
        'PL_RUNNING': 1,
        'PL_PAUSED': 2,
        'PL_DEACTIVATED': 3,
        'PL_HALTED': 4,

        //Plugin validity status codes
        'PV_VALID': 100,
        'PV_NOT_VALID': 300,
        'PV_UNKNOWN_MANIFEST_VERSION': 301,
        'PV_INCOMPATIBLE_MANIFEST_VERSION': 302,
        'PV_INVALID_MANIFEST_FILE': 303,
        'PV_INVALID_MISSING_FILES': 304,
        'PV_INVALID_JAVASCRIPT_FILE': 305,

        //Plugin NRS compatibility status codes
        'PNC_COMPATIBLE': 100,
        'PNC_COMPATIBILITY_MINOR_RELEASE_DIFF': 101,
        'PNC_COMPATIBILITY_WARNING': 200,
        'PNC_COMPATIBILITY_MAJOR_RELEASE_DIFF': 202,
        'PNC_NOT_COMPATIBLE': 300,
        'PNC_COMPATIBILITY_UNKNOWN': 301,
        'PNC_COMPATIBILITY_CLIENT_VERSION_TOO_OLD': 302,

        'VOTING_MODELS': {},
        'MIN_BALANCE_MODELS': {},
        "HASH_ALGORITHMS": {},
        "PHASING_HASH_ALGORITHMS": {},
        "MINTING_HASH_ALGORITHMS": {},
        "REQUEST_TYPES": {},
        "API_TAGS": {},
        'LAST_KNOWN_BLOCK': {},
        "ASSET_EXCHANGE_REQUEST_TYPES": ["transferAsset", "deleteAssetShares", "increaseAssetShares", "placeAskOrder", "placeBidOrder"],

        'SERVER': {},
        'MAX_TAGGED_DATA_DATA_LENGTH': 0,
        'MAX_PRUNABLE_MESSAGE_LENGTH': 0,
        'GENESIS': '',
        'GENESIS_RS': '',
        'EPOCH_BEGINNING': 0,
        'FORGING': 'forging',
        'NOT_FORGING': 'not_forging',
        'UNKNOWN': 'unknown',
        'INITIAL_BASE_TARGET': 153722867
    };

    NRS.loadAlgorithmList = function (algorithmSelect, isPhasingHash) {
        var hashAlgorithms;
        if (isPhasingHash) {
            hashAlgorithms = NRS.constants.PHASING_HASH_ALGORITHMS;
        } else {
            hashAlgorithms = NRS.constants.HASH_ALGORITHMS;
        }
        for (var key in hashAlgorithms) {
            if (hashAlgorithms.hasOwnProperty(key)) {
                algorithmSelect.append($("<option />").val(hashAlgorithms[key]).text(key));
            }
        }
    };

    NRS.getRsAccountRegex = function(accountPrefix, withoutSeparator) {
        if (withoutSeparator) {
            return new RegExp("^(" + (accountPrefix + "|NXT)") + "-[A-Z0-9]{17}", "i");
        }
        return new RegExp(NRS.constants.ACCOUNT_REGEX_STR, "i");
    };

    NRS.getNumericAccountRegex = function() {
        return new RegExp("^\\d+$");
    };

    NRS.processConstants = function(response, resolve) {
        if (response.genesisBlockId) {
            NRS.constants.GENESIS_BLOCK_ID = response.genesisBlockId;
            NRS.constants.SERVER = response;
            NRS.constants.VOTING_MODELS = response.votingModels;
            NRS.constants.MIN_BALANCE_MODELS = response.minBalanceModels;
            NRS.constants.HASH_ALGORITHMS = response.hashAlgorithms;
            NRS.constants.PHASING_HASH_ALGORITHMS = response.phasingHashAlgorithms;
            NRS.constants.MINTING_HASH_ALGORITHMS = response.mintingHashAlgorithms;
            NRS.constants.MAX_TAGGED_DATA_DATA_LENGTH = response.maxTaggedDataDataLength;
            NRS.constants.MAX_PRUNABLE_MESSAGE_LENGTH = response.maxPrunableMessageLength;
            NRS.constants.EPOCH_BEGINNING = response.epochBeginning;
            NRS.constants.REQUEST_TYPES = response.requestTypes;
            NRS.constants.API_TAGS = response.apiTags;
            NRS.constants.SHUFFLING_STAGES = response.shufflingStages;
            NRS.constants.SHUFFLING_PARTICIPANTS_STATES = response.shufflingParticipantStates;
            NRS.constants.DISABLED_APIS = response.disabledAPIs;
            NRS.constants.DISABLED_API_TAGS = response.disabledAPITags;
            NRS.constants.PEER_STATES = response.peerStates;
            NRS.constants.LAST_KNOWN_BLOCK.id = response.lastKnownBlock.id;
            NRS.constants.LAST_KNOWN_BLOCK.height = response.lastKnownBlock.height;
            NRS.constants.PROXY_NOT_FORWARDED_REQUESTS = response.proxyNotForwardedRequests;
            NRS.constants.CHAINS = response.chains;
            NRS.constants.CHAIN_PROPERTIES = response.chainProperties;
            NRS.constants.CURRENCY_TYPES = response.currencyTypes;
            NRS.constants.PROXY_NOT_FORWARDED_REQUESTS = response.proxyNotForwardedRequests;
            NRS.loadTransactionTypeConstants(response);
            NRS.constants.ACCOUNT_PREFIX = response.accountPrefix;
            NRS.constants.ACCOUNT_REGEX_STR = "^(" + (response.accountPrefix + "|NXT)") + "-[A-Z0-9_]{4}-[A-Z0-9_]{4}-[A-Z0-9_]{4}-[A-Z0-9_]{5}";
            NRS.constants.ACCOUNT_RS_MATCH = NRS.getRsAccountRegex(response.accountPrefix);
            NRS.constants.ACCOUNT_NUMERIC_MATCH = NRS.getNumericAccountRegex();
            NRS.constants.ACCOUNT_MASK_ASTERIX = response.accountPrefix + "-****-****-****-*****";
            NRS.constants.ACCOUNT_MASK_UNDERSCORE = response.accountPrefix + "-____-____-____-_____";
            NRS.constants.ACCOUNT_MASK_PREFIX = response.accountPrefix + "-";
            NRS.constants.ACCOUNT_MASK_LEN = NRS.constants.ACCOUNT_MASK_PREFIX.length;
            NRS.constants.INITIAL_BASE_TARGET = parseInt(response.initialBaseTarget);
            console.log("done loading server constants");
            if (resolve) {
                resolve();
            }
        }
    };

    NRS.loadServerConstants = function(resolve, isUnitTest) {
        function processConstants(response) {
            NRS.processConstants(response, resolve);
        }
        if (NRS.isMobileApp() || isUnitTest) {
            jQuery.ajaxSetup({ async: false });
            if (NRS.mobileSettings && NRS.mobileSettings.is_testnet) {
                $.getScript("js/data/constants.testnet.js");
            } else {
                $.getScript("js/data/constants.mainnet.js");
            }
            jQuery.ajaxSetup({async: true});
            processConstants(NRS.constants.SERVER);
        } else {
            if (isNode) {
                client.sendRequest("getConstants", {}, processConstants, false);
            } else {
                NRS.sendRequest("getConstants", {}, processConstants, false);
            }
        }
    };

    function getKeyByValue(map, value) {
        for (var key in map) {
            if (map.hasOwnProperty(key)) {
                if (value === map[key]) {
                    return key;
                }
            }
        }
        return null;
    }

    NRS.getVotingModelName = function (code) {
        return getKeyByValue(NRS.constants.VOTING_MODELS, code);
    };

    NRS.getVotingModelCode = function (name) {
        return NRS.constants.VOTING_MODELS[name];
    };

    NRS.getMinBalanceModelName = function (code) {
        return getKeyByValue(NRS.constants.MIN_BALANCE_MODELS, code);
    };

    NRS.getMinBalanceModelCode = function (name) {
        return NRS.constants.MIN_BALANCE_MODELS[name];
    };

    NRS.getHashAlgorithm = function (code) {
        return getKeyByValue(NRS.constants.HASH_ALGORITHMS, code);
    };

    NRS.getShufflingStage = function (code) {
        return getKeyByValue(NRS.constants.SHUFFLING_STAGES, code);
    };

    NRS.getShufflingParticipantState = function (code) {
        return getKeyByValue(NRS.constants.SHUFFLING_PARTICIPANTS_STATES, code);
    };

    NRS.getPeerState = function (code) {
        return getKeyByValue(NRS.constants.PEER_STATES, code);
    };

    NRS.isRequireBlockchain = function(requestType) {
        if (!NRS.constants.REQUEST_TYPES[requestType]) {
            // For requests invoked before the getConstants request returns,
            // we implicitly assume that they do not require the blockchain
            return false;
        }
        return true == NRS.constants.REQUEST_TYPES[requestType].requireBlockchain;
    };

    NRS.isRequireFullClient = function(requestType) {
        if (!NRS.constants.REQUEST_TYPES[requestType]) {
            // For requests invoked before the getConstants request returns,
            // we implicitly assume that they do not require full client
            return false;
        }
        return true == NRS.constants.REQUEST_TYPES[requestType].requireFullClient;
    };

    NRS.isRequestForwardable = function(requestType) {
        return NRS.isRequireBlockchain(requestType) &&
            !NRS.isRequireFullClient(requestType) &&
            (!(NRS.constants.PROXY_NOT_FORWARDED_REQUESTS instanceof Array) ||
            NRS.constants.PROXY_NOT_FORWARDED_REQUESTS.indexOf(requestType) < 0);
    };

    NRS.isRequirePost = function(requestType) {
        if (!NRS.constants.REQUEST_TYPES[requestType]) {
            // For requests invoked before the getConstants request returns
            // we implicitly assume that they can use GET
            return false;
        }
        return true == NRS.constants.REQUEST_TYPES[requestType].requirePost;
    };

    NRS.isRequestTypeEnabled = function(requestType) {
        if ($.isEmptyObject(NRS.constants.REQUEST_TYPES)) {
            return true;
        }
        if (requestType.indexOf("+") > 0) {
            requestType = requestType.substring(0, requestType.indexOf("+"));
        }
        return !!NRS.constants.REQUEST_TYPES[requestType];
    };

    NRS.isSubmitPassphrase = function (requestType) {
        return requestType == "startForging" ||
            requestType == "stopForging" ||
            requestType == "startShuffler" ||
            requestType == "getForging" ||
            requestType == "markHost" ||
            requestType == "startFundingMonitor" ||
            requestType == "startBundler";
    };

    NRS.getFileUploadConfig = function (requestType, data) {
        var config = {};
        if (requestType == "uploadTaggedData") {
            config.selector = "#upload_file";
            config.requestParam = "file";
            config.errorDescription = "error_file_too_big";
            config.maxSize = NRS.constants.MAX_TAGGED_DATA_DATA_LENGTH;
            return config;
        } else if (requestType == "dgsListing") {
            config.selector = "#dgs_listing_image";
            config.requestParam = "messageFile";
            config.errorDescription = "error_image_too_big";
            config.maxSize = NRS.constants.MAX_PRUNABLE_MESSAGE_LENGTH;
            return config;
        } else if (requestType == "sendMessage") {
            config.selector = "#upload_file_message";
            if (data.encrypt_message) {
                config.requestParam = "encryptedMessageFile";
            } else {
                config.requestParam = "messageFile";
            }
            config.errorDescription = "error_message_too_big";
            config.maxSize = NRS.constants.MAX_PRUNABLE_MESSAGE_LENGTH;
            return config;
        }
        return null;
    };

    NRS.isApiEnabled = function(depends) {
        if (!depends) {
            return true;
        }
        var tags = depends.tags;
        if (tags) {
            for (var i=0; i < tags.length; i++) {
                if (tags[i] && (!tags[i].enabled || tags[i].disabledForChains
                    && tags[i].disabledForChains.indexOf(parseInt(NRS.getActiveChainId())) >= 0)) {
                    return false;
                }
            }
        }
        var apis = depends.apis;
        if (apis) {
            for (i=0; i < apis.length; i++) {
                if (apis[i] && (!apis[i].enabled || apis[i].disabledForChains
                    && apis[i].disabledForChains.indexOf(parseInt(NRS.getActiveChainId())) >= 0)) {
                    return false;
                }
            }
        }
        return true;
    };

    NRS.findChainByName = function(chainName) {
        for (var id in  NRS.constants.CHAIN_PROPERTIES) {
            if (NRS.constants.CHAIN_PROPERTIES.hasOwnProperty(id) &&
                NRS.constants.CHAIN_PROPERTIES[id].name == chainName) {
                return id;
            }
        }
        return false;
    };

    NRS.setActiveChain = function(chain) {
        NRS.mobileSettings.chain = chain;
        NRS.setJSONItem("mobile_settings", NRS.mobileSettings);
        $(".coin-symbol").html(NRS.getActiveChainName());
        $(".parent-coin-symbol").html(NRS.getParentChainName());
        $(".coin-symbol-separator").html(" " + $.t("per") + " ");
        $(".shuffling-node-running-warning").html($.t("shuffling_node_running_warning",
            { deposit: NRS.formatQuantity(NRS.getActiveChain().SHUFFLING_DEPOSIT_NQT, NRS.getActiveChainDecimals()), coin: NRS.getActiveChainName() }
        ));
    };

    NRS.getActiveChain = function() {
        return NRS.constants.CHAIN_PROPERTIES[NRS.mobileSettings.chain];
    };

    NRS.getActiveChainId = function() {
        return NRS.mobileSettings.chain;
    };

    NRS.isParentChain = function() {
        return NRS.getActiveChainId() == 1;
    };

    NRS.getActiveChainName = function() {
        return String(NRS.constants.CHAIN_PROPERTIES[NRS.getActiveChainId()].name).escapeHTML();
    };

    NRS.getParentChainName = function() {
        return String(NRS.constants.CHAIN_PROPERTIES[1].name).escapeHTML();
    };

    NRS.getActiveChainDecimals = function() {
        return parseInt(NRS.constants.CHAIN_PROPERTIES[NRS.getActiveChainId()].decimals);
    };
    NRS.getActiveChainOneCoin = function() {
        return NRS.constants.CHAIN_PROPERTIES[NRS.getActiveChainId()].ONE_COIN;
    };

    NRS.getChain = function(chain) {
        return NRS.constants.CHAIN_PROPERTIES[chain];
    };

    NRS.getChainName = function(chain) {
        return String(NRS.constants.CHAIN_PROPERTIES[chain].name);
    };

    NRS.getChainDecimals = function(chain) {
        return String(NRS.constants.CHAIN_PROPERTIES[chain].decimals);
    };

    NRS.getChainIdByName = function(name) {
        for (var chain in NRS.constants.CHAIN_PROPERTIES) {
            if (!NRS.constants.CHAIN_PROPERTIES.hasOwnProperty(chain)) {
                continue;
            }
            if (NRS.constants.CHAIN_PROPERTIES[chain].name == name) {
                return chain;
            }
        }
        return -1;
    };

    NRS.createChainSelect = function() {
        // Build chain select box for login page
        var chains = $('select[name="chain"]');
        chains.empty();
        $.each(NRS.constants.CHAIN_PROPERTIES, function(id, chain) {
            chains.append('<option value="' + id + '">' + chain.name + '</option>');
        });
        chains.val(NRS.getActiveChainId());
    };

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}