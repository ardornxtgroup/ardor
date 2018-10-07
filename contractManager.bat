rem @ECHO OFF
if exist jre ( 
    @echo when using the --verify flag for contract verification use a Java JDK, for example:
    @echo "c:\Program Files\java\jdk-10.0.2\bin\"
    set javaDir=jre\bin\
)
"%javaDir%java.exe" -Djava.security.manager -Djava.security.policy=contractManager.policy -cp classes;lib\*;conf;addons\classes nxt.tools.ContractManager %*