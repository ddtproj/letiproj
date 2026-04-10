@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_ROOT=%~dp0"
cd /d "%PROJECT_ROOT%"

set "BPMN_PATH=%~1"
if not defined BPMN_PATH set "BPMN_PATH=credit_card_application.bpmn"

set "LOG_PATH=%~2"
if not defined LOG_PATH set "LOG_PATH=log.txt"

set "JSON_PATH=%~3"
if not defined JSON_PATH set "JSON_PATH=parsed-log-dto.json"

set "JAVA_EXE="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

if not defined JAVA_EXE (
    for %%F in (java.exe) do set "JAVA_EXE=%%~$PATH:F"
)

if not defined JAVA_EXE (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot\bin\java.exe" set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot\bin\java.exe"
)

if not defined JAVA_EXE (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-8*") do (
        if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
    )
)

if not defined JAVA_EXE (
    for /d %%D in ("C:\Program Files\Java\jdk1.8*") do (
        if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
    )
)

if not defined JAVA_EXE (
    for /d %%D in ("C:\Program Files\Java\jdk-8*") do (
        if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
    )
)

if not defined JAVA_EXE (
    echo [ERROR] Java ne naidena. Ustanovite JDK 8 ili zadayte JAVA_HOME.
    exit /b 1
)

if not exist "target\classes\Main.class" (
    echo [ERROR] Ne naideny skompilirovannye class-faily v target\classes.
    echo         Snachala soberite proekt.
    exit /b 1
)

if not exist "%BPMN_PATH%" (
    echo [ERROR] BPMN-fail ne naiden: %BPMN_PATH%
    exit /b 1
)

set "JDOM_JAR=lib\jdom2-2.0.6.jar"
set "JAXB_JAR=lib\jaxb-api-2.3.1.jar"
set "COLT_JAR=lib\colt-1.2.0.jar"
set "CONCURRENT_JAR=lib\concurrent-1.3.4.jar"

if not exist "%JDOM_JAR%" set "JDOM_JAR=%USERPROFILE%\.m2\repository\org\jdom\jdom2\2.0.6\jdom2-2.0.6.jar"
if not exist "%JAXB_JAR%" set "JAXB_JAR=%USERPROFILE%\.m2\repository\javax\xml\bind\jaxb-api\2.3.1\jaxb-api-2.3.1.jar"
if not exist "%COLT_JAR%" set "COLT_JAR=%USERPROFILE%\.m2\repository\colt\colt\1.2.0\colt-1.2.0.jar"
if not exist "%CONCURRENT_JAR%" set "CONCURRENT_JAR=%USERPROFILE%\.m2\repository\concurrent\concurrent\1.3.4\concurrent-1.3.4.jar"

for %%F in ("%JDOM_JAR%" "%JAXB_JAR%" "%COLT_JAR%" "%CONCURRENT_JAR%") do (
    if not exist "%%~fF" (
        echo [ERROR] Ne hvataet zavisimosti v .m2: %%~fF
        exit /b 1
    )
)

set "CP=target\classes;%JDOM_JAR%;%JAXB_JAR%;%COLT_JAR%;%CONCURRENT_JAR%"

echo Java: %JAVA_EXE%
echo BPMN: %BPMN_PATH%
echo Log:  %LOG_PATH%
echo JSON: %JSON_PATH%
echo.

"%JAVA_EXE%" -cp "%CP%" Main "%BPMN_PATH%" "%LOG_PATH%" "%JSON_PATH%"
exit /b %ERRORLEVEL%
