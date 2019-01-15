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

    NRS.jsondata = NRS.jsondata||{};

    NRS.jsondata.bundlers = function (response) {
        var fxtDecimals = NRS.getChain(1).decimals;
        var actionsFormatted = "";
        var rulesFormatted = "";
        var minRateNQTPerFXT;
        if (response.bundlerRS == NRS.accountRS) {
            var filteredRulesCount = 0;
            var totalRulesCount = 0;
            for (var i in response.bundlingRules) {
                if (response.bundlingRules[i].filters) {
                    filteredRulesCount++;
                }
                totalRulesCount++;
            }
            rulesFormatted = "<a href='#' data-toggle='modal' data-target='#view_bundling_rules_modal' data-rules='" +
                JSON.stringify(response.bundlingRules) + "' data-chain='" + response.chain + "'>" +
                filteredRulesCount + "/" + totalRulesCount + "</a>";

            actionsFormatted = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#stop_bundler_modal' " +
                "data-account='" + NRS.escapeRespStr(response.bundlerRS) + "' data-chain='" + NRS.escapeRespStr(response.chain) + "'>" + $.t("stop") + "</a>";
            actionsFormatted += "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#start_bundler_modal' data-chain='" +
                            response.chain + "' data-addrule='true'>" + $.t("add_bundling_rule") + "</a>";
            if (response.announcedMinRateNQTPerFXT) {
                minRateNQTPerFXT = NRS.formatQuantity(response.announcedMinRateNQTPerFXT, NRS.getChain(response.chain).decimals);
            } else {
                minRateNQTPerFXT = "-";
            }

        } else {
            actionsFormatted = "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#blacklist_bundler_modal' " +
                            "data-account='" + NRS.escapeRespStr(response.bundlerRS) + "'>" + $.t("blacklist") + "</a>";
            minRateNQTPerFXT = NRS.formatQuantity(response.minRateNQTPerFXT, NRS.getChain(response.chain).decimals);
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
            minRateNQTPerFXT: minRateNQTPerFXT,
            rulesFormatted: rulesFormatted,
            actionsFormatted: actionsFormatted
        };
    };

    NRS.incoming.bundlers = function() {
        NRS.loadPage("bundlers");
    };

    NRS.pages.bundlers = function() {
        NRS.renderBundlersTable($("#bundlers_page_type").find(".active").data("type"));
    };

    NRS.bundlingOptions = null;

    function getBundlingOptions(callback) {
        if (NRS.bundlingOptions == null) {
            NRS.sendRequest("getBundlingOptions", {}, function (response) {
                NRS.bundlingOptions = response;
                callback(NRS.bundlingOptions);
            });
        } else {
            callback(NRS.bundlingOptions);
        }
    }

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

        params["account"] = NRS.accountRS;
        NRS.sendRequest("getBundlers", params, function(getBundlersResponse) {
            if (NRS.isErrorResponse(getBundlersResponse)) {
                view.render({
                    errorMessage: NRS.getErrorMessage(getBundlersResponse),
                    isLoading: false,
                    isEmpty: false,
                    isParentChain: NRS.isParentChain()
                });
                return;
            }
            function addMyBundlers() {
                var response = $.extend({}, getBundlersResponse);
                if (response.bundlers.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.bundlers.pop();
                }
                response.bundlers.forEach(
                    function (bundlerJson) {
                        view.bundlers.push(NRS.jsondata.bundlers(bundlerJson))
                    }
                );
            }
            function renderView() {
                view.render({
                    isLoading: false,
                    isEmpty: view.bundlers.length == 0,
                    isParentChain: NRS.isParentChain()
                });
                NRS.pageLoaded();
            }
            if (type === "my") {
                view.bundlers.length = 0;
                addMyBundlers();
                renderView();
            } else if (type === "all") {
                NRS.sendRequest("getAllBundlerRates", params, function(getAllBundlerRatesResponse) {
                    if (NRS.isErrorResponse(getAllBundlerRatesResponse)) {
                        view.render({
                            errorMessage: NRS.getErrorMessage(getAllBundlerRatesResponse),
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
                            if (rate.chain != NRS.getActiveChainId() && !NRS.isParentChain()
                                || rate.rates[j].accountRS == NRS.accountRS) {
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
                    addMyBundlers();
                    renderView();
                })
            }
        });
    };

    $("#bundlers_page_type").find(".btn").click(function (e) {
        e.preventDefault();
        var bundlersTable = $("#bundlers_table");
        bundlersTable.find("tbody").empty();
        bundlersTable.parent().addClass("data-loading").removeClass("data-empty");
        NRS.renderBundlersTable($(this).data("type"));
    });

    NRS.forms.startBundler = function($modal) {
        var isStartBundler = $(".start_bundler:first").is(":visible");
        var data = NRS.getFormData($modal.find("form:first"));
        if (data.minRateNXTPerFXT === "") {
            return {
                "error": $.t("error_bundler_rate_required")
            };
        }

        var filtersObj = {"filter": []};
        for (var key in data) {
            if (data.hasOwnProperty(key) && key.indexOf("filterName_") != -1) {
                var parameterKey = "filterParameter_" + key.substring("filterName_".length);
                var parameter = data[parameterKey];
                filtersObj.filter.push(data[key] + (parameter ? (":" + parameter) : ""));
                delete data[key];
                delete data[parameterKey];
            }
        }
        $.extend(data, filtersObj);
        delete data.filterName;
        delete data.filterParameter;
        var fxtDecimals = NRS.getChain(1).decimals;
        if (isStartBundler) {
            data.totalFeesLimitFQT = NRS.floatToInt(data.totalFeesLimitFXT, fxtDecimals);
            delete data.chain;
        }
        delete data.totalFeesLimitFXT;
        data.overpayFQTPerFXT = NRS.floatToInt(data.overpayFXTPerFXT, fxtDecimals);
        delete data.overpayFXTPerFXT;
        var result = { data: data };
        if (!isStartBundler) {
            result["requestType"] = "addBundlingRule";
        }
        return result;
    };

    NRS.forms.startBundlerComplete = function() {
        $.growl($.t("bundler_started"));
        NRS.loadPage("bundlers");
    };

    $("#start_bundler_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var isAddRule = $invoker.data("addrule");
        $(this).find(".active_bundling_filter").remove();
        if (isAddRule) {
            $(this).find(".start_bundler").hide();
            var chainId = $invoker.data("chain");
            $(this).find("input[name=chain]").val(chainId);
            $(this).find(".modal-title").text($.t("add_bundling_rule"));
        } else {
            $(this).find(".start_bundler").show();
            $(this).find(".modal-title").text($.t("start_bundler"));
        }
        getBundlingOptions(function(options) {
            var feeCalculatorSelect = $("#fee_calculator_id");
            feeCalculatorSelect.empty();
            $.each(options.availableFeeCalculators, function (index, calculatorName) {
                calculatorName = String(calculatorName).escapeHTML();
                var selectedAttr = (calculatorName == "MIN_FEE" ? "selected='selected'" : "");
                feeCalculatorSelect.append("<option value='" + calculatorName + "' " + selectedAttr + ">" +
                        calculatorName + "</option>");
            });
            feeCalculatorSelect.trigger("change");

            var filterNameSelect = $("#filter_name_id");
            filterNameSelect.empty();
            $.each(options.availableFilters, function (index, filter) {
                filterName = String(filter.name).escapeHTML();
                filterNameSelect.append("<option value='" + filterName + "'>" + filterName + "</option>");
            });
            filterNameSelect.trigger("change");
        });
    });

    $("#fee_calculator_id").on("change", function () {
        var calculatorName = $(this).val();
        var key = "bundler_fee_calculator_help_" + calculatorName.toLowerCase();
        if ($.i18n.exists(key)) {
            $("#fee_calculator_description").show();
            $("#fee_calculator_description").text($.t(key));
        } else {
            $("#fee_calculator_description").hide();
        }
    });

    $("#filter_name_id").on("change", function () {
        var filterName = $(this).val();
        var $helpButton = $(this).parent().parent().find(".bundling_filter_details").first();
        if (filterName) {
            var key = "bundler_filter_help_" + filterName.toLowerCase();
            var description = "";
            if ($.i18n.exists(key)) {
                description = $.t(key);
            } else {
                var filters = NRS.bundlingOptions.availableFilters;
                for (var i = 0; i < filters.length; i++) {
                    if (filters[i].name == "filterName") {
                        description = filters[i].description;
                        break;
                    }
                }
            }
            $helpButton.attr("data-content", description);
        } else {
            $helpButton.attr("data-content", "");
        }
    });

    $("#add_filter_btn_id").on("click", function() {
        var $form = $(this).closest("form");
        var $original = $form.find(".form_group_multi_filters").first();
        var $clone = $original.clone("true", "true");
        $clone.addClass("active_bundling_filter");
        $clone.find("select[name='filterName']").attr("name", "filterName_" + $form.find(".form_group_multi_filters").length);
        $clone.find("input[name='filterParameter']").attr("name", "filterParameter_" + $form.find(".form_group_multi_filters").length);
        $clone.show();

        $form.find(".multi_filters_list_container").append($clone);
        $(function () {
            $form.find("button.bundling_filter_details").last().popover({
                "html": true
            });
        });
    });

    $(".remove_bundling_filter_btn").on("click", function(e) {
        e.preventDefault();
        var $form = $(this).closest("form");
        if ($form.find(".form_group_multi_filters").length == 1) {
            return;
        }
        $(this).closest(".form_group_multi_filters").remove();
    });

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

    $("#blacklist_bundler_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        if (!NRS.needsAdminPassword) {
            $("#blacklist_bundler_admin_password_wrapper").hide();
        } else {
            if (NRS.getAdminPassword() != "") {
                $("#blacklist_bundler_admin_password").val(NRS.getAdminPassword());
            }
        }
        $("#blacklist_bundler_account_id").html($invoker.data("account"));
        $("#blacklist_bundler_field_id").val($invoker.data("account"));
    });

    $("#view_bundling_rules_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var rules = $invoker.data("rules");
        var chain = $invoker.data("chain");
        var fxtDecimals = NRS.getChain(1).decimals;
        //TODO better visualization of the rules
        for (var i in rules) {
            rules[i].minRate = NRS.formatQuantity(rules[i].minRateNQTPerFXT, NRS.getChain(chain).decimals);
            delete rules[i].minRateNQTPerFXT;
            if (rules[i].overpayFQTPerFXT) {
                rules[i].overpay = NRS.formatQuantity(rules[i].overpayFQTPerFXT, fxtDecimals)
                delete rules[i].overpayFQTPerFXT;
            }
        }

        $("#view_bundling_rules_modal_content").val(JSON.stringify(rules, null, 2));
    });

    NRS.forms.blacklistBundlerComplete = function(response) {
        var message;
        var type;
        if (response.errorCode) {
            message = response.errorDescription.escapeHTML();
            type = "danger";
        } else {
            message = $.t("success_blacklist_bundler");
            type = "success";
        }
        $.growl(message, {
            "type": type
        });
        NRS.loadPage("bundlers");
    };

    NRS.forms.bundleTransactions = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        data.deadline = String(Math.min(data.childDeadline - Math.ceil((NRS.toEpochTime() - data.childTimestamp) / 60), 15));
        delete data.childDeadline;
        delete data.childTimestamp;
        return { data: data };
    };

    NRS.forms.bundleTransactionsFeeCalculation = function(feeField, feeNQT) {
        feeField.val(NRS.convertToFXT(feeNQT));
    };

    return NRS;

}(NRS || {}, jQuery));