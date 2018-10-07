#!/bin/sh
echo "***********************************************************************"
echo "* Use this shell script to search for a lost passphrase.              *"
echo "***********************************************************************"

java -Xmx1024m -cp "classes:lib/*:conf" $@ nxt.tools.PassphraseRecovery
exit $?
