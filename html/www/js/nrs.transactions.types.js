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

    NRS.transactionTypes = {
        "-4": {
            'title': "Coin Exchange",
            'i18nKeyTitle': 'coin_exchange',
            'iconHTML': "<i class='far fa-exchange-alt'></i>",
            'chainType': "parent",
            'subTypes': {
                0: {
                    'title': "Issue Order",
                    'i18nKeyTitle': 'issue_order',
                    'iconHTML': "<i class='far fa-money-bill-alt'></i>",
                    'receiverPage': 'open_coin_orders'
                },
                1: {
                    'title': "Cancel Order",
                    'i18nKeyTitle': 'cancel_order',
                    'iconHTML': "<i class='far fa-times'></i>",
                    'receiverPage': 'open_coin_orders'
                }
            }
        },
        "-3": {
            'title': "Account Control",
            'i18nKeyTitle': 'account_control',
            'iconHTML': '<i class="ion-locked"></i>',
            'chainType': "parent",
            'subTypes': {
                0: {
                    'title': "Balance Leasing",
                    'i18nKeyTitle': 'balance_leasing',
                    'iconHTML': '<i class="far fa-arrow-circle-o-right"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        "-2": {
            'title': "Payment",
            'i18nKeyTitle': 'payment',
            'iconHTML': "<i class='ion-calculator'></i>",
            'chainType': "parent",
            'subTypes': {
                0: {
                    'title': "Ordinary Payment",
                    'i18nKeyTitle': 'ordinary_payment',
                    'iconHTML': "<i class='far fa-money-bill-alt'></i>",
                    'receiverPage': 'transactions'
                }
            }
        },
        "-1": {
            'title': "Child Chain Block",
            'i18nKeyTitle': 'child_chain_block',
            'iconHTML': "<i class='far fa-crop'></i>",
            'chainType': "parent",
            'subTypes': {
                0: {
                    'title': "Child Chain Block",
                    'i18nKeyTitle': 'child_chain_block',
                    'iconHTML': "<i class='far fa-crop'></i>",
                    'receiverPage': 'transactions'
                }
            }
        },
        0: {
            'title': "Payment",
            'i18nKeyTitle': 'payment',
            'iconHTML': "<i class='ion-calculator'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Ordinary Payment",
                    'i18nKeyTitle': 'ordinary_payment',
                    'iconHTML': "<i class='far fa-money-bill-alt'></i>",
                    'receiverPage': 'transactions'
                }
            }
        },
        1: {
            'title': "Messaging",
            'i18nKeyTitle': 'messaging',
            'iconHTML': "<i class='fa fa-envelope-square'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Arbitrary Message",
                    'i18nKeyTitle': 'arbitrary_message',
                    'iconHTML': "<i class='far fa-envelope'></i>",
                    'receiverPage': 'messages'
                }
            }
        },
        2: {
            'title': "Asset Exchange",
            'i18nKeyTitle': 'asset_exchange',
            'iconHTML': '<i class="far fa-signal"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Asset Issuance",
                    'i18nKeyTitle': 'asset_issuance',
                    'iconHTML': '<i class="far fa-bullhorn"></i>'
                },
                1: {
                    'title': "Asset Transfer",
                    'i18nKeyTitle': 'asset_transfer',
                    'iconHTML': '<i class="ion-arrow-swap"></i>',
                    'receiverPage': "transfer_history"
                },
                2: {
                    'title': "Ask Order Placement",
                    'i18nKeyTitle': 'ask_order_placement',
                    'iconHTML': '<i class="ion-arrow-graph-down-right"></i>',
                    'receiverPage': "open_orders"
                },
                3: {
                    'title': "Bid Order Placement",
                    'i18nKeyTitle': 'bid_order_placement',
                    'iconHTML': '<i class="ion-arrow-graph-up-right"></i>',
                    'receiverPage': "open_orders"
                },
                4: {
                    'title': "Ask Order Cancellation",
                    'i18nKeyTitle': 'ask_order_cancellation',
                    'iconHTML': '<i class="far fa-times"></i>',
                    'receiverPage': "open_orders"
                },
                5: {
                    'title': "Bid Order Cancellation",
                    'i18nKeyTitle': 'bid_order_cancellation',
                    'iconHTML': '<i class="far fa-times"></i>',
                    'receiverPage': "open_orders"
                },
                6: {
                    'title': "Dividend Payment",
                    'i18nKeyTitle': 'dividend_payment',
                    'iconHTML': '<i class="far fa-gift"></i>',
                    'receiverPage': "transactions"
                },
                7: {
                    'title': "Delete Asset Shares",
                    'i18nKeyTitle': 'delete_asset_shares',
                    'iconHTML': '<i class="far fa-remove"></i>',
                    'receiverPage': "transactions"
                },
                8: {
                    'title': "Increase Asset Shares",
                    'i18nKeyTitle': 'increase_asset_shares',
                    'iconHTML': '<i class="far fa-plus"></i>',
                    'receiverPage': "transactions"
                },
                9: {
                    'title': "Asset Control",
                    'i18nKeyTitle': 'asset_control',
                    'iconHTML': '<i class="far fa-lock"></i>',
                    'receiverPage': "transactions"
                },
                10: {
                    'title': "Set Asset Property",
                    'i18nKeyTitle': 'set_asset_property',
                    'iconHTML': '<i class="far fa-pencil"></i>',
                    'receiverPage': "transactions"
                },
                11: {
                    'title': "Delete Asset Property",
                    'i18nKeyTitle': 'delete_asset_property',
                    'iconHTML': '<i class="far fa-eraser"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        3: {
            'title': "Marketplace",
            'i18nKeyTitle': 'marketplace',
            'iconHTML': '<i class="fa pull-right fa-angle-right fa-shopping-cart"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Marketplace Listing",
                    'i18nKeyTitle': 'marketplace_listing',
                    'iconHTML': '<i class="far fa-bullhorn"></i>'
                },
                1: {
                    'title': "Marketplace Removal",
                    'i18nKeyTitle': 'marketplace_removal',
                    'iconHTML': '<i class="far fa-times"></i>'
                },
                2: {
                    'title': "Marketplace Price Change",
                    'i18nKeyTitle': 'marketplace_price_change',
                    'iconHTML': '<i class="far fa-line-chart"></i>'
                },
                3: {
                    'title': "Marketplace Quantity Change",
                    'i18nKeyTitle': 'marketplace_quantity_change',
                    'iconHTML': '<i class="far fa-sort"></i>'
                },
                4: {
                    'title': "Marketplace Purchase",
                    'i18nKeyTitle': 'marketplace_purchase',
                    'iconHTML': '<i class="far fa-money-bill-alt"></i>',
                    'receiverPage': "pending_orders_dgs"
                },
                5: {
                    'title': "Marketplace Delivery",
                    'i18nKeyTitle': 'marketplace_delivery',
                    'iconHTML': '<i class="far fa-cube"></i>',
                    'receiverPage': "purchased_dgs"
                },
                6: {
                    'title': "Marketplace Feedback",
                    'i18nKeyTitle': 'marketplace_feedback',
                    'iconHTML': '<i class="ion-android-social"></i>',
                    'receiverPage': "completed_orders_dgs"
                },
                7: {
                    'title': "Marketplace Refund",
                    'i18nKeyTitle': 'marketplace_refund',
                    'iconHTML': '<i class="far fa-reply"></i>',
                    'receiverPage': "purchased_dgs"
                }
            }
        },
        4: {
            'title': "Account Control",
            'i18nKeyTitle': 'account_control',
            'iconHTML': '<i class="ion-locked"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Mandatory Approval",
                    'i18nKeyTitle': 'phasing_only',
                    'iconHTML': '<i class="far fa-gavel"></i>',
                    'receiverPage': "transactions"
                }
            }
        },
        5: {
            'title': "Monetary System",
            'i18nKeyTitle': 'monetary_system',
            'iconHTML': '<i class="fa fa-university"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Issue Currency",
                    'i18nKeyTitle': 'issue_currency',
                    'iconHTML': '<i class="far fa-bullhorn"></i>'
                },
                1: {
                    'title': "Reserve Increase",
                    'i18nKeyTitle': 'reserve_increase',
                    'iconHTML': '<i class="far fa-cubes"></i>'
                },
                2: {
                    'title': "Reserve Claim",
                    'i18nKeyTitle': 'reserve_claim',
                    'iconHTML': '<i class="far fa-truck"></i>',
                    'receiverPage': "currencies"
                },
                3: {
                    'title': "Currency Transfer",
                    'i18nKeyTitle': 'currency_transfer',
                    'iconHTML': '<i class="ion-arrow-swap"></i>',
                    'receiverPage': "currencies"
                },
                4: {
                    'title': "Publish Exchange Offer",
                    'i18nKeyTitle': 'publish_exchange_offer',
                    'iconHTML': '<i class="far fa-list-alt "></i>'
                },
                5: {
                    'title': "Buy Currency",
                    'i18nKeyTitle': 'currency_buy',
                    'iconHTML': '<i class="ion-arrow-graph-up-right"></i>',
                    'receiverPage': "currencies"
                },
                6: {
                    'title': "Sell Currency",
                    'i18nKeyTitle': 'currency_sell',
                    'iconHTML': '<i class="ion-arrow-graph-down-right"></i>',
                    'receiverPage': "currencies"
                },
                7: {
                    'title': "Mint Currency",
                    'i18nKeyTitle': 'mint_currency',
                    'iconHTML': '<i class="far fa-money-bill-alt"></i>',
                    'receiverPage': "currencies"
                },
                8: {
                    'title': "Delete Currency",
                    'i18nKeyTitle': 'delete_currency',
                    'iconHTML': '<i class="far fa-times"></i>'
                }
            }
        },
        6: {
            'title': "Data Cloud",
            'i18nKeyTitle': 'tagged_data',
            'iconHTML': '<i class="fa fa-tachometer-alt"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Upload Data",
                    'i18nKeyTitle': 'upload_tagged_data',
                    'iconHTML': '<i class="far fa-upload"></i>'
                }
            }
        },
        7: {
            'title': "Shuffling",
            'i18nKeyTitle': 'shuffling',
            'iconHTML': '<i class="far fa-random"></i>',
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Shuffling Creation",
                    'i18nKeyTitle': 'shuffling_creation',
                    'iconHTML': '<i class="far fa-plus"></i>'
                },
                1: {
                    'title': "Shuffling Registration",
                    'i18nKeyTitle': 'shuffling_registration',
                    'iconHTML': '<i class="far fa-link"></i>'
                },
                2: {
                    'title': "Shuffling Processing",
                    'i18nKeyTitle': 'shuffling_processing',
                    'iconHTML': '<i class="far fa-cog"></i>'
                },
                3: {
                    'title': "Shuffling Recipients",
                    'i18nKeyTitle': 'shuffling_recipients',
                    'iconHTML': '<i class="far fa-spoon"></i>'
                },
                4: {
                    'title': "Shuffling Verification",
                    'i18nKeyTitle': 'shuffling_verification',
                    'iconHTML': '<i class="far fa-check-square"></i>'
                },
                5: {
                    'title': "Shuffling Cancellation",
                    'i18nKeyTitle': 'shuffling_cancellation',
                    'iconHTML': '<i class="far fa-thumbs-down"></i>'
                }
            }
        },
        8: {
            'title': "Aliases",
            'i18nKeyTitle': 'aliases',
            'iconHTML': "<i class='fa fa-envelope-square'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Alias Assignment",
                    'i18nKeyTitle': 'alias_assignment',
                    'iconHTML': "<i class='fa fa-bookmark'></i>"
                },
                1: {
                    'title': "Alias Sale/Transfer",
                    'i18nKeyTitle': 'alias_sale_transfer',
                    'iconHTML': "<i class='far fa-tag'></i>",
                    'receiverPage': "aliases"
                },
                2: {
                    'title': "Alias Buy",
                    'i18nKeyTitle': 'alias_buy',
                    'iconHTML': "<i class='far fa-money-bill-alt'></i>",
                    'receiverPage': "aliases"
                },
                3: {
                    'title': "Alias Deletion",
                    'i18nKeyTitle': 'alias_deletion',
                    'iconHTML': "<i class='far fa-times'></i>"
                }
            }
        },
        9: {
            'title': "Voting",
            'i18nKeyTitle': 'voting',
            'iconHTML': "<i class='far fa-check-square'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Poll Creation",
                    'i18nKeyTitle': 'poll_creation',
                    'iconHTML': "<i class='far fa-check-square'></i>"
                },
                1: {
                    'title': "Vote Casting",
                    'i18nKeyTitle': 'vote_casting',
                    'iconHTML': "<i class='far fa-check'></i>"
                },
                2: {
                    'title': "Transaction Approval",
                    'i18nKeyTitle': 'transaction_approval',
                    'iconHTML': "<i class='far fa-gavel'></i>",
                    'receiverPage': "transactions"
                }
            }
        },
        10: {
            'title': "Account Properties",
            'i18nKeyTitle': 'account_properties',
            'iconHTML': "<i class='fa fa-address-card'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Account Info",
                    'i18nKeyTitle': 'account_info',
                    'iconHTML': "<i class='far fa-info'></i>"
                },
                1: {
                    'title': "Account Property",
                    'i18nKeyTitle': 'account_property',
                    'iconHTML': "<i class='far fa-pencil'></i>",
                    'receiverPage': "transactions"
                },
                2: {
                    'title': "AccountPropertyDelete",
                    'i18nKeyTitle': 'account_property_delete',
                    'iconHTML': "<i class='far fa-eraser'></i>",
                    'receiverPage': "transactions"
                }
            }
        },
        11: {
            'title': "Coin Exchange",
            'i18nKeyTitle': 'coin_exchange',
            'iconHTML': "<i class='far fa-exchange-alt'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Issue Order",
                    'i18nKeyTitle': 'issue_order',
                    'iconHTML': "<i class='far fa-money-bill-alt'></i>",
                    'receiverPage': 'open_coin_orders'
                },
                1: {
                    'title': "Cancel Order",
                    'i18nKeyTitle': 'cancel_order',
                    'iconHTML': "<i class='far fa-times'></i>",
                    'receiverPage': 'open_coin_orders'
                }
            }
        },
        12: {
            'title': "Contract Reference",
            'i18nKeyTitle': 'contract_reference',
            'iconHTML': "<i class='fa fa-address-card'></i>",
            'chainType': "child",
            'subTypes': {
                0: {
                    'title': "Set Contract Reference",
                    'i18nKeyTitle': 'set_contract_reference',
                    'iconHTML': "<i class='far fa-pencil'></i>",
                    'receiverPage': "transactions"
                },
                1: {
                    'title': "Delete Contract Reference",
                    'i18nKeyTitle': 'delete_contract_reference',
                    'iconHTML': "<i class='far fa-eraser'></i>",
                    'receiverPage': "transactions"
                }
            }
        }

    };

    NRS.subtype = {};

    NRS.loadTransactionTypeConstants = function(response) {
        $.each(response.transactionTypes, function (typeIndex, type) {
            if (!(typeIndex in NRS.transactionTypes)) {
                NRS.transactionTypes[typeIndex] = {
                    'title': "Unknown",
                    'i18nKeyTitle': 'unknown',
                    'iconHTML': '<i class="far fa-question-circle"></i>',
                    'subTypes': {}
                }
            }
            $.each(type.subtypes, function (subTypeIndex, subType) {
                if (!(subTypeIndex in NRS.transactionTypes[typeIndex]["subTypes"])) {
                    NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex] = {
                        'title': "Unknown",
                        'i18nKeyTitle': 'unknown',
                        'iconHTML': '<i class="far fa-question-circle"></i>'
                    }
                }
                NRS.transactionTypes[typeIndex]["subTypes"][subTypeIndex]["serverConstants"] = subType;
            });
        });
        NRS.subtype = response.transactionSubTypes;
    };

    NRS.isOfType = function(transaction, typeStr) {
        if (!NRS.subtype[typeStr]) {
            var msg = $.t("unsupported_transaction_type", { type: typeStr });
            $.growl(msg);
            NRS.logConsole(msg);
            return false;
        }
        return transaction.type == NRS.subtype[typeStr].type && transaction.subtype == NRS.subtype[typeStr].subtype;
    };

    NRS.notOfType = function(transaction, typeStr) {
        return !NRS.isOfType(transaction, typeStr);
    };

    return NRS;
}(isNode ? client : NRS || {}, jQuery));

if (isNode) {
    module.exports = NRS;
}