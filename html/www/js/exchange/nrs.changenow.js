/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of the Nxt software, including this file, may be copied, modified, *
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
    var EXCHANGE_NAME = "changenow";

    NRS.pageLoading = function () {
        NRS.hasMorePages = false;
        var $pageHeader = $("#" + NRS.currentPage + "_page .content-header h1");
        $pageHeader.find(".loading_dots").remove();
        $pageHeader.append("<span class='loading_dots'><span>.</span><span>.</span><span>.</span></span>");
    };

    NRS.pages.exchange_changenow = function () {
        NRS.changelly.exchangePageInit(EXCHANGE_NAME);
        loadCoins();
        setTimeout(refreshPage, 60000);
    };

    var refreshPage = function () {
        if (NRS.currentPage === "exchange_changenow") {
            NRS.pages.exchange_changenow();
        }
    };

    $("#exchange_changenow_page").delegate(".exchange", "click", function () {
        var obj = JSON.parse($(this).attr('data-obj'));
        var coin = '';
        var coinSwap = "";
        var coinSwapName = "";
        var $changenowSendAmountNext = $("#changenow_send_amount_next");
        $changenowSendAmountNext.hide();
        var $changenowSendAmountBuySubmit = $("#changenow_send_amount_buy_submit");
        $changenowSendAmountBuySubmit.hide();
        $("#changenow_send_amount_done").hide();
        var $changenowPhrase = $(".changenow_phrase");
        $changenowPhrase.hide();
        var $changeNowWithdrawalAddress = $("#changeNow_withdrawal_address");
        $changeNowWithdrawalAddress.val('');
        var $changenowSendAmountTitle = $("#changenow_send_amount_title");
        var coinName = NRS.getActiveChainName();
        if ($(this).attr('data-op') == 'buy') {
            $(".changenowQR").hide();
            $changenowSendAmountBuySubmit.show();
            $changenowPhrase.show();
            coin = coinName;
            coinSwapName = obj.ticker;
            coinSwap = coin + "/" + obj.ticker;
            $changenowSendAmountTitle.attr("data-type", coin + "_" + obj.ticker);
            $changenowSendAmountTitle.html($.t("exchange_crypto", { from: coinName, to: obj.ticker }));
            $('#changeNow_rate').val(obj.estimatedAmount.toFixed(8));
        } else {
            $(".changenowQR").show();
            $changenowSendAmountNext.show();
            $("#changenow_send_modal #m_send_amount_sell_deposit_address,#changenow_send_modal #m_send_amount_sell_qr_code").html('');
            $changeNowWithdrawalAddress.val(NRS.accountRS);
            coin = obj.ticker;
            coinSwapName = coinName;
            coinSwap = obj.ticker + "/" + coinName;
            $changenowSendAmountTitle.attr("data-type", obj.ticker + "_" + coinName);
            $changenowSendAmountTitle.html($.t("exchange_crypto", { from: obj.ticker, to: coinName }));
            $('#changeNow_rate').val(NRS.getInverse(obj.estimatedAmount));
        }
        $('#changenow_send_modal').modal();
        $("#changeNow_send_amount_coin_name").text(coin);
        $('#changeNow_minAmount_coin_name').text(coin);
        $('#changeNow_rate_coin_name').text(coinSwap);
        $('#changeNow_fee_coin_name').text(coinSwapName);
        $('#changeNow_receive_amount_coin_name').text(coinSwapName);
        $('#changeNow_withdrawal_address_coin_name').text(coinSwapName + ' address');
        $('#changeNow_minAmount').val(obj.minAmount.toFixed(8));
    });

    var coinList = [];

    function loadCoins() {
        getTransactions();
        $.ajax({
            url: NRS.changeNow_url() + 'api/v1/currencies',
            crossDomain: true,
            dataType: "json",
            type: "GET",
            timeout: 30000,
            async: true
        }).done(function (response) {
            coinList = response;
            coinList.forEach(function (v) {
                if (!v.name) {
                    return;
                }
                v.name = v.name.substring(0, 1).toUpperCase() + v.name.substring(1);
                v.ticker = v.ticker.toUpperCase();
                if (v.ticker != NRS.getActiveChainName()) {
                    var txt = v.name + "[" + v.ticker + "]";
                    $("#changenow_coin0").append("<option value='" + v.ticker + "'>" + txt + "</option>");
                    $("#changenow_coin1").append("<option value='" + v.ticker + "'>" + txt + "</option>");
                    $("#changenow_coin2").append("<option value='" + v.ticker + "'>" + txt + "</option>");
                }
            });

            $('#changenow_status').html('ok');
            renderExchangeTable("buy");
            renderExchangeTable("sell");
            NRS.pageLoaded();
            NRS.settings.changeNow_coin.forEach(function (item, index) {
                $("#changenow_coin" + index + " option[value='" + item + "']").attr("selected", "selected");
            });
        }).error(function (response) {
            console.log("error:" + response);
        })
    }

    function getTransactions() {
        var depositAddressesJSON = localStorage[EXCHANGE_NAME + NRS.accountRS];
        var depositAddresses = [];
        if (depositAddressesJSON) {
            depositAddresses = JSON.parse(depositAddressesJSON);
        }

        $("#changenow_clear_my_exchanges").parent().find('.data-loading-container').hide();
        $("#p_changenow_my_table").show();

        var tbody = $("#p_changenow_my_table tbody");
        tbody.html('');
        depositAddresses.forEach(function (item) {
            $.ajax({
                url: NRS.changeNow_url() + 'api/v1/transactions/' + item.to + '/' + NRS.settings.changeNow_api_key,
                crossDomain: true,
                dataType: "json",
                type: "GET",
                timeout: 30000,
                async: true
            }).done(function (v) {
                var tr = $("<tr></tr>");
                tr.append("<td>" + NRS.formatTimestamp(v.updatedAt, false, true) + "</td>");
                tr.append("<td>" + v.status + "</td>");
                tr.append("<td>" + v.payinAddress + "</td>");
                if (v.status == 'finished') {
                    tr.append("<td>" + v.amountSend + "</td>");
                } else {
                    tr.append("<td>" + v.expectedSendAmount + "</td>");
                }
                tr.append("<td>" + v.fromCurrency + "</td>");
                tr.append("<td>" + v.payoutAddress + "</td>");
                if (v.status == 'finished') {
                    tr.append("<td>" + v.amountReceive + "</td>");
                } else {
                    tr.append("<td>" + v.expectedReceiveAmount + "</td>");
                }
                tr.append("<td>" + v.toCurrency + "</td>");
                if (v.status == 'finished' && v.networkFee) {
                    tr.append("<td>" + v.networkFee + "</td>");
                } else {
                    tr.append("<td>0</td>");
                }
                tr.append("<td>" + v.id + "</td>");
                tbody.append(tr);
            })
        })
    }

    function coinToPair(op, coin) {
        return (op == "buy") ? NRS.getActiveChainName() + "_" + coin : coin + "_" + NRS.getActiveChainName();
    }

    function renderExchangeTable(op) {
        var list = [];
        NRS.settings.changeNow_coin.forEach(function (item) {
            if (!item) {
                return;
            }
            minAmount(op, item).then(function (minAmountResponse) {
                exchangeAmount(minAmountResponse.minAmount, op, item).then(function (exchangeAmountResponse) {
                    var coin = {};
                    coinList.forEach(function (v) {
                        if (v.ticker == item) {
                            coin = v;
                        }
                    });
                    var obj = Object.assign(minAmountResponse, exchangeAmountResponse, coin);
                    list.push(obj);
                    if (NRS.settings.changeNow_coin.length == list.length) {
                        exchangeTable(list, op);
                    }
                })
            })
        })
    }

    function minAmount(op, item) {
        return new Promise(function (resolve) {
            $.ajax({
                url: NRS.changeNow_url() + 'api/v1/min-amount/' + coinToPair(op, item),
                crossDomain: true,
                dataType: "json",
                type: "GET",
                timeout: 30000,
                async: true
            }).done(function (response) {
                resolve(response);
            }).fail(function (xhr, textStatus, error) {
                if (error || textStatus == 'error') {
                    resolve({
                        minAmount: 0
                    });
                }
            })
        })
    }

    function exchangeAmount(count, op, item) {
        return new Promise(function (resolve) {
            var coinTo = coinToPair(op, item);
            if (op.indexOf("_") !== -1) {
                coinTo = op;
            }
            $.ajax({
                url: NRS.changeNow_url() + 'api/v1/exchange-amount/' + count + '/' + coinTo,
                crossDomain: true,
                dataType: "json",
                type: "GET",
                timeout: 30000,
                async: true
            }).done(function (response) {
                resolve(response);
            }).fail(function (xhr, textStatus, error) {
                if (error || textStatus == 'error') {
                    resolve({
                        error: error,
                        estimatedAmount: 0
                    });
                }
            })
        })
    }

    function exchangeTable(list, op) {
        var tbody = $("#p_changenow_" + op + "_nxt tbody");
        tbody.html("");
        var $op = $("#p_changenow_" + op + "_nxt");
        $op.parent().find('.data-loading-container').hide();
        $op.show();

        NRS.settings.changeNow_coin.forEach(function (item) {
            list.forEach(function (obj) {
                if (obj.ticker != item) {
                    return;
                }
                var tr = $("<tr></tr>");
                tr.append("<td>" + obj.name + "<img alt='' src='" + obj.image + "' width='16px' height='16px'/></td>");
                tr.append("<td>" + obj.ticker + "</td>");
                var minAmount;
                if (obj.estimatedAmount === undefined || obj.minAmount === undefined || obj.minAmount == 0) {
                    minAmount = "N/A";
                    rate = "N/A";
                } else {
                    var rate = new Big(obj.estimatedAmount).div(new Big(obj.minAmount)).toFixed(8);
                    if (op == "sell") {
                        minAmount = NRS.getInverse(obj.minAmount);
                        rate = NRS.getInverse(rate);
                    } else {
                        minAmount = obj.minAmount.toFixed(8);
                    }
                }
                tr.append("<td>" + minAmount + "</td>");
                tr.append("<td>" + rate + "</td>");
                if (minAmount !== "N/A") {
                    tr.append("<td><a data-obj='" + JSON.stringify(obj) + "' data-op='" + op + "' href='#' class='btn btn-xs btn-default exchange'>" + $.t("exchange") + "</a></td>");
                } else {
                    tr.append("<td></td>")
                }
                tbody.append(tr);
            });
        });
    }

    $("#changenow_accept_exchange_link").on("click", function (e) {
        e.preventDefault();
        NRS.updateSettings("exchange", "1");
        NRS.pages.exchange_changenow();
    });


    $("#changenow_send_amount_next, #changenow_send_amount_done").unbind();

    $("#changenow_send_amount_next").click(function () {
        postTransactions().then(function (response) {
            if (!response.payinAddress) {
                var $modal = $(this).closest(".modal");
                var $btn = NRS.lockForm($modal);
                NRS.unlockForm($modal, $btn, false);
                return;
            }
            $("#changenow_send_modal #m_send_amount_sell_deposit_address").html(response.payinAddress);
            NRS.generateQRCode("#changenow_send_modal #m_send_amount_sell_qr_code", response.payinAddress);
            $("#changenow_send_amount_next").hide();
            $("#changenow_send_amount_done").show();
        })
    });

    $("#changenow_send_amount_buy_submit").click(function () {
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        postTransactions().then(function (response) {
            if (!response.payinAddress) {
                NRS.unlockForm($modal, $btn, false);
                return;
            }
            NRS.sendRequest("sendMoney", {
                "recipient": response.payinAddress,
                "amountNQT": NRS.convertToNQT(response.amount),
                "secretPhrase": $("#changenow_send_password").val(),
                "deadline": "15",
                "feeNXT": $("#changenow_send_fee").val()
            }, function (response) {
                if (response.errorCode) {
                    NRS.showModalError(NRS.translateServerError(response), $modal);
                    return;
                }
                $("#shape_shift_buy_passpharse").val("");
                NRS.unlockForm($modal, $btn, true);
            })
        })
    });

    $("#changenow_clear_my_exchanges").click(function (e) {
        e.preventDefault();
        localStorage.removeItem(EXCHANGE_NAME + NRS.accountRS);
        getTransactions();
    });

    function postTransactions() {
        $("#changenow_send_modal .error_message").html('').hide();

        var data = {"from": "", "to": "", "address": "", "amount": "", "extraId": "", "userId": "", "linkId": ""};
        var type = $("#changenow_send_amount_title").attr("data-type");
        data.from = type.split("_")[0];
        data.to = type.split("_")[1];
        data.address = $('#changeNow_withdrawal_address').val();
        data.amount = $('#changeNow_send_amount').val();

        return new Promise(function (resolve) {
            $.ajax({
                url: NRS.changeNow_url() + 'api/v1/transactions/' + NRS.settings.changeNow_api_key,
                crossDomain: true,
                dataType: "json",
                data: JSON.stringify(data),
                type: "POST",
                timeout: 30000,
                contentType: "application/json; charset=utf-8",
                async: true
            }).done(function (response) {
                if (response.error) {
                    $("#changenow_send_modal .error_message").html(response.error).show();
                    resolve(response);
                    return;
                }
                NRS.addDepositAddress(NRS.accountRS, response.id, response.id, EXCHANGE_NAME + NRS.accountRS);
                getTransactions();
                response.amount = data.amount;
                resolve(response)
            }).fail(function (xhr) {
                var errorText = "";
                if (xhr.responseText || xhr.statusText) {
                    errorText = xhr.responseText || xhr.statusText;
                }
                if (xhr.responseJSON && xhr.responseJSON.error) {
                    errorText = xhr.responseJSON.message;
                }
                $("#changenow_send_modal .error_message").html(errorText).show();
                resolve(errorText);
            })
        })
    }

    $('#changeNow_send_amount').blur(function () {
        var $modal = $(this).closest(".modal");
        if (!isNaN(parseInt($(this).val()))) {
            var amount = parseFloat($(this).val());
            var coinAmount = new Big(amount);
            var minAmount = new Big($("#changeNow_minAmount").val());
            if (coinAmount.lt(minAmount)) {
                NRS.showModalError($.t('error_amount_too_low'), $modal);
                return;
            }
            $modal.find(".error_message").html("").hide();
            var pair = $("#changenow_send_amount_title").attr("data-type");
            exchangeAmount(amount, pair).then(function (v) {
                if (v.error) {
                    NRS.showModalError($.t(v.error), $modal);
                }
                if (v.estimatedAmount != 0) {
                    var chainName = NRS.getActiveChainName();
                    if (pair.substring(0, chainName.length) == chainName) {
                        $('#changeNow_rate').val((v.estimatedAmount / amount).toFixed(8));
                    } else {
                        $('#changeNow_rate').val(NRS.getInverse(v.estimatedAmount / amount));
                    }
                } else {
                    $('#changeNow_rate').val(0);
                }
                if (v.networkFee) {
                    $('#changeNow_fee').val((v.estimatedAmount * v.networkFee / 100).toFixed(8));
                } else {
                    $('#changeNow_fee').val((0).toFixed(8));
                }
                $('#changeNow_receive_amount').val(v.estimatedAmount.toFixed(8));
            });
            $("#changenow_send_modal .error_message").html('').hide();
        } else {
            NRS.showModalError($.t('error_invalid_input_numbers'), $modal);
        }
    });

    $(".changenowSelect").change(function () {
        NRS.settings.changeNow_coin[$(this).attr('id').split("changenow_coin")[1]] = $(this).children('option:selected').val();
        loadCoins();
    });

    return NRS;
}(NRS || {}, jQuery));