var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    const decimals = 2;
    var quantity = 2.5;
    var price = 1.3;
    var data = {
        asset: "16056551815000754623",
        quantityQNT: NRS.convertToQNT(quantity, decimals),
        priceNQTPerShare: NRS.convertToNQT(price),
        secretPhrase: config.secretPhrase,
        chain: config.chain
    };
    data = Object.assign(
        data,
        NRS.getMandatoryParams()
    );
    NRS.sendRequest("placeAskOrder", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
