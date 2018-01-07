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

    var IBAN_REGEX = /LT[0-9]{2}35100[0-9]{11}/;

    var RESERVE_ACCOUNT_ID = "ARDOR-NJ92-R5GB-HQB4-6NW7T";
    var RESERVE_ACCOUNT_ID_TESTNET = "ARDOR-DHKD-5ETL-JU9T-GSKPG";

    var UI_CONFIG_ACCOUNT_ID = "ARDOR-HJAA-ZDYB-PSKX-DGG53";
    var UI_CONFIG_ACCOUNT_ID_TESTNET = "ARDOR-DHKD-5ETL-JU9T-GSKPG";

    var MISTERTANGO_DECIMALS = 2;
    var uiConfig = null;

//    {
//        "commissionPercent": "1.1", //percent with up to 2 decimals precision
//        "minCommission": "1", //AUER
//        "minAmount": "0.02", //AUER
//        "maxAmount": "10000" //AUER
//    };

    function updateUI() {
        if (uiConfig) {
            var amountValue = $("#withdraw_aeur_amount").val();
            var commission = "";
            var finalAmount = "";
            $("#amount_info_min_max").css("color", "green");
            if (amountValue) {
                var amountNQT = new BigInteger(NRS.convertToNQT(amountValue));

                var minNQT = new BigInteger(NRS.convertToNQT(uiConfig.minAmount));
                var maxNQT = new BigInteger(NRS.convertToNQT(uiConfig.maxAmount));
                if (amountNQT.compareTo(minNQT) >= BigInteger.ZERO && amountNQT.compareTo(maxNQT) <= BigInteger.ZERO) {
                    //commission percent precision - 2 decimals
                    var percentInt = new BigInteger(NRS.floatToInt(uiConfig.commissionPercent, 2));
                    var commissionFloat = parseInt(amountNQT.multiply(percentInt).toString()) / 10000;

                    var aeurDecimals = NRS.getActiveChainDecimals();
                    if (aeurDecimals > MISTERTANGO_DECIMALS) {
                        var roundingInteger = Math.pow(10, aeurDecimals - MISTERTANGO_DECIMALS);
                        commissionFloat = Math.ceil(commissionFloat / roundingInteger) * roundingInteger;
                    }
                    var percentCommissionNQT = nbv(Math.ceil(commissionFloat));

                    var commissionNQT = percentCommissionNQT.max(new BigInteger(NRS.convertToNQT(uiConfig.minCommission)));

                    var finalAmountNQT = amountNQT.subtract(commissionNQT);

                    commission = NRS.formatAmount(commissionNQT);
                    finalAmount = NRS.formatAmount(finalAmountNQT);
                } else {
                    $("#amount_info_min_max").css("color", "red");
                }
            }
            varMinMaxStr = $.t("mrtango_amount_min_max", {"min": NRS.escapeRespStr(uiConfig.minAmount), "max": NRS.escapeRespStr(uiConfig.maxAmount)});
            $("#amount_info_min_max").text(varMinMaxStr);

            $("#amount_info_commission").text($.t("mrtango_commission", {"commission": NRS.escapeRespStr(commission)}));
            $("#amount_info_received").text($.t("mrtango_final_amount", {"euro": NRS.escapeRespStr(finalAmount)}));
        } else {
            $("#amount_info_min_max").text("");
            $("#amount_info_commission").text("");
            $("#amount_info_received").text("");
        }

    }

    $("#withdraw_aeur_modal").on("show.bs.modal", function (e) {

        if (uiConfig) {
            updateUI();
        } else {
            var configAccount = NRS.isTestNet ? UI_CONFIG_ACCOUNT_ID_TESTNET : UI_CONFIG_ACCOUNT_ID;
            var params = {
                        "setter": configAccount,
                        "recipient": configAccount,
                        "property": "uiConfig"
                    };
            NRS.sendRequest("getAccountProperties", params,
                function (response) {
                    if ($.isArray(response.properties) && response.properties.length > 0) {
                        uiConfig = JSON.parse(NRS.unescapeRespStr(response.properties[0].value));
                    }
                    updateUI();
                });
        }
    });

    $("#withdraw_aeur_amount").keyup(function () {
        updateUI();
    });

    NRS.forms.mistertangoWithdraw = function($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        var iban = data.recipientIBAN;
        if (!iban) {
            return { error: $.t("mrtango_iban_missing")}
        }
        var recipientName = data.recipientName;
        if (!recipientName) {
            return { error: $.t("mrtango_recipient_name")}
        }

        if (!IBAN_REGEX.test(iban)) {
            return { error: $.t("mrtango_iban_invalid_format")}
        }

        if (!IBAN.isValid(iban)) {
            return { error: $.t("mrtango_iban_checksum_failed")}
        }

        var minNQT = new BigInteger(NRS.convertToNQT(uiConfig.minAmount));
        var maxNQT = new BigInteger(NRS.convertToNQT(uiConfig.maxAmount));
        var amountNQT = new BigInteger(NRS.convertToNQT(data.amountAEUR));
        if (amountNQT.compareTo(minNQT) < 0) {
            return { error: $.t("mrtango_too_small_amount", {"min": NRS.escapeRespStr(uiConfig.minAmount)})}
        }

        if (amountNQT.compareTo(maxNQT) > 0) {
            return { error: $.t("mrtango_too_big_amount", {"max": NRS.escapeRespStr(uiConfig.maxAmount)})}
        }

        var aeurDecimals = NRS.getActiveChainDecimals();
        if (aeurDecimals > MISTERTANGO_DECIMALS) {

            var remainder = amountNQT.remainder(new BigInteger("" + Math.pow(10, aeurDecimals - MISTERTANGO_DECIMALS)));
            if (remainder.compareTo(BigInteger.ZERO) != 0) {
                return { error: $.t("mrtango_unsupported_precision")}
            }
        }

        data.recipient = NRS.isTestNet ? RESERVE_ACCOUNT_ID_TESTNET : RESERVE_ACCOUNT_ID;
        data.amountNXT = data.amountAEUR;

        data.add_message = true;
        data.encrypt_message = true;
        data.message = iban + ":" + recipientName;

        return {
            "requestType": "sendMoney",
            "data": data
        };
    };

    return NRS;
}(NRS || {}, jQuery));
