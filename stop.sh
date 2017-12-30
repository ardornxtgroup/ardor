#!/bin/sh
if [ -e ~/.ardor/nxt.pid ]; then
    PID=`cat ~/.ardor/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.ardor/nxt.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.ardor/nxt.pid
    echo "Ardor server stopped"
fi

