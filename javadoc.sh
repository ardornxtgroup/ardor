#!/bin/sh
PATHSEP=":" 
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";" 
fi

CP="lib/*${PATHSEP}classes${PATHSEP}addons/classes"
SP=src/java/${PATHSEP}addons/src/java/

/bin/rm -rf html/doc/*

javadoc -quiet -sourcepath ${SP} -classpath "${CP}" -protected -splitindex -subpackages nxt:com.jelurida -d html/doc/
