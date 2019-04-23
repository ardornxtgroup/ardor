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

MACVERSION=$3
if [ -x ${MACVERSION} ];
then
MACVERSION=${VERSION}
fi
echo MACVERSION="${MACVERSION}"

FILES="changelogs conf html lib testlib javafx-sdk resource contrib"
FILES="${FILES} ardor.exe ardorservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat contractManager.sh contractManager.bat"
FILES="${FILES} ardor.policy ardordesktop.policy contractManager.policy Ardor_Wallet.url Dockerfile"

echo compile
./compile.sh
rm -rf html/doc/*
rm -rf ardor
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p ardor/
mkdir -p ardor/logs

if [ "${OBFUSCATE}" = "obfuscate" ]; 
then
echo obfuscate
~/proguard/proguard5.3.3/bin/proguard.sh @nxt.pro
mv ../nxt.map ../nxt.map.${VERSION}
else
FILES="${FILES} classes src test addons JPL-Ardor.pdf"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh generateAPICalls.sh"
echo javadoc
./javadoc.sh
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
../jar-tests.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
cd -
rm -rf ardor

echo bundle a dmg file	
/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/bin/javapackager -deploy -outdir . -outfile ardor-client -name ardor-installer -width 34 -height 43 -native dmg -srcfiles ${PACKAGE}.jar -appclass com.izforge.izpack.installer.bootstrap.Installer -v -Bmac.category=Business -Bmac.CFBundleIdentifier=org.ardor.client.installer -Bmac.CFBundleName=Ardor-Installer -Bmac.CFBundleVersion=${MACVERSION} -BappVersion=${MACVERSION} -Bicon=installer/AppIcon.icns -Bmac.signing-key-developer-id-app="Developer ID Application: Stichting NXT (YU63QW5EFW)" > installer/javapackager.log 2>&1
