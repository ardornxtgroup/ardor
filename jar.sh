#!/bin/sh
java -cp classes nxt.tools.ManifestGenerator
/bin/rm -f ardor.jar
jar cfm ardor.jar resource/nxt.manifest.mf -C classes . || exit 1
/bin/rm -f ardorservice.jar
jar cfm ardorservice.jar resource/nxtservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"