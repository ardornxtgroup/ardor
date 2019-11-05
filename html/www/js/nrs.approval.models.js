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
    let _password = null;
	var approvalModels = {};

	NRS.setApprovalModelsPassword = function (password) {
        _password = password;
    };

	NRS.loadApprovalModels = function() {
		approvalModels = NRS.getJSONItem("approvalModels");
		if (!approvalModels) {
			approvalModels = {};
            NRS.setJSONItem("approvalModels", approvalModels);
		}
	};

    function loadApprovalModels() {
        $("#approval_approval_table_container").show();
        var rows = "";
        if (!NRS.isJSONObjectEmpty(approvalModels)) {
            Object.keys(approvalModels).sort().forEach(function(name) {
                name = String(name).escapeHTML();
                var approvalModel = approvalModels[name];
                var description = String(approvalModel.description).escapeHTML();
                if (description && description.length > 100) {
                    description = description.substring(0, 100) + "...";
                } else if (!description) {
                    description = "-";
                }
                rows += "<tr>" +
                    "<td>" + name + "</td>" +
                    "<td>" + description + "</td>" +
                    "<td>" + NRS.getVotingModelName(approvalModel.phasingVotingModel) + "</td>" +
                    "<td>" +
                        "<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#view_approval_model_modal' data-name='" + name + "'>" + $.t("view") + "</a>" +
                        "<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#rename_approval_model_modal' data-name='" + name + "'>" + $.t("rename") + "</a>" +
                        "<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#delete_approval_model_modal' data-name='" + name + "'>" + $.t("delete") + "</a>" +
                    (approvalModel.secretNonce ? ("<a class='btn btn-xs btn-default' href='#' data-toggle='modal' data-target='#reproduce_secret_modal' data-name='" + name + "'>" + $.t("reproduce_secret") + "</a>") : "") +
                    "</td>" +
                    "</tr>";
            });
        }
        NRS.dataLoaded(rows);
    }

    NRS.pages.approval_models = function() {
        loadApprovalModels();
	};

    $("#add_approval_model_modal").on("shown.bs.modal", function() {
        $(this).find('#approval_model_modal a:first').click(); // Set the default tab
    });

    $("#approval_model_modal a.at_hash").on('shown.bs.tab', () => {
        if (NRS.rememberPassword) {
            $('#add_approval_model_password').val(_password).change();
        }
    });

	NRS.forms.addApprovalModel = function($modal) {
		var data = NRS.getFormData($modal.find("form:first"));
		if (data.secretPhrase === "") {
		    delete data.secretPhrase;
        }
		var name = NRS.escapeRespStr(data.name);
		var description = NRS.escapeRespStr(data.description);
		var nonce = data.secretNonce;
		var blockId = data.secretBlockId;
		delete data.name;
		if (!name) {
			return { error: $.t("enter_unique_approval_model_name")}
		}
		if (name.length > 5) {
			return { error: $.t("approval_model_name_length", { length: 5 })}
		}
        if (approvalModels[name]) {
			return { error: $.t("approval_model_already_exists", { name: name })}
		}
		var rc = { stop: true };
		if (data.phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
            NRS.sendRequest("evaluateExpression", { expression: data.phasingExpression }, function(response) {
                if (response.errorCode) {
                    var error = NRS.unescapeRespStr(response.errorDescription);
                    if (response.subPoll) {
                        error += " " + $.t("for_sub_poll") + " " + response.subPoll;
                    }
                    rc = { error: error };
                    return
                }
                delete response.requestProcessingTime;
                if (response.variables) {
                    var subPolls = {};
                    for (var i=0; i < response.variables.length; i++) {
                        var variable = response.variables[i].trim();
                        if (variable == name) {
                            rc = { error: $.t("composite_model_reference_to_itself", { variable: variable }) };
                            return;
                        }
                        if (!approvalModels[variable]) {
                            rc = { error: $.t("composite_model_unknown_variable", { variable: variable }) };
                            return;
                        }
                        if (approvalModels[variable].phasingVotingModel == NRS.constants.VOTING_MODELS.COMPOSITE) {
                            rc = { error: $.t("composite_model_recursive_reference", { variable: variable }) };
                            return;
                        }
                        subPolls[variable] = JSON.stringify(approvalModels[variable]);
                    }
                    data.phasingSubPolls = JSON.stringify(subPolls);
                }
            }, { isAsync: false });
        }
        if (rc.error) {
		    return rc;
        }
        NRS.sendRequest("parsePhasingParams", data, function(response) {
        	if (response.errorCode) {
                var error = NRS.unescapeRespStr(response.errorDescription);
                if (response.subPoll) {
                    error += " " + $.t("for_sub_poll") + " " + response.subPoll;
                }
                rc = { error: error };
        		return
			}
            delete response.requestProcessingTime;
			response.description = description;
			if (nonce) {
                response.secretNonce = nonce;
                response.secretBlockId = blockId;
            }
			if (response.phasingExpression) {
                response.phasingExpression = NRS.unescapeRespStr(response.phasingExpression);
            }
        	approvalModels[name] = response;
        	NRS.setJSONItem("approvalModels", approvalModels);
        	loadApprovalModels();
        }, { isAsync: false });
        return rc;
	};

	$("#view_approval_model_modal").on("show.bs.modal", function(e) {
        NRS.setBackLink();
		var $invoker = $(e.relatedTarget);
        var name = $invoker.data("name");
        var model = $invoker.data("model");
        if (!model) {
            if ($invoker.data("show-asset-control")) {
                model = NRS.getCurrentAssetControl();
            } else {
                model = approvalModels[name];
            }

        }
        $("#view_approval_model_name").val(name);
		$("#view_approval_model_content").val(JSON.stringify(model, null, 2));
	});

	$("#delete_approval_model_modal").on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
        $("#delete_approval_model_name").val($invoker.data("name"));
	});

    $("#rename_approval_model_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        $("#old_approval_model_name").val($invoker.data("name"));
        $("#new_approval_model_name").val($invoker.data("name"));
    });

	NRS.forms.deleteApprovalModel = function() {
		var name = $("#delete_approval_model_name").val();
		delete approvalModels[name];
        NRS.setJSONItem("approvalModels", approvalModels);
        loadApprovalModels();
        return { stop: true };
	};

    NRS.forms.renameApprovalModel = function() {
        var oldName = $("#old_approval_model_name").val();
        var newName = $("#new_approval_model_name").val();
        if (oldName != newName) {
            if (newName in approvalModels) {
                $.growl($.t("approval_model_already_exists", {"name" : newName}), {"type":"danger"}).show();
            } else {
                approvalModels[newName] = approvalModels[oldName];
                delete approvalModels[oldName];
                NRS.setJSONItem("approvalModels", approvalModels);
                loadApprovalModels();
            }

        }
        return { stop: true };
    };

    NRS.forms.importAssetControl = function ($modal) {
        var data = NRS.getFormData($modal.find("form:first"));
        NRS.sendRequest("getPhasingAssetControl", { 'asset': data.id }, function (response) {
            var model = {
                "ASC": $.extend({
                        "description": $.t("imported_from_asset_description", { id: response.asset }  )
                    }, response.controlParams)
            };
            NRS.importApprovalModels(model);
            loadApprovalModels();
            $.growl($.t("done"));
        }, { isAsync: false });
        return { stop: true }
    };

	NRS.exportApprovalModels = function() {
		if (!NRS.isJSONObjectEmpty(approvalModels)) {
		    if (window.java) {
		        window.java.downloadTextFile(JSON.stringify(approvalModels, null, 2), 'approval.models.json');
            } else {
                var models_download = document.createElement('a');
                models_download.href = 'data:text/json,' + JSON.stringify(approvalModels, null, 2);
                models_download.target = '_blank';
                models_download.download = 'approval.models.json';
                document.body.appendChild(models_download);
                models_download.click();
                document.body.removeChild(models_download);
            }
		} else {
			$.growl($.t("error_no_models_available"), {"type":"warning"}).show();
		}
	};

    $("#add_approval_model_evaluate_button").on("click", function(e) {
        e.preventDefault();
        var expression = $("#approved_by_boolean_expression").val();
        var $evaluation = $("#approved_by_boolean_expression_evaluation");
        NRS.sendRequest("evaluateExpression", { expression: expression, checkOptimality: "true" }, function(response) {
            if (response.errorCode) {
                var errorDesc = NRS.unescapeRespStr(response.errorDescription);

                if ("semanticWarnings" in response) {
                    errorDesc += "\n\n";
                    for (var warning in response.semanticWarnings) {
                        errorDesc += NRS.unescapeRespStr(response.semanticWarnings[warning]) + "\n";
                    }
                }
                $evaluation.val(errorDesc);
                return
            }
            delete response.requestProcessingTime;
            var $variablesContainer = $("#add_approval_model_variables");
            $variablesContainer.empty();
            if (response.variables) {
                for (var i=0; i < response.variables.length; i++) {
                    var variable = response.variables[i].trim();
                    $variablesContainer.append("<span>").append(variable).append("</span>&nbsp");
                    if (approvalModels[variable]) {
                        $variablesContainer.append(
                            $("<span></span>").css("color", "green").text(approvalModels[variable].description)
                        );
                    } else {
                        $variablesContainer.append(
                            $("<span></span>").css("color", "red").text($.t("undefined"))
                        );
                    }
                    $variablesContainer.append("<br>")
                }
            }
			$evaluation.val(JSON.stringify(response, null, 2));
        });
    });

    $("#approved_by_boolean_expression").on("change", function() {
        $("#approved_by_boolean_expression_evaluation").val("");
    });

	$("#export_approval_models_button").on("click", function() {
		NRS.exportApprovalModels();
	});

	NRS.importApprovalModels = function(imported_models) {
		$.each(imported_models, function(name, model) {
			approvalModels[name] = model;
		});
        NRS.setJSONItem("approvalModels", approvalModels);
	};

	var importModelsButtonField = $("#import_approval_models_button_field");
    importModelsButtonField.css({'display':'none'});
	importModelsButtonField.on("change", function(button_event) {
		button_event.preventDefault();
		var importModelsButtonField = $("#import_approval_models_button_field");
		var file = importModelsButtonField[0].files[0];
		var reader = new FileReader();
		reader.onload = function (read_event) {
			var imported_models = JSON.parse(read_event.target.result);
			NRS.importApprovalModels(imported_models);
            loadApprovalModels();
            $.growl($.t("approval_models_were_imported_from_file"));
		};
		reader.readAsText(file);
		importModelsButtonField.replaceWith(importModelsButtonFieldClone.clone(true) ); // Recreate button to clean it
		return false;
	});
    var importModelsButtonFieldClone = importModelsButtonField.clone(true);

	$("#import_approval_models_button").on("click", function() {
		if (NRS.isFileReaderSupported()) {
            $("#import_approval_models_button_field").click();
        } else if (window.java) {
            const result = window.java.readApprovalModelsFile();
            if (result !== null) {
                let models;
                try {
                    models = JSON.parse(result);
                } catch (e) {
                    NRS.logConsole(e);
                    $.growl($.t('cannot_parse_json'), {type:'warning'});
                }
                if (models.error) {
                    $.growl($.t('error') + ': ' + models.error, {type:'error'});
                } else {
                    const values = Object.values(models);
                    if (!values || values.length === 0 || values[0].phasingHolding === undefined) {
                        $.growl($.t('wrong_file_format'), {type:'warning'});
                    } else {
                        NRS.importApprovalModels(models);
                        loadApprovalModels();
                        $.growl($.t("approval_models_were_imported_from_file"));
                    }
                }
            }
        }
	});

    $("#import_account_control_approval_models_button").on("click", function() {
        if (NRS.accountInfo && NRS.accountInfo.phasingOnly && NRS.accountInfo.phasingOnly.controlParams) {
            var model = {
                "ACC": $.extend({
                        "description": $.t("imported_from_account_description", { id: NRS.accountRS }  )
                    }, NRS.accountInfo.phasingOnly.controlParams)
            };
            NRS.importApprovalModels(model);
            loadApprovalModels();
            $.growl($.t("done"));
        } else {
            $.growl($.t("import_error_no_account_control"), {"type": "danger"});
        }
    });

    function hashSecret(secret, algorithm, callback) {
        NRS.sendRequest("hash", {
            "hashAlgorithm": algorithm,
            "secret": secret
        }, function (response) {
            if (response.errorCode) {
                $.growl(response.errorDescription);
            } else {
                callback(response.hash);
            }
        });
    }

    $("#add_approval_model_password").change(function() {
        const secretPhrase = $(this).val();
        if (NRS.accountRS != NRS.getAccountId(secretPhrase, true)) {
            $.growl($.t("generate_secret_wrong_passphrase", { account: NRS.accountRS } ));
            return;
        }
        NRS.generateSecret(secretPhrase, function (result) {
            if (result.error) {
                $.growl(result.error)
            } else {
                $("#approved_by_hashed_secret_nonce").val(result.nonce);
                $("#approved_by_hashed_secret_block_id").val(result.blockId);
                $("#approved_by_hashed_secret_generated_secret").val(result.secret);
                hashSecret(result.secret, $(".hash_algorithm_model_group").find("select").val(), function(hash) {
                    $("#approved_by_hashed_secret").val(hash);
                });
            }
        });
    });

    $(".hash_algorithm_model_group").change(function() {
        var generatedSecret = $("#approved_by_hashed_secret_generated_secret").val();
        if (generatedSecret == "") {
            return;
        }
        hashSecret(generatedSecret, $(".hash_algorithm_model_group").find("select").val(), function(hash) {
            $("#approved_by_hashed_secret").val(hash);
        });
    });

    NRS.loadApprovalModelsList = function ($approvalModelSelect, isPhasing) {
        $approvalModelSelect.append($("<option />").val("").text($.t("none")));
        for (var key in approvalModels) {
            if (!approvalModels.hasOwnProperty(key)) {
                continue;
            }
            var votingModel = approvalModels[key].phasingVotingModel;
            if (!isPhasing && (votingModel == 4 || votingModel == 5)) {
                // Setting control by transaction or by hash is not allowed
                continue;
            }
            $approvalModelSelect.append($("<option />").val(key).text(key + " - " + approvalModels[key].description));
        }
    };

    NRS.processApprovalModel = function(data) {
        if (data.phasingApprovalModel !== undefined) {
            if (data.phasingApprovalModel == "" || !approvalModels[data.phasingApprovalModel]) {
                delete data.phased;
                // delete data.phasingFinishHeight; TODO breaks account control
                delete data.phasingParams;
                delete data.phasingApprovalModel;
            } else {
                data.phasingParams = JSON.stringify(approvalModels[data.phasingApprovalModel]);
                data.phased = "true";
            }
        }

        if (data.controlApprovalModel !== undefined) {
            if (data.controlApprovalModel == "" || !approvalModels[data.controlApprovalModel]) {
                delete data.controlParams;
                delete data.controlApprovalModel;
                data.controlVotingModel = "-1"; // to remove existing control
            } else {
                data.controlParams = JSON.stringify(approvalModels[data.controlApprovalModel]);
            }
        }
    };

    $("#reproduce_secret_modal").on("show.bs.modal", function(e) {
        var $invoker = $(e.relatedTarget);
        var name = $invoker.data("name");
        var model = approvalModels[name];
        $("#reproduce_secret_name").val(name);
        $("#reproduce_secret_nonce").val(model.secretNonce);
        $("#reproduce_secret_block_id").val(model.secretBlockId);
        $("#reproduce_secret_hashed_secret").val(model.phasingHashedSecret);
        $("#reproduce_secret_hash_algorithm").val(NRS.getHashAlgorithm(model.phasingHashedSecretAlgorithm));
        if (NRS.rememberPassword) {
            $('#reproduce_secret_password').val(_password).change();
        }
        NRS.sendRequest("getHashedSecretPhasedTransactions", {
            phasingHashedSecret: model.phasingHashedSecret,
            phasingHashedSecretAlgorithm: model.phasingHashedSecretAlgorithm
        }, function (response) {
            var transactions = "";
            for (var i = 0; i < response.transactions.length; i++) {
                var transaction = response.transactions[i];
                if (transaction.approved) {
                    continue;
                }
                transactions += NRS.getTransactionLink(transaction.fullHash, null, false, transaction.chain) + "<br>";
            }
            $("#reproduce_secret_transactions").html(transactions);
        });
    });

    $("#reproduce_secret_password").change(function() {
        var secretPhrase = $("#reproduce_secret_password").val();
        var calculateSecret = NRS.calculateSecret(secretPhrase, $("#reproduce_secret_nonce").val(), $("#reproduce_secret_block_id").val());
        $("#reproduce_secret_generated_secret").val(calculateSecret);
        var $modal = $("#reproduce_secret_modal");
        hashSecret(calculateSecret, NRS.constants.PHASING_HASH_ALGORITHMS[$("#reproduce_secret_hash_algorithm").val()], function (hash) {
            if (hash == $("#reproduce_secret_hashed_secret").val()) {
                $modal.find(".error_message").html("").hide();
                $modal.find(".info_message").html($.t("secret_reproduced")).show();
            } else {
                $modal.find(".error_message").html($.t("secret_does_not_match")).show();
                $modal.find(".info_message").html("").hide();
            }
        })
    });

    NRS.getApprovalModel = function(name) {
        return approvalModels[name];
    };

    return NRS;
}(NRS || {}, jQuery));