#!/bin/bash

# Diretório de saída
OUT_DIR="bin"
SRC_DIR="src"

# Criar o diretório de saída, se não existir
mkdir -p $OUT_DIR

# Compilar todos os arquivos .java em subdiretórios de src/
javac -d $OUT_DIR $(find $SRC_DIR -name "*.java")

echo "Compilação concluída. Arquivos .class gerados no diretório $OUT_DIR"
