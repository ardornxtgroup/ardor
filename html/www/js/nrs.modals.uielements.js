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
 * @depends {3rdparty/jquery-2.1.0.js}
 */
var NRS = (function(NRS, $) {

	var _delay = (function(){
  		var timer = 0;
  		return function(callback, ms){
    		clearTimeout (timer);
    		timer = setTimeout(callback, ms);
  		};
	})();

    function _updateBlockHeightEstimates($bhmElem) {
		var $input = $bhmElem.find(' .bhm_ue_time_input');
		var blockHeight = $input.val();
		var output = "<i class='far fa-clock'></i> " + NRS.getBlockHeightTimeEstimate(blockHeight) + " ";
		$bhmElem.find(".bhm_ue_time_estimate").html(output);
	}

	function _changeBlockHeightFromButton($btn, add) {
		var $bhmElem = $btn.closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		var numBlocks = parseInt($btn.data('bhmUeNumBlocks'));
		var $input = $bhmElem.find(' .bhm_ue_time_input');
		var blockHeight = parseInt($input.val());
		if(add) {
			$input.val(String(blockHeight + numBlocks));
		} else {
			$input.val(String(blockHeight - numBlocks));
		}
	}

    var $body = $("body");
    $body.on("show.bs.modal", '.modal', function() {
		var $bhmElems = $(this).find('div[data-modal-ui-element="block_height_modal_ui_element"]');
		$bhmElems.each(function(key, bhmElem) {
			_updateBlockHeightEstimates($(bhmElem));
			$(bhmElem).find(".bhm_ue_current_block_height").html(String(NRS.lastBlockHeight));
			$(bhmElem).find(".bhm_ue_use_current_block_height").data('CurrentBlockHeight', NRS.lastBlockHeight);
		});
		var $algorithm = $(this).find('div[data-modal-ui-element="hash_algorithm_model_modal_ui_element"]');
		var $algoSelect = $algorithm.find('select');
		NRS.loadAlgorithmList($algoSelect, ($(this).attr('id') != 'hash_modal'));
		var $phasingApprovalModels = $(this).find('.phasing_model_group');
		var $phasingApprovalModelsSelect = $phasingApprovalModels.find('select');
		NRS.loadApprovalModelsList($phasingApprovalModelsSelect, true);
		var $controlApprovalModels = $(this).find('.control_model_group');
		var $controlApprovalModelsSelect = $controlApprovalModels.find('select');
		NRS.loadApprovalModelsList($controlApprovalModelsSelect, false);
	});

	$body.on('keyup', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_time_input', function() {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_updateBlockHeightEstimates($bhmElem);
	});

	$body.on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_use_current_block_height', function() {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		$bhmElem.find('.bhm_ue_time_input').val($(this).data('CurrentBlockHeight'));
		_updateBlockHeightEstimates($bhmElem);
	});

	$body.on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_reduce_height_btn', function() {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_changeBlockHeightFromButton($(this), false);
		_updateBlockHeightEstimates($bhmElem);
	});

	$body.on('click', '.modal div[data-modal-ui-element="block_height_modal_ui_element"] .bhm_ue_add_height_btn', function() {
		var $bhmElem = $(this).closest('div[data-modal-ui-element="block_height_modal_ui_element"]');
		_changeBlockHeightFromButton($(this), true);
		_updateBlockHeightEstimates($bhmElem);
	});


	//add_currency_modal_ui_element
	var _currencyCode = null;
	var _acmElem = null;
    var _setCurrencyInfoNotExisting = function () {
        $(_acmElem).find('.acm_ue_currency_id').html($.t('not_existing', 'Not existing'));
        $(_acmElem).find('.acm_ue_currency_id_input').val("");
        $(_acmElem).find('.acm_ue_currency_id_input').prop("disabled", true);
        $(_acmElem).find('.acm_ue_currency_decimals_input').val("");
        $(_acmElem).find('.acm_ue_currency_decimals_input').prop("disabled", true);
    };

	var _loadCurrencyInfoForCode = function() {
		if (_currencyCode && _currencyCode.length >= 3) {
			NRS.sendRequest("getCurrency", {
				"code": _currencyCode
			}, function(response) {
				if (response && response.currency) {
					var idString = String(response.currency) + "&nbsp; (" + $.t('decimals', 'Decimals') + ": " + String(response.decimals) + ")";
					$(_acmElem).find('.acm_ue_currency_id').html(idString);
					$(_acmElem).find('.acm_ue_currency_id_input').val(String(response.currency));
					$(_acmElem).find('.acm_ue_currency_id_input').prop("disabled", false);
					$(_acmElem).find('.acm_ue_currency_decimals').html(String(response.decimals));
					$(_acmElem).find('.acm_ue_currency_decimals_input').val(String(response.decimals));
					$(_acmElem).find('.acm_ue_currency_decimals_input').prop("disabled", false);
				} else {
					_setCurrencyInfoNotExisting();
				}
			});
		} else {
			_setCurrencyInfoNotExisting();
		}
	};

	$body.on('keyup', '.modal div[data-modal-ui-element="add_currency_modal_ui_element"] .acm_ue_currency_code_input', function() {
		_acmElem = $(this).closest('div[data-modal-ui-element="add_currency_modal_ui_element"]');
		_currencyCode = $(this).val();
		_delay(_loadCurrencyInfoForCode, 1000 );
	});

	//add_asset_modal_ui_element
	var _assetId = null;
	var _aamElem = null;
	var _setAssetInfoNotExisting = function() {
		$(_aamElem).find('.aam_ue_asset_name').html($.t('not_existing', 'Not existing'));
		$(_aamElem).find('.aam_ue_asset_decimals_input').val("");
		$(_aamElem).find('.aam_ue_asset_decimals_input').prop("disabled", true);
	};

	var _loadAssetInfoForId = function() {
		if (_assetId && _assetId.length > 0) {
			NRS.sendRequest("getAsset", {
				"asset": _assetId
			}, function(response) {
				if (response && response.asset) {
					var nameString = String(response.name) + "&nbsp; (" + $.t('decimals', 'Decimals') + ": " + String(response.decimals) + ")";
					$(_aamElem).find('.aam_ue_asset_name').html(nameString);
					$(_aamElem).find('.aam_ue_asset_decimals_input').val(String(response.decimals));
					$(_aamElem).find('.aam_ue_asset_decimals_input').prop("disabled", false);
					$(_aamElem).find('.aam_ue_asset_name_input').val(response.name);
				} else {
					_setAssetInfoNotExisting();
				}
			});
		} else {
			_setAssetInfoNotExisting();
		}
	};

	$body.on('keyup', '.modal div[data-modal-ui-element="add_asset_modal_ui_element"] .aam_ue_asset_id_input', function() {
		_aamElem = $(this).closest('div[data-modal-ui-element="add_asset_modal_ui_element"]');
		_assetId = $(this).val();
		_delay(_loadAssetInfoForId, 1000 );
	});

	//multi_accounts_modal_ui_element
	$body.on('click', '.modal div[data-modal-ui-element="multi_accounts_modal_ui_element"] .add_account_btn', function() {
    	var $accountBox = $(this).closest('div[data-modal-ui-element="multi_accounts_modal_ui_element"]');
        var $clone = $accountBox.find(".form_group_multi_accounts_ue").first().clone();
        $clone.find("input").val("");
        $clone.find(".pas_contact_info").text("");
        $accountBox.find(".multi_accounts_ue_added_account_list").append($clone);
    });

    $body.on('click', '.modal div[data-modal-ui-element="multi_accounts_modal_ui_element"] .remove_account_btn', function(e) {
    	e.preventDefault();
    	var $accountBox = $(this).closest('div[data-modal-ui-element="multi_accounts_modal_ui_element"]');
    	if ($accountBox.find(".form_group_multi_accounts_ue").length == 1) {
            return;
        }
        $(this).closest(".form_group_multi_accounts_ue").remove();
    });

	//multi_piece_modal_ui_element
	$body.on('click', '.modal div[data-modal-ui-element="multi_piece_modal_ui_element"] .add-piece-btn', function() {
    	var $pieceBox = $(this).closest('div[data-modal-ui-element="multi_piece_modal_ui_element"]');
        var $clone = $pieceBox.find(".form_group_multi_piece").first().clone();
        $clone.find("input").val("");
        $pieceBox.find(".multi_piece_list").append($clone);
    });

    $body.on('click', '.modal div[data-modal-ui-element="multi_piece_modal_ui_element"] .remove-piece-btn', function(e) {
    	e.preventDefault();
    	var $pieceBox = $(this).closest('div[data-modal-ui-element="multi_piece_modal_ui_element"]');
    	if ($pieceBox.find(".form_group_multi_piece").length == 1) {
            return;
        }
        $(this).closest(".form_group_multi_piece").remove();
    });

	$body.on('click', '.modal div[data-modal-ui-element="multi_piece_modal_ui_element"] .combine-pieces-btn', function() {
		var $pieces = $(this).closest("form").find('.piece-selector-input');

		// Load the pieces from multiple fields
		var pieceData = $pieces.map(function() {
			return $(this).val();
		}).get();

		// Remove duplicate entries
		pieceData = pieceData.filter(function (x, i, a) {
			return a.indexOf(x) == i && x !== "";
		});
		try {
			var secretPhrase = sss.combineSecret(pieceData);
		} catch(e) {
			$(this).closest(".modal").find(".error_message").html($.t(e.message)).show();
			return;
		}
		var $secretPhraseInput = $(this).closest("form").find('.secret-phrase-input');
		var rsAccount = NRS.getAccountId(secretPhrase, true);
		var isVoucher = $(this).closest("form").find("input[name=isVoucher]").prop("checked");
		if (rsAccount != NRS.accountRS && !isVoucher) {
			$(this).closest(".modal").find(".error_message").html($.t("error_passphrase_incorrect_v2", { account: rsAccount })).show();
			return;
		}
		$(this).closest(".modal").find(".error_message").html("").hide();
		$.growl($.t("secret_phrase_reproduced"));
		$secretPhraseInput.val(secretPhrase).change();
	});

	$body.on('click', '.modal div[data-modal-ui-element="multi_piece_modal_ui_element"] .scan-qr-code', function() {
		var $elem = $(this);
		var data = $elem.data();
		NRS.scanQRCode(data.reader, function(text) {
			$elem.closest(".input-group").find("input").val(text);
		});
	});

	return NRS;
}(NRS || {}, jQuery));