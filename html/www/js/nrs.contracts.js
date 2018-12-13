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
var NRS = (function(NRS) {

    NRS.pages.contracts = function() {
        NRS.renderContracts();
	};

    NRS.renderContracts = function () {
        NRS.hasMorePages = false;
        var view = NRS.simpleview.get('contracts_page', {
            errorMessage: null,
            infoMessage: null,
            isLoading: true,
            isEmpty: false,
            contracts: [],
            loadingDotsClass: "loading_dots"
        });
        var params = {
            "adminPassword": NRS.getAdminPassword(),
            "firstIndex": NRS.pageNumber * NRS.itemsPerPage - NRS.itemsPerPage,
            "lastIndex": NRS.pageNumber * NRS.itemsPerPage
        };
        NRS.sendRequest("getSupportedContracts", params,
            function(response) {
                if (NRS.isErrorResponse(response)) {
                    view.render({
                        errorMessage: NRS.getErrorMessage(response),
                        isLoading: false,
                        isEmpty: false,
                        contracts_header_visibility: "none",
                        contracts_table_visibility: "none"
                    });
                    return;
                }
                var header = {
                    "status": response.status,
                    "contractRunnerAccountFormatted": NRS.getAccountLink(response, "contractRunnerAccount"),
                    "hasSecretPhrase": response.hasSecretPhrase,
                    "isValidator": response.isValidator,
                    "hasValidatorSecretPhrase": response.hasValidatorSecretPhrase,
                    "hasRandomSeed": response.hasRandomSeed
                };
                if (!response.supportedContracts) {
                    view.render(Object.assign({
                        isLoading: false,
                        isEmpty: true,
                        errorMessage: "",
                        header_visibility: "inline",
                        contracts_table_visibility: "none"
                    }, header));
                    return;
                }
                view.contracts.length = 0;
                response.supportedContracts.forEach(
                    function(contractsJson) {
                        view.contracts.push(NRS.jsondata.contracts(contractsJson, response.contractRunnerAccountRS));
                    }
                );
                view.render(Object.assign({
                    status: response.status,
                    isLoading: false,
                    isEmpty: view.contracts.length == 0,
                    loadingDotsClass: "",
                    contracts_header_visibility: "inline",
                    contracts_table_visibility: "inline"
                }, header));
                NRS.pageLoaded();
            }
        );
    };

    NRS.jsondata.contracts = function(contract, account) {
        var isTransactionInvocation = false;
        for (var i=0; i<contract.invocationTypes.length; i++) {
            if (contract.invocationTypes[i].type == "TRANSACTION") {
                isTransactionInvocation = true;
                break;
            }
        }
        return {
            nameFormatted: NRS.getEntityLink({ request: "getContractReferences", key: "contractName", id: contract.contractReference.name, key2: "account", id2: account, responseArray: "contractReferences"}),
            accountFormatted: NRS.getTransactionLink(contract.uploadTransaction.fullHash, contract.uploadTransaction.senderRS, contract.uploadTransaction.chain),
            paramsLink: isTransactionInvocation ? "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#view_contract_params_modal' " +
                "data-params='" + JSON.stringify(contract.supportedInvocationParams, null, 2) + "' " +
                "data-name='" + NRS.escapeRespStr(contract.name) + "'>" + $.t("parameters") + "</a>" : "",
            validatyChecksLink: contract.validityChecks.length == 0 ? "" : "<a href='#' class='btn btn-xs' data-toggle='modal' data-target='#view_contract_validity_checks_modal' " +
                "data-validity-checks='" + JSON.stringify(contract.validityChecks, null, 2) + "' " +
                "data-name='" + NRS.escapeRespStr(contract.name) + "'>" + $.t("validity_checks") + "</a>"
        };
    };

    NRS.incoming.contracts = function() {
        NRS.renderContracts();
    };

    $("#config_contract_runner_modal").on("show.bs.modal", function() {
        var $modal = $(this).closest(".modal");
        var $form = $modal.find("form:first");
        $form.find("input[name=adminPassword]").val(NRS.getAdminPassword());
    });

    NRS.forms.uploadContractRunnerConfigurationComplete = function() {
        NRS.renderContracts();
    };

    $("#view_contract_params_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var name = $invoker.data("name");
        $("#view_contract_params_name").val(name);
        var params = $invoker.data("params");
        $("#view_contract_params_content").val(JSON.stringify(params, null, 2));
        var template = { contract: name };
        var paramsTemplate = {};
        for (var i=0; i<params.length; i++) {
            var param = params[i];
            if (param.type === "nxt.addons.JO") {
                paramsTemplate[param.name] = {"[key]": "[value]"};
            } else {
                var typeNameParts = param.type.split(".");
                paramsTemplate[param.name] = "[" + typeNameParts[typeNameParts.length - 1] + "]";
            }
        }
        template["params"] = paramsTemplate;
        $("#view_contract_params_template").val(JSON.stringify(template, null, 2));
    });

    $("#view_contract_validity_checks_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var name = $invoker.data("name");
        $("#view_contract_validity_checks_name").val(name);
        var validityChecks = $invoker.data("validityChecks");
        $("#view_contract_validity_checks_content").val(JSON.stringify(validityChecks, null, 2));
    });

	return NRS;
}(NRS || {}, jQuery));