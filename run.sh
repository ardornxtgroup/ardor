#!/bin/sh
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -Xms256M -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* nxt.Nxt
