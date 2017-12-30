/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2017 Jelurida IP B.V.                                     *
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
        return {
            accountFormatted: NRS.getAccountLink(response, "bundler"),
            chainFormatted: NRS.getChainLink(response.chain),
            totalFeesLimitFQT: response.totalFeesLimitFQT ? NRS.formatQuantity(response.totalFeesLimitFQT, fxtDecimals) : "",
            currentTotalFeesFQT: response.currentTotalFeesFQT ? NRS.formatQuantity(response.currentTotalFeesFQT, fxtDecimals) : "",
            currentFeeLimitFQT: response.currentFeeLimitFQT ? NRS.formatQuantity(response.currentFeeLimitFQT, fxtDecimals) :
                NRS.formatQuantity(response.totalFeesLimitFQT - response.currentTotalFeesFQT, fxtDecimals),
            minRateNQTPerFXT: NRS.formatQuantity(response.minRateNQTPerFXT, NRS.getChain(response.chain).decimals),
            overpayFQTPerFXT: response.overpayFQTPerFXT ? NRS.formatQuantity(response.overpayFQTPerFXT, fxtDecimals) : "",
            stopLinkFormatted: "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#stop_bundler_modal' " +
                "data-account='" + NRS.escapeRespStr(response.bundlerRS) + "' data-chain='" + NRS.escapeRespStr(response.chain) + "'>" + $.t("stop") + "</a>"
        };
    };

    NRS.incoming.bundlers = function() {
        NRS.loadPage("bundlers");
    };

    NRS.pages.bundlers = function () {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('bundlers_page', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            bundlers: []
        });
        var params = {
            "nochain": true,
            "adminPassword": NRS.getAdminPassword(),
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
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
                for (var i=0; i<getAllBundlerRatesResponse.rates.length; i++) {
                    var rateJson = getAllBundlerRatesResponse.rates[i];
                    for (var j=0; j < rateJson.rates.length; j++) {
                        response.bundlers.push({
                            "bundler": rateJson.rates[j].account,
                            "bundlerRS": rateJson.rates[j].accountRS,
                            "chain": rateJson.chain,
                            "minRateNQTPerFXT": rateJson.rates[j].minRateNQTPerFXT,
                            "currentFeeLimitFQT": rateJson.rates[j].currentFeeLimitFQT
                        });
                    }
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

            })
        })
    };

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