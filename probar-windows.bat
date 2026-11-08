@echo off
rem Compila el programa y las pruebas, y ejecuta todas las pruebas.
cd /d "%~dp0"
if exist build rmdir /s /q build
mkdir build
(dir /s /b src\*.java & dir /s /b test\*.java) > fuentes.txt
javac -encoding UTF-8 -d build @fuentes.txt
if errorlevel 1 (del fuentes.txt & pause & exit /b 1)
del fuentes.txt
for %%T in (PruebaModeloInicial PruebaExpensas PruebaGestorExpensas PruebaReclamos PruebaGestorReclamos PruebaMantenimiento PruebaAccesos PruebaPersistencia) do (
    java -cp build %%T
    if errorlevel 1 (pause & exit /b 1)
)
echo Todas las pruebas terminaron correctamente.
pause
