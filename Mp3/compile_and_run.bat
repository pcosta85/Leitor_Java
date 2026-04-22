@echo off
setlocal enabledelayedexpansion

set JAVAFX_PATH=C:\javafx-sdk\lib

echo.
echo ========================================
echo Compilando LeitorMP3...
echo ========================================
echo.

javac -cp "%JAVAFX_PATH%\*;." --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml,javafx.media -d ../bin LeitorMP3.java

if errorlevel 1 (
    echo.
    echo ERRO: Compilacao falhou!
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilacao bem-sucedida!
echo Iniciando LeitorMP3...
echo ========================================
echo.

java -cp "%JAVAFX_PATH%\*;../bin" --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml,javafx.media LeitorMP3

pause