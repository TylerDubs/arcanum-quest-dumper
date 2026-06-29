@echo off
setlocal
cd /d "%~dp0"

set GRADLE_VERSION=8.8
set GRADLE_DIR=%CD%\.gradle-local\gradle-%GRADLE_VERSION%
set GRADLE_BAT=%GRADLE_DIR%\bin\gradle.bat
set GRADLE_ZIP=%CD%\.gradle-local\gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%CD%\.gradle-local" mkdir "%CD%\.gradle-local"

if not exist "%GRADLE_BAT%" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
    if errorlevel 1 goto fail

    echo Extracting Gradle...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%CD%\.gradle-local' -Force"
    if errorlevel 1 goto fail
)

echo Building mod...
"%GRADLE_BAT%" --no-daemon build
if errorlevel 1 goto fail

echo.
echo Build complete.
echo Jar location:
echo %CD%\build\libs\arcanum-quest-dumper-1.0.0.jar
goto end

:fail
echo.
echo Build failed. Copy the red error text and send it back.
exit /b 1

:end
endlocal
