#!/bin/sh

echo "***********************************************"
echo "** DEPRECATED: Use 'run.sh --daemon' instead **"
echo "***********************************************"
sleep 1

if [ -e ~/.ardor/nxt.pid ]; then
    PID=`cat ~/.ardor/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Ardor server already running"
        exit 1
    fi
fi
mkdir -p ~/.ardor/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -Xms256M -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* nxt.Nxt > /dev/null 2>&1 &
echo $! > ~/.ardor/nxt.pid
cd - > /dev/null
