@echo off
setlocal

set "ROOT=%~dp0"
set "NODE_HOME=C:\Program Files\nodejs"
set "PATH=%NODE_HOME%;%PATH%"

cd /d "%ROOT%simulation-dashboard\frontend"

if not exist "node_modules" (
  echo Installing frontend dependencies...
  call npm install
  if errorlevel 1 (
    echo.
    echo Failed to install frontend dependencies.
    exit /b 1
  )
  echo.
)

echo Starting frontend...
call npm run dev

endlocal
