#!/bin/sh

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
    JAVAC=./jdk/bin/javac
    JAR=./jdk/bin/jar
else
    JAVA=java
    JAVAC=javac
    JAR=jar
fi

PATHSEP=":"
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";" 
fi

CP="lib/*${PATHSEP}classes${PATHSEP}javafx-sdk/lib/*"
SP=src/java/

/bin/rm -f ardor.jar
/bin/rm -f ardorservice.jar
/bin/rm -rf classes
/bin/mkdir -p classes/
/bin/rm -rf addons/classes
/bin/mkdir -p addons/classes/

echo "compiling core..."
find src/java/nxt/ -name "*.java" > sources.tmp
${JAVAC} -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ @sources.tmp || exit 1
echo "core class files compiled successfully"

echo "compiling desktop..."
find src/java/nxtdesktop/ -name "*.java" > sources.tmp
${JAVAC} -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ @sources.tmp
if [ $? -eq 0 ]; then
    echo "desktop class files compiled successfully"
else
    echo "if javafx is not supported, desktop compile errors are safe to ignore, but desktop wallet will not be available"
fi

rm -f sources.tmp

find addons/src/ -name "*.java" > addons.tmp
if [ -s addons.tmp ]; then
    echo "compiling add-ons..."
    ${JAVAC} -encoding utf8 -sourcepath "${SP}${PATHSEP}addons/src" -classpath "${CP}${PATHSEP}addons/classes${PATHSEP}addons/lib/*" -d addons/classes @addons.tmp || exit 1
    echo "add-ons compiled successfully"
else
    echo "no add-ons to compile"
fi
rm -f addons.tmp

find test/java/ -name "*.java" > tests.tmp
if [ -s tests.tmp ]; then
    echo "compiling tests..."
    /bin/rm -rf test/classes
    /bin/mkdir -p test/classes/
    ${JAVAC} -encoding utf8 -sourcepath "${SP}${PATHSEP}test/java${PATHSEP}addons/test/java" -classpath "${CP}${PATHSEP}addons/classes${PATHSEP}testlib/*" -d test/classes @tests.tmp || exit 1
    echo "tests compiled successfully"
else
    echo "no tests to compile"
fi
rm -f tests.tmp

find installer/panels/src -name "*.java" > panels.tmp
if [ -s panels.tmp ]; then
    echo "compiling installer panels..."
    /bin/rm -rf installer/panels/classes
    /bin/mkdir -p installer/panels/classes/
    ${JAVAC} -encoding utf8 -sourcepath "installer/panels/src" -classpath "${CP}${PATHSEP}installer/lib/*" -d installer/panels/classes @panels.tmp || exit 1
    echo "installer panels compiled successfully"
else
    echo "no installer panels to compile"
fi
rm -f panels.tmp

echo "compilation done"
