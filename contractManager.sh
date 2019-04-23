#!/bin/sh
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -Djava.security.manager -Djava.security.policy=contractManager.policy -cp classes:lib/*:conf:addons/classes nxt.tools.ContractManager $@
