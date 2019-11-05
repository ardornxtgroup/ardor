#!/bin/bash

PATHSEP=":"
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";"
fi

SETUP="setup.xml"
if [ "$OSTYPE" = "linux-gnu" ] ; then
SETUP="setup-unix.xml"
fi

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
    JAR=./jdk/bin/jar
    JAVAC=./jdk/bin/javac
elif [ -x ../jdk/bin/java ]; then
    JAVA=../jdk/bin/java
    JAR=../jdk/bin/jar
    JAVAC=../jdk/bin/javac
else
    JAVA=java
    JAR=jar
    JAVAC=javac
fi

CLASSDIR=../installer/panels/classes
CONFDIR=$CLASSDIR/nxt/installer/resources
JARFILE=$CLASSDIR/ardor-panels.jar

# build custom panels
rm -rf $CLASSDIR
mkdir -p $CLASSDIR
${JAVAC} -cp "../installer/lib/*" -d $CLASSDIR -sourcepath ../installer/panels/src ../installer/panels/src/nxt/installer/*.java

mkdir -p $CONFDIR
cp ../conf/examples/*.properties $CONFDIR
(cd $CONFDIR; ls *.properties > settings.txt)

${JAR} cf0 $JARFILE -C $CLASSDIR nxt

# package the installer
${JAVA} -Xmx512m -cp "../installer/lib/*${PATHSEP}${JARFILE}" com.izforge.izpack.compiler.bootstrap.CompilerLauncher ../installer/${SETUP} -o $1.jar > ../installer/build-installer.log 2>&1

# cleanup
rm -rf $CLASSDIR
