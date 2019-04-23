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

${JAVA} -cp "classes${PATHSEP}lib/*${PATHSEP}conf" nxt.http.APICallGenerator

