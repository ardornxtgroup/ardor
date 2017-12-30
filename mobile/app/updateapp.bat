cd wallet
rmdir /s /q www
xcopy /y/i/s ..\..\..\html\www www
xcopy /y/i/s ..\..\..\html\config.xml config.xml
echo f | xcopy /y/i/s ..\..\..\html\www\img\icon.source.png icon.png
call cordova-icon
echo f | xcopy /y/i/s ..\..\..\html\www\img\splash.source.png splash.png
call cordova-splash
cd ..