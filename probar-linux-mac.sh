#!/usr/bin/env bash
# Compila el programa y las pruebas, y ejecuta todas las pruebas.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf build
mkdir -p build
javac -encoding UTF-8 -d build $(find src test -name '*.java')
for prueba in PruebaModeloInicial PruebaExpensas PruebaGestorExpensas PruebaReclamos PruebaGestorReclamos PruebaMantenimiento PruebaAccesos PruebaPersistencia; do
    java -cp build "$prueba"
done
echo "Todas las pruebas terminaron correctamente."
