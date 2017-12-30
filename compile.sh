#!/bin/sh
CP="lib/*:classes"
SP=src/java/

/bin/rm -f ardor.jar
/bin/rm -f ardorservice.jar
/bin/rm -rf classes
/bin/mkdir -p classes/
/bin/rm -rf addons/classes
/bin/mkdir -p addons/classes/

echo "compiling nxt core..."
find src/java/nxt/ -name "*.java" > sources.tmp
javac -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ @sources.tmp || exit 1
echo "nxt core class files compiled successfully"

echo "compiling nxt desktop..."
find src/java/nxtdesktop/ -name "*.java" > sources.tmp
javac -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ @sources.tmp
if [ $? -eq 0 ]; then
    echo "nxt desktop class files compiled successfully"
else
    echo "if javafx is not supported, nxt desktop compile errors are safe to ignore, but desktop wallet will not be available"
fi

rm -f sources.tmp

find addons/src/ -name "*.java" > addons.tmp
if [ -s addons.tmp ]; then
    echo "compiling add-ons..."
    javac -encoding utf8 -sourcepath "${SP}:addons/src" -classpath "${CP}:addons/classes:addons/lib/*" -d addons/classes @addons.tmp || exit 1
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
    javac -encoding utf8 -sourcepath "${SP}:test/java" -classpath "${CP}:testlib/*" -d test/classes @tests.tmp || exit 1
    echo "tests compiled successfully"
else
    echo "no tests to compile"
fi
rm -f tests.tmp

echo "compilation done"
