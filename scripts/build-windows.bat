@echo off
setlocal EnableDelayedExpansion

REM InventoryFlow Windows Build Script
REM Creates a signed .exe installer

set APP_NAME=InventoryFlow
set APP_VERSION=1.0.0
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..

echo ========================================
echo   Building %APP_NAME% for Windows
echo ========================================

cd /d "%PROJECT_DIR%"

REM Check for Java
echo.
echo Checking prerequisites...

java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Install JDK 17+
    exit /b 1
)
echo   Java OK

REM Check for Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven not found. Install Maven and add to PATH
    exit /b 1
)
echo   Maven OK

REM Check for jpackage
jpackage --version >nul 2>&1
if errorlevel 1 (
    echo Error: jpackage not found. Ensure JDK 17+ is installed
    exit /b 1
)
echo   jpackage OK

REM Check for icon
set ICON_PATH=%PROJECT_DIR%\packaging\windows\InventoryFlow.ico
set ICON_OPTION=
if exist "%ICON_PATH%" (
    echo   Icon OK
    set ICON_OPTION=--icon "%ICON_PATH%"
) else (
    echo   Warning: No icon found, building without custom icon
)

REM Clean and build
echo.
echo Building application...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Error: Maven build failed
    exit /b 1
)

REM Create installer directory
set INSTALLER_DIR=%PROJECT_DIR%\target\installer
if not exist "%INSTALLER_DIR%" mkdir "%INSTALLER_DIR%"

REM Build module path
set MODULE_PATH=%PROJECT_DIR%\target\lib;%PROJECT_DIR%\target\inventoryflow-%APP_VERSION%.jar

REM Run jpackage for EXE installer
echo.
echo Creating .exe installer...

jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "Your Company Name" ^
    --copyright "Copyright 2024 Your Company Name" ^
    --description "Shopify Inventory Management" ^
    --module-path "%MODULE_PATH%" ^
    --module com.inventoryflow/com.inventoryflow.App ^
    --dest "%INSTALLER_DIR%" ^
    --win-menu ^
    --win-menu-group "InventoryFlow" ^
    --win-shortcut ^
    --win-dir-chooser ^
    --java-options "-Dfile.encoding=UTF-8" ^
    %ICON_OPTION%

if errorlevel 1 (
    echo Error: jpackage failed
    exit /b 1
)

REM Code signing (if signtool is available and certificate is configured)
REM Set WINDOWS_CERT_PATH and WINDOWS_CERT_PASSWORD environment variables
set EXE_FILE=%INSTALLER_DIR%\%APP_NAME%-%APP_VERSION%.exe

if exist "%EXE_FILE%" (
    if defined WINDOWS_CERT_PATH (
        if exist "%WINDOWS_CERT_PATH%" (
            echo.
            echo Signing installer...
            signtool sign /f "%WINDOWS_CERT_PATH%" /p "%WINDOWS_CERT_PASSWORD%" /tr http://timestamp.digicert.com /td sha256 /fd sha256 "%EXE_FILE%"
            if errorlevel 1 (
                echo Warning: Signing failed, installer is unsigned
            ) else (
                echo Signing complete!
            )
        )
    ) else (
        echo.
        echo Skipping code signing (WINDOWS_CERT_PATH not set)
        echo For distribution, set up code signing - see docs\CODE_SIGNING.md
    )
)

REM Summary
echo.
echo ========================================
echo   Build Complete!
echo ========================================
echo.

if exist "%EXE_FILE%" (
    echo Installer: %EXE_FILE%
    for %%A in ("%EXE_FILE%") do echo Size: %%~zA bytes
) else (
    echo Error: EXE file not created
    exit /b 1
)

endlocal
