#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=ardor-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=ardor-client-${VERSION}.changelog.txt
OBFUSCATE=$2

FILES="changelogs conf html lib resource contrib logs"
FILES="${FILES} ardor.exe ardorservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat"
FILES="${FILES} nxt.policy nxtdesktop.policy Ardor_Wallet.url Dockerfile"

# unix2dos *.bat
echo compile
./win-compile.sh
rm -rf html/doc/*
rm -rf ardor
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p ardor/
mkdir -p ardor/logs
mkdir -p ardor/addons/src

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo obfuscate
proguard.bat @nxt.pro
mv ../nxt.map ../nxt.map.${VERSION}
mkdir -p ardor/src/
else
FILES="${FILES} classes src JPL-Ardor.pdf"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"
echo javadoc
./win-javadoc.sh
fi
echo copy resources
cp installer/lib/JavaExe.exe ardor.exe
cp installer/lib/JavaExe.exe ardorservice.exe
cp -a ${FILES} ardor
cp -a logs/placeholder.txt ardor/logs
echo gzip
for f in `find ardor/html -name *.gz`
do
	rm -f "$f"
done
for f in `find ardor/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd ardor
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
echo create installer exe
../installer/build-exe.bat ${PACKAGE}
echo create installer zip
cd -
zip -q -X -r ${PACKAGE}.zip ardor -x \*/.idea/\* \*/.gitignore \*/.git/\* \*.iml ardor/conf/nxt.properties ardor/conf/logging.properties ardor/conf/localstorage/\*
rm -rf ardor

echo creating change log ${CHANGELOG}
echo -e "Release $1\n" > ${CHANGELOG}
echo -e "https://www.jelurida.com/\n" >> ${CHANGELOG}
echo -e "sha256 checksums:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.exe >> ${CHANGELOG}

sha256sum ${PACKAGE}.jar >> ${CHANGELOG}

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo -e "\n\nThis is an experimental release for testing only. Source code is not provided." >> ${CHANGELOG}
fi
echo -e "\n\nChange log:\n" >> ${CHANGELOG}

cat changelogs/${CHANGELOG} >> ${CHANGELOG}
echo >> ${CHANGELOG}
