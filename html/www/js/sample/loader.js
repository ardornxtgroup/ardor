try {
    var loader = require("./../nrs.node.bridge.js"); // during development
} catch(e) {
    console.log("Release mode");
}

try {
    loader = require("ardor-blockchain"); // when using the NPM module
} catch(e) {
    console.log("Development mode");
}

loader.config = require("./config.json");
var config = loader.config;

loader.init({
    url: config.url,
    secretPhrase: config.secretPhrase,
    isTestNet: config.isTestNet,
    chain: config.chain,
    adminPassword: config.adminPassword
});

module.exports = loader;