var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    const decimals = 8;
    var quantity = 2.5;
    var price = 1.3;
    var data = {
        exchange: "1",
        quantityQNT: NRS.convertToQNT(quantity, decimals),
        priceNQTPerCoin: NRS.convertToNQT(price),
        secretPhrase: config.secretPhrase,
        chain: config.chain
    };
    data = Object.assign(
        data,
        NRS.getMandatoryParams()
    );
    NRS.sendRequest("exchangeCoins", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
