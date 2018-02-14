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
var NRS = (function (NRS, $, undefined) {
    var _password;

    NRS.setServerPassword = function (password) {
        _password = password;
    };

    NRS.sendRequest = function (requestType, data, callback, options) {
        if (!options) {
            options = {};
        }
        if (requestType == undefined) {
            NRS.logConsole("Undefined request type");
            return;
        }
        if (!NRS.isRequestTypeEnabled(requestType)) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("request_of_type", {
                    type: requestType
                })
            });
            return;
        }
        if (data == undefined) {
            NRS.logConsole("Undefined data for " + requestType);
            return;
        }
        if (callback == undefined) {
            NRS.logConsole("Undefined callback function for " + requestType);
            return;
        }

        $.each(data, function (key, val) {
            if (key != "secretPhrase") {
                if (typeof val == "string") {
                    data[key] = $.trim(val);
                }
            }
        });
        //convert coin to NQT...
        var field = "N/A";
        try {
            var nxtFields = [
                ["feeNXT", "feeNQT"],
                ["amountNXT", "amountNQT"],
                ["priceNXT", "priceNQT"],
                ["refundNXT", "refundNQT"],
                ["discountNXT", "discountNQT"],
                ["phasingQuorumNXT", "phasingQuorum"],
                ["phasingMinBalanceNXT", "phasingMinBalance"],
                ["controlQuorumNXT", "controlQuorum"],
                ["controlMinBalanceNXT", "controlMinBalance"],
                ["controlMaxFeesNXT", "controlMaxFees"],
                ["minBalanceNXT", "minBalance"],
                ["shufflingAmountNXT", "amount"],
                ["monitorAmountNXT", "amount"],
                ["monitorThresholdNXT", "threshold"],
                ["minRateNXTPerFXT", "minRateNQTPerFXT"]
            ];

            for (i = 0; i < nxtFields.length; i++) {
                var nxtField = nxtFields[i][0];
                var nqtField = nxtFields[i][1];
                if (nxtField in data) {
                    data[nqtField] = NRS.convertToNQT(data[nxtField]);
                    delete data[nxtField];
                }
            }
        } catch (err) {
            callback({
                "errorCode": 1,
                "errorDescription": "NXT decimal conversion error " + err + " (" + $.t(field) + ")"
            });
            return;
        }
        // convert asset/currency decimal amount to base unit
        try {
            var currencyFields = [
                ["phasingQuorumQNTf", "phasingHoldingDecimals"],
                ["phasingMinBalanceQNTf", "phasingHoldingDecimals"],
                ["controlQuorumQNTf", "controlHoldingDecimals"],
                ["controlMinBalanceQNTf", "controlHoldingDecimals"],
                ["minBalanceQNTf", "create_poll_asset_decimals"],
                ["minBalanceQNTf", "create_poll_ms_decimals"],
                ["amountQNTf", "shuffling_asset_decimals"],
                ["amountQNTf", "shuffling_ms_decimals"]
            ];
            var toDelete = [];
            for (i = 0; i < currencyFields.length; i++) {
                var decimalUnitField = currencyFields[i][0];
                var decimalsField = currencyFields[i][1];
                field = decimalUnitField.replace("QNTf", "");
                if (decimalUnitField in data && decimalsField in data) {
                    var unitField = data[decimalUnitField];
                    if (!unitField) {
                        continue;
                    }
                    data[field] = NRS.convertToQNT(parseFloat(unitField), parseInt(data[decimalsField]));
                    toDelete.push(decimalUnitField);
                    toDelete.push(decimalsField);
                }
            }
            for (var i = 0; i < toDelete.length; i++) {
                delete data[toDelete[i]];
            }
        } catch (err) {
            callback({
                "errorCode": 1,
                "errorDescription": "currency decimal conversion error " + err + " (" + $.t(field) + ")"
            });
            return;
        }
        var feeFields = ["controlMaxFees"];
        for (i = 0; i < feeFields.length; i++) {
            field = feeFields[i];
            if (!data[field]) {
                continue;
            }
            if (data[field] == "0") {
                delete data[field];
            } else {
                data[field] = NRS.getActiveChainId() + ":" + data[field];
            }
        }

        var hasAccountControl = requestType != "approveTransaction" && NRS.hasAccountControl();
        var hasAssetControl = NRS.isSubjectToAssetControl && NRS.isSubjectToAssetControl(requestType);
        if (hasAccountControl && hasAssetControl
                && (NRS.accountInfo.phasingOnly.controlParams.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE
                    || NRS.getCurrentAssetControl().phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE)) {
            //User should have set the phasing manually
        } else if (hasAccountControl || hasAssetControl) {
            //Fill phasing parameters when account control or asset control are enabled
            var phasingControl;
            if (hasAccountControl) {
                phasingControl = NRS.accountInfo.phasingOnly;
                var maxFees = new BigInteger(phasingControl.maxFees);
                if (maxFees > 0 && new BigInteger(data.feeNQT).compareTo(new BigInteger(phasingControl.maxFees)) > 0) {
                    callback({
                        "errorCode": 1,
                        "errorDescription": $.t("error_fee_exceeds_max_account_control_fee", {
                            "maxFee": NRS.convertToNXT(phasingControl.maxFees)
                        })
                    });
                    return;
                }
                var phasingDuration = parseInt(data.phasingFinishHeight) - NRS.lastBlockHeight;
                var minDuration = parseInt(phasingControl.minDuration) > 0 ? parseInt(phasingControl.minDuration) : 0;
                var maxDuration = parseInt(phasingControl.maxDuration) > 0 ? parseInt(phasingControl.maxDuration) : NRS.constants.SERVER.maxPhasingDuration;

                if (phasingDuration < minDuration || phasingDuration > maxDuration) {
                    callback({
                        "errorCode": 1,
                        "errorDescription": $.t("error_finish_height_out_of_account_control_interval", {
                            "min": NRS.lastBlockHeight + minDuration,
                            "max": NRS.lastBlockHeight + maxDuration
                        })
                    });
                    return;
                }
                if (hasAssetControl) {
                    //Build composite phasing that satisfies both controls
                    var compositePhasingParameters = {
                       "phasingHolding": "0",
                       "phasingQuorum": 1,
                       "phasingMinBalance": 0,
                       "phasingMinBalanceModel": 0,
                       "phasingExpression": "ACC&ASC",
                       "phasingSubPolls": {
                         "ACC": phasingControl.controlParams,
                         "ASC": NRS.getCurrentAssetControl()
                       },
                       "phasingVotingModel": 6
                    };
                    data.phasingParams = JSON.stringify(compositePhasingParameters);
                } else {
                    data.phasingParams = JSON.stringify(phasingControl.controlParams);
                }
            } else {
                data.phasingParams = JSON.stringify(NRS.getCurrentAssetControl());
            }

            data.phased = true;

            delete data.phasingHashedSecret;
            delete data.phasingHashedSecretAlgorithm;
            delete data.phasingLinkedFullHash;
            delete data.phasingLinkedTransaction;
        }

        if (!data.recipientPublicKey) {
            delete data.recipientPublicKey;
        }
        if (!data.referencedTransactionFullHash) {
            delete data.referencedTransactionFullHash;
        }

        // get account id from passphrase then call getAccount so that the passphrase is not submitted to the server
        var accountId;
        if (requestType == "getAccountId") {
            accountId = NRS.getAccountId(data.secretPhrase);
            NRS.sendRequest("getAccount", { account: accountId }, function(response) {
                callback(response);
            });
            return;
        }

        if (!data.chain && !data.nochain) {
            data.chain = NRS.getActiveChainId();
        } else {
            delete data.nochain;
        }

        //check to see if secretPhrase supplied matches logged in account, if not - show error.
        if ("secretPhrase" in data) {
            accountId = NRS.getAccountId(NRS.rememberPassword ? _password : data.secretPhrase);
            if (accountId != NRS.account) {
                callback({
                    "errorCode": 1,
                    "errorDescription": $.t("error_passphrase_incorrect")
                });
            } else {
                //ok, accountId matches..continue with the real request.
                NRS.processAjaxRequest(requestType, data, callback, options);
            }
        } else {
            NRS.processAjaxRequest(requestType, data, callback, options);
        }
    };

    function isVolatileRequest(doNotSign, type, requestType, secretPhrase) {
        if (secretPhrase && NRS.isMobileApp()) {
            return true;
        }
        return (NRS.isPassphraseAtRisk() || doNotSign) && type == "POST" && !NRS.isSubmitPassphrase(requestType);
    }

    NRS.requestId = 0;

    NRS.processAjaxRequest = function (requestType, data, callback, options) {
        var extra = null;
        if (data["_extra"]) {
            extra = data["_extra"];
            delete data["_extra"];
        }
        var currentPage = null;
        var currentSubPage = null;

        //means it is a page request, not a global request.. Page requests can be aborted.
        if (requestType.slice(-1) == "+") {
            requestType = requestType.slice(0, -1);
            currentPage = NRS.currentPage;
        } else {
            //not really necessary... we can just use the above code..
            var plusCharacter = requestType.indexOf("+");

            if (plusCharacter > 0) {
                requestType = requestType.substr(0, plusCharacter);
                currentPage = NRS.currentPage;
            }
        }

        if (currentPage && NRS.currentSubPage) {
            currentSubPage = NRS.currentSubPage;
        }

        var httpMethod = (NRS.isRequirePost(requestType) || "secretPhrase" in data || "doNotSign" in data || "adminPassword" in data ? "POST" : "GET");
        if (httpMethod == "GET") {
            if (typeof data == "string") {
                data += "&random=" + Math.random();
            } else {
                data.random = Math.random();
            }
        }

        if ((NRS.isRequirePost(requestType) || "secretPhrase" in data) &&
            NRS.isRequireBlockchain(requestType) && NRS.accountInfo.errorCode && NRS.accountInfo.errorCode == 5) {
            callback({
                "errorCode": 2,
                "errorDescription": $.t("error_new_account")
            }, data);
            return;
        }

        if (data.referencedTransactionFullHash) {
            if (!/^[a-z0-9]{64}$/.test(data.referencedTransactionFullHash)) {
                callback({
                    "errorCode": -1,
                    "errorDescription": $.t("error_invalid_referenced_transaction_hash")
                }, data);
                return;
            }
        }

        var secretPhrase = "";
        var isVolatile = isVolatileRequest(data.doNotSign, httpMethod, requestType, data.secretPhrase);
        if (isVolatile) {
            if (NRS.rememberPassword) {
                secretPhrase = _password;
            } else {
                secretPhrase = data.secretPhrase;
            }

            delete data.secretPhrase;

            if (NRS.accountInfo && NRS.accountInfo.publicKey) {
                data.publicKey = NRS.accountInfo.publicKey;
            } else if (!data.doNotSign && secretPhrase) {
                data.publicKey = NRS.generatePublicKey(secretPhrase);
                NRS.accountInfo.publicKey = data.publicKey;
            }
            var ecBlock = NRS.constants.LAST_KNOWN_BLOCK;
            data.ecBlockId = ecBlock.id;
            data.ecBlockHeight = ecBlock.height;
        } else if (httpMethod == "POST" && NRS.rememberPassword) {
            data.secretPhrase = _password;
        }

        $.support.cors = true;
        // Used for passing row query string which is too long for a GET request
        if (data.querystring) {
            data = data.querystring;
            httpMethod = "POST";
        }
        var contentType;
        var processData;
        var formData = null;

        var config = NRS.getFileUploadConfig(requestType, data);
        if (config && $(config.selector)[0] && $(config.selector)[0].files[0]) {
            // inspired by http://stackoverflow.com/questions/5392344/sending-multipart-formdata-with-jquery-ajax
            contentType = false;
            processData = false;
            formData = new FormData();
            var file;
            if (data.messageFile) {
                file = data.messageFile;
                delete data.messageFile;
                delete data.encrypt_message;
            } else {
                file = $(config.selector)[0].files[0];
            }
            if (!file && requestType == "uploadTaggedData") {
                callback({
                    "errorCode": 3,
                    "errorDescription": $.t("error_no_file_chosen")
                }, data);
                return;
            }
            if (file && file.size > config.maxSize) {
                callback({
                    "errorCode": 3,
                    "errorDescription": $.t(config.errorDescription, {
                        "size": file.size,
                        "allowed": config.maxSize
                    })
                }, data);
                return;
            }
            httpMethod = "POST";
            formData.append(config.requestParam, file);
            for (var key in data) {
                if (!data.hasOwnProperty(key)) {
                    continue;
                }
                if (data[key] instanceof Array) {
                    for (var i = 0; i < data[key].length; i++) {
                        formData.append(key, data[key][i]);
                    }
                } else {
                    formData.append(key, data[key]);
                }
            }
        } else {
            // JQuery defaults
            contentType = "application/x-www-form-urlencoded; charset=UTF-8";
            processData = true;
        }
        var url;
        if (options.remoteNode) {
            url = options.remoteNode.getUrl() + "/nxt";
        } else {
            url = NRS.getRequestPath(options.noProxy);
        }
        url += "?requestType=" + requestType;

        var currentRequestId = NRS.requestId++;
        NRS.logConsole("Send request " + requestType + " to url " + url + " id=" + currentRequestId);

        $.ajax({
            url: url,
            crossDomain: true,
            dataType: "json",
            type: httpMethod,
            timeout: (options.timeout === undefined ? 30000 : options.timeout),
            async: (options.isAsync === undefined ? true : options.isAsync),
            currentPage: currentPage,
            currentSubPage: currentSubPage,
            shouldRetry: (httpMethod == "GET" ? 2 : undefined),
            traditional: true,
            data: (formData != null ? formData : data),
            contentType: contentType,
            processData: processData
        }).done(function (response) {
            if (typeof data == "string") {
                data = { "querystring": data };
                if (extra) {
                    data["_extra"] = extra;
                }
            }
            if (!options.remoteNode && NRS.isConfirmResponse() &&
                !(response.errorCode || response.errorDescription || response.errorMessage || response.error)) {
                var requestRemoteNode = NRS.isMobileApp() ? NRS.getRemoteNode() : {address: "localhost", announcedAddress: "localhost"}; //TODO unify getRemoteNode with apiProxyPeer
                NRS.confirmResponse(requestType, data, response, requestRemoteNode);
            }
            if (!options.doNotEscape) {
                NRS.escapeResponseObjStrings(response, ["transactionJSON"]);
            }
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, response);
            }
            addAddressData(data);
            if (secretPhrase &&
                response.unsignedTransactionBytes && !data.doNotSign &&
                !response.errorCode && !response.error &&
                !response.bundlerRateNQTPerFXT && !data.calculateFee) {
                var publicKey = NRS.generatePublicKey(secretPhrase);
                var signature = NRS.signBytes(response.unsignedTransactionBytes, converters.stringToHexString(secretPhrase));

                if (!NRS.verifySignature(signature, response.unsignedTransactionBytes, publicKey, callback)) {
                    return;
                }
                addMissingData(data);
                if (file && NRS.isFileReaderSupported()) {
                    var r = new FileReader();
                    r.onload = function (e) {
                        data.filebytes = e.target.result;
                        data.filename = file.name;
                        NRS.verifyAndBroadcast(signature, requestType, data, callback, response, extra, isVolatile);
                    };
                    r.readAsArrayBuffer(file);
                } else {
                    NRS.verifyAndBroadcast(signature, requestType, data, callback, response, extra, isVolatile);
                }
            } else {
                if (response.errorCode || response.errorDescription || response.errorMessage || response.error) {
                    response.errorDescription = NRS.translateServerError(response);
                    delete response.fullHash;
                    if (!response.errorCode) {
                        response.errorCode = -1;
                    }
                    callback(response, data);
                } else {
                    if (response.broadcasted == false && !data.calculateFee) {
                        async.waterfall([
                            function (callback) {
                                addMissingData(data);
                                if (!response.unsignedTransactionBytes) {
                                    callback(null);
                                }
                                if (file && NRS.isFileReaderSupported()) {
                                    var r = new FileReader();
                                    r.onload = function (e) {
                                        data.filebytes = e.target.result;
                                        data.filename = file.name;
                                        callback(null);
                                    };
                                    r.readAsArrayBuffer(file);
                                } else {
                                    callback(null);
                                }
                            },
                            function (callback) {
                                if (response.unsignedTransactionBytes &&
                                    !NRS.verifyTransactionBytes(converters.hexStringToByteArray(response.unsignedTransactionBytes), requestType, data, response.transactionJSON.attachment, isVolatile)) {
                                    callback({
                                        "errorCode": 1,
                                        "errorDescription": $.t("error_bytes_validation_server")
                                    }, data);
                                    return;
                                }
                                callback(null);
                            }
                        ], function () {
                            NRS.logConsole("before showRawTransactionModal response.broadcasted == false && !data.calculateFee");
                            NRS.showRawTransactionModal(response);
                        });
                    } else {
                        if (extra) {
                            data["_extra"] = extra;
                        }
                        callback(response, data);
                        if (data.referencedTransactionFullHash && !response.errorCode) {
                            $.growl($.t("info_referenced_transaction_hash"), {
                                "type": "info"
                            });
                        }
                    }
                }
            }
        }).fail(function (xhr, textStatus, error) {
            NRS.logConsole("Node " + (options.remoteNode ? options.remoteNode.getUrl() : NRS.getRemoteNodeUrl()) + " received an error for request type " + requestType +
                " status " + textStatus + " error " + error + " id=" + currentRequestId);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, error, true);
            }

            if ((error == "error" || textStatus == "error") && (xhr.status == 404 || xhr.status == 0)) {
                if (httpMethod == "POST") {
                    NRS.connectionError();
                }
            }

            if (error != "abort") {
                if (options.remoteNode) {
                    options.remoteNode.blacklist();
                } else {
                    NRS.resetRemoteNode(true);
                }
                if (error == "timeout") {
                    error = $.t("error_request_timeout");
                }
                callback({
                    "errorCode": -1,
                    "errorDescription": error
                }, {});
            }
        });
    };

    NRS.verifyAndBroadcast = function (signature, requestType, data, callback, response, extra, isVerifyECBlock) {
        var transactionBytes = response.unsignedTransactionBytes;
        if (!NRS.verifyTransactionBytes(converters.hexStringToByteArray(transactionBytes), requestType, data, response.transactionJSON.attachment, isVerifyECBlock)) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("error_bytes_validation_server")
            }, data);
            return;
        }
        var sigPos = 2 * 69; // 2 * (bytes before signature from TransactionImpl newTransactionBuilder())
        var sigLen = 2 * 64;
        var payload = transactionBytes.substr(0, sigPos) + signature + transactionBytes.substr(sigPos + sigLen);
        if (data.broadcast == "false") {
            response.transactionBytes = payload;
            response.transactionJSON.signature = signature;
            NRS.logConsole("before showRawTransactionModal data.broadcast == false");
            NRS.showRawTransactionModal(response);
        } else {
            if (extra) {
                data["_extra"] = extra;
            }
            NRS.broadcastTransactionBytes(payload, callback, response, data, requestType);
        }
    };

    NRS.verifyTransactionBytes = function (byteArray, requestType, data, attachment, isVerifyECBlock) {
        try {
            var transaction = {};
            var pos = 0;
            transaction.chain = String(converters.byteArrayToSignedInt32(byteArray, pos));
            pos += 4;
            transaction.type = byteArray[pos++];
            // Patch until I find the official way of converting JS byte to signed byte
            if (transaction.type >= 128) {
                transaction.type -= 256;
            }
            transaction.subtype = byteArray[pos++];
            transaction.version = byteArray[pos++];
            transaction.timestamp = String(converters.byteArrayToSignedInt32(byteArray, pos));
            pos += 4;
            transaction.deadline = String(converters.byteArrayToSignedShort(byteArray, pos));
            pos += 2;
            transaction.publicKey = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            transaction.recipient = String(converters.byteArrayToBigInteger(byteArray, pos));
            pos += 8;
            transaction.amountNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
            pos += 8;
            transaction.feeNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
            pos += 8;
            transaction.signature = byteArray.slice(pos, pos + 64);
            pos += 64;
            transaction.ecBlockHeight = String(converters.byteArrayToSignedInt32(byteArray, pos));
            pos += 4;
            transaction.ecBlockId = String(converters.byteArrayToBigInteger(byteArray, pos));
            pos += 8;
            transaction.flags = String(converters.byteArrayToSignedInt32(byteArray, pos));
            pos += 4;
            if (isVerifyECBlock) {
                var ecBlock = NRS.constants.LAST_KNOWN_BLOCK;
                if (transaction.ecBlockHeight != ecBlock.height) {
                    return false;
                }
                if (transaction.ecBlockId != ecBlock.id) {
                    return false;
                }
            }

            if (transaction.publicKey != NRS.accountInfo.publicKey && transaction.publicKey != data.publicKey) {
                return false;
            }

            if (transaction.deadline !== data.deadline) {
                return false;
            }

            if (transaction.recipient !== data.recipient) {
                if (!((data.recipient === undefined || data.recipient == "") && transaction.recipient == "0")) {
                    return false;
                }
            }

            if (transaction.amountNQT !== data.amountNQT && !(requestType === "exchangeCoins" && transaction.amountNQT === "0")) {
                return false;
            }

            if ("referencedTransactionFullHash" in data) {
                if (transaction.referencedTransactionFullHash !== data.referencedTransactionFullHash) {
                    return false;
                }
            } else if (transaction.referencedTransactionFullHash && transaction.referencedTransactionFullHash !== "") {
                return false;
            }
            //has empty attachment, so no attachmentVersion byte...
            if (!(requestType == "sendMoney" || requestType == "sendMessage")) {
                pos++;
            }
            return NRS.verifyTransactionTypes(byteArray, transaction, requestType, data, pos, attachment);
        } catch (e) {
            NRS.logConsole("Exception in verifyTransactionBytes " + e.message);
            return false;
        }
    };

    NRS.verifyTransactionTypes = function (byteArray, transaction, requestType, data, pos, attachment) {
        var length = 0;
        var i = 0;
        var serverHash, sha256, utfBytes, isText, hashWords, calculatedHash;
        switch (requestType) {
            case "sendMoney":
                if (NRS.notOfType(transaction, "FxtPayment") && NRS.notOfType(transaction, "OrdinaryPayment")) {
                    return false;
                }
                break;
            case "sendMessage":
                if (NRS.notOfType(transaction, "ArbitraryMessage")) {
                    return false;
                }
                break;
            case "setAlias":
                if (NRS.notOfType(transaction, "AliasAssignment")) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.aliasName = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.aliasURI = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.aliasName !== data.aliasName || transaction.aliasURI !== data.aliasURI) {
                    return false;
                }
                break;
            case "createPoll":
                if (NRS.notOfType(transaction, "PollCreation")) {
                    return false;
                }
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.finishHeight = converters.byteArrayToSignedInt32(byteArray, pos);
                pos += 4;
                var nr_options = byteArray[pos];
                pos++;

                for (i = 0; i < nr_options; i++) {
                    var optionLength = converters.byteArrayToSignedShort(byteArray, pos);
                    pos += 2;
                    transaction["option" + (i < 10 ? "0" + i : i)] = converters.byteArrayToString(byteArray, pos, optionLength);
                    pos += optionLength;
                }
                transaction.votingModel = String(byteArray[pos]);
                pos++;
                transaction.minNumberOfOptions = String(byteArray[pos]);
                pos++;
                transaction.maxNumberOfOptions = String(byteArray[pos]);
                pos++;
                transaction.minRangeValue = String(byteArray[pos]);
                pos++;
                transaction.maxRangeValue = String(byteArray[pos]);
                pos++;
                transaction.minBalance = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.minBalanceModel = String(byteArray[pos]);
                pos++;
                transaction.holding = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;

                if (transaction.name !== data.name || transaction.description !== data.description ||
                    transaction.minNumberOfOptions !== data.minNumberOfOptions || transaction.maxNumberOfOptions !== data.maxNumberOfOptions) {
                    return false;
                }

                for (i = 0; i < nr_options; i++) {
                    if (transaction["option" + (i < 10 ? "0" + i : i)] !== data["option" + (i < 10 ? "0" + i : i)]) {
                        return false;
                    }
                }

                if (("option" + (i < 10 ? "0" + i : i)) in data) {
                    return false;
                }
                break;
            case "castVote":
                if (NRS.notOfType(transaction, "VoteCasting")) {
                    return false;
                }
                transaction.poll = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                var voteLength = byteArray[pos];
                pos++;
                transaction.votes = [];

                for (i = 0; i < voteLength; i++) {
                    transaction["vote" + (i < 10 ? "0" + i : i)] = byteArray[pos];
                    pos++;
                }
                if (transaction.poll !== data.poll) {
                    return false;
                }
                break;
            case "setAccountInfo":
                if (NRS.notOfType(transaction, "AccountInfo")) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.name !== data.name || transaction.description !== data.description) {
                    return false;
                }
                break;
            case "sellAlias":
                if (NRS.notOfType(transaction, "AliasSell")) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.alias !== data.aliasName || transaction.priceNQT !== data.priceNQT) {
                    return false;
                }
                break;
            case "buyAlias":
                if (NRS.notOfType(transaction, "AliasBuy")) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.alias !== data.aliasName) {
                    return false;
                }
                break;
            case "deleteAlias":
                if (NRS.notOfType(transaction, "AliasDelete")) {
                    return false;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.alias !== data.aliasName) {
                    return false;
                }
                break;
            case "approveTransaction":
                if (NRS.notOfType(transaction, "PhasingVoteCasting")) {
                    return false;
                }
                var fullHashesLength = byteArray[pos];
                if (fullHashesLength !== 1) {
                    return false;
                }
                pos++;
                var phasedTransaction = converters.byteArrayToSignedInt32(byteArray, pos);
                pos += 4;
                phasedTransaction += ":" + converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                if (phasedTransaction !== data.phasedTransaction) {
                    return false;
                }
                var numberOfSecrets = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                if (numberOfSecrets < 0 || numberOfSecrets > 1
                    || numberOfSecrets == 0 && data.revealedSecretText !== ""
                    || numberOfSecrets == 1 && data.revealedSecretText === "") {
                    return false;
                }
                // We only support one secret per phasing model
                transaction.revealedSecretLength = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                if (transaction.revealedSecretLength > 0) {
                    transaction.revealedSecret = converters.byteArrayToHexString(byteArray.slice(pos, pos + transaction.revealedSecretLength));
                    pos += transaction.revealedSecretLength;
                }
                if (transaction.revealedSecret !== data.revealedSecret &&
                    transaction.revealedSecret !== converters.byteArrayToHexString(NRS.getUtf8Bytes(data.revealedSecretText))) {
                    return false;
                }
                break;
            case "setAccountProperty":
                if (NRS.notOfType(transaction, "AccountProperty")) {
                    return false;
                }
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.property) {
                    return false;
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.value) {
                    return false;
                }
                pos += length;
                break;
            case "deleteAccountProperty":
                if (NRS.notOfType(transaction, "AccountPropertyDelete")) {
                    return false;
                }
                // no way to validate the property id, just skip it
                String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                break;
            case "issueAsset":
                if (NRS.notOfType(transaction, "AssetIssuance")) {
                    return false;
                }
                length = byteArray[pos];
                pos++;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.decimals = String(byteArray[pos]);
                pos++;
                if (transaction.name !== data.name || transaction.description !== data.description || transaction.quantityQNT !== data.quantityQNT || transaction.decimals !== data.decimals) {
                    return false;
                }
                break;
            case "transferAsset":
                if (NRS.notOfType(transaction, "AssetTransfer")) {
                    return false;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return false;
                }
                break;
            case "placeAskOrder":
            case "placeBidOrder":
                if (NRS.notOfType(transaction, "AskOrderPlacement") && NRS.notOfType(transaction, "BidOrderPlacement")) {
                    return false;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.priceNQTPerShare = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT || transaction.priceNQTPerShare !== data.priceNQTPerShare) {
                    return false;
                }
                break;
            case "cancelAskOrder":
            case "cancelBidOrder":
                if (NRS.notOfType(transaction, "AskOrderCancellation") && NRS.notOfType(transaction, "BidOrderCancellation")) {
                    return false;
                }
                transaction.order = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.order !== data.order) {
                    return false;
                }
                break;
            case "deleteAssetShares":
                if (NRS.notOfType(transaction, "AssetDelete")) {
                    return false;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return false;
                }
                break;
            case "increaseAssetShares":
                if (NRS.notOfType(transaction, "AssetIncrease")) {
                    return false;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return false;
                }
                break;
            case "dividendPayment":
                if (NRS.notOfType(transaction, "DividendPayment")) {
                    return false;
                }
                transaction.holding = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.holdingType = String(byteArray[pos]);
                pos ++;
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.height = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                transaction.amountNQTPerShare = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.holding !== data.holding ||
                    transaction.holdingType !== data.holdingType ||
                    transaction.asset !== data.asset ||
                    transaction.height !== data.height ||
                    transaction.amountNQTPerShare !== data.amountNQTPerShare) {
                    return false;
                }
                break;
            case "setPhasingAssetControl":
                if (NRS.notOfType(transaction, "SetPhasingAssetControl")) {
                    return false;
                }
                if (data.asset !== String(converters.byteArrayToBigInteger(byteArray, pos))) {
                    return false;
                }
                pos += 8;
                pos = validateControlPhasingData(data, byteArray, pos, false);
                if (pos == -1) {
                    return false;
                }
                break;
            case "dgsListing":
                if (NRS.notOfType(transaction, "DigitalGoodsListing")) {
                    return false;
                }
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.tags = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.quantity = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.name !== data.name || transaction.description !== data.description || transaction.tags !== data.tags || transaction.quantity !== data.quantity || transaction.priceNQT !== data.priceNQT) {
                    return false;
                }
                break;
            case "dgsDelisting":
                if (NRS.notOfType(transaction, "DigitalGoodsDelisting")) {
                    return false;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.goods !== data.goods) {
                    return false;
                }
                break;
            case "dgsPriceChange":
                if (NRS.notOfType(transaction, "DigitalGoodsPriceChange")) {
                    return false;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.goods !== data.goods || transaction.priceNQT !== data.priceNQT) {
                    return false;
                }
                break;
            case "dgsQuantityChange":
                if (NRS.notOfType(transaction, "DigitalGoodsQuantityChange")) {
                    return false;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.deltaQuantity = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                if (transaction.goods !== data.goods || transaction.deltaQuantity !== data.deltaQuantity) {
                    return false;
                }
                break;
            case "dgsPurchase":
                if (NRS.notOfType(transaction, "DigitalGoodsPurchase")) {
                    return false;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantity = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.deliveryDeadlineTimestamp = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                if (transaction.goods !== data.goods || transaction.quantity !== data.quantity || transaction.priceNQT !== data.priceNQT || transaction.deliveryDeadlineTimestamp !== data.deliveryDeadlineTimestamp) {
                    return false;
                }
                break;
            case "dgsDelivery":
                if (NRS.notOfType(transaction, "DigitalGoodsDelivery")) {
                    return false;
                }
                transaction.purchase = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                var encryptedGoodsLength = converters.byteArrayToSignedShort(byteArray, pos);
                var goodsLength = converters.byteArrayToSignedInt32(byteArray, pos);
                transaction.goodsIsText = goodsLength < 0; // ugly hack??
                if (goodsLength < 0) {
                    goodsLength &= NRS.constants.MAX_INT_JAVA;
                }
                pos += 4;
                transaction.goodsData = converters.byteArrayToHexString(byteArray.slice(pos, pos + encryptedGoodsLength));
                pos += encryptedGoodsLength;
                transaction.goodsNonce = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                transaction.discountNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                var goodsIsText = (transaction.goodsIsText ? "true" : "false");
                if (goodsIsText != data.goodsIsText) {
                    return false;
                }
                if (transaction.purchase !== data.purchase || transaction.goodsData !== data.goodsData || transaction.goodsNonce !== data.goodsNonce || transaction.discountNQT !== data.discountNQT) {
                    return false;
                }
                break;
            case "dgsFeedback":
                if (NRS.notOfType(transaction, "DigitalGoodsFeedback")) {
                    return false;
                }
                transaction.purchase = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.purchase !== data.purchase) {
                    return false;
                }
                break;
            case "dgsRefund":
                if (NRS.notOfType(transaction, "DigitalGoodsRefund")) {
                    return false;
                }
                transaction.purchase = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.refundNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.purchase !== data.purchase || transaction.refundNQT !== data.refundNQT) {
                    return false;
                }
                break;
            case "leaseBalance":
                if (NRS.notOfType(transaction, "EffectiveBalanceLeasing")) {
                    return false;
                }
                transaction.period = String(converters.byteArrayToSignedShort(byteArray, pos));
                pos += 2;
                if (transaction.period !== data.period) {
                    return false;
                }
                break;
            case "setPhasingOnlyControl":
                if (NRS.notOfType(transaction, "SetPhasingOnly")) {
                    return false;
                }
                pos = validateControlPhasingData(data, byteArray, pos, true);
                if (pos == -1) {
                    return false;
                }
                break;
            case "issueCurrency":
                if (NRS.notOfType(transaction, "CurrencyIssuance")) {
                    return false;
                }
                length = byteArray[pos];
                pos++;
                transaction.name = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                var codeLength = byteArray[pos];
                pos++;
                transaction.code = converters.byteArrayToString(byteArray, pos, codeLength);
                pos += codeLength;
                length = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                transaction.description = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.type = String(byteArray[pos]);
                pos++;
                transaction.initialSupplyQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.reserveSupplyQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.maxSupplyQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.issuanceHeight = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                transaction.minReservePerUnitNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.minDifficulty = String(byteArray[pos]);
                pos++;
                transaction.maxDifficulty = String(byteArray[pos]);
                pos++;
                transaction.ruleset = String(byteArray[pos]);
                pos++;
                transaction.algorithm = String(byteArray[pos]);
                pos++;
                transaction.decimals = String(byteArray[pos]);
                pos++;
                if (transaction.name !== data.name || transaction.code !== data.code || transaction.description !== data.description ||
                    transaction.type != data.type || transaction.initialSupplyQNT !== data.initialSupplyQNT || transaction.reserveSupplyQNT !== data.reserveSupplyQNT ||
                    transaction.maxSupplyQNT !== data.maxSupplyQNT || transaction.issuanceHeight !== data.issuanceHeight ||
                    transaction.ruleset !== data.ruleset || transaction.algorithm !== data.algorithm || transaction.decimals !== data.decimals) {
                    return false;
                }
                if (transaction.minReservePerUnitNQT !== "0" && transaction.minReservePerUnitNQT !== data.minReservePerUnitNQT) {
                    return false;
                }
                if (transaction.minDifficulty !== "0" && transaction.minDifficulty !== data.minDifficulty) {
                    return false;
                }
                if (transaction.maxDifficulty !== "0" && transaction.maxDifficulty !== data.maxDifficulty) {
                    return false;
                }
                break;
            case "currencyReserveIncrease":
                if (NRS.notOfType(transaction, "ReserveIncrease")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.amountPerUnitNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.amountPerUnitNQT !== data.amountPerUnitNQT) {
                    return false;
                }
                break;
            case "currencyReserveClaim":
                if (NRS.notOfType(transaction, "ReserveClaim")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.unitsQNT !== data.unitsQNT) {
                    return false;
                }
                break;
            case "transferCurrency":
                if (NRS.notOfType(transaction, "CurrencyTransfer")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.unitsQNT !== data.unitsQNT) {
                    return false;
                }
                break;
            case "publishExchangeOffer":
                if (NRS.notOfType(transaction, "PublishExchangeOffer")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.buyRateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.sellRateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.totalBuyLimitQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.totalSellLimitQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.initialBuySupplyQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.initialSellSupplyQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.expirationHeight = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                if (transaction.currency !== data.currency || transaction.buyRateNQTPerUnit !== data.buyRateNQTPerUnit || transaction.sellRateNQTPerUnit !== data.sellRateNQTPerUnit ||
                    transaction.totalBuyLimitQNT !== data.totalBuyLimitQNT || transaction.totalSellLimitQNT !== data.totalSellLimitQNT ||
                    transaction.initialBuySupplyQNT !== data.initialBuySupplyQNT || transaction.initialSellSupplyQNT !== data.initialSellSupplyQNT || transaction.expirationHeight !== data.expirationHeight) {
                    return false;
                }
                break;
            case "currencyBuy":
                if (NRS.notOfType(transaction, "ExchangeBuy")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.rateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.rateNQTPerUnit !== data.rateNQTPerUnit || transaction.unitsQNT !== data.unitsQNT) {
                    return false;
                }
                break;
            case "currencySell":
                if (NRS.notOfType(transaction, "ExchangeSell")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.rateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.rateNQTPerUnit !== data.rateNQTPerUnit || transaction.unitsQNT !== data.unitsQNT) {
                    return false;
                }
                break;
            case "currencyMint":
                if (NRS.notOfType(transaction, "CurrencyMinting")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.nonce = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.counter = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.nonce !== data.nonce || transaction.unitsQNT !== data.unitsQNT ||
                    transaction.counter !== data.counter) {
                    return false;
                }
                break;
            case "deleteCurrency":
                if (NRS.notOfType(transaction, "CurrencyDeletion")) {
                    return false;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency) {
                    return false;
                }
                break;
            case "uploadTaggedData":
                if (NRS.notOfType(transaction, "TaggedDataUpload")) {
                    return false;
                }
                if (byteArray[pos] != 0) {
                    return false;
                }
                pos++;
                serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                pos += 32;
                sha256 = CryptoJS.algo.SHA256.create();
                utfBytes = NRS.getUtf8Bytes(data.name);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.description);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.tags);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(attachment.type);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                utfBytes = NRS.getUtf8Bytes(data.channel);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                isText = [];
                if (attachment.isText) {
                    isText.push(1);
                } else {
                    isText.push(0);
                }
                sha256.update(converters.byteArrayToWordArrayEx(isText));
                utfBytes = NRS.getUtf8Bytes(data.filename);
                sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
                var dataBytes = new Int8Array(data.filebytes);
                sha256.update(converters.byteArrayToWordArrayEx(dataBytes));
                hashWords = sha256.finalize();
                calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
                if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                    return false;
                }
                break;
            case "shufflingCreate":
                if (NRS.notOfType(transaction, "ShufflingCreation")) {
                    return false;
                }
                var holding = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (holding !== "0" && holding !== data.holding ||
                    holding === "0" && data.holding !== undefined && data.holding !== "" && data.holding !== "0") {
                    return false;
                }
                pos += 8;
                var holdingType = String(byteArray[pos]);
                if (holdingType !== "0" && holdingType !== data.holdingType ||
                    holdingType === "0" && data.holdingType !== undefined && data.holdingType !== "" && data.holdingType !== "0") {
                    return false;
                }
                pos++;
                var amount = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (amount !== data.amount) {
                    return false;
                }
                pos += 8;
                var participantCount = String(byteArray[pos]);
                if (participantCount !== data.participantCount) {
                    return false;
                }
                pos++;
                var registrationPeriod = converters.byteArrayToSignedShort(byteArray, pos);
                if (registrationPeriod !== data.registrationPeriod) {
                    return false;
                }
                pos += 2;
                break;
            case "exchangeCoins":
                var chain = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (chain !== data.chain) {
                    return false;
                }
                pos += 4;
                var exchange = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (exchange !== data.exchange) {
                    return false;
                }
                pos += 4;
                var quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (quantityQNT !== data.quantityQNT) {
                    return false;
                }
                pos += 8;
                var priceNQTPerCoin = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (priceNQTPerCoin !== data.priceNQTPerCoin) {
                    return false;
                }
                pos += 8;
                break;
            case "cancelCoinExchange":
                var orderHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                if (NRS.fullHashToId(orderHash) !== data.order) {
                    return false;
                }
                pos += 32;
                break;
            case "bundleTransactions":
                var isPrunable = byteArray[pos];
                if (isPrunable != 0) {
                    return false;
                }
                pos++;
                chain = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (chain != data.childChain) {
                    return false;
                }
                pos += 4;
                serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                sha256 = CryptoJS.algo.SHA256.create();
                // We assume here that only one transaction was bundled
                sha256.update(converters.byteArrayToWordArrayEx(converters.hexStringToByteArray(data.transactionFullHash)));
                hashWords = sha256.finalize();
                calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
                if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                    return false;
                }
                pos += 32;
                break;
            default:
                //invalid requestType..
                return false;
        }

        return NRS.verifyAppendix(byteArray, transaction, requestType, data, pos);
    };

    NRS.verifyAppendix = function (byteArray, transaction, requestType, data, pos) {
        var attachmentVersion;
        var flags;

        // MessageAppendix
        if ((transaction.flags & 1) != 0 ||
            ((requestType == "sendMessage" && data.message && !(data.messageIsPrunable === "true")))) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageIsText = flags && 1;
            var messageIsText = (transaction.messageIsText ? "true" : "false");
            if (messageIsText != data.messageIsText) {
                return false;
            }
            var messageLength = converters.byteArrayToSignedShort(byteArray, pos);
            if (messageLength < 0) {
                messageLength &= NRS.constants.MAX_SHORT_JAVA;
            }
            pos += 2;
            if (transaction.messageIsText) {
                transaction.message = converters.byteArrayToString(byteArray, pos, messageLength);
            } else {
                var slice = byteArray.slice(pos, pos + messageLength);
                transaction.message = converters.byteArrayToHexString(slice);
            }
            pos += messageLength;
            if (transaction.message !== data.message) {
                return false;
            }
        } else if (data.message && !(data.messageIsPrunable === "true")) {
            return false;
        }

        // EncryptedMessageAppendix
        if ((transaction.flags & 2) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageToEncryptIsText = flags && 1;
            var messageToEncryptIsText = (transaction.messageToEncryptIsText ? "true" : "false");
            if (messageToEncryptIsText != data.messageToEncryptIsText) {
                return false;
            }
            var encryptedMessageLength = converters.byteArrayToSignedShort(byteArray, pos);
            if (encryptedMessageLength < 0) {
                encryptedMessageLength &= NRS.constants.MAX_SHORT_JAVA;
            }
            pos += 2;
            transaction.encryptedMessageData = converters.byteArrayToHexString(byteArray.slice(pos, pos + encryptedMessageLength));
            pos += encryptedMessageLength;
            transaction.encryptedMessageNonce = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            if (transaction.encryptedMessageData !== data.encryptedMessageData || transaction.encryptedMessageNonce !== data.encryptedMessageNonce) {
                return false;
            }
        } else if (data.encryptedMessageData && !(data.encryptedMessageIsPrunable === "true")) {
            return false;
        }

        // EncryptToSelfMessageAppendix
        if ((transaction.flags & 4) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageToEncryptToSelfIsText = flags && 1;
            var messageToEncryptToSelfIsText = (transaction.messageToEncryptToSelfIsText ? "true" : "false");
            if (messageToEncryptToSelfIsText != data.messageToEncryptToSelfIsText) {
                return false;
            }
            var encryptedToSelfMessageLength = converters.byteArrayToSignedShort(byteArray, pos);
            if (encryptedToSelfMessageLength < 0) {
                encryptedToSelfMessageLength &= NRS.constants.MAX_SHORT_JAVA;
            }
            pos += 2;
            transaction.encryptToSelfMessageData = converters.byteArrayToHexString(byteArray.slice(pos, pos + encryptedToSelfMessageLength));
            pos += encryptedToSelfMessageLength;
            transaction.encryptToSelfMessageNonce = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            if (transaction.encryptToSelfMessageData !== data.encryptToSelfMessageData || transaction.encryptToSelfMessageNonce !== data.encryptToSelfMessageNonce) {
                return false;
            }
        } else if (data.encryptToSelfMessageData) {
            return false;
        }

        // PrunablePlainMessageAppendix
        var utfBytes;
        if ((transaction.flags & 8) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            if (flags != 0) {
                return false;
            }
            var serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            var sha256 = CryptoJS.algo.SHA256.create();
            var isText = [];
            if (data.messageIsText == "true") {
                isText.push(1);
            } else {
                isText.push(0);
            }
            sha256.update(converters.byteArrayToWordArrayEx(isText));
            if (data.filebytes) {
                utfBytes = new Int8Array(data.filebytes);
            } else {
                utfBytes = NRS.getUtf8Bytes(data.message);
            }
            sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
            var hashWords = sha256.finalize();
            var calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
            if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                return false;
            }
        }

        // PrunableEncryptedMessageAppendix
        if ((transaction.flags & 16) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            if (flags != 0) {
                return false;
            }
            serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            sha256 = CryptoJS.algo.SHA256.create();
            if (data.messageToEncryptIsText == "true") {
                sha256.update(converters.byteArrayToWordArrayEx([1]));
            } else {
                sha256.update(converters.byteArrayToWordArrayEx([0]));
            }
            sha256.update(converters.byteArrayToWordArrayEx([1])); // compression
            if (data.filebytes) {
                utfBytes = new Int8Array(data.filebytes);
            } else {
                utfBytes = converters.hexStringToByteArray(data.encryptedMessageData);
            }
            sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
            sha256.update(converters.byteArrayToWordArrayEx(converters.hexStringToByteArray(data.encryptedMessageNonce)));
            hashWords = sha256.finalize();
            calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
            if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                return false;
            }
        }

        // PublicKeyAnnouncementAppendix
        if ((transaction.flags & 32) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return false;
            }
            pos++;
            var recipientPublicKey = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            if (recipientPublicKey != data.recipientPublicKey) {
                return false;
            }
            pos += 32;
        } else if (data.recipientPublicKey) {
            return false;
        }

        // PhasingAppendix
        if ((transaction.flags & 64) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 1) {
                return false;
            }
            pos++;
            if (String(converters.byteArrayToSignedInt32(byteArray, pos)) !== data.phasingFinishHeight) {
                return false;
            }
            pos += 4;
            var params = JSON.parse(data["phasingParams"]);
            pos = validateCommonPhasingData(byteArray, pos, params);
            if (pos == -1) {
                return false;
            }
            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.TRANSACTION) {
                var linkedFullHashesLength = byteArray[pos];
                pos++;
                if (linkedFullHashesLength > 1) {
                    NRS.logConsole("currently only 1 full hash is supported");
                    return false;
                }
                if (linkedFullHashesLength == 1) {
                    var tokens = String(params.phasingLinkedTransaction).split(":");
                    if (tokens.length != 2) {
                        return false;
                    }
                    var chain = tokens[0];
                    if (chain != String(converters.byteArrayToSignedInt32(byteArray, pos))) {
                        return false;
                    }
                    pos += 4;
                    var fullHash = tokens[1];
                    if (fullHash != converters.byteArrayToHexString(byteArray.slice(pos, pos + 32))) {
                        return false;
                    }
                    pos += 32;
                }
            }
            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.HASH) {
                var hashedSecretLength = byteArray[pos];
                pos++;
                if (hashedSecretLength > 0 && converters.byteArrayToHexString(byteArray.slice(pos, pos + hashedSecretLength)) !== params.phasingHashedSecret) {
                    return false;
                }
                pos += hashedSecretLength;
                var algorithm = String(byteArray[pos]);
                if (algorithm !== "0" && algorithm != params.phasingHashedSecretAlgorithm) {
                    return false;
                }
            }

            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
                pos = validateCompositePhasingData(byteArray, pos, params);
                if (pos == -1) {
                    return false;
                }
            }
        }
        return true;
    };

    NRS.broadcastTransactionBytes = function (transactionData, callback, originalResponse, originalData, requestType) {
        var data = {
            "transactionBytes": transactionData,
            "prunableAttachmentJSON": JSON.stringify(originalResponse.transactionJSON.attachment),
            "adminPassword": NRS.getAdminPassword()
        };
        requestType = NRS.state.apiProxy ? "sendTransaction": "broadcastTransaction";
        $.ajax({
            url: NRS.getRequestPath() + "?requestType=" + requestType,
            crossDomain: true,
            dataType: "json",
            type: "POST",
            timeout: 30000,
            async: true,
            data: data
        }).done(function (response) {
            NRS.escapeResponseObjStrings(response);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, response);
            }

            if (response.errorCode) {
                if (!response.errorDescription) {
                    response.errorDescription = (response.errorMessage ? response.errorMessage : "Unknown error occurred.");
                }
                callback(response, originalData);
            } else if (response.error) {
                response.errorCode = 1;
                response.errorDescription = response.error;
                callback(response, originalData);
            } else {
                if ("transactionBytes" in originalResponse) {
                    delete originalResponse.transactionBytes;
                }
                originalResponse.broadcasted = true;
                originalResponse.transaction = response.transaction;
                originalResponse.fullHash = response.fullHash;
                callback(originalResponse, originalData);
                if (originalData.referencedTransactionFullHash) {
                    $.growl($.t("info_referenced_transaction_hash"), {
                        "type": "info"
                    });
                }
            }
        }).fail(function (xhr, textStatus, error) {
            NRS.logConsole("request failed, status: " + textStatus + ", error: " + error);
            if (NRS.console) {
                NRS.addToConsole(this.url, this.type, this.data, error, true);
            }
            NRS.resetRemoteNode(true);
            if (error == "timeout") {
                error = $.t("error_request_timeout");
            }
            callback({
                "errorCode": -1,
                "errorDescription": error
            }, {});
        });
    };

    NRS.generateQRCode = function(target, qrCodeData, minType, cellSize) {
        var type = minType ? minType : 2;
        while (type <= 40) {
            try {
                var qr = qrcode(type, 'M');
                qr.addData(qrCodeData);
                qr.make();
                var img = qr.createImgTag(cellSize);
                NRS.logConsole("Encoded QR code of type " + type + " with cell size " + cellSize);
                if (target) {
                    $(target).empty().append(img);
                }
                return img;
            } catch (e) {
                type++;
            }
        }
        $(target).empty().html($.t("cannot_encode_message", qrCodeData.length));
    };

    function addAddressData(data) {
        if (typeof data == "object" && ("recipient" in data)) {
            var address = NRS.createRsAddress();
            if (NRS.isRsAccount(data.recipient)) {
                data.recipientRS = data.recipient;
                if (address.set(data.recipient)) {
                    data.recipient = address.account_id();
                }
            } else {
                if (address.set(data.recipient)) {
                    data.recipientRS = address.toString();
                }
            }
        }
    }

    function addMissingData(data) {
        if (!("amountNQT" in data)) {
            data.amountNQT = "0";
        }
        if (!("recipient" in data)) {
            NRS.logConsole("No recipient in data");
        }
    }

    function validateControlPhasingData(data, byteArray, pos, hasFeeBurningData) {
        var params;
        if (byteArray[pos] == 0xFF) {
            // Removal of account control
            if (data.controlVotingModel != NRS.constants.VOTING_MODELS.NONE) {
                return -1;
            }
            params = { phasingVotingModel: "-1", phasingQuorum: "0", phasingMinBalance: "0" }; // The server puts these bytes as control params so make sure they are there
            pos = validateCommonPhasingData(byteArray, pos, params);
        } else {
            params = JSON.parse(data["controlParams"]);
            pos = validateCommonPhasingData(byteArray, pos, params);
            if (pos == -1) {
                return -1;
            }
        }
        if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
            pos = validateCompositePhasingData(byteArray, pos, params);
            if (pos == -1) {
                return -1;
            }
        }
        if (hasFeeBurningData) {
            var maxFeesSize = byteArray[pos];
            pos++;
            for (var i = 0; i < maxFeesSize; i++) {
                // for now the client can only submit 0 or 1 control fees
                // in case this is ever enhanced, we'll need to revisit this code
                var controlMaxFees = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                controlMaxFees += ":" + String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (controlMaxFees != data.controlMaxFees) {
                    return -1;
                }
            }
            var minDuration = converters.byteArrayToSignedShort(byteArray, pos);
            pos += 2;
            if (data.controlMinDuration && minDuration != data.controlMinDuration) {
                return -1;
            }
            var maxDuration = converters.byteArrayToSignedShort(byteArray, pos);
            pos += 2;
            if (data.controlMaxDuration && maxDuration != data.controlMaxDuration) {
                return -1;
            }
        }
        return pos;
    }

    function validateCommonPhasingData(byteArray, pos, params) {
        if (byteArray[pos] != (parseInt(params["phasingVotingModel"]) & 0xFF)) {
            return -1;
        }
        pos++;
        var quorum = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (quorum !== "0" && quorum !== String(params["phasingQuorum"])) {
            return -1;
        }
        pos += 8;
        var minBalance = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (minBalance !== "0" && minBalance !== params["phasingMinBalance"]) {
            return -1;
        }
        pos += 8;
        var whiteListLength = byteArray[pos];
        pos++;
        for (var i = 0; i < whiteListLength; i++) {
            var accountId = converters.byteArrayToBigInteger(byteArray, pos);
            var accountRS = NRS.convertNumericToRSAccountFormat(accountId);
            pos += 8;
            if (String(accountId) !== params["phasingWhitelist"][i] && String(accountRS) !== params["phasingWhitelist"][i]) {
                return -1;
            }
        }
        var holdingId = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (holdingId !== "0" && holdingId !== params["phasingHolding"]) {
            return -1;
        }
        pos += 8;
        var minBalanceModel = String(byteArray[pos]);
        if (minBalanceModel !== "0" && minBalanceModel !== String(params["phasingMinBalanceModel"])) {
            return -1;
        }
        pos++;

        if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.PROPERTY) {
            if (params.phasingSenderProperty === undefined) {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== "0") {
                    return false;
                }
                pos += 8;
                if (byteArray[pos] !== 0) {
                    return false;
                }
                pos++;
                if (byteArray[pos] !== 0) {
                    return false;
                }
                pos++;
            } else {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== params.phasingSenderProperty.setter) {
                    return false;
                }
                pos += 8;
                var length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingSenderProperty.name) {
                    return false;
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (params.phasingSenderProperty.value === undefined) {
                    if (length !== 0) {
                        return false;
                    }
                } else {
                    if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingSenderProperty.value) {
                        return false;
                    }
                }
                pos += length;
            }
            if (params.phasingRecipientProperty === undefined) {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== "0") {
                    return false;
                }
                pos += 8;
                if (byteArray[pos] !== 0) {
                    return false;
                }
                pos++;
                if (byteArray[pos] !== 0) {
                    return false;
                }
                pos++;
            } else {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== params.phasingRecipientProperty.setter) {
                    return false;
                }
                pos += 8;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingRecipientProperty.name) {
                    return false;
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (params.phasingRecipientProperty.value === undefined) {
                    if (length !== 0) {
                        return false;
                    }
                } else {
                    if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingRecipientProperty.value) {
                        return false;
                    }
                }
                pos += length;
            }
        }

        return pos;
    }

    function validateCompositePhasingData(byteArray, pos, params) {
        var length = converters.byteArrayToSignedShort(byteArray, pos);
        pos += 2;
        var expression = converters.byteArrayToString(byteArray, pos, length);
        if (params.phasingExpression != expression) {
            return -1;
        }
        pos += length;
        length = byteArray[pos];
        pos++;
        for (var i = 0; i < length; i++) {
            var subPollNameLength = byteArray[pos];
            pos++;
            var subPollName = converters.byteArrayToString(byteArray, pos, subPollNameLength);
            pos += subPollNameLength;
            if (!params.phasingSubPolls[subPollName]) {
                return -1;
            }
            pos = validateCommonPhasingData(byteArray, pos, params.phasingSubPolls[subPollName]);
            if (pos == -1) {
                return -1;
            }
        }
        return pos;
    }

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}
