#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/br/com/alocacao/*.java
java -cp out br.com.alocacao.Main
