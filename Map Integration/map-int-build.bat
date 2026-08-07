@echo off
setlocal
title Map Integration Builder

set "SCRIPT_DIR=%~dp0"
set "GRADLE_EXE=%SCRIPT_DIR%gradle-runtime\gradle-9.2.0\bin\gradle.bat"
set "LOG=%SCRIPT_DIR%build-log.txt"
set "MODSDIR_BAREBONE=C:\Users\Areo\AppData\Roaming\norisk\NoRiskClientV3\data\profiles\Barebones\mods\nrc-1.21.11-fabric"
set "MODSDIR_MINIONS=C:\Users\Areo\AppData\Roaming\norisk\NoRiskClientV3\data\profiles\Minions\mods\nrc-1.21.11-fabric"

echo.
echo  +======================================+
echo  ^|  Map Integration -- Auto Builder     ^|
echo  +======================================+
echo.

echo [1/5] Checking for Java 21...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java not found.
    pause & exit /b 1
)
echo Java found.

if not exist "%GRADLE_EXE%" (
    echo [ERROR] Gradle not found.
    pause & exit /b 1
)
echo Gradle found.

echo.
echo [2/5] Building...
set "JAVA_HOME=C:\Users\Areo\AppData\Local\Programs\Microsoft\jdk-21.0.11.10-hotspot"
call "%GRADLE_EXE%" build --no-daemon --stacktrace > "%LOG%" 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build failed. Last 60 lines:
    echo.
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Content '%LOG%' | Select-Object -Last 60"
    echo.
    pause & exit /b 1
)

echo.
echo [3/5] Locating JAR...
set "FOUND="
for %%F in ("%SCRIPT_DIR%build\libs\*.jar") do (
    echo %%~nxF | findstr /i "\-dev\." >nul
    if errorlevel 1 (
        echo %%~nxF | findstr /i "\-sources\." >nul
        if errorlevel 1 (
            set "FOUND=%%~fF"
            set "JARNAME=%%~nxF"
        )
    )
)
if not defined FOUND (
    echo [ERROR] JAR not found in build\libs.
    pause & exit /b 1
)
echo Found: %JARNAME%

echo.
echo [4/5] Killing Minecraft...
taskkill /F /IM javaw.exe >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1
ping -n 3 127.0.0.1 >nul 2>&1

echo.
echo [5/5] Copying JAR to mods folders...
if exist "%MODSDIR_BAREBONE%" (
    for %%F in ("%MODSDIR_BAREBONE%\map-integration-*.jar") do del /f /q "%%F" >nul 2>&1
    copy /Y "%FOUND%" "%MODSDIR_BAREBONE%\%JARNAME%" >nul
    echo Copied to Barebones.
) else (
    echo [WARNING] Barebones mods folder not found.
)
if exist "%MODSDIR_MINIONS%" (
    for %%F in ("%MODSDIR_MINIONS%\map-integration-*.jar") do del /f /q "%%F" >nul 2>&1
    copy /Y "%FOUND%" "%MODSDIR_MINIONS%\%JARNAME%" >nul
    echo Copied to Minions.
) else (
    echo [WARNING] Minions mods folder not found.
)

echo.
echo  Build complete! JAR: %JARNAME%
echo.
pause
