@echo off
setlocal

REM JavaFX SDK path
set JAVAFX_PATH=C:\javafx-sdk
set JAVAFX_LIB=%JAVAFX_PATH%\lib

REM MySQL JDBC Driver
set MYSQL_JAR=mysql-connector-j-9.7.0.jar

REM Compile
echo Compiling LeitorMP3 and DatabaseUtil...
javac --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.media -cp "%MYSQL_JAR%" LeitorMP3.java DatabaseUtil.java

REM Check if compilation was successful
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation successful!
    echo Running application...
    echo ========================================
    echo.
    java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.media -cp ".;%MYSQL_JAR%" LeitorMP3
) else (
    echo.
    echo ========================================
    echo ERROR: Compilation FAILED!
    echo ========================================
    echo Check the errors above.
    pause
)

endlocal
pause