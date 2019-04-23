#!/bin/sh
if [ -x jdk/bin/java ]; then
    JAR=./jdk/bin/jar
elif [ -x ../jdk/bin/java ]; then
    JAR=../jdk/bin/jar
else
    JAR=jar
fi

/bin/rm -f ardor-tests.jar
${JAR} cf ardor-tests.jar -C ./test/classes . || exit 1


echo "jar file generated successfully"
