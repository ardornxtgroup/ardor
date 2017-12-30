#!/bin/sh
/bin/rm -f ardor-tests.jar
jar cf ardor-tests.jar -C ./test/classes . || exit 1


echo "jar file generated successfully"
