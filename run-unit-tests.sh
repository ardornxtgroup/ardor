#!/bin/sh
PATHSEP=":"
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";"
fi

CP=conf/${PATHSEP}classes/${PATHSEP}lib/*${PATHSEP}testlib/*${PATHSEP}addons/classes${PATHSEP}addons/lib/*
SP=src/java/${PATHSEP}test/java/

if [ $# -eq 0 ]; then
TESTS="nxt.FullAutoSuite"
else
TESTS=$@
fi

/bin/rm -f nxt.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath ${SP} -classpath ${CP} -d classes/ src/java/nxt/*.java src/java/nxt/*/*.java test/java/nxt/*.java test/java/nxt/*/*.java || exit 1

cp test/java/gitlab-ci-logging.properties  classes/gitlab-ci-logging.properties

for TEST in ${TESTS} ; do
java ${TEST_SYSTEM_PROPERTIES} -classpath ${CP} nxt.JUnitCoreWithListeners ${TEST} ;
done



