/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2019 Jelurida IP B.V.                                     *
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
                ["standbyShufflerMinAmountNXT", "minAmount"],
                ["standbyShufflerMaxAmountNXT", "maxAmount"],
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
                ["amountQNTf", "shuffling_ms_decimals"],
                ["minAmountQNTf", "standbyshuffler_asset_decimals"],
                ["minAmountQNTf", "standbyshuffler_ms_decimals"],
                ["maxAmountQNTf", "standbyshuffler_asset_decimals"],
                ["maxAmountQNTf", "standbyshuffler_ms_decimals"]
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
            if (accountId != NRS.account && !data.isVoucher) {
                if (data.secretPhrase === "") {
                    callback({
                        "errorCode": 1,
                        "errorDescription": $.t("error_passphrase_not_specified")
                    });
                } else {
                    callback({
                        "errorCode": 1,
                        "errorDescription": $.t("error_passphrase_incorrect_v2", { account: NRS.getAccountId(data.secretPhrase, true) })
                    });
                }
            } else {
                NRS.processAjaxRequest(requestType, data, callback, options);
            }
        } else {
            NRS.processAjaxRequest(requestType, data, callback, options);
        }
    };

    function isVolatileRequest(data, type, requestType) {
        if (data.secretPhrase && NRS.isMobileApp()) {
            return true;
        }
        return (NRS.isPassphraseAtRisk() || data.doNotSign || data.isVoucher) && type == "POST" && !NRS.isSubmitPassphrase(requestType);
    }

    NRS.requestId = 0;

    NRS.processAjaxRequest = function (requestType, data, callback, options) {
        var extra = {};
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
        var isVolatile = isVolatileRequest(data, httpMethod, requestType);
        if (isVolatile) {
            if (NRS.rememberPassword) {
                secretPhrase = _password;
            } else {
                secretPhrase = data.secretPhrase;
                if (data.isVoucher) {
                    extra.voucherSecretPhrase = secretPhrase;
                }
            }
            // Delete the secret phrase from the submitted data and only use it to sign the response locally
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
        if (NRS.isLogConsole(10)) {
            NRS.logConsole("Send request " + requestType + " to url " + url + " id=" + currentRequestId);
        }
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
                                if (response.unsignedTransactionBytes) {
                                    var result = NRS.verifyTransactionBytes(response.unsignedTransactionBytes, requestType, data, response.transactionJSON.attachment, isVolatile);
                                    if (result.fail) {
                                        callback({
                                            "errorCode": 1,
                                            "errorDescription": $.t("error_bytes_validation_server_v2", { param: result.param, expected: result.expected, actual: result.actual })
                                        }, data);
                                        return;
                                    }
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
        var result = NRS.verifyTransactionBytes(transactionBytes, requestType, data, response.transactionJSON.attachment, isVerifyECBlock);
        if (result.fail) {
            callback({
                "errorCode": 1,
                "errorDescription": $.t("error_bytes_validation_server_v2", { param: result.param, expected: result.expected, actual: result.actual })
            }, data);
            return;
        }
        var sigPos = 2 * NRS.constants.SIGNATURE_POSITION;
        var sigLen = 2 * NRS.constants.SIGNATURE_LENGTH;
        var payload = transactionBytes.substr(0, sigPos) + signature + transactionBytes.substr(sigPos + sigLen);
        if (data.broadcast == "false") {
            response.transactionBytes = payload;
            response.transactionJSON.signature = signature;
            NRS.logConsole("before showRawTransactionModal data.broadcast == false");
            if (data.isVoucher) {
                signature = NRS.signBytes(response.unsignedTransactionBytes, converters.stringToHexString(extra.voucherSecretPhrase));
                var publicKey = NRS.getPublicKey(converters.stringToHexString(extra.voucherSecretPhrase));
                delete extra.voucherSecretPhrase;
                NRS.showVoucherModal(response, signature, publicKey, requestType);
            } else {
                NRS.showRawTransactionModal(response);
            }
        } else {
            if (extra) {
                data["_extra"] = extra;
            }
            NRS.broadcastTransactionBytes(payload, callback, response, data);
        }
    };

    NRS.verifyTransactionBytes = function (transactionBytes, requestType, data, attachment, isVerifyECBlock) {
        try {
            var byteArray = converters.hexStringToByteArray(transactionBytes);
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
                    return { fail: true, param: "ecBlockHeight", actual: transaction.ecBlockHeight, expected: ecBlock.height };
                }
                if (transaction.ecBlockId != ecBlock.id) {
                    return { fail: true, param: "ecBlockId", actual: transaction.ecBlockId, expected: ecBlock.id };
                }
            }

            if (transaction.chain != data.chain && !(transaction.chain == "1" && data.isParentChainTransaction == "1")) {
                return {fail: true, param: "chain", actual: transaction.chain, expected: data.chain};
            }

            if (transaction.publicKey != NRS.accountInfo.publicKey && transaction.publicKey != data.publicKey && transaction.publicKey != data.senderPublicKey) {
                return { fail: true, param: "publicKey", actual: transaction.publicKey, expected: NRS.accountInfo.publicKey || data.publicKey };
            }

            if (transaction.deadline != data.deadline) {
                return { fail: true, param: "deadline", actual: transaction.deadline, expected: data.deadline };
            }

            if (transaction.recipient !== data.recipient) {
                if (!((data.recipient === undefined || data.recipient == "") && transaction.recipient == "0")) {
                    if (!NRS.isSpecialRecipient(requestType)) {
                        return { fail: true, param: "recipient", actual: transaction.recipient, expected: data.recipient };
                    }
                }
            }

            if (transaction.amountNQT !== data.amountNQT && !(requestType === "exchangeCoins" && transaction.amountNQT === "0")) {
                return { fail: true, param: "chain", actual: transaction.chain, expected: data.chain };
            }

            if ("referencedTransactionFullHash" in data) {
                if (transaction.referencedTransactionFullHash !== data.referencedTransactionFullHash) {
                    return { fail: true, param: "referencedTransactionFullHash", actual: transaction.referencedTransactionFullHash, expected: data.referencedTransactionFullHash };
                }
            } else if (transaction.referencedTransactionFullHash && transaction.referencedTransactionFullHash !== "") {
                return { fail: true, param: "referencedTransactionFullHash", actual: transaction.referencedTransactionFullHash, expected: "" };
            }
            //has empty attachment, so no attachmentVersion byte...
            if (!(requestType == "sendMoney" || requestType == "sendMessage")) {
                pos++;
            }
            return NRS.verifyTransactionTypes(byteArray, transaction, requestType, data, pos, attachment);
        } catch (e) {
            NRS.logException(e);
            return { fail: true, param: "exception", actual: e.message, expected: "" };
        }
    };

    NRS.verifyTransactionTypes = function (byteArray, transaction, requestType, data, pos, attachment) {
        var length = 0;
        var i = 0;
        var serverHash, sha256, utfBytes, isText, hashWords, calculatedHash, result;
        var notOfTypeError = { fail: true, param: "requestType", actual: requestType, expected: "type:" + transaction.type + ", subtype:" + transaction.subtype};
        switch (requestType) {
            case "sendMoney":
                if (NRS.notOfType(transaction, "FxtPayment") && NRS.notOfType(transaction, "OrdinaryPayment")) {
                    return notOfTypeError;
                }
                break;
            case "sendMessage":
                if (NRS.notOfType(transaction, "ArbitraryMessage")) {
                    return notOfTypeError;
                }
                break;
            case "setAlias":
                if (NRS.notOfType(transaction, "AliasAssignment")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: data.aliasName + "/" + data.aliasURI, expected: transaction.aliasName + "/" + transaction.aliasURI};
                }
                break;
            case "createPoll":
                if (NRS.notOfType(transaction, "PollCreation")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType + "Poll", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }

                for (i = 0; i < nr_options; i++) {
                    if (transaction["option" + (i < 10 ? "0" + i : i)] !== data["option" + (i < 10 ? "0" + i : i)]) {
                        return { fail: true, param: requestType + "Options", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                    }
                }

                if (("option" + (i < 10 ? "0" + i : i)) in data) {
                    return { fail: true, param: requestType + "Option", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "castVote":
                if (NRS.notOfType(transaction, "VoteCasting")) {
                    return notOfTypeError;
                }
                transaction.poll = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                var voteLength = byteArray[pos];
                pos++;
                transaction.votes = [];

                for (i = 0; i < voteLength; i++) {
                    transaction["vote" + (i < 10 ? "0" + i : i)] = byteArray[pos];
                    pos++;
                    // TODO validate vote bytes against data
                }
                if (transaction.poll !== data.poll) {
                    return { fail: true, param: requestType + "Poll", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "setAccountInfo":
                if (NRS.notOfType(transaction, "AccountInfo")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "sellAlias":
                if (NRS.notOfType(transaction, "AliasSell")) {
                    return notOfTypeError;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.alias !== data.aliasName || transaction.priceNQT !== data.priceNQT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "buyAlias":
                if (NRS.notOfType(transaction, "AliasBuy")) {
                    return notOfTypeError;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.alias !== data.aliasName) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "deleteAlias":
                if (NRS.notOfType(transaction, "AliasDelete")) {
                    return notOfTypeError;
                }
                length = parseInt(byteArray[pos], 10);
                pos++;
                transaction.alias = converters.byteArrayToString(byteArray, pos, length);
                pos += length;
                if (transaction.alias !== data.aliasName) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "approveTransaction":
                if (NRS.notOfType(transaction, "PhasingVoteCasting")) {
                    return notOfTypeError;
                }
                var fullHashesLength = byteArray[pos];
                pos++;
                if (fullHashesLength > 1) {
                    return { fail: true, param: requestType + "fullHashesLength", actual: fullHashesLength, expected: 1 };
                }
                if (fullHashesLength === 1) {
                    var phasedTransaction = converters.byteArrayToSignedInt32(byteArray, pos);
                    pos += 4;
                    phasedTransaction += ":" + converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                    pos += 32;
                    if (phasedTransaction !== data.phasedTransaction) {
                        return { fail: true, param: requestType + "PhasedTransaction", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                    }
                }
                var numberOfSecrets = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                if (numberOfSecrets < 0 || numberOfSecrets > 1
                    || numberOfSecrets == 0 && (data.revealedSecretText && data.revealedSecretText !== "" || data.revealedSecret && data.revealedSecret !== "")
                        || numberOfSecrets == 1 && (!data.revealedSecretText || data.revealedSecretText === "") && (!data.revealedSecret || data.revealedSecret === "")) {
                    return { fail: true, param: requestType + "NumberOfSecrets", actual: JSON.stringify(data), expected: numberOfSecrets };
                }
                // We only support one secret per phasing model
                transaction.revealedSecretLength = converters.byteArrayToSignedShort(byteArray, pos);
                pos += 2;
                if (transaction.revealedSecretLength > 0) {
                    transaction.revealedSecret = converters.byteArrayToHexString(byteArray.slice(pos, pos + transaction.revealedSecretLength));
                    pos += transaction.revealedSecretLength;
                } else {
                    transaction.revealedSecret = "";
                }
                if (transaction.revealedSecret !== data.revealedSecret &&
                    transaction.revealedSecret !== converters.byteArrayToHexString(NRS.getUtf8Bytes(data.revealedSecretText))) {
                    return { fail: true, param: requestType + "RevealedSecret", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "setAccountProperty":
                if (NRS.notOfType(transaction, "AccountProperty")) {
                    return notOfTypeError;
                }
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.property) {
                    return { fail: true, param: requestType + "Key", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.value) {
                    return { fail: true, param: requestType + "Value", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                pos += length;
                break;
            case "deleteAccountProperty":
                if (NRS.notOfType(transaction, "AccountPropertyDelete")) {
                    return notOfTypeError;
                }
                // no way to validate the property id, just skip it
                String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                break;
            case "issueAsset":
                if (NRS.notOfType(transaction, "AssetIssuance")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "transferAsset":
                if (NRS.notOfType(transaction, "AssetTransfer")) {
                    return notOfTypeError;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "placeAskOrder":
            case "placeBidOrder":
                if (NRS.notOfType(transaction, "AskOrderPlacement") && NRS.notOfType(transaction, "BidOrderPlacement")) {
                    return notOfTypeError;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.priceNQTPerShare = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT || transaction.priceNQTPerShare !== data.priceNQTPerShare) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "cancelAskOrder":
            case "cancelBidOrder":
                if (NRS.notOfType(transaction, "AskOrderCancellation") && NRS.notOfType(transaction, "BidOrderCancellation")) {
                    return notOfTypeError;
                }
                transaction.order = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.order !== data.order) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "deleteAssetShares":
                if (NRS.notOfType(transaction, "AssetDelete")) {
                    return notOfTypeError;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "increaseAssetShares":
                if (NRS.notOfType(transaction, "AssetIncrease")) {
                    return notOfTypeError;
                }
                transaction.asset = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.asset !== data.asset || transaction.quantityQNT !== data.quantityQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dividendPayment":
                if (NRS.notOfType(transaction, "DividendPayment")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "setPhasingAssetControl":
                if (NRS.notOfType(transaction, "SetPhasingAssetControl")) {
                    return notOfTypeError;
                }
                if (data.asset !== String(converters.byteArrayToBigInteger(byteArray, pos))) {
                    return { fail: true, param: requestType + "Asset", actual: JSON.stringify(data), expected: String(converters.byteArrayToBigInteger(byteArray, pos))};
                }
                pos += 8;
                result = validateControlPhasingData(data, byteArray, pos, false);
                if (result.fail) {
                    return result;
                } else {
                    pos = result.pos;
                }
                break;
            case "setAssetProperty":
                if (NRS.notOfType(transaction, "AssetProperty")) {
                    return notOfTypeError;
                }
                if (data.asset !== String(converters.byteArrayToBigInteger(byteArray, pos))) {
                    return { fail: true, param: requestType + "Asset", actual: JSON.stringify(data), expected: String(converters.byteArrayToBigInteger(byteArray, pos))};
                }
                pos += 8;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.property) {
                    return { fail: true, param: requestType + "Key", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== data.value) {
                    return { fail: true, param: requestType + "Value", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                pos += length;
                break;
            case "deleteAssetProperty":
                if (NRS.notOfType(transaction, "AssetPropertyDelete")) {
                    return notOfTypeError;
                }
                // no way to validate the property id, just skip it
                String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                break;
            case "dgsListing":
                if (NRS.notOfType(transaction, "DigitalGoodsListing")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsDelisting":
                if (NRS.notOfType(transaction, "DigitalGoodsDelisting")) {
                    return notOfTypeError;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.goods !== data.goods) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsPriceChange":
                if (NRS.notOfType(transaction, "DigitalGoodsPriceChange")) {
                    return notOfTypeError;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.priceNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.goods !== data.goods || transaction.priceNQT !== data.priceNQT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsQuantityChange":
                if (NRS.notOfType(transaction, "DigitalGoodsQuantityChange")) {
                    return notOfTypeError;
                }
                transaction.goods = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.deltaQuantity = String(converters.byteArrayToSignedInt32(byteArray, pos));
                pos += 4;
                if (transaction.goods !== data.goods || transaction.deltaQuantity !== data.deltaQuantity) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsPurchase":
                if (NRS.notOfType(transaction, "DigitalGoodsPurchase")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsDelivery":
                if (NRS.notOfType(transaction, "DigitalGoodsDelivery")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType + "IsText", actual: goodsIsText, expected: data.goodsIsText };
                }
                if (transaction.purchase !== data.purchase || transaction.goodsData !== data.goodsData || transaction.goodsNonce !== data.goodsNonce || transaction.discountNQT !== data.discountNQT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsFeedback":
                if (NRS.notOfType(transaction, "DigitalGoodsFeedback")) {
                    return notOfTypeError;
                }
                transaction.purchase = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.purchase !== data.purchase) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "dgsRefund":
                if (NRS.notOfType(transaction, "DigitalGoodsRefund")) {
                    return notOfTypeError;
                }
                transaction.purchase = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.refundNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.purchase !== data.purchase || transaction.refundNQT !== data.refundNQT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "leaseBalance":
                if (NRS.notOfType(transaction, "EffectiveBalanceLeasing")) {
                    return notOfTypeError;
                }
                transaction.period = String(converters.byteArrayToSignedShort(byteArray, pos));
                pos += 2;
                if (transaction.period !== data.period) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "setPhasingOnlyControl":
                if (NRS.notOfType(transaction, "SetPhasingOnly")) {
                    return notOfTypeError;
                }
                result = validateControlPhasingData(data, byteArray, pos, true);
                if (result.fail) {
                    return result;
                } else {
                    pos = result.pos;
                }
                break;
            case "issueCurrency":
                if (NRS.notOfType(transaction, "CurrencyIssuance")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                if (transaction.minReservePerUnitNQT !== "0" && transaction.minReservePerUnitNQT !== data.minReservePerUnitNQT) {
                    return { fail: true, param: requestType + "MinReservePerUnitNQT", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                if (transaction.minDifficulty !== "0" && transaction.minDifficulty !== data.minDifficulty) {
                    return { fail: true, param: requestType + "MinDifficulty", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                if (transaction.maxDifficulty !== "0" && transaction.maxDifficulty !== data.maxDifficulty) {
                    return { fail: true, param: requestType + "MaxDifficulty", actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "currencyReserveIncrease":
                if (NRS.notOfType(transaction, "ReserveIncrease")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.amountPerUnitNQT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.amountPerUnitNQT !== data.amountPerUnitNQT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "currencyReserveClaim":
                if (NRS.notOfType(transaction, "ReserveClaim")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.unitsQNT !== data.unitsQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "transferCurrency":
                if (NRS.notOfType(transaction, "CurrencyTransfer")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.unitsQNT !== data.unitsQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "publishExchangeOffer":
                if (NRS.notOfType(transaction, "PublishExchangeOffer")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "currencyBuy":
                if (NRS.notOfType(transaction, "ExchangeBuy")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.rateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.rateNQTPerUnit !== data.rateNQTPerUnit || transaction.unitsQNT !== data.unitsQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "currencySell":
                if (NRS.notOfType(transaction, "ExchangeSell")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.rateNQTPerUnit = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                transaction.unitsQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency || transaction.rateNQTPerUnit !== data.rateNQTPerUnit || transaction.unitsQNT !== data.unitsQNT) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "currencyMint":
                if (NRS.notOfType(transaction, "CurrencyMinting")) {
                    return notOfTypeError;
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
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "deleteCurrency":
                if (NRS.notOfType(transaction, "CurrencyDeletion")) {
                    return notOfTypeError;
                }
                transaction.currency = String(converters.byteArrayToBigInteger(byteArray, pos));
                pos += 8;
                if (transaction.currency !== data.currency) {
                    return { fail: true, param: requestType, actual: JSON.stringify(data), expected: JSON.stringify(transaction) };
                }
                break;
            case "uploadTaggedData":
                if (NRS.notOfType(transaction, "TaggedDataUpload")) {
                    return notOfTypeError;
                }
                if (byteArray[pos] != 0) {
                    return { fail: true, param: requestType + "Pos", actual: JSON.stringify(data), expected: "" };
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
                    return { fail: true, param: requestType + "ServerHash", actual: serverHash, expected: calculatedHash };
                }
                break;
            case "shufflingCreate":
                if (NRS.notOfType(transaction, "ShufflingCreation")) {
                    return notOfTypeError;
                }
                var holding = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (holding !== "0" && holding !== data.holding ||
                    holding === "0" && data.holding !== undefined && data.holding !== "" && data.holding !== "0") {
                    return { fail: true, param: requestType, actual: holding, expected: data.holding };
                }
                pos += 8;
                var holdingType = String(byteArray[pos]);
                if (holdingType !== "0" && holdingType !== data.holdingType ||
                    holdingType === "0" && data.holdingType !== undefined && data.holdingType !== "" && data.holdingType !== "0") {
                    return { fail: true, param: requestType, actual: holdingType, expected: data.holdingType };
                }
                pos++;
                var amount = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (amount !== data.amount) {
                    return { fail: true, param: requestType, actual: amount, expected: data.amount };
                }
                pos += 8;
                var participantCount = String(byteArray[pos]);
                if (participantCount !== data.participantCount) {
                    return { fail: true, param: requestType, actual: participantCount, expected: data.participantCount };
                }
                pos++;
                var registrationPeriod = converters.byteArrayToSignedShort(byteArray, pos);
                if (registrationPeriod !== data.registrationPeriod) {
                    return { fail: true, param: requestType, actual: registrationPeriod, expected: data.registrationPeriod };
                }
                pos += 2;
                break;
            case "exchangeCoins":
                var typeStr = transaction.chain == "1" ? "FxtCoinExchangeOrderIssue" : "CoinExchangeOrderIssue";
                if (NRS.notOfType(transaction, typeStr)) {
                    return notOfTypeError;
                }
                var chain = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (chain != data.chain) {
                    return { fail: true, param: requestType, actual: chain, expected: data.chain };
                }
                pos += 4;
                var exchange = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (exchange != data.exchange) {
                    return { fail: true, param: requestType, actual: exchange, expected: data.exchange };
                }
                pos += 4;
                var quantityQNT = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (quantityQNT !== data.quantityQNT) {
                    return { fail: true, param: requestType, actual: quantityQNT, expected: data.quantityQNT };
                }
                pos += 8;
                var priceNQTPerCoin = String(converters.byteArrayToBigInteger(byteArray, pos));
                if (priceNQTPerCoin !== data.priceNQTPerCoin) {
                    return { fail: true, param: requestType, actual: priceNQTPerCoin, expected: data.priceNQTPerCoin };
                }
                pos += 8;
                break;
            case "cancelCoinExchange":
                typeStr = transaction.chain == "1" ? "FxtCoinExchangeOrderCancel" : "CoinExchangeOrderCancel";
                if (NRS.notOfType(transaction, typeStr)) {
                    return notOfTypeError;
                }
                var orderHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                if (NRS.fullHashToId(orderHash) !== data.order) {
                    return { fail: true, param: requestType, actual: NRS.fullHashToId(orderHash), expected: data.order };
                }
                pos += 32;
                break;
            case "bundleTransactions":
                if (NRS.notOfType(transaction, "ChildChainBlock")) {
                    return notOfTypeError;
                }
                var isPrunable = byteArray[pos];
                if (isPrunable != 0) {
                    return { fail: true, param: requestType, actual: isPrunable, expected: 0 };
                }
                pos++;
                chain = String(converters.byteArrayToSignedInt32(byteArray, pos));
                if (chain != data.childChain) {
                    return { fail: true, param: requestType, actual: chain, expected: data.childChain };
                }
                pos += 4;
                serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
                sha256 = CryptoJS.algo.SHA256.create();
                // We assume here that only one transaction was bundled
                sha256.update(converters.byteArrayToWordArrayEx(converters.hexStringToByteArray(data.transactionFullHash)));
                hashWords = sha256.finalize();
                calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
                if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                    return { fail: true, param: requestType, actual: converters.byteArrayToHexString(calculatedHash), expected: serverHash };
                }
                pos += 32;
                break;
            default:
                return notOfTypeError;
        }

        return NRS.verifyAppendix(byteArray, transaction, requestType, data, pos);
    };

    NRS.verifyAppendix = function (byteArray, transaction, requestType, data, pos) {
        var attachmentVersion;
        var flags;
        var result;

        // MessageAppendix
        if ((transaction.flags & 1) != 0 ||
            ((requestType == "sendMessage" && data.message && !(data.messageIsPrunable === "true")))) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "MessageAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageIsText = flags && 1;
            var messageIsText = (transaction.messageIsText ? "true" : "false");
            // TODO special case bytes false data true
            if (messageIsText != data.messageIsText) {
                return { fail: true, param: "MessageAppendix", actual: messageIsText, expected: data.messageIsText };
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
                return { fail: true, param: "MessageAppendix", actual: message, expected: data.message };
            }
        } else if (data.message && !(data.messageIsPrunable === "true" || data.messageHash)) {
            return { fail: true, param: "MessageAppendix", actual: "message", expected: "" };
        }

        // EncryptedMessageAppendix
        if ((transaction.flags & 2) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "EncryptedMessageAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageToEncryptIsText = flags && 1;
            var messageToEncryptIsText = (transaction.messageToEncryptIsText ? "true" : "false");
            if (messageToEncryptIsText != data.messageToEncryptIsText && transaction.messageToEncryptIsText != data.isText) {
                return { fail: true, param: "EncryptedMessageAppendix", actual: messageToEncryptIsText, expected: data.messageToEncryptIsText };
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
            if (transaction.encryptedMessageData !== (data.encryptedMessageData || data.data) || transaction.encryptedMessageNonce !== (data.encryptedMessageNonce || data.nonce)) {
                return { fail: true, param: "EncryptedMessageAppendix", actual: JSON.stringify(transaction), expected: JSON.stringify(data) };
            }
        } else if (data.encryptedMessageData && !(data.encryptedMessageIsPrunable === "true")) {
            return { fail: true, param: "EncryptedMessageAppendix", actual: "", expected: JSON.stringify(data) };
        }

        // EncryptToSelfMessageAppendix
        if ((transaction.flags & 4) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "EncryptToSelfMessageAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            transaction.messageToEncryptToSelfIsText = flags && 1;
            var messageToEncryptToSelfIsText = (transaction.messageToEncryptToSelfIsText ? "true" : "false");
            if (messageToEncryptToSelfIsText != data.messageToEncryptToSelfIsText) {
                return { fail: true, param: "EncryptToSelfMessageAppendix", actual: messageToEncryptToSelfIsText, expected: data.messageToEncryptToSelfIsText };
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
                return { fail: true, param: "EncryptToSelfMessageAppendix", actual: JSON.stringify(transaction), expected: JSON.stringify(data) };
            }
        } else if (data.encryptToSelfMessageData) {
            return { fail: true, param: "EncryptToSelfMessageAppendix", actual: data.encryptToSelfMessageData, expected: "" };
        }

        // PrunablePlainMessageAppendix
        var utfBytes;
        if ((transaction.flags & 8) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "PrunablePlainMessageAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            if (flags != 0) {
                return { fail: true, param: "PrunablePlainMessageAppendix", actual: flags, expected: JSON.stringify(data) };
            }
            var serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            var sha256 = CryptoJS.algo.SHA256.create();
            var isText = [];
            if (String(data.messageIsText) == "true") {
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
                return { fail: true, param: "PrunablePlainMessageAppendix", actual: converters.byteArrayToHexString(calculatedHash), expected: serverHash };
            }
        }

        // PrunableEncryptedMessageAppendix
        if ((transaction.flags & 16) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "PrunableEncryptedMessageAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            flags = byteArray[pos];
            pos++;
            if (flags != 0) {
                return { fail: true, param: "PrunableEncryptedMessageAppendix", actual: flags, expected: JSON.stringify(data) };
            }
            serverHash = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            pos += 32;
            sha256 = CryptoJS.algo.SHA256.create();
            if (data.isText == true || data.messageToEncryptIsText == "true") {
                sha256.update(converters.byteArrayToWordArrayEx([1]));
            } else {
                sha256.update(converters.byteArrayToWordArrayEx([0]));
            }
            sha256.update(converters.byteArrayToWordArrayEx([1])); // compression
            if (data.filebytes) {
                utfBytes = new Int8Array(data.filebytes);
            } else {
                var encryptedMessageData = data.encryptedMessageData || data.data;
                utfBytes = converters.hexStringToByteArray(encryptedMessageData);
            }
            sha256.update(converters.byteArrayToWordArrayEx(utfBytes));
            var messageNonce = data.encryptedMessageNonce || data.nonce;
            sha256.update(converters.byteArrayToWordArrayEx(converters.hexStringToByteArray(messageNonce)));
            hashWords = sha256.finalize();
            calculatedHash = converters.wordArrayToByteArrayEx(hashWords);
            if (serverHash !== converters.byteArrayToHexString(calculatedHash)) {
                return { fail: true, param: "PrunableEncryptedMessageAppendix", actual: converters.byteArrayToHexString(calculatedHash), expected: serverHash };
            }
        }

        // PublicKeyAnnouncementAppendix
        if ((transaction.flags & 32) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 2) {
                return { fail: true, param: "PublicKeyAnnouncementAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            var recipientPublicKey = converters.byteArrayToHexString(byteArray.slice(pos, pos + 32));
            if (recipientPublicKey != data.recipientPublicKey) {
                return { fail: true, param: "PublicKeyAnnouncementAppendix", actual: recipientPublicKey, expected: data.recipientPublicKey };
            }
            pos += 32;
        } else if (data.recipientPublicKey) {
            return { fail: true, param: "PublicKeyAnnouncementAppendix", actual: data.recipientPublicKey, expected: "undefined" };
        }

        // PhasingAppendix
        if ((transaction.flags & 64) != 0) {
            attachmentVersion = byteArray[pos];
            if (attachmentVersion < 0 || attachmentVersion > 1) {
                return { fail: true, param: "PhasingAppendix", actual: attachmentVersion, expected: JSON.stringify(data) };
            }
            pos++;
            if (String(converters.byteArrayToSignedInt32(byteArray, pos)) !== data.phasingFinishHeight) {
                return { fail: true, param: "PhasingAppendix", actual: String(converters.byteArrayToSignedInt32(byteArray, pos)), expected: data.phasingFinishHeight };
            }
            pos += 4;
            var params = JSON.parse(data["phasingParams"]);
            result = validateCommonPhasingData(byteArray, pos, params);
            if (result.fail) {
                return result;
            } else {
                pos = result.pos;
            }
            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.TRANSACTION) {
                var linkedFullHashesLength = byteArray[pos];
                pos++;
                if (linkedFullHashesLength > 1) {
                    NRS.logConsole("currently only 1 full hash is supported");
                    return { fail: true, param: "PhasingAppendix", actual: linkedFullHashesLength, expected: 1 };
                }
                if (linkedFullHashesLength == 1) {
                    var transactionId = params.phasingLinkedTransactions[0];
                    if (transactionId.chain != String(converters.byteArrayToSignedInt32(byteArray, pos))) {
                        return { fail: true, param: "PhasingAppendixChain", actual: String(converters.byteArrayToSignedInt32(byteArray, pos)), expected: transactionId.chain };
                    }
                    pos += 4;
                    if (transactionId.transactionFullHash != converters.byteArrayToHexString(byteArray.slice(pos, pos + 32))) {
                        return { fail: true, param: "PhasingAppendixFullHash", actual: converters.byteArrayToHexString(byteArray.slice(pos, pos + 32)), expected: transactionId.transactionFullHash };
                    }
                    pos += 32;
                }
            }
            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.HASH) {
                var hashedSecretLength = byteArray[pos];
                pos++;
                if (hashedSecretLength > 0 && converters.byteArrayToHexString(byteArray.slice(pos, pos + hashedSecretLength)) !== params.phasingHashedSecret) {
                    return { fail: true, param: "PhasingAppendixSecret", actual: "", expected: params.phasingHashedSecret };
                }
                pos += hashedSecretLength;
                var algorithm = String(byteArray[pos]);
                if (algorithm !== "0" && algorithm != params.phasingHashedSecretAlgorithm) {
                    return { fail: true, param: "PhasingAppendixAlgorithm", actual: algorithm, expected:  params.phasingHashedSecretAlgorithm };
                }
            }

            if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
                result = validateCompositePhasingData(byteArray, pos, params);
                if (result.fail) {
                    return result;
                } else {
                    pos = result.pos;
                }
            }
        }
        return { pos: pos };
    };

    NRS.broadcastTransactionBytes = function (transactionBytes, callback, originalResponse, originalData) {
        var data = {
            "transactionBytes": transactionBytes,
            "prunableAttachmentJSON": JSON.stringify(originalResponse.transactionJSON.attachment),
            "adminPassword": NRS.getAdminPassword()
        };
        var requestType = NRS.state.apiProxy ? "sendTransaction": "broadcastTransaction";
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
        var result;
        if (byteArray[pos] == 0xFF) {
            // Removal of account control
            if (data.controlVotingModel != NRS.constants.VOTING_MODELS.NONE) {
                return { fail: true, param: "validateControlPhasingDataRemoval", actual: data.controlVotingModel, expected: NRS.constants.VOTING_MODELS.NONE};
            }
            params = { phasingVotingModel: "-1", phasingQuorum: "0", phasingMinBalance: "0" }; // The server puts these bytes as control params so make sure they are there
            result = validateCommonPhasingData(byteArray, pos, params);
            if (result.fail) {
                return result;
            } else {
                pos = result.pos;
            }
        } else {
            params = JSON.parse(data["controlParams"]);
            result = validateCommonPhasingData(byteArray, pos, params);
            if (result.fail) {
                return result;
            } else {
                pos = result.pos;
            }
        }
        if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
            result = validateCompositePhasingData(byteArray, pos, params);
            if (result.fail) {
                return result;
            } else {
                pos = result.pos;
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
                    return { fail: true, param: "validateControlPhasingControlMaxFees", actual: controlMaxFees, expected: data.controlMaxFees };
                }
            }
            var minDuration = converters.byteArrayToSignedShort(byteArray, pos);
            pos += 2;
            if (data.controlMinDuration && minDuration != data.controlMinDuration || !data.controlMinDuration && minDuration != 0) {
                return { fail: true, param: "validateControlPhasingControlMinDuration", actual: minDuration, expected: data.controlMinDuration };
            }
            var maxDuration = converters.byteArrayToSignedShort(byteArray, pos);
            pos += 2;
            if (data.controlMaxDuration && maxDuration != data.controlMaxDuration || !data.controlMaxDuration && maxDuration != 0) {
                return { fail: true, param: "validateControlPhasingControlMaxDuration", actual: minDuration, expected: data.controlMaxDuration };
            }
        }
        return { pos: pos };
    }

    function validateCommonPhasingData(byteArray, pos, params) {
        if (byteArray[pos] != (parseInt(params["phasingVotingModel"]) & 0xFF)) {
            return { fail: true, param: "validateCommonPhasingData", actual: byteArray[pos], expected: parseInt(params["phasingVotingModel"]) & 0xFF };
        }
        pos++;
        var quorum = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (quorum !== "0" && quorum !== String(params["phasingQuorum"])) { // TODO improve this validation
            return { fail: true, param: "validateCommonPhasingDataQuorum", actual: quorum, expected: String(params["phasingQuorum"]) };
        }
        pos += 8;
        var minBalance = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (minBalance !== "0" && minBalance !== params["phasingMinBalance"]) { // TODO improve this validation
            return { fail: true, param: "validateCommonPhasingDataMinBalance", actual: minBalance, expected: params["phasingMinBalance"] };
        }
        pos += 8;
        var whiteListLength = byteArray[pos];
        pos++;
        for (var i = 0; i < whiteListLength; i++) {
            var accountId = converters.byteArrayToBigInteger(byteArray, pos);
            var accountRS = NRS.convertNumericToRSAccountFormat(accountId);
            pos += 8;
            if (String(accountId) !== params["phasingWhitelist"][i] && String(accountRS) !== params["phasingWhitelist"][i]) {
                return { fail: true, param: "validateCommonPhasingDataAccount", actual: accountId + " or " + accountRS, expected: params["phasingWhitelist"][i]};
            }
        }
        var holdingId = String(converters.byteArrayToBigInteger(byteArray, pos));
        if (holdingId !== "0" && holdingId !== params["phasingHolding"]) { // TODO improve this validation
            return { fail: true, param: "validateCommonPhasingDataHolding", actual: holdingId, expected: params["phasingHolding"]};
        }
        pos += 8;
        var minBalanceModel = String(byteArray[pos]);
        if (minBalanceModel !== "0" && minBalanceModel !== String(params["phasingMinBalanceModel"])) {
            return { fail: true, param: "validateCommonPhasingDataMinBalanceModel", actual: minBalanceModel, expected: params["phasingMinBalanceModel"]};
        }
        pos++;

        if (params.phasingVotingModel == NRS.constants.VOTING_MODELS.PROPERTY) {
            if (params.phasingSenderProperty === undefined) {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== "0") {
                    return { fail: true, param: "validateCommonPhasingDataNoSenderProperty1", actual: "", expected: ""};
                }
                pos += 8;
                if (byteArray[pos] !== 0) {
                    return { fail: true, param: "validateCommonPhasingDataNoSenderProperty2", actual: "", expected: ""};
                }
                pos++;
                if (byteArray[pos] !== 0) {
                    return { fail: true, param: "validateCommonPhasingDataNoSenderProperty3", actual: "", expected: ""};
                }
                pos++;
            } else {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== params.phasingSenderProperty.setter) {
                    return { fail: true, param: "validateCommonPhasingDataSenderProperty", actual: String(converters.byteArrayToBigInteger(byteArray, pos)), expected: params.phasingSenderProperty.setter };
                }
                pos += 8;
                var length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingSenderProperty.name) {
                    return { fail: true, param: "validateCommonPhasingDataSenderPropertyName", actual: converters.byteArrayToString(byteArray, pos, length), expected: params.phasingSenderProperty.name };
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (params.phasingSenderProperty.value === undefined) {
                    if (length !== 0) {
                        return { fail: true, param: "validateCommonPhasingDataSenderPropertyNoValue", actual: "", expected: "" };
                    }
                } else {
                    if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingSenderProperty.value) {
                        return { fail: true, param: "validateCommonPhasingDataSenderPropertyValue", actual: converters.byteArrayToString(byteArray, pos, length), expected: params.phasingSenderProperty.value };
                    }
                }
                pos += length;
            }
            if (params.phasingRecipientProperty === undefined) {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== "0") {
                    return { fail: true, param: "validateCommonPhasingDataNoRecipientProperty1", actual: "", expected: ""};
                }
                pos += 8;
                if (byteArray[pos] !== 0) {
                    return { fail: true, param: "validateCommonPhasingDataNoRecipientProperty2", actual: "", expected: ""};
                }
                pos++;
                if (byteArray[pos] !== 0) {
                    return { fail: true, param: "validateCommonPhasingDataNoRecipientProperty3", actual: "", expected: ""};
                }
                pos++;
            } else {
                if (String(converters.byteArrayToBigInteger(byteArray, pos)) !== params.phasingRecipientProperty.setter) {
                    return { fail: true, param: "validateCommonPhasingDataRecipientProperty", actual: String(converters.byteArrayToBigInteger(byteArray, pos)), expected: params.phasingRecipientProperty.setter };
                }
                pos += 8;
                length = byteArray[pos];
                pos++;
                if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingRecipientProperty.name) {
                    return { fail: true, param: "validateCommonPhasingDataRecipientPropertyName", actual: converters.byteArrayToString(byteArray, pos, length), expected: params.phasingRecipientProperty.name };
                }
                pos += length;
                length = byteArray[pos];
                pos++;
                if (params.phasingRecipientProperty.value === undefined) {
                    if (length !== 0) {
                        return { fail: true, param: "validateCommonPhasingDataRecipientPropertyNoValue", actual: "", expected: "" };
                    }
                } else {
                    if (converters.byteArrayToString(byteArray, pos, length) !== params.phasingRecipientProperty.value) {
                        return { fail: true, param: "validateCommonPhasingDataRecipientPropertyValue", actual: converters.byteArrayToString(byteArray, pos, length), expected: params.phasingRecipientProperty.value };
                    }
                }
                pos += length;
            }
        }

        return { pos: pos };
    }

    function validateCompositePhasingData(byteArray, pos, params) {
        var length = converters.byteArrayToSignedShort(byteArray, pos);
        pos += 2;
        var expression = converters.byteArrayToString(byteArray, pos, length);
        if (params.phasingExpression != expression) {
            return { fail: true, param: "validateCompositePhasingDataExpression", actual: expression, expected: params.phasingExpression };
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
                return { fail: true, param: "validateCompositePhasingDataNoPollName", actual: "", expected: subPollName };
            }
            var result = validateCommonPhasingData(byteArray, pos, params.phasingSubPolls[subPollName]);
            if (result.fail) {
                return result;
            } else {
                pos = result.pos;
            }
        }
        return { pos: pos };
    }

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}
