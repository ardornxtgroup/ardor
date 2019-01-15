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
	NRS.automaticallyCheckRecipient = function() {
        var $recipientFields = $("#send_money_recipient, #transfer_asset_recipient, #transfer_currency_recipient, " +
        "#send_message_recipient, #add_contact_account_id, #update_contact_account_id, #lease_balance_recipient, " +
        "#transfer_alias_recipient, #sell_alias_recipient, #set_account_property_recipient, #delete_account_property_recipient, " +
		"#add_monitored_account_recipient");

		$recipientFields.on("blur", function() {
			$(this).trigger("checkRecipient");
		});

		$recipientFields.on("checkRecipient", function() {
			var value = $(this).val();
			var modal = $(this).closest(".modal");

			if (value && value != NRS.getAccountMask("_")) {
				NRS.checkRecipient(value, modal);
			} else {
				modal.find(".account_info").hide();
			}
		});

		$recipientFields.on("oldRecipientPaste", function() {
			var modal = $(this).closest(".modal");
			var callout = modal.find(".account_info").first();
			callout.removeClass("callout-info callout-danger callout-warning").addClass("callout-danger").html($.t("error_numeric_ids_not_allowed")).show();
		});

		$(".recipient_contract_reference_selector").find("select").on("change", function() {
			if (!$currentModal) {
				return;
			}
			var contractName = $currentModal.find(".recipient_contract_reference_selector").find("select").val();
            var $messageInput = $currentModal.find("textarea[name=message]");
			var $permanentMessageCheckbox = $currentModal.find("input[name=permanent_message]");
			var $addMessageCheckbox = $currentModal.find("input[name=add_message]");

			if (contractName == "") {
                $messageInput.val("");
				$permanentMessageCheckbox.prop("disabled", false);
                $addMessageCheckbox.prop("checked", false);
                $addMessageCheckbox.trigger("change");
				return;
			}
			var account = $currentModal.find("input[name=converted_account_id]").val();
			if (account === "") {
				account = $currentModal.find("input[name=recipient]").val();
			}
			NRS.sendRequest("getContractReferences", { account: account, contractName: contractName, includeContract: true }, function(response) {
				if (!response.contractReferences || response.contractReferences.length != 1) {
                    $messageInput.val("");
					$permanentMessageCheckbox.prop("disabled", false);
                    $addMessageCheckbox.prop("checked", false);
                    $addMessageCheckbox.trigger("change");
					return;
				}
				var contract = response.contractReferences[0].contract;
				var paramsTemplate = NRS.getInvocationParamsTemplate(contractName, contract.supportedInvocationParams);
                $messageInput.val(JSON.stringify(paramsTemplate, null, 2));
				$permanentMessageCheckbox.prop("checked", false);
				$permanentMessageCheckbox.prop("disabled", true);
                $addMessageCheckbox.prop("checked", true);
                $addMessageCheckbox.trigger("change");
			});
		})
	};

	var $currentModal;
	$(".has-recipient").on("show.bs.modal", function(e) {
		var $invoker = $(e.relatedTarget);
		$currentModal = $(this);
		var account = $invoker.data("account");
		if (!account) {
			account = $invoker.data("contact");
		}
		if (account) {
			var $inputField = $(this).find("input[name=recipient], input[name=account_id]").not("[type=hidden]");
			if (!NRS.isRsAccount(account)) {
				$inputField.addClass("noMask");
			}
			$inputField.val(account).trigger("checkRecipient");
		}
	});

	//todo later: http://twitter.github.io/typeahead.js/
	var $allModals = $(".modal");
    $allModals.on("click", "span.recipient_selector button, span.plain_address_selector button", function(e) {
		if (!Object.keys(NRS.contacts).length && !NRS.getStrItem("savedNxtAccounts")) {
			e.preventDefault();
			e.stopPropagation();
			return;
		}
		var $list = $(this).parent().find("ul");
		$list.empty();
		for (var accountId in NRS.contacts) {
			if (!NRS.contacts.hasOwnProperty(accountId)) {
				continue;
			}
			$list.append("<li><a href='#' data-contact-id='" + accountId + "' data-contact='" + String(NRS.contacts[accountId].name).escapeHTML() + "'>" + String(NRS.contacts[accountId].name).escapeHTML() + "</a></li>");
		}
        var accounts = NRS.getStrItem("savedNxtAccounts").split(";");
        for (var index in accounts) {
        	var account = accounts[index];
        	if (account === "" || NRS.contacts[account]) {
        		continue;
			}
            $list.append("<li><a href='#' data-contact-id='" + account + "' data-contact='" + account + "'>" + account + "</a></li>");
        }
	});

	$allModals.on("click", "span.recipient_selector ul li a", function(e) {
		e.preventDefault();
		$(this).closest("form").find("input[name=converted_account_id]").val("");
		$(this).closest("form").find("input[name=recipient],input[name=account_id]").not("[type=hidden]").trigger("unmask").val($(this).data("contact")).trigger("blur");
	});

	$allModals.on("click", "span.plain_address_selector ul li a", function(e) {
		e.preventDefault();
		$(this).closest(".input-group").find("input.plain_address_selector_input").not("[type=hidden]").trigger("unmask").val($(this).data("contact-id")).trigger("blur");
	});

	$allModals.on("keyup blur show", ".plain_address_selector_input", function() {
		var currentValue = $(this).val();
		var contactInfo;
		if (NRS.contacts[currentValue]) {
			contactInfo = NRS.contacts[currentValue]['name'];
		} else {
			contactInfo = " ";
		}
		$(this).closest(".input-group").find(".pas_contact_info").text(contactInfo);
	});

	NRS.forms.sendMoneyComplete = function(response, data) {
		if (!(data["_extra"] && data["_extra"].convertedAccount) && !(data.recipient in NRS.contacts)) {
			$.growl($.t("success_send_money", { "coin": NRS.getActiveChainName() }) + " <a href='#' data-account='" + NRS.getAccountFormatted(data, "recipient") + "' data-toggle='modal' data-target='#add_contact_modal' style='text-decoration:underline'>" + $.t("add_recipient_to_contacts_q") + "</a>", {
				"type": "success"
			});
		} else {
			$.growl($.t("send_money_submitted"), {
				"type": "success"
			});
		}
	};

	NRS.checkAccountStatus = function(accountId, callback) {
		NRS.sendRequest("getAccount", {
			"account": accountId
		}, function(response) {
            NRS.sendRequest("getBalance", {
                "account": accountId,
                "chain": NRS.getActiveChainId()
            }, function (balance) {
                var status;
                if (response.publicKey) {
                    if (response.name) {
                        status = {
                            "type": "info",
                            "message": $.t("recipient_info_with_name", {
                                "name": NRS.unescapeRespStr(response.name),
                                "nxt": NRS.formatAmount(balance.unconfirmedBalanceNQT, false, true),
								"coin": NRS.getActiveChainName()
                            }),
                            "account": response
                        };
                    } else {
                        status = {
                            "type": "info",
                            "message": $.t("recipient_info", {
                                "nxt": NRS.formatAmount(balance.unconfirmedBalanceNQT, false, true),
                                "coin": NRS.getActiveChainName()
                            }),
                            "account": response
                        };
                    }
                } else {
                    if (response.errorCode) {
                        if (response.errorCode == 4) {
                            status = {
                                "type": "danger",
                                "message": $.t("recipient_malformed") + (!NRS.isRsAccount(accountId) ? " " + $.t("recipient_alias_suggestion") : ""),
                                "account": null
                            };
                        } else if (response.errorCode == 5) {
                            status = {
                                "type": "warning",
                                "message": $.t("recipient_unknown_pka"),
                                "account": null,
                                "noPublicKey": true
                            };
                        } else {
                            status = {
                                "type": "danger",
                                "message": $.t("recipient_problem") + " " + NRS.unescapeRespStr(response.errorDescription),
                                "account": null
                            };
                        }
                    } else {
                        status = {
                            "type": "warning",
                            "message": $.t("recipient_no_public_key_pka", {
                                "nxt": NRS.formatAmount(balance.unconfirmedBalanceNQT, false, true),
                                "coin": NRS.getActiveChainName()
                            }),
                            "account": response,
                            "noPublicKey": true
                        };
                    }
                }
                status.message = status.message.escapeHTML();
                callback(status);
            });
        });
	};

	NRS.correctAddressMistake = function(el) {
		$(el).closest(".modal-body").find("input[name=recipient],input[name=account_id]").val($(el).data("address")).trigger("blur");
	};

	function displayPublicKey(response, account, modal) {
		if (response.noPublicKey && account != NRS.accountRS) {
			modal.find(".recipient_public_key").show();
		} else {
			modal.find("input[name=recipientPublicKey]").val("");
			modal.find(".recipient_public_key").hide();
		}
	}

	NRS.checkRecipient = function(account, modal) {
		var classes = "callout-info callout-danger callout-warning";
		var callout = modal.find(".account_info").first();
		var accountInputField = modal.find("input[name=converted_account_id]");
		var merchantInfoField = modal.find("input[name=merchant_info]");

		accountInputField.val("");
		merchantInfoField.val("");
		modal.find("input[name=encrypt_message]").attr('disabled', false);
		account = $.trim(account);

		if (NRS.isRsAccount(account)) {
			var address = new NxtAddress();
			if (address.set(account)) {
				NRS.checkAccountStatus(account, function(response) {
					displayPublicKey(response, account, modal);
					loadContractReferences(account);
					updateRecipientOptions(response, modal);
					if (account == NRS.accountRS) {
						callout.removeClass(classes).addClass("callout-" + response.type).html("This is your account").show();
					} else {
						callout.removeClass(classes).addClass("callout-" + response.type).html(response.message).show();
					}
				});
			} else {
				if (address.guess.length == 1) {
					callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed_suggestion", {
						"recipient": "<span class='malformed_address' data-address='" + NRS.escapeRespStr(address.guess[0]) + "' onclick='NRS.correctAddressMistake(this);'>" + address.format_guess(address.guess[0], account) + "</span>"
					})).show();
				} else if (address.guess.length > 1) {
					var html = $.t("recipient_malformed_suggestion", {
						"count": address.guess.length
					}) + "<ul>";
					for (var i = 0; i < address.guess.length; i++) {
						html += "<li><span class='malformed_address' data-address='" + NRS.escapeRespStr(address.guess[i]) + "' onclick='NRS.correctAddressMistake(this);'>" + address.format_guess(address.guess[i], account) + "</span></li>";
					}
					callout.removeClass(classes).addClass("callout-danger").html(html).show();
				} else {
					callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
				}
			}
		} else if (!NRS.isNumericAccount(account)) {
			if (account.charAt(0) != '@') {
				NRS.storageSelect("contacts", [{
					"name": account
				}], function(error, contact) {
					if (!error && contact.length) {
						contact = contact[0];
						NRS.checkAccountStatus(contact.accountRS, function(response) {
							displayPublicKey(response, contact.accountRS, modal);
							loadContractReferences(contact.accountRS);
							updateRecipientOptions(response, modal);
							callout.removeClass(classes).addClass("callout-" + response.type).html($.t("contact_account_link", {
								"account_id": NRS.getAccountFormatted(contact, "account")
							}) + " " + response.message).show();

							if (response.type == "info" || response.type == "warning") {
								accountInputField.val(contact.accountRS);
							}
						});
					} else if (/^[a-z0-9]+$/i.test(account)) {
						NRS.checkRecipientAlias(account, modal);
					} else {
						callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
					}
				});
			} else if (/^[a-z0-9@]+$/i.test(account)) {
				if (account.charAt(0) == '@') {
					account = account.substring(1);
					NRS.checkRecipientAlias(account, modal);
				}
			} else {
				callout.removeClass(classes).addClass("callout-danger").html($.t("recipient_malformed")).show();
			}
		} else {
			callout.removeClass(classes).addClass("callout-danger").html($.t("error_numeric_ids_not_allowed")).show();
		}
	};

	NRS.getAccountAlias = function(account) {
		var result = {};
		NRS.sendRequest("getAlias", {
			"aliasName": account,
			"chain": 2 // always use Ignis
		}, function(response) {
			if (response.errorCode) {
				result.error = response.errorDescription;
			} else {
				if (response.aliasURI) {
					var alias = String(response.aliasURI);
					var regex_1 = /acct:(.*)@nxt/;
					var regex_2 = /nacc:(.*)/;
					var match = alias.match(regex_1);
					if (!match) {
						match = alias.match(regex_2);
					}
					if (match && match[1]) {
						match[1] = String(match[1]).toUpperCase();
						if (/^\d+$/.test(match[1])) {
							var address = new NxtAddress();
							if (address.set(match[1])) {
								match[1] = address.toString();
							} else {
								esult.error = $.t("error_invalid_account_id");
								return;
							}
						}
						result.id = match[1];
					} else {
						result.error = $.t("alias_account_no_link") + (!alias ? $.t("error_uri_empty") : $.t("uri_is", {
							"uri": NRS.escapeRespStr(alias)
						}));
					}
				} else if (response.aliasName) {
					result.error = $.t("error_alias_empty_uri");
				} else {
					result.error = response.errorDescription ? $.t("error") + ": " + NRS.escapeRespStr(response.errorDescription) : $.t("error_alias");
				}
			}
		}, { isAsync: false });
		return result;
	};

	NRS.checkRecipientAlias = function(account, modal) {
		var classes = "callout-info callout-danger callout-warning";
		var callout = modal.find(".account_info").first();
		var accountInputField = modal.find("input[name=converted_account_id]");

		accountInputField.val("");

		NRS.sendRequest("getAlias", {
			"aliasName": account,
            "chain": 2 // always use Ignis
        }, function(response) {
			if (response.errorCode) {
				callout.removeClass(classes).addClass("callout-danger").html($.t("error_invalid_account_id")).show();
			} else {
				if (response.aliasURI) {
					var alias = String(response.aliasURI);
					var timestamp = response.timestamp;
					var regex_1 = /acct:(.*)@nxt/;
					var regex_2 = /nacc:(.*)/;
					var match = alias.match(regex_1);
					if (!match) {
						match = alias.match(regex_2);
					}

					if (match && match[1]) {
						account = match[1];
						account = String(account).toUpperCase();
						if (/^\d+$/.test(account)) {
							var address = new NxtAddress();
							if (address.set(account)) {
								account = address.toString();
							} else {
								accountInputField.val("");
								callout.removeClass(classes).addClass("callout-danger").html($.t("error_invalid_account_id")).show();
								return;
							}
						}
						NRS.checkAccountStatus(account, function(response) {
							displayPublicKey(response, account, modal);
							loadContractReferences(account);
							updateRecipientOptions(response, modal);
							callout.removeClass(classes).addClass("callout-" + response.type).html($.t("alias_account_link", {
								"account_id": NRS.escapeRespStr(account)
							}) + " " + response.message + " " + $.t("alias_last_adjusted", {
								"timestamp": NRS.formatTimestamp(timestamp)
							})).show();

							if (response.type == "info" || response.type == "warning") {
								accountInputField.val(NRS.escapeRespStr(account));
							}
						});
					} else {
						callout.removeClass(classes).addClass("callout-danger").html($.t("alias_account_no_link") + (!alias ? $.t("error_uri_empty") : $.t("uri_is", {
							"uri": NRS.escapeRespStr(alias)
						}))).show();
					}
				} else if (response.aliasName) {
					callout.removeClass(classes).addClass("callout-danger").html($.t("error_alias_empty_uri")).show();
				} else {
					callout.removeClass(classes).addClass("callout-danger").html(response.errorDescription ? $.t("error") + ": " + NRS.escapeRespStr(response.errorDescription) : $.t("error_alias")).show();
				}
			}
		});
	};

    function updateRecipientOptions(accountResponse, modal) {
        if (accountResponse.account) {
            if (accountResponse.account.description) {
                checkForMerchant(accountResponse.account.description, modal);
            }
            var params = {
				"setter": accountResponse.account.account,
				"recipient": accountResponse.account.account,
				"property": "nrs_recipient_ui_options"
			};
            NRS.sendRequest("getAccountProperties", params,
		function (response) {
                    var options = {};
                    if ($.isArray(response.properties) && response.properties.length > 0) {
                        try {
                            options = JSON.parse(NRS.unescapeRespStr(response.properties[0].value));
                        } catch (e) {
                            NRS.logConsole("Cannot parse nrs_recipient_ui_options JSON");
                            NRS.logException(e);
                        }
                    }
                    if (typeof options.message_format == "string") {
                        checkForMerchant("#merchant:" + NRS.escapeRespStr(options.message_format) + "#", modal);
                    }
                    var encryptMessageCheckbox = modal.find("input[name=encrypt_message]");
                    if (typeof options.encrypt_message == "boolean") {
                        encryptMessageCheckbox.prop("checked", options.encrypt_message);
                    }
                    if (options.encrypt_message_disabled === true) {
                        encryptMessageCheckbox.attr('disabled', true);
                    }
                }
			);
		}
    }

    function loadContractReferences(account) {
    	if (!$currentModal) {
    		return;
		}
		NRS.sendRequest("getContractReferences", { account: account, includeContract: true }, function(response) {
			var $recipientContractReferenceSelector = $currentModal.find(".recipient_contract_reference_selector");
			if (!response.contractReferences || response.contractReferences.length == 0) {
				if ($recipientContractReferenceSelector.is(":visible")) {
                    var $messageInput = $currentModal.find("textarea[name=message]");
                    $messageInput.val("");
					var $permanentMessageCheckbox = $currentModal.find("input[name=permanent_message]");
					$permanentMessageCheckbox.prop("disabled", false);
                    var $addMessageCheckbox = $currentModal.find("input[name=add_message]");
                    $addMessageCheckbox.prop("checked", false);
                    $addMessageCheckbox.trigger("change");
				}
				$recipientContractReferenceSelector.hide();
				return;
			}
			$recipientContractReferenceSelector.show();
			var $contractsSelector = $recipientContractReferenceSelector.find("select");
			$contractsSelector.empty();
			$contractsSelector.append("<option value='' selected>" + $.t("select_contract_to_trigger") + " </option>");
			for (var key in response.contractReferences) {
				if (response.contractReferences.hasOwnProperty(key)) {
					var name = response.contractReferences[key].name;
					var contract = response.contractReferences[key].contract;
					for (var i=0; i < contract.invocationTypes.length; i++) {
						if (contract.invocationTypes[i].type == "TRANSACTION" || contract.invocationTypes[i].type == "VOUCHER") {
							$contractsSelector.append("<option value='" + name + "'>" + name + "</option>");
							break;
						}
					}
				}
			}
		});
	}

	function checkForMerchant(accountInfo, modal) {
		var requestType = modal.find("input[name=request_type]").val(); // only works for single request type per modal
		if (requestType == "sendMoney" || requestType == "transferAsset" || requestType == "transferCurrency") {
			if (accountInfo.match(/merchant/i)) {
				modal.find("input[name=merchant_info]").val(accountInfo);
				var checkbox = modal.find("input[name=add_message]");
				if (!checkbox.is(":checked")) {
					checkbox.prop("checked", true).trigger("change");
				}
			}
		}
	}

	return NRS;
}(NRS || {}, jQuery));