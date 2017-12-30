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
        var request;
        var key;
        if (typeof $(this).data("id") == "object") {
            var dataObject = $(this).data("id");
            chain = dataObject["chain"];
            id = dataObject["id"];
            request = dataObject["request"];
            key = dataObject["key"];
        } else {
            chain = $(this).data("chain");
            id = $(this).data("id");
            request = $(this).data("request");
            key = $(this).data("key");
        }
        if ($(this).data("back") == "true") {
            NRS.modalStack.pop(); // The forward modal
            NRS.modalStack.pop(); // The current modal
        }
        NRS.showEntityDetailsModal(chain, id, request, key);
	});

	NRS.showEntityDetailsModal = function (chain, id, request, key) {
        try {
            NRS.setBackLink();
    		NRS.modalStack.push({ class: "show_entity_modal_action", key: "id", value: {
    		    chain: chain,
                id: id,
                request: request,
                key: key
    		}});
            $("#entity_details_type").html($.t(key));
            $("#entity_details_id").html(id);
            data = {};
            data[key] = id;
            data["chain"] = chain;
            NRS.sendRequest(request, data, function(response) {
                var entity = $.extend({}, response);
                var callout = $("#entity_info_callout");
                if (NRS[request + "Callout"]) {
                    NRS[request + "Callout"](callout, response);
                    callout.show();
                } else {
                    callout.hide();
                }
                if (response.decimals) {
                    for (var key in response) {
                        if (!response.hasOwnProperty(key)) {
                            continue;
                        }
                        if (key.endsWith("QNT")) {
                            entity[key.slice(0, -3)] = NRS.intToFloat(response[key], response.decimals);
                            delete entity[key];
                        }
                    }
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