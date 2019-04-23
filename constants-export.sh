#!/bin/sh

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi

PATHSEP=":" 
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";" 
fi

${JAVA} -cp "classes${PATHSEP}lib/*${PATHSEP}conf" -Dnxt.properties=./conf/nxt-default.properties nxt.tools.ConstantsExporter html/www/js/data/constants.mainnet.js

${JAVA} -cp "classes${PATHSEP}lib/*${PATHSEP}conf" -Dnxt.properties=./conf/examples/testnet.properties nxt.tools.ConstantsExporter html/www/js/data/constants.testnet.js

