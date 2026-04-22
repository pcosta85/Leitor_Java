@echo off

if not exist bin mkdir bin

echo Compilando...
javac -d bin --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.media ^
-cp ".;lib\mysql-connector-j-9.7.0.jar" src\*.java

if %errorlevel% neq 0 (
    echo ERRO na compilacao
    pause
    exit /b
)

echo Executando...
java --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.media ^
-cp "bin;lib\mysql-connector-j-9.7.0.jar" LeitorMP3

pause