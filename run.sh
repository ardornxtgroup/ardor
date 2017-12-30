#!/bin/sh
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
${JAVA} -Xms256M -cp classes:lib/*:conf:addons/classes:addons/lib/* nxt.Nxt
