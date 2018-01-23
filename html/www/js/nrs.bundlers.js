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
var NRS = (function(NRS, $) {

    function isErrorResponse(response) {
        return response.errorCode || response.errorDescription || response.errorMessage || response.error;
    }

    function getErrorMessage(response) {
        return response.errorDescription || response.errorMessage || response.error;
    } 

    NRS.jsondata = NRS.jsondata||{};

    NRS.jsondata.bundlers = function (response) {
        var fxtDecimals = NRS.getChain(1).decimals;
        var stopLinkFormatted = "";
        if (response.bundlerRS == NRS.accountRS) {
            stopLinkFormatted = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#stop_bundler_modal' " +
                "data-account='" + NRS.escapeRespStr(response.bundlerRS) + "' data-chain='" + NRS.escapeRespStr(response.chain) + "'>" + $.t("stop") + "</a>";
        }
        var currentFeeLimitFQT = "";
        if (response.currentFeeLimitFQT) {
            if (response.currentFeeLimitFQT !== NRS.constants.MAX_LONG_JAVA) {
                currentFeeLimitFQT = NRS.formatQuantity(response.currentFeeLimitFQT, fxtDecimals);
            }
        } else {
            currentFeeLimitFQT = NRS.formatQuantity(response.totalFeesLimitFQT - response.currentTotalFeesFQT, fxtDecimals)
        }
        return {
            accountFormatted: NRS.getAccountLink(response, "bundler"),
            chainFormatted: NRS.getChainLink(response.chain),
            totalFeesLimitFQT: response.totalFeesLimitFQT ? NRS.formatQuantity(response.totalFeesLimitFQT, fxtDecimals) : "",
            currentTotalFeesFQT: response.currentTotalFeesFQT ? NRS.formatQuantity(response.currentTotalFeesFQT, fxtDecimals) : "",
            currentFeeLimitFQT: currentFeeLimitFQT,
            minRateNQTPerFXT: NRS.formatQuantity(response.minRateNQTPerFXT, NRS.getChain(response.chain).decimals),
            overpayFQTPerFXT: response.overpayFQTPerFXT ? NRS.formatQuantity(response.overpayFQTPerFXT, fxtDecimals) : "",
            stopLinkFormatted: stopLinkFormatted
        };
    };

    NRS.incoming.bundlers = function() {
        NRS.loadPage("bundlers");
    };

    NRS.pages.bundlers = function() {
        NRS.renderBundlersTable($("#bundlers_page_type").find(".active").data("type"));
    };

    NRS.renderBundlersTable = function(type) {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('bundlers_section', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            bundlers: []
        });
        var params = {
            "adminPassword": NRS.getAdminPassword(),
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        if (NRS.isParentChain()) {
            // For Ardor show bundlers from all chains and disable the start button
            params["nochain"] = true;
            $("#bundlers_start_btn").prop("disabled", true);
        } else {
            $("#bundlers_start_btn").prop("disabled", false);
        }
        if (type === "my") {
            params["account"] = NRS.accountRS;
            NRS.sendRequest("getBundlers", params, function(getBundlersResponse) {
                if (isErrorResponse(getBundlersResponse)) {
                    view.render({
                        errorMessage: getErrorMessage(getBundlersResponse),
                        isLoading: false,
                        isEmpty: false,
                        isParentChain: NRS.isParentChain()
                    });
                    return;
                }
                var response = $.extend({}, getBundlersResponse);
                if (response.bundlers.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.bundlers.pop();
                }
                view.bundlers.length = 0;
                response.bundlers.forEach(
                    function (bundlerJson) {
                        view.bundlers.push(NRS.jsondata.bundlers(bundlerJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.bundlers.length == 0,
                    isParentChain: NRS.isParentChain()
                });
                NRS.pageLoaded();
            });
        } else if (type === "all") {
            NRS.sendRequest("getAllBundlerRates", params, function(getAllBundlerRatesResponse) {
                if (isErrorResponse(getAllBundlerRatesResponse)) {
                    view.render({
                        errorMessage: getErrorMessage(getAllBundlerRatesResponse),
                        isLoading: false,
                        isEmpty: false,
                        isParentChain: NRS.isParentChain()
                    });
                    return;
                }
                var response = $.extend({}, getAllBundlerRatesResponse);
                response.bundlers = [];
                for (var i=0; i < response.rates.length; i++) {
                    var rate = response.rates[i];
                    for (var j=0; j < rate.rates.length; j++) {
                        if (rate.chain != NRS.getActiveChainId() && !NRS.isParentChain()) {
                            continue;
                        }
                        response.bundlers.push({
                            "bundler": rate.rates[j].account,
                            "bundlerRS": rate.rates[j].accountRS,
                            "chain": rate.chain,
                            "minRateNQTPerFXT": rate.rates[j].minRateNQTPerFXT,
                            "currentFeeLimitFQT": rate.rates[j].currentFeeLimitFQT
                        });
                    }
                }
                response.bundlers.sort(function (a, b) {
                    if (a.chain > b.chain) {
                        return 1;
                    } else if (a.chain < b.chain) {
                        return -1;
                    } else {
                        var rate1 = new BigInteger(NRS.floatToInt(a.minRateNQTPerFXT, NRS.getActiveChain().decimals));
                        var rate2 = new BigInteger(NRS.floatToInt(b.minRateNQTPerFXT, NRS.getActiveChain().decimals));
                        return rate1.compareTo(rate2);
                    }
                });
                view.bundlers.length = 0;
                response.bundlers.forEach(
                    function (bundlerJson) {
                        view.bundlers.push(NRS.jsondata.bundlers(bundlerJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.bundlers.length == 0,
                    isParentChain: NRS.isParentChain()
                });
                NRS.pageLoaded();
            })
        }
    };

    $("#bundlers_page_type").find(".btn").click(function (e) {
        e.preventDefault();
        var bundlersTable = $("#bundlers_table");
        bundlersTable.find("tbody").empty();
        bundlersTable.parent().addClass("data-loading").removeClass("data-empty");
        NRS.renderBundlersTable($(this).data("type"));
    });

    NRS.forms.startBundler = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var fxtDecimals = NRS.getChain(1).decimals;
        data.totalFeesLimitFQT = NRS.floatToInt(data.totalFeesLimitFXT, fxtDecimals);
        delete data.totalFeesLimitFXT;
        data.overpayFQTPerFXT = NRS.floatToInt(data.overpayFXTPerFXT, fxtDecimals);
        delete data.overpayFXTPerFXT;
        return { data: data };
    };

    NRS.forms.startBundlerComplete = function() {
        $.growl($.t("bundler_started"));
        NRS.loadPage("bundlers");
    };

    $("#stop_bundler_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var account = $invoker.data("account");
        if (account) {
            $("#stop_bundler_account").val(account);
        }
        var chain = $invoker.data("chain");
        if (chain) {
            $("#stop_bundler_chain").val(chain);
        }
        if (NRS.getAdminPassword()) {
            $("#stop_bundler_admin_password").val(NRS.getAdminPassword());
        }
    });

    NRS.forms.stopBundlerComplete = function() {
        $.growl($.t("bundler_stopped"));
        NRS.loadPage("bundlers");
    };

    NRS.forms.bundleTransactions = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        data.deadline = String(data.childDeadline - Math.ceil((NRS.toEpochTime() - data.childTimestamp) / 60));
        delete data.childDeadline;
        delete data.childTimestamp;
        return { data: data };
    };

    NRS.forms.bundleTransactionsFeeCalculation = function(feeField, feeNQT) {
        feeField.val(NRS.convertToFXT(feeNQT));
    };

    return NRS;

}(NRS || {}, jQuery));