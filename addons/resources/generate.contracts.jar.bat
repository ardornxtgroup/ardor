rem quick and dirty creation of this sample Jar file
xcopy /e/k/s/h/i ..\..\classes\com\jelurida\ardor\contracts\IgnisArdorRates* com\jelurida\ardor\contracts
xcopy /e/k/s/h/i ..\..\classes\com\jelurida\ardor\contracts\Bittrex* com\jelurida\ardor\contracts
jar -cvf contracts.jar com
rmdir /s /q com