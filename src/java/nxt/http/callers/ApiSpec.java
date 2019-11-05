// Auto generated code, do not modify
package nxt.http.callers;

import java.util.Arrays;
import java.util.List;

public enum ApiSpec {
    getLastExchanges(true, null, "chain", "currencies", "currencies", "currencies", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    startFundingMonitor(true, null, "chain", "holdingType", "holding", "property", "amount", "threshold", "interval", "secretPhrase", "feeRateNQTPerFXT", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getExpectedAskOrders(true, null, "chain", "asset", "sortByPrice", "requireBlock", "requireLastBlock"),

    getAccountPublicKey(false, null, "account", "requireBlock", "requireLastBlock"),

    detectMimeType(false, "file", "data", "filename", "isText"),

    getBlocks(false, null, "firstIndex", "lastIndex", "timestamp", "includeTransactions", "includeExecutedPhased", "adminPassword", "requireBlock", "requireLastBlock"),

    getAssetsByIssuer(false, null, "account", "account", "account", "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    getExchangesByOffer(true, null, "chain", "offer", "includeCurrencyInfo", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAllOpenBidOrders(true, null, "chain", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    dgsPurchase(true, null, "chain", "goods", "priceNQT", "quantity", "deliveryDeadlineTimestamp", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAccountBlockCount(false, null, "account", "requireBlock", "requireLastBlock"),

    deleteAlias(true, null, "chain", "alias", "aliasName", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    decodeFileToken(false, "file", "token"),

    getPlugins(false, null, ""),

    addBundlingRule(true, null, "chain", "secretPhrase", "minRateNQTPerFXT", "totalFeesLimitFQT", "overpayFQTPerFXT", "feeCalculatorName", "filter", "filter", "filter", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getPhasingAssetControl(false, null, "asset", "requireBlock", "requireLastBlock"),

    getDataTagsLike(true, null, "chain", "tagPrefix", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getFundingMonitor(true, null, "chain", "holdingType", "holding", "property", "secretPhrase", "includeMonitoredAccounts", "includeHoldingInfo", "account", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getPolls(true, null, "chain", "account", "firstIndex", "lastIndex", "timestamp", "includeFinished", "finishedOnly", "adminPassword", "requireBlock", "requireLastBlock"),

    downloadTaggedData(true, null, "chain", "transactionFullHash", "retrieve", "requireBlock", "requireLastBlock"),

    getDataTags(true, null, "chain", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    processVoucher(false, "voucher", "secretPhrase", "validate", "broadcast", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getPollVote(true, null, "chain", "poll", "account", "includeWeights", "requireBlock", "requireLastBlock"),

    addPeer(false, null, "peer", "adminPassword"),

    getSharedKey(false, null, "account", "secretPhrase", "nonce", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    decodeToken(false, null, "website", "token"),

    popOff(false, null, "numBlocks", "height", "keepTransactions", "adminPassword"),

    getAccountPhasedTransactions(true, null, "chain", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAvailableToBuy(true, null, "chain", "currency", "unitsQNT", "requireBlock", "requireLastBlock"),

    getExecutedTransactions(true, null, "chain", "height", "numberOfConfirmations", "type", "subtype", "sender", "recipient", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getNextBlockGenerators(false, null, "limit"),

    getExpectedAssetDeletes(true, null, "chain", "asset", "account", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getCoinExchangeOrderIds(true, null, "chain", "exchange", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    setContractReference(true, null, "chain", "contractName", "contractParams", "contract", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    startForging(false, null, "secretPhrase", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    triggerContractByVoucher(false, "voucher", "contractName", "requireBlock", "requireLastBlock"),

    getAssetAccounts(false, null, "asset", "height", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getCurrencyFounders(true, null, "chain", "currency", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    currencyBuy(true, null, "chain", "currency", "rateNQTPerUnit", "unitsQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    decodeQRCode(false, null, "qrCodeBase64"),

    getAllExchanges(true, null, "chain", "timestamp", "firstIndex", "lastIndex", "includeCurrencyInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getCurrencyTransfers(false, null, "currency", "account", "firstIndex", "lastIndex", "timestamp", "includeCurrencyInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getExpectedOrderCancellations(true, null, "chain", "requireBlock", "requireLastBlock"),

    eventRegister(false, null, "event", "event", "event", "token", "add", "remove"),

    scan(false, null, "numBlocks", "height", "validate", "adminPassword"),

    getAllBundlerRates(false, null, "minBundlerBalanceFXT"),

    hexConvert(false, null, "string"),

    getPhasingOnlyControl(false, null, "account", "requireBlock", "requireLastBlock"),

    getDGSTagCount(true, null, "chain", "inStockOnly", "requireBlock", "requireLastBlock"),

    getOffer(true, null, "chain", "offer", "requireBlock", "requireLastBlock"),

    encodeQRCode(false, null, "qrCodeData", "width", "height"),

    getChannelTaggedData(true, null, "chain", "channel", "account", "firstIndex", "lastIndex", "includeData", "adminPassword", "requireBlock", "requireLastBlock"),

    getAvailableToSell(true, null, "chain", "currency", "unitsQNT", "requireBlock", "requireLastBlock"),

    cancelBidOrder(true, null, "chain", "order", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    shufflingCancel(true, null, "chain", "shufflingFullHash", "cancellingAccount", "shufflingStateHash", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAssetProperties(false, null, "asset", "setter", "property", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    parsePhasingParams(false, null, "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingMinBalanceModel", "phasingHolding", "chain", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingExpression", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue"),

    getAccount(false, null, "account", "includeLessors", "includeAssets", "includeCurrencies", "includeEffectiveBalance", "requireBlock", "requireLastBlock"),

    blacklistAPIProxyPeer(false, null, "peer", "adminPassword"),

    getPeer(false, null, "peer"),

    getAccountCurrentAskOrderIds(true, null, "chain", "account", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getUnconfirmedTransactionIds(true, null, "chain", "account", "account", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountShufflings(true, null, "chain", "account", "includeFinished", "includeHoldingInfo", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getExpectedSellOffers(true, null, "chain", "currency", "account", "sortByRate", "requireBlock", "requireLastBlock"),

    getBundlingOptions(false, null, ""),

    dgsPriceChange(true, null, "chain", "goods", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAliasesLike(true, null, "chain", "aliasPrefix", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    dgsListing(true, "messageFile", "chain", "name", "description", "tags", "quantity", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getBidOrder(true, null, "chain", "order", "requireBlock", "requireLastBlock"),

    sendMessage(true, null, "chain", "recipient", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAllBroadcastedTransactions(false, null, "adminPassword", "requireBlock", "requireLastBlock"),

    placeBidOrder(true, null, "chain", "asset", "quantityQNT", "priceNQTPerShare", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAccountBlocks(false, null, "account", "timestamp", "firstIndex", "lastIndex", "includeTransactions", "adminPassword", "requireBlock", "requireLastBlock"),

    getShuffling(true, null, "chain", "shufflingFullHash", "includeHoldingInfo", "requireBlock", "requireLastBlock"),

    setAPIProxyPeer(false, null, "peer", "adminPassword"),

    getAccountCurrencies(false, null, "account", "currency", "height", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getExpectedTransactions(true, null, "chain", "account", "account", "account", "requireBlock", "requireLastBlock"),

    getAccountCurrentBidOrderIds(true, null, "chain", "account", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAllPhasingOnlyControls(false, null, "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getExpectedCoinExchangeOrderCancellations(true, null, "chain", "requireBlock", "requireLastBlock"),

    dgsRefund(true, null, "chain", "purchase", "refundNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAssetIds(false, null, "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getTaggedData(true, null, "chain", "transactionFullHash", "includeData", "retrieve", "requireBlock", "requireLastBlock"),

    stopStandbyShuffler(true, null, "chain", "secretPhrase", "holdingType", "holding", "account", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    searchAccounts(false, null, "query", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountLedger(false, null, "account", "firstIndex", "lastIndex", "eventType", "event", "holdingType", "holding", "includeTransactions", "includeHoldingInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountAssets(false, null, "account", "asset", "height", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    deleteAccountProperty(true, null, "chain", "recipient", "property", "setter", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getBlockchainTransactions(true, null, "chain", "account", "timestamp", "type", "subtype", "firstIndex", "lastIndex", "numberOfConfirmations", "withMessage", "phasedOnly", "nonPhasedOnly", "includeExpiredPrunable", "includePhasingResult", "executedOnly", "adminPassword", "requireBlock", "requireLastBlock"),

    sendMoney(true, null, "chain", "recipient", "amountNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getMyInfo(false, null, ""),

    getAccountTaggedData(true, null, "chain", "account", "firstIndex", "lastIndex", "includeData", "adminPassword", "requireBlock", "requireLastBlock"),

    getAllTrades(true, null, "chain", "timestamp", "firstIndex", "lastIndex", "includeAssetInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    uploadContractRunnerConfiguration(false, "config", "adminPassword", "requireBlock", "requireLastBlock"),

    splitSecret(false, null, "secretPhrase", "totalPieces", "minimumPieces", "primeFieldSize"),

    getStackTraces(false, null, "depth", "adminPassword"),

    rsConvert(false, null, "account"),

    searchTaggedData(true, null, "chain", "query", "tag", "channel", "account", "firstIndex", "lastIndex", "includeData", "adminPassword", "requireBlock", "requireLastBlock"),

    getAllTaggedData(true, null, "chain", "firstIndex", "lastIndex", "includeData", "adminPassword", "requireBlock", "requireLastBlock"),

    calculateFee(false, null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT"),

    getDGSPendingPurchases(true, null, "chain", "seller", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getECBlock(false, null, "timestamp", "requireBlock", "requireLastBlock"),

    getCoinExchangeTrades(true, null, "chain", "exchange", "account", "orderFullHash", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    generateFileToken(false, "file", "secretPhrase", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    searchDGSGoods(true, null, "chain", "query", "tag", "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountPhasedTransactionCount(true, null, "chain", "account", "requireBlock", "requireLastBlock"),

    getCurrencyAccounts(false, null, "currency", "height", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    shufflingCreate(true, null, "chain", "holding", "holdingType", "amount", "participantCount", "registrationPeriod", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    stopBundler(true, null, "chain", "account", "secretPhrase", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAlias(true, null, "chain", "alias", "aliasName", "requireBlock", "requireLastBlock"),

    canDeleteCurrency(false, null, "account", "currency", "requireBlock", "requireLastBlock"),

    managePeersNetworking(false, null, "operation", "adminPassword"),

    getPhasingPollVote(true, null, "chain", "transactionFullHash", "account", "requireBlock", "requireLastBlock"),

    stopFundingMonitor(true, null, "chain", "holdingType", "holding", "property", "secretPhrase", "account", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getTime(false, null, ""),

    buyAlias(true, null, "chain", "alias", "aliasName", "amountNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    searchPolls(true, null, "chain", "query", "firstIndex", "lastIndex", "includeFinished", "adminPassword", "requireBlock", "requireLastBlock"),

    simulateCoinExchange(true, null, "chain", "exchange", "quantityQNT", "priceNQTPerCoin"),

    eventWait(false, null, "token", "timeout"),

    castVote(true, null, "chain", "poll", "vote00", "vote01", "vote02", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getMintingTarget(false, null, "currency", "account", "unitsQNT", "requireBlock", "requireLastBlock"),

    generateToken(false, null, "website", "secretPhrase", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    longConvert(false, null, "id"),

    getBlockId(false, null, "height", "requireBlock", "requireLastBlock"),

    getLastTrades(true, null, "chain", "assets", "assets", "assets", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getExpectedBidOrders(true, null, "chain", "asset", "sortByPrice", "requireBlock", "requireLastBlock"),

    setPhasingAssetControl(true, null, "chain", "asset", "controlVotingModel", "controlQuorum", "controlMinBalance", "controlMinBalanceModel", "controlHolding", "controlWhitelisted", "controlWhitelisted", "controlWhitelisted", "controlSenderPropertySetter", "controlSenderPropertyName", "controlSenderPropertyValue", "controlRecipientPropertySetter", "controlRecipientPropertyName", "controlRecipientPropertyValue", "controlExpression", "controlParams", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    cancelCoinExchange(true, null, "chain", "order", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getBidOrderIds(true, null, "chain", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getBlockchainStatus(false, null, ""),

    getConstants(false, null, ""),

    getTransaction(true, null, "chain", "fullHash", "includePhasingResult", "requireBlock", "requireLastBlock"),

    getBlock(false, null, "block", "height", "timestamp", "includeTransactions", "includeExecutedPhased", "requireBlock", "requireLastBlock"),

    verifyTaggedData(true, "file", "chain", "transactionFullHash", "name", "description", "tags", "type", "channel", "isText", "filename", "data", "requireBlock", "requireLastBlock"),

    getExchangesByExchangeRequest(true, null, "chain", "transaction", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getPrunableMessage(true, null, "chain", "transactionFullHash", "secretPhrase", "sharedKey", "retrieve", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    dividendPayment(true, null, "chain", "holding", "holdingType", "asset", "height", "amountNQTPerShare", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    broadcastTransaction(false, null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON"),

    currencySell(true, null, "chain", "currency", "rateNQTPerUnit", "unitsQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    blacklistPeer(false, null, "peer", "adminPassword"),

    dgsDelivery(true, null, "chain", "purchase", "discountNQT", "goodsToEncrypt", "goodsIsText", "goodsData", "goodsNonce", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    setAccountProperty(true, null, "chain", "recipient", "property", "value", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    startStandbyShuffler(true, null, "chain", "secretPhrase", "holdingType", "holding", "minAmount", "maxAmount", "minParticipants", "feeRateNQTPerFXT", "recipientPublicKeys", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getShufflers(false, null, "account", "shufflingFullHash", "secretPhrase", "adminPassword", "includeParticipantState", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getDGSGoodsPurchaseCount(true, null, "chain", "goods", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    sendTransaction(false, null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "adminPassword"),

    getAssignedShufflings(true, null, "chain", "account", "includeHoldingInfo", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getGuaranteedBalance(false, null, "account", "numberOfConfirmations", "requireBlock", "requireLastBlock"),

    fullHashToId(false, null, "fullHash"),

    getExpectedBuyOffers(true, null, "chain", "currency", "account", "sortByRate", "requireBlock", "requireLastBlock"),

    getAskOrders(true, null, "chain", "asset", "firstIndex", "lastIndex", "showExpectedCancellations", "adminPassword", "requireBlock", "requireLastBlock"),

    stopForging(false, null, "secretPhrase", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAccountExchangeRequests(true, null, "chain", "account", "currency", "includeCurrencyInfo", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    downloadPrunableMessage(true, null, "chain", "transactionFullHash", "secretPhrase", "sharedKey", "retrieve", "save", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAsset(false, null, "asset", "includeCounts", "requireBlock", "requireLastBlock"),

    clearUnconfirmedTransactions(false, null, "adminPassword"),

    getBundlers(true, null, "chain", "account", "secretPhrase", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getHoldingShufflings(true, null, "chain", "holding", "stage", "includeFinished", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAssetDividends(true, null, "chain", "asset", "firstIndex", "lastIndex", "timestamp", "includeHoldingInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getEffectiveBalance(false, null, "account", "height", "requireBlock", "requireLastBlock"),

    getAssetPhasedTransactions(true, null, "chain", "asset", "account", "withoutWhitelist", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountCurrentBidOrders(true, null, "chain", "account", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    dgsQuantityChange(true, null, "chain", "goods", "deltaQuantity", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getExpectedCurrencyTransfers(true, null, "chain", "currency", "account", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    cancelAskOrder(true, null, "chain", "order", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    evaluateExpression(false, null, "expression", "checkOptimality", "evaluate", "vars", "vars", "vars", "values", "values", "values"),

    searchAssets(false, null, "query", "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    triggerContractByHeight(false, null, "contractName", "height", "apply", "adminPassword", "requireBlock", "requireLastBlock"),

    getDataTagCount(true, null, "chain", "requireBlock", "requireLastBlock"),

    dgsDelisting(true, null, "chain", "goods", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    deleteCurrency(true, null, "chain", "currency", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAssetTransfers(false, null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getBalance(true, null, "chain", "account", "height", "requireBlock", "requireLastBlock"),

    getCurrencyPhasedTransactions(true, null, "chain", "currency", "account", "withoutWhitelist", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    setPhasingOnlyControl(true, null, "chain", "controlVotingModel", "controlQuorum", "controlMinBalance", "controlMinBalanceModel", "controlHolding", "controlWhitelisted", "controlWhitelisted", "controlWhitelisted", "controlSenderPropertySetter", "controlSenderPropertyName", "controlSenderPropertyValue", "controlRecipientPropertySetter", "controlRecipientPropertyName", "controlRecipientPropertyValue", "controlExpression", "controlMaxFees", "controlMaxFees", "controlMaxFees", "controlMinDuration", "controlMaxDuration", "controlParams", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getCurrencies(false, null, "currencies", "currencies", "currencies", "includeCounts", "requireBlock", "requireLastBlock"),

    getDGSGoods(true, null, "chain", "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    currencyReserveIncrease(true, null, "chain", "currency", "amountPerUnitNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    deleteAssetShares(true, null, "chain", "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    setLogging(false, null, "logLevel", "communicationLogging", "adminPassword"),

    getAliasCount(true, null, "chain", "account", "requireBlock", "requireLastBlock"),

    getTransactionBytes(true, null, "chain", "fullHash", "requireBlock", "requireLastBlock"),

    retrievePrunedTransaction(true, null, "chain", "transactionFullHash"),

    getExpectedAssetTransfers(true, null, "chain", "asset", "account", "includeAssetInfo", "requireBlock", "requireLastBlock"),

    getAllAssets(false, null, "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    hash(false, null, "hashAlgorithm", "secret", "secretIsText"),

    createPoll(true, null, "chain", "name", "description", "finishHeight", "votingModel", "minNumberOfOptions", "maxNumberOfOptions", "minRangeValue", "maxRangeValue", "minBalance", "minBalanceModel", "holding", "option00", "option01", "option02", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    verifyPrunableMessage(true, null, "chain", "transactionFullHash", "message", "messageIsText", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "compressMessageToEncrypt", "requireBlock", "requireLastBlock"),

    getDGSPurchase(true, null, "chain", "purchase", "secretPhrase", "sharedKey", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getReferencingTransactions(true, null, "chain", "transactionFullHash", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getForging(false, null, "secretPhrase", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    readMessage(true, null, "chain", "transactionFullHash", "secretPhrase", "sharedKey", "retrieve", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    luceneReindex(false, null, "adminPassword"),

    deleteAssetProperty(true, null, "chain", "asset", "property", "setter", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getExpectedCoinExchangeOrders(true, null, "chain", "exchange", "account", "requireBlock", "requireLastBlock"),

    fullReset(false, null, "adminPassword"),

    getAccountBlockIds(false, null, "account", "timestamp", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getPollResult(true, null, "chain", "poll", "votingModel", "holding", "minBalance", "minBalanceModel", "requireBlock", "requireLastBlock"),

    exchangeCoins(true, null, "chain", "exchange", "quantityQNT", "priceNQTPerCoin", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getDGSPurchaseCount(true, null, "chain", "seller", "buyer", "withPublicFeedbacksOnly", "completed", "requireBlock", "requireLastBlock"),

    getAllWaitingTransactions(false, null, "requireBlock", "requireLastBlock"),

    decryptFrom(false, null, "account", "data", "nonce", "decryptedMessageIsText", "uncompressDecryptedMessage", "secretPhrase", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAccountAssetCount(false, null, "account", "height", "requireBlock", "requireLastBlock"),

    getAssets(false, null, "assets", "assets", "assets", "includeCounts", "requireBlock", "requireLastBlock"),

    getCurrenciesByIssuer(false, null, "account", "account", "account", "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    getBundlerRates(false, null, "minBundlerBalanceFXT", "minBundlerFeeLimitFQT"),

    getPeers(false, null, "active", "state", "service", "service", "service", "includePeerInfo", "version", "includeNewer", "connect", "adminPassword"),

    getAllShufflings(true, null, "chain", "includeFinished", "includeHoldingInfo", "finishedOnly", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    placeAskOrder(true, null, "chain", "asset", "quantityQNT", "priceNQTPerShare", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    rebroadcastUnconfirmedTransactions(false, null, "adminPassword"),

    startBundler(true, null, "chain", "secretPhrase", "minRateNQTPerFXT", "totalFeesLimitFQT", "overpayFQTPerFXT", "feeCalculatorName", "filter", "filter", "filter", "bundlingRulesJSON", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAllCurrencies(false, null, "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    setAccountInfo(true, null, "chain", "name", "description", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getDGSGood(true, null, "chain", "goods", "includeCounts", "requireBlock", "requireLastBlock"),

    getAskOrderIds(true, null, "chain", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountCurrencyCount(false, null, "account", "height", "requireBlock", "requireLastBlock"),

    getAskOrder(true, null, "chain", "order", "requireBlock", "requireLastBlock"),

    getFxtTransaction(false, null, "transaction", "fullHash", "includeChildTransactions", "requireBlock", "requireLastBlock"),

    getExpectedExchangeRequests(true, null, "chain", "account", "currency", "includeCurrencyInfo", "requireBlock", "requireLastBlock"),

    getCurrencyIds(false, null, "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    shufflingProcess(true, null, "chain", "shufflingFullHash", "recipientSecretPhrase", "recipientPublicKey", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    requeueUnconfirmedTransactions(false, null, "adminPassword"),

    signTransaction(false, null, "unsignedTransactionJSON", "unsignedTransactionBytes", "prunableAttachmentJSON", "secretPhrase", "validate", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    deleteContractReference(true, null, "chain", "contractName", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAliases(true, null, "chain", "timestamp", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    trimDerivedTables(false, null, "adminPassword"),

    getSellOffers(true, null, "chain", "currency", "account", "availableOnly", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAssetHistory(false, null, "asset", "account", "firstIndex", "lastIndex", "timestamp", "deletesOnly", "increasesOnly", "includeAssetInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getLog(false, null, "count", "adminPassword"),

    getCoinExchangeOrders(true, null, "chain", "exchange", "account", "firstIndex", "lastIndex", "showExpectedCancellations", "adminPassword", "requireBlock", "requireLastBlock"),

    getAccountLedgerEntry(false, null, "ledgerId", "includeTransaction", "includeHoldingInfo"),

    transferAsset(true, null, "chain", "recipient", "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    triggerContractByTransaction(true, null, "chain", "triggerFullHash", "apply", "validate", "adminPassword", "requireBlock", "requireLastBlock"),

    stopShuffler(false, null, "account", "shufflingFullHash", "secretPhrase", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    bundleTransactions(true, null, "chain", "transactionFullHash", "transactionFullHash", "transactionFullHash", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getBalances(true, null, "chain", "chain", "chain", "account", "height", "requireBlock", "requireLastBlock"),

    getContractReferences(false, null, "account", "contractName", "includeContract", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    publishExchangeOffer(true, null, "chain", "currency", "buyRateNQTPerUnit", "sellRateNQTPerUnit", "totalBuyLimitQNT", "totalSellLimitQNT", "initialBuySupplyQNT", "initialSellSupplyQNT", "expirationHeight", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getLinkedPhasedTransactions(false, null, "linkedFullHash", "requireBlock", "requireLastBlock"),

    triggerContractByRequest(false, null, "contractName", "setupParams", "adminPassword", "requireBlock", "requireLastBlock"),

    approveTransaction(true, null, "chain", "phasedTransaction", "phasedTransaction", "phasedTransaction", "revealedSecret", "revealedSecret", "revealedSecret", "revealedSecretIsText", "revealedSecretText", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getDGSTagsLike(true, null, "chain", "tagPrefix", "inStockOnly", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    parseTransaction(false, null, "transactionJSON", "transactionBytes", "prunableAttachmentJSON", "requireBlock", "requireLastBlock"),

    getCurrency(true, null, "chain", "currency", "code", "includeCounts", "includeDeleted", "requireBlock", "requireLastBlock"),

    increaseAssetShares(true, null, "chain", "asset", "quantityQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getBidOrders(true, null, "chain", "asset", "firstIndex", "lastIndex", "showExpectedCancellations", "adminPassword", "requireBlock", "requireLastBlock"),

    getCoinExchangeOrder(false, null, "order", "requireBlock", "requireLastBlock"),

    getDGSGoodsCount(true, null, "chain", "seller", "inStockOnly", "requireBlock", "requireLastBlock"),

    getCurrencyAccountCount(false, null, "currency", "height", "requireBlock", "requireLastBlock"),

    getDGSPurchases(true, null, "chain", "seller", "buyer", "firstIndex", "lastIndex", "withPublicFeedbacksOnly", "completed", "adminPassword", "requireBlock", "requireLastBlock"),

    getShufflingParticipants(true, null, "chain", "shufflingFullHash", "requireBlock", "requireLastBlock"),

    getAccountLessors(false, null, "account", "height", "requireBlock", "requireLastBlock"),

    setAssetProperty(true, null, "chain", "asset", "property", "value", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getSupportedContracts(false, null, "requireBlock", "requireLastBlock"),

    startShuffler(true, null, "chain", "secretPhrase", "shufflingFullHash", "recipientSecretPhrase", "recipientPublicKey", "feeRateNQTPerFXT", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    blacklistBundler(false, null, "account", "adminPassword"),

    getPoll(true, null, "chain", "poll", "requireBlock", "requireLastBlock"),

    getVoterPhasedTransactions(true, null, "chain", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    transferCurrency(true, null, "chain", "recipient", "currency", "unitsQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    leaseBalance(true, null, "chain", "period", "recipient", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    setAlias(true, null, "chain", "aliasName", "aliasURI", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    shutdown(false, null, "scan", "adminPassword"),

    getHashedSecretPhasedTransactions(false, null, "phasingHashedSecret", "phasingHashedSecretAlgorithm", "requireBlock", "requireLastBlock"),

    getDGSExpiredPurchases(true, null, "chain", "seller", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    searchCurrencies(false, null, "query", "firstIndex", "lastIndex", "includeCounts", "adminPassword", "requireBlock", "requireLastBlock"),

    shufflingRegister(true, null, "chain", "shufflingFullHash", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    currencyReserveClaim(true, null, "chain", "currency", "unitsQNT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getPollVotes(true, null, "chain", "poll", "firstIndex", "lastIndex", "includeWeights", "adminPassword", "requireBlock", "requireLastBlock"),

    getStandbyShufflers(true, null, "chain", "secretPhrase", "holdingType", "holding", "account", "includeHoldingInfo", "adminPassword", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getAccountCurrentAskOrders(true, null, "chain", "account", "asset", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getDGSTags(true, null, "chain", "inStockOnly", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getOrderTrades(true, null, "chain", "askOrderFullHash", "bidOrderFullHash", "includeAssetInfo", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    sellAlias(true, null, "chain", "alias", "aliasName", "recipient", "priceNQT", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    dumpPeers(false, null, "version", "includeNewer", "connect", "adminPassword", "service", "service", "service"),

    getAllOpenAskOrders(true, null, "chain", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getAllPrunableMessages(true, null, "chain", "firstIndex", "lastIndex", "timestamp", "adminPassword", "requireBlock", "requireLastBlock"),

    dgsFeedback(true, null, "chain", "purchase", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getPhasingPoll(true, null, "chain", "transactionFullHash", "countVotes", "requireBlock", "requireLastBlock"),

    shufflingVerify(true, null, "chain", "shufflingFullHash", "shufflingStateHash", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getDGSGoodsPurchases(true, null, "chain", "goods", "buyer", "firstIndex", "lastIndex", "withPublicFeedbacksOnly", "completed", "adminPassword", "requireBlock", "requireLastBlock"),

    getAssetAccountCount(false, null, "asset", "height", "requireBlock", "requireLastBlock"),

    getPhasingPollVotes(true, null, "chain", "transactionFullHash", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    retrievePrunedData(true, null, "chain", "adminPassword"),

    getUnconfirmedTransactions(true, null, "chain", "account", "account", "account", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    encryptTo(false, null, "recipient", "messageToEncrypt", "messageToEncryptIsText", "compressMessageToEncrypt", "secretPhrase", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getBuyOffers(true, null, "chain", "currency", "account", "availableOnly", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getState(true, null, "chain", "includeCounts", "adminPassword"),

    issueCurrency(true, null, "chain", "name", "code", "description", "type", "initialSupplyQNT", "reserveSupplyQNT", "maxSupplyQNT", "issuanceHeight", "minReservePerUnitNQT", "minDifficulty", "maxDifficulty", "ruleset", "algorithm", "decimals", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAccountId(false, null, "secretPhrase", "publicKey", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    getCoinExchangeTrade(false, null, "orderFullHash", "matchFullHash", "requireBlock", "requireLastBlock"),

    issueAsset(true, null, "chain", "name", "description", "quantityQNT", "decimals", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    combineSecret(false, null, "pieces", "pieces", "pieces"),

    getTrades(true, null, "chain", "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "adminPassword", "requireBlock", "requireLastBlock"),

    getPrunableMessages(true, null, "chain", "account", "otherAccount", "secretPhrase", "firstIndex", "lastIndex", "timestamp", "adminPassword", "requireBlock", "requireLastBlock", "sharedPieceAccount", "sharedPiece", "sharedPiece", "sharedPiece"),

    calculateFullHash(false, null, "unsignedTransactionBytes", "unsignedTransactionJSON", "signatureHash"),

    currencyMint(true, null, "chain", "currency", "nonce", "unitsQNT", "counter", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    uploadTaggedData(true, "file", "chain", "name", "description", "tags", "type", "channel", "isText", "filename", "data", "secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT", "minBundlerFeeLimitFQT", "deadline", "referencedTransaction", "broadcast", "timestamp", "message", "messageIsText", "messageIsPrunable", "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt", "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf", "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel", "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls", "phasingSenderPropertySetter", "phasingSenderPropertyName", "phasingSenderPropertyValue", "phasingRecipientPropertySetter", "phasingRecipientPropertyName", "phasingRecipientPropertyValue", "phasingExpression", "recipientPublicKey", "ecBlockId", "ecBlockHeight", "voucher", "sharedPiece", "sharedPiece", "sharedPiece", "sharedPieceAccount"),

    getAccountProperties(false, null, "recipient", "property", "setter", "firstIndex", "lastIndex", "adminPassword", "requireBlock", "requireLastBlock"),

    getExchanges(true, null, "chain", "currency", "account", "firstIndex", "lastIndex", "timestamp", "includeCurrencyInfo", "adminPassword", "requireBlock", "requireLastBlock");

    private final boolean isChainSpecific;

    private final String fileParameter;

    private final List<String> parameters;

    ApiSpec(boolean isChainSpecific, String fileParameter, String... parameters) {
        this.isChainSpecific = isChainSpecific;
        this.fileParameter = fileParameter;
        this.parameters = Arrays.asList(parameters);}

    public boolean isChainSpecific() {
        return isChainSpecific;
    }

    public String getFileParameter() {
        return fileParameter;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
