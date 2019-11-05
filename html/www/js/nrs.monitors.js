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
    var currentMonitor;

    NRS.jsondata = NRS.jsondata||{};

    NRS.jsondata.monitors = function (response) {
        const monitorJSON = JSON.stringify(response);
        return {
            accountFormatted: NRS.getAccountLink(response, "account"),
            property: NRS.escapeRespStr(response.property),
            holdingFormatted: NRS.escapeRespStr(response.holdingInfo.name),
            amountFormatted: NRS.formatAmountDecimals(response.amount, false, false, false, response.holdingInfo.decimals),
            thresholdFormatted: NRS.formatAmountDecimals(response.threshold, false, false, false, response.holdingInfo.decimals),
            interval: NRS.escapeRespStr(response.interval),
            statusLinkFormatted: "<a href='#' class='btn btn-xs' " +
                        "onclick='NRS.goToMonitor(" + monitorJSON + ");'>" +
                         $.t("status") + "</a>",
            stopLinkFormatted: "<a href='#' class='btn btn-xs' data-toggle='modal' " +
                        "data-target='#stop_funding_monitor_modal' " +
                        "data-monitor='" + monitorJSON + "'>" + $.t("stop") + "</a>"
        };
    };

    NRS.jsondata.monitoredAccount = function (response) {
        try {
            var value = JSON.parse(NRS.unescapeRespStr(response.value)); // all strings on a response are escaped (NRS.escapeResponseObjStrings)
        } catch (e) {
            NRS.logConsole(e.message);
        }
        const fmt = x => NRS.formatAmountDecimals(x, false, false, false, currentMonitor.holdingInfo.decimals);
        return {
            accountFormatted: NRS.getAccountLink(response, "recipient"),
            property: NRS.escapeRespStr(response.property),
            holdingFormatted: NRS.escapeRespStr(currentMonitor.holdingInfo.name),
            amountFormatted: (value && value.amount) ? "<b>" + fmt(value.amount) : fmt(currentMonitor.amount),
            thresholdFormatted: (value && value.threshold) ? "<b>" + fmt(value.threshold) : fmt(currentMonitor.threshold),
            intervalFormatted: (value && value.interval) ? "<b>" + NRS.escapeRespStr(value.interval) : NRS.escapeRespStr(currentMonitor.interval),
            removeLinkFormatted: "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#remove_monitored_account_modal' " +
                        "data-recipient='" + NRS.escapeRespStr(response.recipientRS) + "' " +
                        "data-property='" + NRS.escapeRespStr(response.property) + "' " +
                        "data-value='" + NRS.normalizePropertyValue(response.value) + "'>" + $.t("remove") + "</a>"
        };
    };

    NRS.incoming.funding_monitors = function() {
        NRS.loadPage("funding_monitors");
    };

    NRS.pages.funding_monitors = function () {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('funding_monitors_page', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            monitors: []
        });
        var params = {
            "account": NRS.accountRS,
            "adminPassword": NRS.getAdminPassword(),
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage,
            "holding": NRS.getActiveChainId(),
            "includeHoldingInfo": true
        };
        NRS.sendRequest("getFundingMonitor", params,
            function (response) {
                if (NRS.isErrorResponse(response)) {
                    view.render({
                        errorMessage: NRS.getErrorMessage(response),
                        isLoading: false,
                        isEmpty: false
                    });
                    return;
                }
                if (response.monitors.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.monitors.pop();
                }
                view.monitors.length = 0;
                response.monitors.forEach(
                    function (monitorJson) {
                        view.monitors.push(NRS.jsondata.monitors(monitorJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.monitors.length == 0
                });
                NRS.pageLoaded();
            }
        )
    };

    $("#start_funding_monitor_modal").on("show.bs.modal", function() {
        let context = {
            labelText: "Currency",
            labelI18n: "currency",
            inputCodeName: "funding_monitor_ms_code",
            inputIdName: "holding",
            inputDecimalsName: "funding_monitor_ms_decimals",
            helpI18n: "add_currency_modal_help"
        };
        NRS.initModalUIElement($(this), '.funding_monitor_holding_currency', 'add_currency_modal_ui_element', context);

        context = {
            labelText: "Asset",
            labelI18n: "asset",
            inputIdName: "holding",
            inputDecimalsName: "funding_monitor_asset_decimals",
            helpI18n: "add_asset_modal_help"
        };
        NRS.initModalUIElement($(this), '.funding_monitor_holding_asset', 'add_asset_modal_ui_element', context);

        if (NRS.getActiveChainId() === "1") {
            $('#funding_monitor_holding_type').change().parent().hide();
        } else {
            $('#funding_monitor_holding_type').change().parent().show();
        }

        // Activating context help popovers - from some reason this code is activated
        // after the same event in nrs.modals.js which doesn't happen for create pool thus it's necessary
        // to explicitly enable the popover here. strange ...
        $(function () {
            $("[data-toggle='popover']").popover({
                "html": true
            });
        });
    });

    $('#funding_monitor_holding_type').change(function () {
        const holdingType = $("#funding_monitor_holding_type");
        if(holdingType.val() === "0") {
            $("#funding_monitor_asset_id_group").css("display", "none");
            $("#funding_monitor_ms_currency_group").css("display", "none");
            $("#start_monitor_amount").attr('name', 'monitorAmountNXT');
            $("#start_monitor_threshold").attr('name', 'monitorThresholdNXT');
        } if(holdingType.val() === "1") {
            $("#funding_monitor_asset_id_group").css("display", "inline");
            $("#funding_monitor_ms_currency_group").css("display", "none");
            $("#start_monitor_amount").attr('name', 'amountQNTf');
            $("#start_monitor_threshold").attr('name', 'thresholdQNTf');
        } else if(holdingType.val() === "2") {
            $("#funding_monitor_asset_id_group").css("display", "none");
            $("#funding_monitor_ms_currency_group").css("display", "inline");
            $("#start_monitor_amount").attr('name', 'amountQNTf');
            $("#start_monitor_threshold").attr('name', 'thresholdQNTf');
        }
    });

    NRS.forms.startFundingMonitor = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        if (data.holdingType === "0") {
            data.holding = NRS.getActiveChainId();
        }
        data.feeRateNQTPerFXT = NRS.convertToNQT(data.feeRateNXTPerFXT);
        delete data.feeRateNXTPerFXT;
        return {
            "data": data
        };
    };

    NRS.forms.startFundingMonitorComplete = function() {
        $.growl($.t("monitor_started"));
        NRS.loadPage("funding_monitors");
    };

    $("#stop_funding_monitor_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        const monitor = $invoker.data("monitor");
        $("#stop_monitor_account").val(monitor.accountRS);
        $("#stop_monitor_property").val(monitor.property);
        $("#stop_monitor_holdingType").val(monitor.holdingType);
        $("#stop_monitor_holding").val(monitor.holding);
        if (NRS.getAdminPassword()) {
            $("#stop_monitor_admin_password").val(NRS.getAdminPassword());
        }
    });

    NRS.forms.stopFundingMonitorComplete = function() {
        $.growl($.t("monitor_stopped"));
        NRS.loadPage("funding_monitors");
    };

    NRS.goToMonitor = function(monitor) {
   		NRS.goToPage("funding_monitor_status", function() {
            return monitor;
        });
   	};

    NRS.incoming.funding_monitors_status = function() {
        NRS.loadPage("funding_monitor_status");
    };

    NRS.pages.funding_monitor_status = function (callback) {
        currentMonitor = callback();
        $("#monitor_funding_account").html(NRS.escapeRespStr(currentMonitor.account));
        $("#monitor_control_property").html(NRS.escapeRespStr(currentMonitor.property));
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('funding_monitor_status_page', {
            errorMessage: null,
            isLoading: true,
            isEmpty: false,
            monitoredAccount: []
        });
        var params = {
            "setter": currentMonitor.account,
            "property": currentMonitor.property,
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        NRS.sendRequest("getAccountProperties", params,
            function (response) {
                if (NRS.isErrorResponse(response)) {
                    view.render({
                        errorMessage: NRS.getErrorMessage(response),
                        isLoading: false,
                        isEmpty: false
                    });
                    return;
                }
                if (response.properties.length > NRS.itemsPerPage) {
                    NRS.hasMorePages = true;
                    response.properties.pop();
                }
                view.monitoredAccount.length = 0;
                response.properties.forEach(
                    function (propertiesJson) {
                        view.monitoredAccount.push(NRS.jsondata.monitoredAccount(propertiesJson))
                    }
                );
                view.render({
                    isLoading: false,
                    isEmpty: view.monitoredAccount.length == 0,
                    fundingAccountFormatted: NRS.getAccountLink(currentMonitor, "account"),
                    controlProperty: currentMonitor.property
                });
                NRS.pageLoaded();
            }
        )
    };

    $("#add_monitored_account_modal").on("show.bs.modal", function() {
        $("#add_monitored_account_property").val(currentMonitor.property);
        $("#add_monitored_account_amount").val(NRS.intToFloat(currentMonitor.amount, currentMonitor.holdingInfo.decimals));
        $("#add_monitored_account_threshold").val(NRS.intToFloat(currentMonitor.threshold, currentMonitor.holdingInfo.decimals));
        $("#add_monitored_account_interval").val(currentMonitor.interval);
        $("#add_monitored_account_value").val("");
        $(this).find(".holding-name").text(currentMonitor.holdingInfo.name);
    });

    $(".add_monitored_account_value").on('change', function() {
        if (!currentMonitor) {
            return;
        }
        var value = {};
        try {
            const amount = NRS.convertToQNT($("#add_monitored_account_amount").val(), currentMonitor.holdingInfo.decimals);
            if (currentMonitor.amount !== amount) {
                value.amount = amount;
            }
            const threshold = NRS.convertToQNT($("#add_monitored_account_threshold").val(), currentMonitor.holdingInfo.decimals);
            if (currentMonitor.threshold !== threshold) {
                value.threshold = threshold;
            }
            const interval = $("#add_monitored_account_interval").val();
            if (currentMonitor.interval != interval) {
                value.interval = interval;
            }
            if (jQuery.isEmptyObject(value)) {
                value = "";
            } else {
                value = JSON.stringify(value);
            }
            $("#add_monitored_account_value").val(value);
            $(".error_message").hide();
        } catch (e) {
            $(".error_message").text(e).show();
        }
    });

    $("#remove_monitored_account_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        $("#remove_monitored_account_recipient").val($invoker.data("recipient"));
        $("#remove_monitored_account_property").val($invoker.data("property"));
        $("#remove_monitored_account_value").val(NRS.normalizePropertyValue($invoker.data("value")));
    });

    return NRS;

}(NRS || {}, jQuery));