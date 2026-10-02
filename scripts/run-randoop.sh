#!/usr/bin/env bash
# Gera testes com o Randoop e os coloca em tests-randoop/ (RD-Cn em tests.md).
# Uso:  RANDOOP_JAR=/caminho/randoop-all-X.Y.Z.jar scripts/run-randoop.sh [segundos] [semente]
# Pré-requisitos: JDK 17+ e o jar do Randoop (o Maven vem pelo wrapper mvnw).
set -euo pipefail

: "${RANDOOP_JAR:?defina RANDOOP_JAR com o caminho do randoop-all-*.jar}"
TIME_LIMIT="${1:-60}"
SEED="${2:-42}"          # registrar a semente no tests.md para reprodutibilidade
OUT=tests-randoop

if [ -f ./mvnw ]; then bash ./mvnw -q -DskipTests compile; else mvn -q -DskipTests compile; fi
rm -rf "$OUT" && mkdir -p "$OUT"

# Separador do classpath: ':' (Linux/macOS/Git Bash usa ';' apenas no Windows nativo)
SEP=':'; JAR="$RANDOOP_JAR"; CLASSES=target/classes
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=';'; JAR="$(cygpath -m "$RANDOOP_JAR")"; CLASSES="$(cygpath -m "$PWD/target/classes")";;
esac

java -classpath "${JAR}${SEP}${CLASSES}" randoop.main.Main gentests \
  --testclass=br.pucrs.vv.fraction.Fraction \
  --time-limit="$TIME_LIMIT" \
  --randomseed="$SEED" \
  --specifications=randoop/fraction-specs.json \
  --literals-file=randoop/literals.txt \
  --literals-level=CLASS \
  --junit-output-dir="$OUT" \
  --junit-package-name=br.pucrs.vv.fraction.randoop

echo "Testes gerados em $OUT/. Rode com: ./mvnw -P randoop verify"

# O Randoop deixa classes auxiliares das condições do JSON na pasta atual
rm -rf br
