var loader = require("./loader");
var config = loader.config;

loader.load(function(NRS) {
    const decimals = 2;
    var quantity = 123.45;
    var price = 1.2;
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
    NRS.sendRequest("placeBidOrder", data, function (response) {
        NRS.logConsole(JSON.stringify(response));
    });
});
