@echo off
setlocal EnableDelayedExpansion
title Chuck Pack Builder

set "DOWNLOADS=%~dp0"

rem Auto-detect the project location: this script may sit either directly
rem inside the chuck-pack project (next to build.gradle) or in a parent
rem "Downloads" folder alongside a "chuck-pack" subfolder.
if exist "%DOWNLOADS%build.gradle" (
    set "PROJECT=%DOWNLOADS%"
) else if exist "%DOWNLOADS%chuck-pack\build.gradle" (
    set "PROJECT=%DOWNLOADS%chuck-pack"
) else (
    set "PROJECT="
)

set "GRADLE_VER=9.4.0"
set "GRADLE_ZIP=%DOWNLOADS%gradle-%GRADLE_VER%-bin.zip"
set "GRADLE_DIR=%DOWNLOADS%gradle-runtime"
set "GRADLE_EXE=%GRADLE_DIR%\gradle-%GRADLE_VER%\bin\gradle.bat"
set "LOG=%DOWNLOADS%build-log.txt"
set "MODSDIR_BAREBONE=C:\Users\Chuck\AppData\Roaming\norisk\NoRiskClientV3\data\profiles\Barebones\mods\nrc-26.1.2-fabric"
set "MODSDIR_MINIONS=C:\Users\Chuck\AppData\Roaming\norisk\NoRiskClientV3\data\profiles\Minions\mods\nrc-26.1.2-fabric"

echo. > "%LOG%"
echo.
echo  +======================================+
echo  ^|      Chuck Pack -- Auto Builder      ^|
echo  +======================================+
echo.

if not defined PROJECT (
    echo [ERROR] Could not find chuck-pack project ^(build.gradle^) next to this
    echo         script or in a "chuck-pack" subfolder. Place this .bat either
    echo         inside the project folder or one level above it.
    pause & exit /b 1
)

echo [1/6] Checking for Java 25...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Download from https://adoptium.net/
    pause & exit /b 1
)
echo Java found.

if not exist "%GRADLE_EXE%" (
    echo.
    echo [2/6] Downloading Gradle %GRADLE_VER%...
    if exist "%GRADLE_ZIP%" del /f /q "%GRADLE_ZIP%"
    if exist "%GRADLE_DIR%" rmdir /s /q "%GRADLE_DIR%"

    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VER%-bin.zip' -OutFile '%GRADLE_ZIP%' -UseBasicParsing"

    if not exist "%GRADLE_ZIP%" (
        echo [ERROR] Download failed.
        pause & exit /b 1
    )

    echo Extracting...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%GRADLE_DIR%' -Force"

    del /f /q "%GRADLE_ZIP%" >nul 2>&1

    if not exist "%GRADLE_EXE%" (
        echo [ERROR] Extraction failed.
        pause & exit /b 1
    )
    echo Gradle ready.
) else (
    echo [2/6] Gradle already downloaded.
)

echo.
echo [3/6] Building... (log saved to Downloads\build-log.txt)
echo.
set "JAVA_HOME=C:\Users\Chuck\AppData\Local\Programs\Microsoft\jdk-25.0.3.9-hotspot"
cd /d "%PROJECT%"
call "%GRADLE_EXE%" build --no-daemon --stacktrace > "%LOG%" 2>&1
if errorlevel 1 (
    echo.
    echo [ERROR] Build failed. Last 60 lines:
    echo.
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Content '%LOG%' | Select-Object -Last 60"
    echo.
    pause & exit /b 1
)

echo.
echo [4/6] Copying JAR to Downloads...
set "LIBS=%PROJECT%\build\libs"
set "FOUND="

for %%F in ("%LIBS%\*.jar") do (
    set "FNAME=%%~nxF"
    echo !FNAME! | findstr /i "\-dev\." >nul
    if errorlevel 1 (
        echo !FNAME! | findstr /i "\-sources\." >nul
        if errorlevel 1 (
            set "FOUND=%%~fF"
            set "JARNAME=!FNAME!"
        )
    )
)
if not defined FOUND (
    echo [ERROR] JAR not found.
    pause & exit /b 1
)
copy /Y "!FOUND!" "%DOWNLOADS%!JARNAME!" >nul

echo.
echo [5/6] Killing Minecraft...
taskkill /F /IM javaw.exe >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1
ping -n 3 127.0.0.1 >nul 2>&1

echo.
echo [6/6] Copying JAR to mods folders...
if exist "!MODSDIR_BAREBONE!" (
    for %%F in ("!MODSDIR_BAREBONE!\chuck-pack-*.jar") do del /f /q "%%F" >nul 2>&1
    copy /Y "!FOUND!" "!MODSDIR_BAREBONE!\!JARNAME!" >nul
    if errorlevel 1 (
        echo [WARNING] Could not copy to Barebones mods folder.
    ) else (
        echo Copied to Barebones mods folder.
    )
) else (
    echo [WARNING] Barebones mods folder not found, skipping: !MODSDIR_BAREBONE!
)
if exist "!MODSDIR_MINIONS!" (
    for %%F in ("!MODSDIR_MINIONS!\chuck-pack-*.jar") do del /f /q "%%F" >nul 2>&1
    copy /Y "!FOUND!" "!MODSDIR_MINIONS!\!JARNAME!" >nul
    if errorlevel 1 (
        echo [WARNING] Could not copy to Minions mods folder.
    ) else (
        echo Copied to Minions mods folder.
    )
) else (
    echo [WARNING] Minions mods folder not found, skipping: !MODSDIR_MINIONS!
)

echo.
echo [7/7] Regenerating features file...
if exist "%DOWNLOADS%generate-features.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%DOWNLOADS%generate-features.ps1"
) else (
    echo [WARNING] generate-features.ps1 not found next to this script, skipping.
)

echo.
echo  Build complete! JAR: !JARNAME!
echo.
pause
