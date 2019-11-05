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
    const EXCHANGE_NAME = 'changehero';
    const TRANSACTIONS_KEY = 'changehero.transactions';

    const apiCall = NRS.changelly.generateApiCall(EXCHANGE_NAME);

    NRS.pages.exchange_changehero = function() {
        NRS.changelly.exchangePageInit(EXCHANGE_NAME);
        apiCall('getCurrencies', {}, NRS.changelly.getGetCurrenciesCallback(EXCHANGE_NAME, apiCall,["ARDR", "IGNIS"]));
        NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
        NRS.pageLoaded();
        setTimeout(refreshPage, 60000);
    };

    const refreshPage = function() {
        if (NRS.currentPage === "exchange_changehero") {
            NRS.pages.exchange_changehero();
        }
    };

    $("#changehero_accept_exchange_link").on("click", function(e) {
   		e.preventDefault();
   		NRS.updateSettings("exchange", "1");
        NRS.pages.exchange_changehero();
   	});

    $("#changehero_clear_my_exchanges").on("click", function(e) {
   		e.preventDefault();
   		localStorage.removeItem(TRANSACTIONS_KEY);
        NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
   	});

    $('.coin-select.changehero').change(function() {
        const id = $(this).attr('id');
        const coins = NRS.getCoins(EXCHANGE_NAME);
        coins[parseInt(id.slice(-1))] = $(this).val();
        NRS.setCoins(EXCHANGE_NAME, coins);
        NRS.changelly.renderExchangeTable(EXCHANGE_NAME, 'buy', apiCall);
        NRS.changelly.renderExchangeTable(EXCHANGE_NAME, 'sell', apiCall);
    });

	NRS.setup.exchange_changehero = function() {
        $('#p_changehero_my_table').on('click', 'td a.delete-tx', function (ev) {
            ev.preventDefault();
            NRS.changelly.removeExchangeTransaction($(this).data('id'), TRANSACTIONS_KEY, EXCHANGE_NAME);
        });
        // Do not implement connection to a 3rd party site here to prevent privacy leak
    };

    $("#changehero_buy_modal").on("show.bs.modal", NRS.changelly.getOnShowBuyModalCallback(EXCHANGE_NAME));

    $("#changehero_buy_submit").on("click", NRS.changelly.getOnBuySubmitCallback(EXCHANGE_NAME, apiCall, TRANSACTIONS_KEY));

    $('#changehero_buy_amount').change(NRS.changelly.getBuyAmountChangeCallback(EXCHANGE_NAME, apiCall));

    $("#changehero_sell_modal").on("show.bs.modal", NRS.changelly.getOnShowSellModalCallback(EXCHANGE_NAME, apiCall));

    $('#changehero_sell_amount').change(NRS.changelly.getSellAmountChangeCallback(EXCHANGE_NAME, apiCall, TRANSACTIONS_KEY));

    $("#changehero_sell_done").on("click", function(e) {
        e.preventDefault();
        const $modal = $(this).closest(".modal");
        const $btn = NRS.lockForm($modal);
        const deposit = $("#changehero_sell_deposit_address").html();
        if (deposit !== "") {
            NRS.changelly.renderMyExchangesTable(TRANSACTIONS_KEY, apiCall, EXCHANGE_NAME);
            NRS.unlockForm($modal, $btn, true);
        }
    });

    const $viewTransactionModal = $("#changehero_view_transaction");
    $viewTransactionModal.on("show.bs.modal", function(e) {
        const $invoker = $(e.relatedTarget);
        const id = $invoker.data("id");
        const content = $invoker.data("content");
        $("#changehero_identifier").val(id);
        var viewContent = $("#changehero_view_content");
        viewContent.html(JSON.stringify(content, null, 2));
        hljs.highlightBlock(viewContent[0]);
    });

    $viewTransactionModal.on('hidden.bs.modal', function () {
        $("#changehero_search").prop("disabled", false);
    });

    $("#changehero_search").on("click", function(e) {
        e.preventDefault();
        $("#changehero_search").prop("disabled", true);
        const key = $("#changehero_search_key").val();
        const id = $("#changehero_search_id").val();
        const params = {};
        params[key] = id;
        apiCall("getTransactions", params, function(response) {
            $(this).data("id", id);
            $(this).data("content", response);
            $("#changehero_view_transaction").modal({}, $(this));
        });
    });

    $("#changehero_scratchpad_link").on("click", function(e) {
        e.preventDefault();
        $("#changehero_scratchpad").modal({}, $(this));
    });

    $("#changehero_submit").on("click", NRS.changelly.getOnClickSubmitCallback(EXCHANGE_NAME, apiCall));

    return NRS;
}(NRS || {}, jQuery));