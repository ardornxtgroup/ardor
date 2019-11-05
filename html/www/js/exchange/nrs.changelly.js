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

    const apiCall = NRS.changelly.generateApiCall(EXCHANGE_NAME);

    NRS.pages.exchange_changelly = function() {
        NRS.changelly.exchangePageInit(EXCHANGE_NAME);
        apiCall('getCurrencies', {}, NRS.changelly.getGetCurrenciesCallback(EXCHANGE_NAME, apiCall,["ARDR", "IGNIS"]));
        NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
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
        NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
   	});

    $('.coin-select.changelly').change(function() {
        var id = $(this).attr('id');
        var coins = NRS.getCoins(EXCHANGE_NAME);
        coins[parseInt(id.slice(-1))] = $(this).val();
        NRS.setCoins(EXCHANGE_NAME, coins);
        NRS.changelly.renderExchangeTable(EXCHANGE_NAME, 'buy', apiCall);
        NRS.changelly.renderExchangeTable(EXCHANGE_NAME, 'sell', apiCall);
    });

	NRS.setup.exchange_changelly = function() {
        $('#p_changelly_my_table').on('click', 'td a.delete-tx', function (ev) {
            ev.preventDefault();
            NRS.changelly.removeExchangeTransaction($(this).data('id'), TRANSACTIONS_KEY, EXCHANGE_NAME);
        });
        // Do not implement connection to a 3rd party site here to prevent privacy leak
    };

    $("#changelly_buy_modal").on("show.bs.modal", NRS.changelly.getOnShowBuyModalCallback(EXCHANGE_NAME));

    $("#changelly_buy_submit").on("click", NRS.changelly.getOnBuySubmitCallback(EXCHANGE_NAME, apiCall, TRANSACTIONS_KEY));

    $('#changelly_buy_amount').change(NRS.changelly.getBuyAmountChangeCallback(EXCHANGE_NAME, apiCall));

    $("#changelly_sell_modal").on("show.bs.modal", NRS.changelly.getOnShowSellModalCallback(EXCHANGE_NAME, apiCall));

    $('#changelly_sell_amount').change(NRS.changelly.getSellAmountChangeCallback(EXCHANGE_NAME, apiCall, TRANSACTIONS_KEY));

    $("#changelly_sell_done").on("click", function(e) {
        e.preventDefault();
        var $modal = $(this).closest(".modal");
        var $btn = NRS.lockForm($modal);
        var deposit = $("#changelly_sell_deposit_address").html();
        if (deposit !== "") {
            NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
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

    $("#changelly_submit").on("click", NRS.changelly.getOnClickSubmitCallback(EXCHANGE_NAME, apiCall));

    NRS.getFundAccountLink = function() {
        return "<div class='callout callout-danger'>" +
            "<span>" + $.t("fund_account_warning_11") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_2") + "</span><br>" +
            "<span>" + $.t("fund_account_warning_3") + "</span><br>" +
            "</div>" +
            "<a href='#' class='btn btn-xs btn-default' data-toggle='modal' data-target='#changelly_sell_modal' " +
            "data-from='BTC' data-to=" + NRS.getActiveChainName() + ">" + $.t("fund_account_message2", { coin: NRS.getActiveChainName()}) + "</a>";
    };

    return NRS;
}(NRS || {}, jQuery));