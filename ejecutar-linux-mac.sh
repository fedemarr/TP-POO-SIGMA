#!/usr/bin/env bash
# Compila y abre SIGMA. Sin argumentos abre la ventana; con "consola" abre el menu por consola.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf build
mkdir -p build
javac -encoding UTF-8 -d build $(find src -name '*.java')
java -cp build sigma.Main "$@"
