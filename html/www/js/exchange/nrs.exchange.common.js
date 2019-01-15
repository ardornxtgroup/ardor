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

    NRS.invert = function(rate) {
        var bigRate = new Big(rate);
        var bigOne = new Big(1);
        var result = bigOne.div(bigRate);
        return result.toFixed(8);
    };

    NRS.getCoins = function(exchange) {
        var coins = [];
        for (var i=0; i<3; i++) {
            coins.push(NRS.settings[exchange + "_coin" + i]);
        }
        return coins;
    };

    NRS.setCoins = function(exchange, coins) {
        for (var i=0; i<coins.length; i++) {
            NRS.updateSettings(exchange + "_coin" + i, coins[i]);
        }
    };


    NRS.addDepositAddress = function(address, from, to, key) {
        var json = localStorage[key];
        var addresses;
        if (json === undefined) {
            addresses = [];
        } else {
            addresses = JSON.parse(json);
            if (addresses.length > 10) {
                addresses.splice(10, addresses.length - 10);
            }
        }
        var item = { address: address, from: from, to: to, time: Date.now() };
        for (var i=0; i < addresses.length; i++) {
            if (item.address == addresses[i].address && item.from == addresses[i].from && item.to == addresses[i].to) {
                NRS.logConsole("deposit address " + item.address + " from " + item.from + " to " + item.to + " already exists");
                return;
            }
        }
        addresses.splice(0, 0, item);
        NRS.logConsole("deposit address " + address + " from " + from + " to " + to + " added");
        localStorage[key] = JSON.stringify(addresses);
    };

    NRS.getExchangeAddressLink = function (address, coin) {
        coin = coin.toUpperCase();
        if (coin === "IGNIS" || coin === "ARDR") {
            return NRS.getAccountLink({ accountRS: address }, "account");
        }
        if (coin === "BTC") {
            return "<a target='_blank' href='https://blockchain.info/address/" + address + "'>" + address + "</a>";
        }
        return address;
    };

    NRS.getExchangeTransactionLink = function (payoutHash, coin) {
        coin = coin.toUpperCase();
        var briefHash = payoutHash.substring(0, 8) + "...";
        if (coin === "ARDR") {
            return "<a href='#' class='show_transaction_modal_action' data-chain='1' data-fullhash='" + payoutHash + "'>" + briefHash + "</a>";
        } else if (coin === "IGNIS") {
            return "<a href='#' class='show_transaction_modal_action' data-chain='2' data-fullhash='" + payoutHash + "'>" + briefHash + "</a>";
        } else if (coin === "BTC") {
            return "<a target='_blank' href='https://blockchain.info/tx/" + payoutHash + "'>" + briefHash + "</a>";
        }
        return payoutHash;
    };

    return NRS;
}(NRS || {}, jQuery));

