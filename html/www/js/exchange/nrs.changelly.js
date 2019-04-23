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
var NRS = (function(NRS, $) {
    var EXCHANGE_NAME = "changelly";
    var TRANSACTIONS_KEY = "changelly.transactions";
    var SUPPORTED_COINS = {};

    var apiCall = function (method, params, doneCallback, ignoreError, $modal) {
        var postData = {};
        postData.method = method;
        postData.jsonrpc = "2.0";
        postData.params = params;
        postData.id = "" + Math.random();
        var hmac = CryptoJS.algo.HMAC.create(CryptoJS.algo.SHA512, NRS.settings.changelly_api_secret);
        hmac.update(JSON.stringify(postData));
        var signature = hmac.finalize();
        NRS.logConsole("changelly api call method: " + method + " post data: " + JSON.stringify(postData) + " api-key: " + NRS.settings.changelly_api_key + " signature:" + signature +
            (ignoreError ? " ignore " + ignoreError : "") + ($modal ? " modal " + $modal : ""));
        $.ajax({
            url: NRS.getChangellyUrl(),
            beforeSend: function(xhr) {
                xhr.setRequestHeader("api-key", NRS.settings.changelly_api_key);
                xhr.setRequestHeader("sign", signature);
                xhr.setRequestHeader("Content-Type", "application/json; charset=utf-8");
            },
            crossDomain: true,
            dataType: "json",
            type: "POST",
            timeout: 30000,
            async: true,
            data: JSON.stringify(postData)
        }).done(function(response, status) {
            if (status !== "success") {
                var statusMsg = method + ' status ' + status;
                NRS.logConsole(statusMsg);
                if ($modal) {
                    NRS.showModalError(statusMsg, $modal);
                    $modal.css('cursor', 'default');
                } else {
                    $.growl(statusMsg);
                }
            }
            if (response.error) {
                var error = response.error;
                var msg;
                if (error.code) {
                    msg = ' code ' + error.code + ' message ' + error.message;
                    NRS.logConsole(method + msg + " params:" + JSON.stringify(params));
                } else {
                    msg = error;
                    NRS.logConsole(method + ' error ' + error);
                }
                if ($modal) {
                    NRS.showModalError(msg, $modal);
                    $modal.css('cursor', 'default');
                } else {
                    $.growl(msg);
                }
                if (ignoreError === false) {
                    return;
                }
            }
            doneCallback(response);
        }).fail(function (xhr, textStatus, error) {
            var message = "Request failed, method " + method + " status " + textStatus + " error " + error;
            if ($modal) {
                NRS.showModalError(message, $modal);
                $modal.css('cursor', 'default');
            } else {
                $.growl(message);
            }
            NRS.logConsole(message);
            throw message;
        })
    };

    var renderExchangeTable = function (op) {
        var table = $("#p_changelly_" + op + "_nxt");
        table.find("tbody").empty();
        table.parent().addClass("data-loading").removeClass("data-empty");
        // We execute the getMinAmount and getExchangeAmount APIs one after the other, then render the results provided
        // by both into a single table. Note that both APIs accept multiple trading pairs.
        async.waterfall([
            function (callback) {
                var pairs = [];
                var coins = NRS.getCoins(EXCHANGE_NAME);
                for (var i = 0; i < coins.length; i++) {
                    var from, to;
                    if (op == "buy") {
                        from = NRS.getActiveChainName();
                        to = coins[i];
                    } else {
                        from = coins[i];
                        to = NRS.getActiveChainName();
                    }
                    pairs.push({from: from, to: to})
                }
                apiCall("getMinAmount", pairs, function (response) {
                    callback(response.error, response);
                })
            },
            function (getMinAmountResponse, callback) {
                var pairs = [];
                var results = getMinAmountResponse.result;
                for (var i = 0; i < results.length; i++) {
                    // In the absence of better data we set the expected amount to "1" coin
                    // Once the amount is known in the modals we provide more accurate rate calculation
                    pairs.push({from: results[i].from, to: results[i].to, amount: results[i].minAmount})
                }
                apiCall("getExchangeAmount", pairs, function (getExchangeAmountResponse) {
                    callback(null, getExchangeAmountResponse.result);
                })
            }
        ], function (err, exchangeAmountResults) {
            if (err) {
                callback(err, err);
                return;
            }
            var rows = "";
            for (var i = 0; i < exchangeAmountResults.length; i++) {
                var result = exchangeAmountResults[i];
                result.from = result.from.toUpperCase();
                result.to = result.to.toUpperCase();
                var rate = new Big(result.result).div(new Big(result.amount)).toFixed(8);
                var symbol;
                if (op === "sell") {
                    rate = NRS.getInverse(rate);
                    symbol = result.from;
                } else {
                    rate = new Big(rate).toFixed(8);
                    symbol = result.to;
                }
                rows += "<tr><td>" + symbol + "</td>";
                rows += "<td><span>" + String(result.amount).escapeHTML() + "</span>&nbsp[<span>" + result.from + "</span>]</td>";
                rows += "<td>" + String(rate).escapeHTML() + "</td>";
                var opText = ($.t("buy") + " " + result.to);
                rows += "<td><a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#changelly_" + op + "_modal' " +
                    "data-from='" + result.from + "' data-to='" + result.to + "' data-rate='" + rate + "' data-min='" + result.amount + "'>" + opText + "</a>";
            }
            NRS.logConsole(rows);
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        })
    };

    var renderMyExchangesTable = function () {
        var transactionsJSON = localStorage[TRANSACTIONS_KEY];
        var transactions = [];
        if (transactionsJSON) {
            transactions = JSON.parse(transactionsJSON);
        }
        var tasks = [];
        for (var i = 0; i < transactions.length; i++) {
            tasks.push((function (i) {
                return function (callback) {
                    apiCall("getTransactions", {id: transactions[i].id}, function(response) {
                        NRS.logConsole("my exchanges iteration " + i + " transaction id " + transactions[i].id);
                        var rows = "";
                        for (var j=0; j < response.result.length; j++) {
                            var transaction = response.result[j];
                            var row = "";
                            row += "<tr>";
                            var date = parseInt(transaction.createdAt) * 1000;
                            row += "<td>" + NRS.formatTimestamp(date, false, true) + "</td>";
                            row += "<td>" + transaction.status + "</td>";
                            row += "<td>" + NRS.getExchangeAddressLink(transaction.payinAddress, transaction.currencyFrom) + "</td>";
                            row += "<td>" + transaction.amountFrom + "</td>";
                            row += "<td>" + transaction.currencyFrom + "</td>";
                            row += "<td>" + NRS.getExchangeAddressLink(transaction.payoutAddress, transaction.currencyTo) + "</td>";
                            row += "<td>" + transaction.amountTo + "</td>";
                            row += "<td>" + transaction.currencyTo + "</td>";
                            var transactionLink;
                            if (transaction.payoutHash) {
                                transactionLink = NRS.getExchangeTransactionLink(transaction.payoutHash, transaction.currencyTo);
                            } else {
                                transactionLink = "N/A";
                            }
                            row += "<td>" + transactionLink + "</td>";
                            row += "<td><a href='#' data-toggle='modal' data-target='#changelly_view_transaction' " +
                                "data-id='" + transaction.id + "' data-content='" + JSON.stringify(transaction) + "'>" + String(transaction.id).escapeHTML() + "</a></td>";
                            var code = "NRS.removeChangllyTransaction(\"" + transaction.id + "\"); return false;";
                            row += "<td><a href='#' onclick='" + code + "'>x</a></td>";
                            NRS.logConsole(row);
                            rows += row;
                        }
                        callback(null, rows);
                    }, true);
                }
            })(i));
        }
        NRS.logConsole(tasks.length + " tasks ready to run");
        var table = $("#p_changelly_my_table");
        if (tasks.length === 0) {
            table.find("tbody").empty();
            NRS.dataLoadFinished(table);
            return;
        }
        async.series(tasks, function (err, results) {
            if (err) {
                NRS.logConsole("Err: ", err, "\nResults:", results);
                table.find("tbody").empty();
                NRS.dataLoadFinished(table);
                return;
            }
            NRS.logConsole("results", results);
            var rows = "";
            for (i = 0; i < results.length; i++) {
                rows += results[i];
            }
            NRS.logConsole("rows " + rows);
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        });
    };

    function loadCoins() {
        var coin0 = EXCHANGE_NAME + "_coin0";
        var coin1 = EXCHANGE_NAME + "_coin1";
        var coin2 = EXCHANGE_NAME + "_coin2";
        var inputFields = [];
        inputFields.push($('#' + coin0));
        inputFields.push($('#' + coin1));
        inputFields.push($('#' + coin2));
        var selectedCoins = [];
        selectedCoins.push(NRS.settings[coin0]);
        selectedCoins.push(NRS.settings[coin1]);
        selectedCoins.push(NRS.settings[coin2]);
        NRS.changellySelectCoins(inputFields, selectedCoins);
    }

    NRS.changellySelectCoins = function(inputFields, selectedCoins) {
        var excludedCoins = ["ARDR", "IGNIS"];
        apiCall('getCurrencies', {}, function (data) {
            SUPPORTED_COINS = data.result;
            for (var i = 0; i < inputFields.length; i++) {
                inputFields[i].empty();
                var isSelectionAvailable = false;
                for (var j = 0; j < data.result.length; j++) {
                    var code = String(data.result[j]).toUpperCase();
                    if (code !== NRS.getActiveChainName() && excludedCoins.indexOf(code) === -1) {
                        inputFields[i].append('<option value="' + code + '">' + code + '</option>');
                        SUPPORTED_COINS[code] = code;
                    }
                    if (selectedCoins[i] === code) {
                        isSelectionAvailable = true;
                    }
                }
                if (isSelectionAvailable) {
                    inputFields[i].val(selectedCoins[i]);
                }
            }
            $('#changelly_status').html('ok');
        });
    };

    NRS.pages.exchange_changelly = function() {
        var exchangeDisabled = $(".exchange_disabled");
        var exchangePageHeader = $(".exchange_page_header");
        var exchangePageContent = $(".exchange_page_content");
        if (NRS.settings.exchange !== "1") {
            exchangeDisabled.show();
            exchangePageHeader.hide();
            exchangePageContent.hide();
            return;
        }
        exchangeDisabled.hide();
        exchangePageHeader.show();
        exchangePageContent.show();
        NRS.pageLoading();
        $("#changelly_exchange_from_crypto_header").text($.t("exchange_crypto", { from: "Crypto", to: NRS.getActiveChainName() }));
        $("#changelly_exchange_to_crypto_header").text($.t("exchange_crypto", { from: NRS.getActiveChainName(), to: "Crypto" }));
        loadCoins();
        renderExchangeTable("buy");
        renderExchangeTable("sell");
        renderMyExchangesTable();
        NRS.pageLoaded();
        setTimeout(refreshPage, 60000);
    };

    var refreshPage = function() {
        if (NRS.currentPage === "exchange_changelly") {
            NRS.pages.exchange_changelly();
        }
    };

    $("#changelly_accept_exchange_link").on("click", function(e) {
   		e.preventDefault();
   		NRS.updateSettings("exchange", "1");
        NRS.pages.exchange_changelly();
   	});

    $("#changelly_clear_my_exchanges").on("click", function(e) {
   		e.preventDefault();
   		localStorage.removeItem(TRANSACTIONS_KEY);
        renderMyExchangesTable();
   	});

    $('.coin-select.changelly').change(function() {
        var id = $(this).attr('id');
        var coins = NRS.getCoins(EXCHANGE_NAME);
        coins[parseInt(id.slice(-1))] = $(this).val();
        NRS.setCoins(EXCHANGE_NAME, coins);
        renderExchangeTable('buy');
        renderExchangeTable('sell');
    });

	NRS.setup.exchange = function() {
        // Do not implement connection to a 3rd party site here to prevent privacy leak
    };

    $("#changelly_buy_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var from = invoker.data("from");
        var to = invoker.data("to");
        $("#changelly_buy_from").val(from);
        $("#changelly_buy_to").val(to);
        NRS.logConsole("modal invoked from " + from + " to " + to);
        $("#changelly_buy_title").html($.t("exchange_crypto", { from: from, to: to }));
        $("#changelly_buy_min").val(invoker.data("min"));
        $("#changelly_buy_min_coin").html(NRS.getActiveChainName());
        $("#changelly_buy_rate").val(new Big(invoker.data("rate")).toFixed(8));
        $("#changelly_buy_rate_text").html(to + " " + $.t("per") + " " + NRS.getActiveChainName());
        $('#changelly_buy_estimated_amount').val("");
        $("#changelly_buy_estimated_amount_text").html(to);
        $("#changelly_withdrawal_address_coin").html(to);
    });

    $("#changelly_buy_submit").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var amountNXT = $("#changelly_buy_amount").val();
        var minAmount = $("#changelly_buy_min").val();
        if (parseFloat(amountNXT) <= parseFloat(minAmount)) {
            var msg = "amount is lower tham minimum amount " + minAmount;
            NRS.logConsole(msg);
            NRS.showModalError(msg, $modal);
            return;
        }
        var amountNQT = NRS.convertToNQT(amountNXT);
        var withdrawal = $("#changelly_buy_withdrawal_address").val();
        var from = $("#changelly_buy_from").val();
        var to = $("#changelly_buy_to").val();
        NRS.logConsole('changelly withdrawal to address ' + withdrawal + " coin " + to);
        apiCall('createTransaction', {
            from: from,
            to: to,
            address: withdrawal,
            amount: amountNXT
        }, function (data) {
            var msg;
            if (data.error) {
                NRS.logConsole("Changelly createTransaction error " + data.error.code + " " + data.error.message);
                return;
            }
            var depositAddress = data.result.payinAddress;
            if (!depositAddress) {
                msg = "changelly did not return a deposit address for id " + data.result.id;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            addTransaction(data.result.id);
            NRS.logConsole(NRS.getActiveChainName() + " deposit address " + depositAddress);
            NRS.sendRequest("sendMoney", {
                "recipient": depositAddress,
                "amountNQT": amountNQT,
                "secretPhrase": $("#changelly_buy_password").val(),
                "feeNXT": $("#changelly_buy_fee").val(),
                "deadline": "15"
            }, function (response) {
                if (response.errorCode) {
                    NRS.logConsole("sendMoney response " + response.errorCode + " " + response.errorDescription.escapeHTML());
                    NRS.showModalError(NRS.translateServerError(response), $modal);
                    return;
                }
                renderMyExchangesTable();
                $("#changelly_buy_passpharse").val("");
                NRS.unlockForm($modal, $btn, true);
            })
        }, true, $modal);
    });

    $('#changelly_buy_amount').change(function () {
        var $modal = $(this).closest(".modal");
        var amount = $('#changelly_buy_amount').val();
        var from = $('#changelly_buy_from').val();
        var to = $('#changelly_buy_to').val();
        var $estimatedAmount = $('#changelly_buy_estimated_amount');
        if (!amount) {
            $estimatedAmount.val("");
            return;
        }
        var coinAmount = new Big(amount);
        var minAmount = new Big($("#changelly_buy_min").val());
        if (coinAmount.lt(minAmount)) {
            $modal.find(".error_message").html($.t("error_amount_too_low")).show();
            $modal.css('cursor', 'default');
            return;
        }
        $modal.find(".error_message").html("").hide();
        $modal.css('cursor', 'wait');
        apiCall('getExchangeAmount', {
            amount: amount,
            from: from,
            to: to
        }, function (response) {
            if (response.error) {
                $estimatedAmount.val("");
                $modal.css('cursor', 'default');
                return;
            }
            $estimatedAmount.val(new Big(response.result).toFixed(8));
            $modal.css('cursor', 'default');
        })
    });

    $("#changelly_sell_modal").on("show.bs.modal", function (e) {
        var invoker = $(e.relatedTarget);
        var from = invoker.data("from");
        var to = invoker.data("to");
        var rate = invoker.data("rate");
        var min = invoker.data("min");
        NRS.logConsole("sell modal exchange from " + from + " to " + to);
        $("#changelly_sell_title").html($.t("exchange_crypto", { from: from, to: to }));
        $("#changelly_sell_qr_code").html("");
        if (min && rate) {
            $("#changelly_sell_min").val(min);
            $("#changelly_sell_rate").val(rate);
        } else {
            apiCall("getMinAmount", { from: from, to: to }, function (getMinAmountResponse) {
                $("#changelly_sell_min").val(getMinAmountResponse.result);
                apiCall("getExchangeAmount", { from: from, to: to, amount: getMinAmountResponse.result }, function (response) {
                    $("#changelly_sell_rate").val(NRS.getInverse(new Big(response.result).div(new Big(getMinAmountResponse.result)).toFixed(8)));
                })
            })
        }
        $("#changelly_sell_min_coin").html(from);
        $("#changelly_sell_rate_text").html(from + " " + $.t("per") + " " + NRS.getActiveChainName());
        $("#changelly_sell_amount_text").html(from);
        $("#changelly_sell_estimated_amount").val("");
        $("#changelly_sell_from").val(from);
        $("#changelly_sell_to").val(to);
    });

    $('#changelly_sell_amount').change(function () {
        var $modal = $(this).closest(".modal");
        var amount = $('#changelly_sell_amount').val();
        var from = $('#changelly_sell_from').val();
        var to = $('#changelly_sell_to').val();
        var $estimatedAmount = $('#changelly_sell_estimated_amount');
        var depositAddress = $("#changelly_sell_deposit_address").html();
        if (!amount) {
            $estimatedAmount.val("");
            NRS.generateQRCode("#changelly_sell_qr_code", depositAddress);
            return;
        }
        var coinAmount = new Big(amount);
        var minAmount = new Big($("#changelly_sell_min").val());
        if (!minAmount) {
            $modal.find(".error_message").html($.t("unknown_min_amount")).show();
            $estimatedAmount.val("");
            $modal.css('cursor', 'default');
            return;
        }
        if (coinAmount.lt(minAmount)) {
            $modal.find(".error_message").html($.t("error_amount_too_low")).show();
            $estimatedAmount.val("");
            $modal.css('cursor', 'default');
            return;
        }
        $modal.find(".error_message").html("").hide();
        $modal.css('cursor', 'wait');
        var publicKey = NRS.publicKey;
        if (publicKey === "" && NRS.accountInfo) {
            publicKey = NRS.accountInfo.publicKey;
        }
        if (!publicKey || publicKey === "") {
            NRS.showModalError("Account has no public key, please login using your passphrase", modal);
            return;
        }
        apiCall('createTransaction', {
            from: from,
            to: to,
            address: NRS.accountRS,
            amount: amount,
            extraId: publicKey
        }, function (data) {
            $modal.css('cursor', 'default');
            var msg;
            if (data.error) {
                msg = "Changelly createTransaction error " + data.error.code + " " + data.error.message;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            var depositAddress = data.result.payinAddress;
            if (!depositAddress) {
                msg = "changelly did not return a deposit address for id " + data.result.id;
                NRS.logConsole(msg);
                NRS.showModalError(msg, $modal);
                return;
            }
            NRS.logConsole(from + " deposit address " + depositAddress);
            $("#changelly_sell_deposit_address").html(depositAddress);
            addTransaction(data.result.id);
            apiCall('getExchangeAmount', {
                amount: amount,
                from: from,
                to: to
            }, function (response) {
                if (response.error) {
                    $estimatedAmount.val("");
                    NRS.generateQRCode("#changelly_sell_qr_code", depositAddress);
                    $modal.css('cursor', 'default');
                    return;
                }
                $estimatedAmount.val(response.result);
                NRS.generateQRCode("#changelly_sell_qr_code", "bitcoin:" + depositAddress + "?amount=" + amount);
                $modal.css('cursor', 'default');
            });
        })
    });

    $("#changelly_sell_done").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var deposit = $("#changelly_sell_deposit_address").html();
        if (deposit !== "") {
            renderMyExchangesTable();
            NRS.unlockForm($modal, $btn, true);
        }
    });

    var $viewTransactionModal = $("#changelly_view_transaction");
    $viewTransactionModal.on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var id = $invoker.data("id");
        var content = $invoker.data("content");
        $("#changelly_identifier").val(id);
        var viewContent = $("#changelly_view_content");
        viewContent.html(JSON.stringify(content, null, 2));
        hljs.highlightBlock(viewContent[0]);
    });

    $viewTransactionModal.on('hidden.bs.modal', function () {
        $("#changelly_search").prop("disabled", false);
    });

    $("#changelly_search").on("click", function(e) {
        e.preventDefault();
        $("#changelly_search").prop("disabled", true);
        var key = $("#changelly_search_key").val();
        var id = $("#changelly_search_id").val();
        var params = {};
        params[key] = id;
        apiCall("getTransactions", params, function(response) {
            $(this).data("id", id);
            $(this).data("content", response);
            $("#changelly_view_transaction").modal({}, $(this));
        });
    });

    $("#changelly_scratchpad_link").on("click", function(e) {
        e.preventDefault();
        $("#changelly_scratchpad").modal({}, $(this));
    });

    $("#changelly_submit").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        $modal.find(".error_message").html("").hide();
        var viewContent = $("#changelly_response");
        viewContent.html("");
        var api = $("#changelly_api").val();
        var paramsText = $("#changelly_parameters").val();
        if (paramsText !== "") {
            try {
                var params = JSON.parse(paramsText);
            } catch(e) {
                NRS.showModalError("Invalid JSON " + e.message, $modal);
                return;
            }
        }
        apiCall(api, params, function(response) {
            viewContent.html(JSON.stringify(response, null, 2));
            hljs.highlightBlock(viewContent[0]);
        }, false, $modal);
    });

    NRS.getFundAccountLink = function() {
        return "<div class='callout callout-danger'>" +
            "<span>" + $.t("fund_account_warning_11") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_2") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_3") + "</span><br>" +
            "</div>" +
            "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#changelly_sell_modal' " +
            "data-from='BTC' data-to=" + NRS.getActiveChainName() + ">" + $.t("fund_account_message2", { coin: NRS.getActiveChainName()}) + "</a>";
    };

    function addTransaction(id) {
        var json = localStorage[TRANSACTIONS_KEY];
        var transactions;
        if (json === undefined) {
            transactions = [];
        } else {
            transactions = JSON.parse(json);
            if (transactions.length > 10) {
                transactions.splice(10, transactions.length - 10);
            }
        }
        var item = { id: id };
        for (var i=0; i < transactions.length; i++) {
            if (item.id === transactions[i].id) {
                NRS.logConsole("transaction id " + item.id + " already exists");
                return;
            }
        }
        transactions.splice(0, 0, item);
        NRS.logConsole("transaction id " + id + " added");
        localStorage[TRANSACTIONS_KEY] = JSON.stringify(transactions);
    }

    NRS.removeChangllyTransaction = function(id) {
        var json = localStorage[TRANSACTIONS_KEY];
        if (json === undefined) {
            return;
        }
        var transactions = JSON.parse(json);
        for (var i=0; i < transactions.length; i++) {
            if (id === transactions[i].id) {
                NRS.logConsole("remove transaction id " + id);
                transactions.splice(i, 1);
                $('#p_changelly_my_table tr').eq(i+1).remove();
                break;
            }
        }
        localStorage[TRANSACTIONS_KEY] = JSON.stringify(transactions);
    };

    return NRS;
}(NRS || {}, jQuery));