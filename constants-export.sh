#!/bin/sh

PATHSEP=":" 
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";" 
fi

java -cp "classes${PATHSEP}lib/*${PATHSEP}conf" -Dnxt.properties=./conf/nxt-default.properties nxt.tools.ConstantsExporter html/www/js/data/constants.mainnet.js

java -cp "classes${PATHSEP}lib/*${PATHSEP}conf" -Dnxt.properties=./conf/examples/testnet.properties nxt.tools.ConstantsExporter html/www/js/data/constants.testnet.js

