@echo off
REM Script para ejecutar Automatas usando Maven
REM Este script ejecuta la aplicación directamente con todas las dependencias

cd /d "%~dp0"

echo Iniciando Automatas...
echo.

REM Ejecutar usando el plugin de JavaFX de Maven
mvn javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: No se pudo ejecutar la aplicación.
    echo Verifica que tengas Maven y Java 21 instalados.
    pause
    exit /b 1
)
