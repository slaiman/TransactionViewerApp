@echo off
setlocal

REM Get project root (parent of scripts folder)
set "PROJECT_ROOT=%~dp0.."

echo Project root:
echo %PROJECT_ROOT%
echo.

echo Starting backend...

start "Transaction Backend" cmd /k "cd /d "%PROJECT_ROOT%\backend" && mvn spring-boot:run"

timeout /t 5 /nobreak >nul

echo Starting frontend...

start "Transaction Frontend" cmd /k "cd /d "%PROJECT_ROOT%\frontend" && npm run dev"

echo.
echo Applications started.
pause