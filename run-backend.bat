@echo off
setlocal

set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-26"
set "MAVEN_HOME=C:\apache-maven-3.9.14"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo [1/2] Compiling letitsim-main...
cd /d "%ROOT%letitsim-main"
call mvn compile
if errorlevel 1 (
  echo.
  echo Failed to compile letitsim-main.
  exit /b 1
)

echo.
echo [2/2] Starting backend-java...
cd /d "%ROOT%simulation-dashboard\backend-java"
call mvn spring-boot:run

endlocal
