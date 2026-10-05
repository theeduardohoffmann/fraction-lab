# Gera testes com o Randoop e os coloca em tests-randoop/ (versão PowerShell de run-randoop.sh).
# Uso:
#   $env:RANDOOP_JAR = "C:\caminho\randoop-all-4.3.4.jar"
#   .\scripts\run-randoop.ps1 [-TimeLimit 60] [-Seed 42]
# Pré-requisitos: JDK 17+ e o jar do Randoop (o Maven vem pelo wrapper mvnw.cmd).
param(
    [int]$TimeLimit = 60,
    [int]$Seed = 42     # registrar a semente no tests.md para reprodutibilidade
)

$ErrorActionPreference = "Stop"

if (-not $env:RANDOOP_JAR) {
    throw "Defina `$env:RANDOOP_JAR com o caminho do randoop-all-*.jar"
}
$out = "tests-randoop"

if (Test-Path ".\mvnw.cmd") { & ".\mvnw.cmd" -q -DskipTests compile } else { mvn -q -DskipTests compile }
if ($LASTEXITCODE -ne 0) { throw "Falha ao compilar o projeto" }

if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory $out | Out-Null

$classpath = "$env:RANDOOP_JAR;$PWD\target\classes"

java -classpath $classpath randoop.main.Main gentests `
    "--testclass=br.pucrs.vv.fraction.Fraction" `
    "--time-limit=$TimeLimit" `
    "--randomseed=$Seed" `
    "--npe-on-non-null-input=ERROR" `
    "--specifications=randoop/fraction-specs.json" `
    "--literals-file=randoop/literals.txt" `
    "--literals-level=CLASS" `
    "--junit-output-dir=$out" `
    "--junit-package-name=br.pucrs.vv.fraction.randoop"
if ($LASTEXITCODE -ne 0) { throw "O Randoop terminou com erro" }

Write-Host "Testes gerados em $out/. Rode com: .\mvnw.cmd -P randoop verify"

# O Randoop deixa classes auxiliares das condições do JSON na pasta atual
if (Test-Path "br") { Remove-Item "br" -Recurse -Force }
