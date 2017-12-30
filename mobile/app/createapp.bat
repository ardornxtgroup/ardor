rmdir /s /q wallet
call cordova create wallet com.jelurida.ardor.wallet "Ardor Mobile Wallet" --template ..\..\html
cd wallet
rmdir /s /q plugins
xcopy /y/i/s ..\..\plugins plugins
call cordova platform add android@6.2.3
xcopy /y/i/s ..\..\platforms platforms
cd ..