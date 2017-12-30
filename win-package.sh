#!/bin/sh
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=ardor-client-${VERSION}.zip
echo PACKAGE="${PACKAGE}"

FILES="changelogs classes conf html lib src resource addons"
FILES="${FILES} ardor.jar ardorservice.jar"
FILES="${FILES} 3RD-PARTY-LICENSES.txt LICENSE.txt JPL-Ardor.pdf"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat"
FILES="${FILES} nxt.policy nxtdesktop.policy Ardor_Wallet.url"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"

echo compile
./win-compile.sh
echo jar
./jar.sh
echo javadoc
rm -rf html/doc/*
./win-javadoc.sh

rm -rf ardor
rm -rf ${PACKAGE}
mkdir -p ardor/
mkdir -p ardor/logs
echo copy resources
cp -a ${FILES} ardor
echo gzip
for f in `find ardor/html -name *.gz`
do
	rm -f "$f"
done
for f in `find ardor/html -name *.html -o -name *.js -o -name *.css -o -name *.json -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
echo zip
zip -q -X -r ${PACKAGE} ardor -x \*/.idea/\* \*/.gitignore \*/.git/\* \*/\*.log \*.iml ardor/conf/nxt.properties ardor/conf/logging.properties ardor/conf/localstorage/\*
rm -rf ardor
echo done
