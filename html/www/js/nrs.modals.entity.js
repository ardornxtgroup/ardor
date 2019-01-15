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
 * @depends {nrs.modals.js}
 */
var NRS = (function(NRS, $) {
	$("body").on("click", ".show_entity_modal_action", function(event) {
		event.preventDefault();
		if (NRS.fetchingModalData) {
			return;
		}
		NRS.fetchingModalData = true;
        var chain;
        var id;
        var id2;
        var request;
        var key;
        var key2;
        var responseArray;
        if (typeof $(this).data("id") == "object") {
            var dataObject = $(this).data("id");
            chain = dataObject["chain"];
            id = dataObject["id"];
            id2 = dataObject["id2"];
            request = dataObject["request"];
            key = dataObject["key"];
            key2 = dataObject["key2"];
            responseArray = dataObject["responseArray"];
        } else {
            chain = $(this).data("chain");
            id = $(this).data("id");
            id2 = $(this).data("id2");
            request = $(this).data("request");
            key = $(this).data("key");
            key2 = $(this).data("key2");
            responseArray = $(this).data("responseArray");
        }
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // The current modal
        }
        NRS.showEntityDetailsModal(chain, id, id2, request, key, key2, responseArray);
	});

	NRS.showEntityDetailsModal = function (chain, id, id2, request, key, key2, responseArray) {
        try {
            NRS.setBackLink();
    		NRS.modalStack.push({ class: "show_entity_modal_action", key: "id", value: {
    		    chain: chain,
                id: id,
                id2: id2,
                request: request,
                key: key,
                key2: key2,
                responseArray: responseArray
    		}});
            $("#entity_details_type").html($.t(key.toLowerCase()));
            $("#entity_details_id").html(id);
            data = {};
            data[key] = id;
            if (key2) {
                data[key2] = id2;
            }
            data["chain"] = chain;
            NRS.sendRequest(request, data, function(response) {
                var entity;
                entity = $.extend({}, response);
                if (responseArray && responseArray !== "undefined") {
                    var entry = $.extend({}, response[responseArray][0]);
                    delete entity[responseArray];
                    entity = Object.assign(entity, entry);
                }
                var callout = $("#entity_info_callout");
                if (NRS[request + "Callout"]) {
                    NRS[request + "Callout"](callout, response);
                    callout.show();
                } else {
                    callout.hide();
                }
                if (entity.decimals) {
                    for (var key in entity) {
                        if (!entity.hasOwnProperty(key)) {
                            continue;
                        }
                        if (key.endsWith("QNT")) {
                            entity[key.slice(0, -3)] = NRS.intToFloat(entity[key], entity.decimals);
                            delete entity[key];
                        }
                    }
                }
                if (entity.contract) {
                    entity.contract_formatted_html = NRS.getTransactionLink(entity.contract.transactionFullHash, entity.id, true, entity.contract.chain);
                    delete entity.contract;
                    delete entity.id;
                }
                var detailsTable = $("#entity_details_table");
                detailsTable.find("tbody").empty().append(NRS.createInfoTable(entity));
                detailsTable.show();
                $("#entity_details_modal").modal("show");
            });
        } finally {
            NRS.fetchingModalData = false;
        }
	};

	return NRS;
}(NRS || {}, jQuery));