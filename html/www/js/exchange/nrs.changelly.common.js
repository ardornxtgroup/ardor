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

// Helper methods for changelly-like exchange integrations (Changelly, ChangeHero...)

/**
 * @depends {nrs.js}
 */
var NRS = (function(NRS, $) {

    NRS.changelly = {};

    function showGrowlOrModalError($modal, message) {
        if ($modal) {
            NRS.showModalError(message, $modal);
            $modal.css('cursor', 'default');
        } else {
            $.growl(message);
        }
    }

    /**
     * Generates a function for API calls to a Changelly-compatible platform.
     *
     * @param exchange the exchange name as it's present on DOM id labels and other keys
     * @returns {Function} a funtion to make calls to the exchange API
     */
    NRS.changelly.generateApiCall = function (exchange) {
        return function (method, params, doneCallback, ignoreError, $modal) {
            const postData = JSON.stringify({
                method: method,
                jsonrpc: "2.0",
                params: params,
                id: "" + Math.random()
            });
            const hmac = CryptoJS.algo.HMAC.create(CryptoJS.algo.SHA512, NRS.settings[exchange + '_api_secret']);
            hmac.update(postData);
            const signature = hmac.finalize();
            const apiKey = NRS.settings[`${exchange}_api_key`];
            const apiUrl = NRS.settings[`${exchange}_url`];
            NRS.logConsole(`Exchange api call method: ${method} post data: ${postData} api-key: ${apiKey} ` +
                `signature:${signature}${ignoreError ? " ignore " + ignoreError : ""}${$modal ? " modal " + $modal : ""}`);
            $.ajax({
                url: apiUrl,
                beforeSend: xhr => {
                    xhr.setRequestHeader("api-key", apiKey);
                    xhr.setRequestHeader("sign", signature);
                    xhr.setRequestHeader("Content-Type", "application/json; charset=utf-8");
                },
                crossDomain: true,
                dataType: "json",
                type: "POST",
                timeout: 30000,
                async: true,
                data: postData
            }).done(function(response, status) {
                if (status !== "success") {
                    const statusMsg = `${method} status ${status}`;
                    NRS.logConsole(statusMsg);
                    showGrowlOrModalError($modal, statusMsg);
                }
                if (response.error) {
                    const error = response.error;
                    let msg;
                    if (error.code) {
                        msg = ` code ${error.code} message ${error.message}`;
                        NRS.logConsole(`${method + msg} params:${JSON.stringify(params)}`);
                    } else {
                        msg = error;
                        NRS.logConsole(`${method} error ${error}`);
                    }
                    showGrowlOrModalError($modal, msg);
                    if (ignoreError === false) {
                        return;
                    }
                }
                doneCallback(response);
            }).fail(function (xhr, textStatus, error) {
                const message = `Request failed, method ${method} status ${textStatus} error ${error}`;
                showGrowlOrModalError($modal, message);
                NRS.logConsole(message);
                throw message;
            });
        }
    };

    /**
     * Initializes the exchange page. Mostly for changelly compatible exchanges.
     *
     * @param exchange the exchange name as it's present on DOM id labels and other keys
     * @returns {boolean} whether the initialization went ok
     */
    NRS.changelly.exchangePageInit = function (exchange) {
        if (NRS.settings.exchange === "1") {
            $(".exchange_disabled").hide();
            $(".exchange_page_header").show();
            $(".exchange_page_content").show();
            NRS.pageLoading();
            $(`#${exchange}_exchange_from_crypto_header`).text($.t("exchange_crypto", {
                from: "Crypto",
                to: NRS.getActiveChainName()
            }));
            $(`#${exchange}_exchange_to_crypto_header`).text($.t("exchange_crypto", {
                from: NRS.getActiveChainName(),
                to: "Crypto"
            }));
            return true;
        } else {
            $(".exchange_disabled").show();
            $(".exchange_page_header").hide();
            $(".exchange_page_content").hide();
            return false;
        }
    };

    /**
     * Generate a callback function for the getCurrencies request (changelly format). The function populates the
     * coin selects.
     *
     * @param exchange the exchange name as it's present on DOM id labels and other keys
     * @param apiCall the method to send requests to the exchange API
     * @param excludedCoins the list of coins to exclude from the selects (our coins mostly)
     * @returns {Function} a callback function for the getCurrencies request
     */
    NRS.changelly.getGetCurrenciesCallback = function (exchange, apiCall, excludedCoins) {
        const coins =  Array.from({length:3}, (v,i) => `${exchange}_coin${i}`);
        return function (data) {
            const inputFields = coins.map(coin => $('#' + coin));

            if (data.result.includes(NRS.getActiveChainName().toLowerCase())) {
                $(`#p_${exchange}_buy_nxt,#p_${exchange}_sell_nxt`).parents('div.box').show();
                inputFields[0].parent().show();
                const selectedCoins = coins.map(coin => NRS.settings[coin]);

                for (let i = 0; i < inputFields.length; i++) {
                    inputFields[i].empty();
                    let isSelectionAvailable = false;
                    for (let j = 0; j < data.result.length; j++) {
                        const code = String(data.result[j]).toUpperCase();
                        if (code !== NRS.getActiveChainName() && excludedCoins.indexOf(code) === -1) {
                            inputFields[i].append(`<option value="${code}">${code}</option>`);
                        }
                        if (selectedCoins[i] === code) {
                            isSelectionAvailable = true;
                        }
                    }
                    if (isSelectionAvailable) {
                        inputFields[i].val(selectedCoins[i]);
                    }
                }
                $(`#${exchange}_status`).html('ok');
                NRS.changelly.renderExchangeTable(exchange, 'buy', apiCall);
                NRS.changelly.renderExchangeTable(exchange, 'sell', apiCall);
            } else {
                $(`#p_${exchange}_buy_nxt,#p_${exchange}_sell_nxt`).parents('div.box').hide();
                inputFields[0].parent().hide();
                $(`#${exchange}_status`).html($.t('external_exchange_currency_not_found', {currency: NRS.getActiveChainName()}));
            }
        };
    };

    NRS.changelly.renderExchangeTable = function (exchange, op, apiCall) {
        const table = $(`#p_${exchange}_${op}_nxt`);
        table.find("tbody").empty();
        table.parent().addClass("data-loading").removeClass("data-empty");
        // We execute the getMinAmount and getExchangeAmount APIs one after the other, then render the results provided
        // by both into a single table. Note that both APIs accept multiple trading pairs.
        async.waterfall([
            function (callback) {
                const pairs = [];
                const coins = NRS.getCoins(exchange);
                for (let i = 0; i < coins.length; i++) {
                    if (op === 'buy') {
                        pairs.push({from: NRS.getActiveChainName(), to: coins[i]})
                    } else {
                        pairs.push({from: coins[i], to: NRS.getActiveChainName()})
                    }
                }
                apiCall('getMinAmount', pairs, response => callback(response.error, response));
            },
            function (getMinAmountResponse, callback) {
                // In the absence of better data we set the expected amount to "1" coin
                // Once the amount is known in the modals we provide more accurate rate calculation
                const pairs = getMinAmountResponse.result.map(result => ({
                    from  : result.from,
                    to    : result.to,
                    amount: result.minAmount
                }));
                apiCall('getExchangeAmount', pairs, response => callback(null, response.result));
            }
        ], function (err, exchangeAmountResults) {
            if (err) {
                $.growl(err);
                return;
            }
            let rows = "";
            for (let i = 0; i < exchangeAmountResults.length; i++) {
                const result = exchangeAmountResults[i];
                result.from = result.from.toUpperCase();
                result.to = result.to.toUpperCase();
                let rate = new Big(result.result).div(new Big(result.amount)).toFixed(8);
                let symbol;
                if (op === 'sell') {
                    rate = NRS.getInverse(rate);
                    symbol = result.from;
                } else {
                    rate = new Big(rate).toFixed(8);
                    symbol = result.to;
                }
                rows += `<tr>
                         <td>${symbol}</td>
                         <td><span>${String(result.amount).escapeHTML()}</span>&nbsp[<span>${result.from}</span>]</td>
                         <td>${String(rate).escapeHTML()}</td>
                         <td><a href='#' class='btn btn-xs btn-default' data-toggle='modal' 
                         data-target='#${exchange}_${op}_modal' data-from='${result.from}' data-to='${result.to}' 
                         data-rate='${rate}' data-min='${result.amount}'>${$.t("buy")} ${result.to}</a></td>
                         </tr>`;
            }
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        })
    };

    NRS.changelly.renderMyExchangesTable = function (TRANSACTIONS_KEY, apiCall, exchange) {
        const transactionsJSON = localStorage[TRANSACTIONS_KEY];
        const transactions = transactionsJSON ? JSON.parse(transactionsJSON) : [];
        const tasks = transactions.map((tx2, i) =>
            function (callback) {
                apiCall('getTransactions', {id: tx2.id}, response => {
                    NRS.logConsole(`my exchanges iteration ${i} transaction id ${tx2.id}`);
                    let rows = "";
                    for (let j = 0; j < response.result.length; j++) {
                        const tx = response.result[j];
                        const dt = typeof tx.createdAt === 'number' ? parseInt(tx.createdAt) * 1000 : tx.createdAt;
                        let row = "";
                        row += "<tr>";
                        row += "<td>" + NRS.formatTimestamp(dt, false, true) + "</td>";
                        row += "<td>" + tx.status + "</td>";
                        row += `<td class='text-center'>
                                    ${NRS.getExchangeAddressLink(tx.payinAddress, tx.currencyFrom)}
                                    <br><i class='fa fa-arrow-down'></i><br>
                                    ${NRS.getExchangeAddressLink(tx.payoutAddress, tx.currencyTo)}
                                </td>`;
                        row += `<td class='text-center'>
                                    ${NRS.amountToPrecision(tx.amountFrom, 8)} ${tx.currencyFrom.toUpperCase()}
                                    <br><i class='fa fa-arrow-down'></i><br>
                                    ${NRS.amountToPrecision(tx.amountTo, 8)} ${tx.currencyTo.toUpperCase()}
                                </td>`;
                        row += "<td>" + (tx.payoutHash ? NRS.getExchangeTransactionLink(tx.payoutHash, tx.currencyTo) : "N/A") + "</td>";
                        row += `<td><a href='#' data-toggle='modal' data-target='#${exchange}_view_transaction' 
                                data-id='${tx.id}' data-content='${JSON.stringify(tx)}'>${String(tx.id).escapeHTML()}</a></td>`;
                        row += `<td><a href='#' class='delete-tx' data-id='${tx.id}'>x</a></td>`;
                        rows += row;
                    }
                    callback(null, rows);
                }, true);
            });
        NRS.logConsole(tasks.length + " tasks ready to run");
        const table = $(`#p_${exchange}_my_table`);
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
            const rows = results.join('');
            table.find("tbody").empty().append(rows);
            NRS.dataLoadFinished(table);
        });
    };

    NRS.changelly.removeExchangeTransaction = function (txId, TRANSACTIONS_KEY, exchange) {
        const json = localStorage[TRANSACTIONS_KEY];
        if (json === undefined) {
            return;
        }
        const transactions = JSON.parse(json);
        for (let i=0; i < transactions.length; i++) {
            if (txId === transactions[i].id) {
                NRS.logConsole(`remove transaction id ${txId}`);
                transactions.splice(i, 1);
                $(`#p_${exchange}_my_table tr`).eq(i+1).remove();
                break;
            }
        }
        localStorage[TRANSACTIONS_KEY] = JSON.stringify(transactions);
    };

    NRS.changelly.getOnShowBuyModalCallback = function (exchange) {
        return function (e) {
            const invoker = $(e.relatedTarget);
            const from = invoker.data("from");
            const to = invoker.data("to");
            NRS.logConsole(`modal invoked from ${from} to ${to}`);
            $(`#${exchange}_buy_from`).val(from);
            $(`#${exchange}_buy_to`).val(to);
            $(`#${exchange}_buy_title`).html($.t("exchange_crypto", { from: from, to: to }));
            $(`#${exchange}_buy_min`).val(invoker.data("min"));
            $(`#${exchange}_buy_min_coin`).html(NRS.getActiveChainName());
            $(`#${exchange}_buy_rate`).val(new Big(invoker.data("rate")).toFixed(8));
            $(`#${exchange}_buy_rate_text`).html(to + " " + $.t("per") + " " + NRS.getActiveChainName());
            $(`#${exchange}_buy_estimated_amount`).val("");
            $(`#${exchange}_buy_estimated_amount_text`).html(to);
            $(`#${exchange}_withdrawal_address_coin`).html(to);
        };
    };

    NRS.changelly.getOnBuySubmitCallback = function (exchange, apiCall, TRANSACTIONS_KEY) {
        return function (e) {
            e.preventDefault();
            const $modal = $(this).closest(".modal");
            const $btn = NRS.lockForm($modal);
            const amountNXT = $(`#${exchange}_buy_amount`).val();
            const minAmount = $(`#${exchange}_buy_min`).val();
            if (parseFloat(amountNXT) <= parseFloat(minAmount)) {
                NRS.showModalError($.t('amount_should_be_greater', {min:minAmount}), $modal);
                return;
            }
            const amountNQT = NRS.convertToNQT(amountNXT);
            const withdrawal = $(`#${exchange}_buy_withdrawal_address`).val();
            const from = $(`#${exchange}_buy_from`).val();
            const to = $(`#${exchange}_buy_to`).val();
            NRS.logConsole(`changehero withdrawal to address ${withdrawal} coin ${to}`);
            apiCall('createTransaction', {
                from: from,
                to: to,
                address: withdrawal,
                amount: amountNXT
            }, function (data) {
                if (data.error) {
                    NRS.logConsole(`${exchange} createTransaction error ${data.error.code} ${data.error.message}`);
                    return;
                }
                const depositAddress = data.result.payinAddress;
                if (!depositAddress) {
                    const msg = `${exchange} did not return a deposit address for id ${data.result.id}`;
                    NRS.logConsole(msg);
                    NRS.showModalError(msg, $modal);
                    return;
                }
                addTransaction(data.result.id, TRANSACTIONS_KEY);
                NRS.logConsole(NRS.getActiveChainName() + " deposit address " + depositAddress);
                NRS.sendRequest("sendMoney", {
                    recipient: depositAddress,
                    amountNQT: amountNQT,
                    secretPhrase: $(`#${exchange}_buy_password`).val(),
                    feeNXT: $(`#${exchange}_buy_fee`).val(),
                    deadline: "15"
                }, function (response) {
                    if (response.errorCode) {
                        NRS.logConsole(`sendMoney response ${response.errorCode} ${response.errorDescription.escapeHTML()}`);
                        NRS.showModalError(NRS.translateServerError(response), $modal);
                        return;
                    }
                    NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, exchange);
                    $(`#${exchange}_buy_passpharse`).val("");
                    NRS.unlockForm($modal, $btn, true);
                });
            }, true, $modal);
        };
    };

    function addTransaction(id, TRANSACTIONS_KEY) {
        const json = localStorage[TRANSACTIONS_KEY];
        let transactions;
        if (json === undefined) {
            transactions = [];
        } else {
            transactions = JSON.parse(json);
            if (transactions.length > 10) {
                transactions.splice(10, transactions.length - 10);
            }
        }
        const item = { id: id };
        for (let i = 0; i < transactions.length; i++) {
            if (item.id === transactions[i].id) {
                NRS.logConsole(`transaction id ${item.id} already exists`);
                return;
            }
        }
        transactions.splice(0, 0, item);
        NRS.logConsole(`transaction id ${id} added`);
        localStorage[TRANSACTIONS_KEY] = JSON.stringify(transactions);
    }

    NRS.changelly.getBuyAmountChangeCallback = function (exchange, apiCall) {
        return function () {
            const $modal = $(this).closest(".modal");
            const amount = $(`#${exchange}_buy_amount`).val();
            const from = $(`#${exchange}_buy_from`).val();
            const to = $(`#${exchange}_buy_to`).val();
            const $estimatedAmount = $(`#${exchange}_buy_estimated_amount`);
            if (!amount) {
                $estimatedAmount.val("");
                return;
            }
            const coinAmount = new Big(amount);
            const minAmount = new Big($(`#${exchange}_buy_min`).val());
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
            });
        };
    };

    NRS.changelly.getOnShowSellModalCallback = function (exchange, apiCall) {
        return function (e) {
            const invoker = $(e.relatedTarget);
            const from = invoker.data("from");
            const to = invoker.data("to");
            const rate = invoker.data("rate");
            const min = invoker.data("min");
            NRS.logConsole(`sell modal exchange from ${from} to ${to}`);
            $(`#${exchange}_sell_title`).html($.t("exchange_crypto", { from: from, to: to }));
            $(`#${exchange}_sell_qr_code`).html("");
            if (min && rate) {
                $(`#${exchange}_sell_min`).val(min);
                $(`#${exchange}_sell_rate`).val(rate);
            } else {
                apiCall("getMinAmount", { from: from, to: to }, function (getMinAmountResponse) {
                    $(`#${exchange}_sell_min`).val(getMinAmountResponse.result);
                    apiCall("getExchangeAmount", { from: from, to: to, amount: getMinAmountResponse.result }, function (response) {
                        const sellRate = NRS.getInverse(new Big(response.result).div(new Big(getMinAmountResponse.result)).toFixed(8));
                        $(`#${exchange}_sell_rate`).val(sellRate);
                    });
                });
            }
            $(`#${exchange}_sell_min_coin`).html(from);
            $(`#${exchange}_sell_rate_text`).html(from + " " + $.t("per") + " " + NRS.getActiveChainName());
            $(`#${exchange}_sell_amount_text`).html(from);
            $(`#${exchange}_sell_estimated_amount`).val("");
            $(`#${exchange}_sell_from`).val(from);
            $(`#${exchange}_sell_to`).val(to);
        };
    };

    NRS.changelly.getSellAmountChangeCallback = function (exchange, apiCall, TRANSACTIONS_KEY) {
        return function () {
            const $modal = $(this).closest(".modal");
            const amount = $(`#${exchange}_sell_amount`).val();
            const from = $(`#${exchange}_sell_from`).val();
            const to = $(`#${exchange}_sell_to`).val();
            const $estimatedAmount = $(`#${exchange}_sell_estimated_amount`);
            const depositAddress = $(`#${exchange}_sell_deposit_address`).html();
            if (!amount) {
                $estimatedAmount.val("");
                NRS.generateQRCode(`#${exchange}_sell_qr_code`, depositAddress);
                return;
            }
            const coinAmount = new Big(amount);
            const minAmount = new Big($(`#${exchange}_sell_min`).val());
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
            let publicKey = NRS.publicKey;
            if (publicKey === "" && NRS.accountInfo) {
                publicKey = NRS.accountInfo.publicKey;
            }
            if (!publicKey || publicKey === "") {
                NRS.showModalError("Account has no public key, please login using your passphrase", $modal);
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
                if (data.error) {
                    const msg = `${exchange} createTransaction error ${data.error.code} ${data.error.message}`;
                    NRS.logConsole(msg);
                    NRS.showModalError(msg, $modal);
                    return;
                }
                const depositAddress = data.result.payinAddress;
                if (!depositAddress) {
                    const msg = `${exchange} did not return a deposit address for id ${data.result.id}`;
                    NRS.logConsole(msg);
                    NRS.showModalError(msg, $modal);
                    return;
                }
                NRS.logConsole(`${from} deposit address ${depositAddress}`);
                $(`#${exchange}_sell_deposit_address`).html(depositAddress);
                addTransaction(data.result.id, TRANSACTIONS_KEY);
                apiCall('getExchangeAmount', {
                    amount: amount,
                    from: from,
                    to: to
                }, function (response) {
                    if (response.error) {
                        $estimatedAmount.val("");
                        NRS.generateQRCode(`#${exchange}_sell_qr_code`, depositAddress);
                        $modal.css('cursor', 'default');
                        return;
                    }
                    $estimatedAmount.val(response.result);
                    NRS.generateQRCode(`#${exchange}_sell_qr_code`, "bitcoin:" + depositAddress + "?amount=" + amount);
                    $modal.css('cursor', 'default');
                });
            })
        };
    };

    NRS.changelly.getOnClickSubmitCallback = function (exchange, apiCall) {
        return function (e) {
            e.preventDefault();
            const $modal = $(this).closest(".modal");
            $modal.find(".error_message").html("").hide();
            const viewContent = $(`#${exchange}_response`);
            viewContent.html("");
            const api = $(`#${exchange}_api`).val();
            const paramsText = $(`#${exchange}_parameters`).val();
            if (paramsText !== "") {
                let params;
                try {
                    params = JSON.parse(paramsText);
                } catch(e) {
                    NRS.showModalError("Invalid JSON " + e.message, $modal);
                    return;
                }
                apiCall(api, params, response => {
                    viewContent.html(JSON.stringify(response, null, 2));
                    hljs.highlightBlock(viewContent[0]);
                }, false, $modal);
            }
        };
    };

    return NRS;
}(NRS || {}, jQuery));

