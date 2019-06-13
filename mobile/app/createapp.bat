rmdir /s /q wallet
call cordova create wallet com.jelurida.ardor.wallet "Ardor Mobile Wallet" --template ..\..\html
cd wallet
call cordova platform add android
call cordova platform rm android
call cordova platform add android
xcopy /y/i/s ..\..\platforms platforms
cd ..