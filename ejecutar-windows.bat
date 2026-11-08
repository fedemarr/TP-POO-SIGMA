@echo off
rem Compila y abre SIGMA. Sin argumentos abre la ventana; con "consola" abre el menu por consola.
cd /d "%~dp0"
if exist build rmdir /s /q build
mkdir build
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d build @fuentes.txt
if errorlevel 1 (del fuentes.txt & pause & exit /b 1)
del fuentes.txt
java -cp build sigma.Main %*
if errorlevel 1 pause
