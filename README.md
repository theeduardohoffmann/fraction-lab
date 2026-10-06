# fraction-lab

[![CI](https://github.com/theeduardohoffmann/fraction-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/theeduardohoffmann/fraction-lab/actions/workflows/ci.yml)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
![JUnit 5](https://img.shields.io/badge/JUnit-5-green)
![jqwik](https://img.shields.io/badge/jqwik-1.9.3-orange)

Trabalho T1 de **Verificação e Validação** (PUCRS, Escola Politécnica, 2026/I).

O kata é `Fraction`: uma classe Java para números racionais (frações), imutável e sempre normalizada. Ela é testada com duas técnicas, e os resultados são comparados na resenha crítica:

1. **Geração aleatória guiada por feedback com contratos como oráculo**, usando o [Randoop](https://randoop.github.io/randoop/) (base: Pacheco et al., 2007).
2. **Teste baseado em propriedades**, usando o [jqwik](https://jqwik.net/) (base conceitual: Fink e Bishop, 1997).

Também há um conjunto pequeno de **testes de exemplo** (partição de equivalência e valor limite) como baseline de comparação.

## Entregáveis

| Item | Onde |
|------|------|
| Código do kata | [`src/`](src/br/pucrs/vv/fraction/Fraction.java) |
| Código dos testes | [`tests/`](tests/br/pucrs/vv/fraction) |
| Casos de teste documentados e resultados | [`tests.md`](tests.md) |
| Resenha crítica (PDF) | [`resenha/resenha.pdf`](resenha/resenha.pdf) |
| Apresentação (slides) | [`apresentacao/apresentacao.pdf`](apresentacao/apresentacao.pdf) |

## A classe `Fraction`

Representa `numerador/denominador` com `long`. Toda instância respeita:

- denominador > 0;
- fração irredutível (mdc = 1);
- zero representado como `0/1`;
- numerador nunca é `Long.MIN_VALUE` (assim `negate()` e `abs()` não estouram).

Operações: `of`, `parse`, `plus`, `minus`, `times`, `dividedBy`, `negate`, `reciprocal`, `abs`, `signum`, `doubleValue`, `compareTo`, `equals`, `hashCode`, `toString`. Overflow é sinalizado com `ArithmeticException`, nunca com resultado errado silencioso. Os contratos de cada método estão no Javadoc de [`Fraction.java`](src/br/pucrs/vv/fraction/Fraction.java).

Decisão de projeto: argumento `null` em `plus/minus/times/dividedBy/compareTo` lança `NullPointerException` (compatível com o contrato do Randoop), enquanto `parse(null)` lança `IllegalArgumentException`.

## Estrutura do repositório

```
fraction-lab/
├── src/                       código do kata
│   └── br/pucrs/vv/fraction/Fraction.java
├── tests/                     código dos testes (JUnit 5 + jqwik)
│   └── br/pucrs/vv/fraction/
│       ├── FractionExampleTest.java      baseline: exemplos (EX-01…EX-15)
│       └── FractionPropertiesTest.java   propriedades jqwik (PB-P1…PB-P14)
├── randoop/
│   ├── fraction-specs.json    contratos adicionais registrados no Randoop (C7–C10)
│   └── literals.txt           valores `long` fornecidos ao gerador do Randoop
├── scripts/
│   ├── run-randoop.ps1        gera os testes com o Randoop (PowerShell)
│   └── run-randoop.sh         o mesmo, para Linux, macOS e Git Bash
├── resenha/                   resenha crítica (resenha.pdf e o HTML-fonte)
├── apresentacao/              slides da apresentação (apresentacao.pdf)
├── tests.md                   casos de teste, rastreabilidade e resultados
├── .github/workflows/ci.yml   CI: build, testes e cobertura (GitHub Actions)
├── mvnw, mvnw.cmd, .mvn/      Maven Wrapper (baixa o Maven sozinho; não precisa instalar)
├── pom.xml                    build Maven (JUnit 5, jqwik, JaCoCo, perfil do Randoop)
└── README.md
```

`tests-randoop/` (testes gerados pelo Randoop) e `target/` (build) são criados na execução e não ficam no git.

## Pré-requisitos

- JDK 17 ou superior (`java -version` para conferir).
- Maven: **não precisa instalar**. O repositório inclui o Maven Wrapper (`mvnw.cmd` no Windows, `mvnw` no Linux e macOS), que baixa o Maven 3.9.9 sozinho na primeira execução (precisa de internet).
- Para o Randoop: o arquivo `randoop-all-<versão>.jar`, baixado em <https://github.com/randoop/randoop/releases> (o projeto foi executado com a versão 4.3.4).

Os comandos abaixo são para o terminal do VS Code com **PowerShell** (Windows). No Linux, macOS ou Git Bash, troque `.\mvnw.cmd` por `./mvnw` e use `scripts/run-randoop.sh`.

## Como rodar os testes (JUnit 5 + jqwik)

```powershell
.\mvnw.cmd test
```

Roda os testes de exemplo (`EX-*`) e as propriedades do jqwik (`PB-*`). O jqwik imprime, para cada propriedade, o número de tentativas e a semente aleatória usada, o que permite reproduzir uma execução. Resultado esperado: `Tests run: 37, Failures: 0, Errors: 0` (15 exemplos e 22 métodos de propriedades) e `BUILD SUCCESS`.

## Como rodar o Randoop

1. Gere os testes. Tempo (segundos) e semente são opcionais; o padrão é 60 e 42:

   ```powershell
   $env:RANDOOP_JAR = "C:\caminho\para\randoop-all-4.3.4.jar"
   .\scripts\run-randoop.ps1 -TimeLimit 60 -Seed 42
   ```

   Se o PowerShell bloquear o script (política de execução), rode antes `Set-ExecutionPolicy -Scope Process Bypass`.

   No Linux, macOS ou Git Bash:

   ```bash
   RANDOOP_JAR=/caminho/para/randoop-all-4.3.4.jar scripts/run-randoop.sh 60 42
   ```

   O script compila o projeto e executa o Randoop sobre `br.pucrs.vv.fraction.Fraction` com `--npe-on-non-null-input=ERROR` (NPE com argumentos não nulos é falha), as pós-condições de `randoop/fraction-specs.json` e os literais de `randoop/literals.txt`. Os testes gerados ficam em `tests-randoop/`.

2. Compile e rode os testes gerados, junto com os demais, ativando o perfil `randoop`:

   ```powershell
   .\mvnw.cmd -P randoop verify
   ```

   O Randoop limita a geração por **tempo**; por isso a quantidade de testes gerados varia entre execuções, mesmo com a mesma semente.

### Contratos

O artigo (Figura 4) lista como contratos padrão: `equals` reflexivo, `equals`/`hashCode`/`toString` sem exceção, sem NPE quando nenhum argumento era null e sem `AssertionError`. A versão 4.3.4 da ferramenta verifica ainda simetria e transitividade de `equals`, `hashCode` consistente com `equals` e contratos de `compareTo`. Os contratos adicionais do domínio são registrados em `randoop/fraction-specs.json` como pós-condições dos métodos:

| Contrato | Método | Condição |
|----------|--------|----------|
| C7 | `getDenominator()` | `result > 0` |
| C8 | `getNumerator()` | mdc(\|numerador\|, denominador) = 1 |
| C9 | `compareTo(other)` | `compareTo == 0` se e somente se `equals` |
| C10 | `toString()` | não vazio e `parse(toString())` igual ao original |

Para acrescentar um contrato, inclua uma nova entrada `post` no JSON e rode o script de novo. Registre em `tests.md` a versão do Randoop, o tempo, a semente e o número de testes gerados.

## Como ver a cobertura (JaCoCo)

```powershell
.\mvnw.cmd verify
```

O relatório HTML fica em `target/site/jacoco/index.html`. Com `.\mvnw.cmd -P randoop verify`, a cobertura inclui também os testes gerados pelo Randoop.

## Casos de teste e rastreabilidade

Todos os casos estão documentados em [`tests.md`](tests.md), com ID, técnica, contrato/propriedade/requisito, entrada ou gerador, saída esperada e resultado obtido. O ID aparece no `@DisplayName`/`@Label` de cada teste (por exemplo, `PB-P8`, `EX-12`).

| Prefixo | Técnica | Onde está |
|---------|---------|-----------|
| `RD-Cn` | Randoop, contrato Cn como oráculo | `tests-randoop/` (gerado) e `randoop/fraction-specs.json` |
| `PB-Pn` | jqwik, propriedade Pn | `tests/.../FractionPropertiesTest.java` |
| `EX-nn` | Baseline: exemplos | `tests/.../FractionExampleTest.java` |

## Resultados em resumo

Detalhes, parâmetros e ressalvas estão em [`tests.md`](tests.md), seção 5.

- **Código correto:** nos experimentos de 02/10/2026, os 15 testes de exemplo e os 18 métodos de propriedades (P1–P14) passaram. O Randoop (60 s, semente 42) gerou 1.467 testes de regressão e nenhum teste revelador de erro; no total, 1.500 testes, sem falhas. Depois disso, as propriedades P15–P18 foram acrescentadas: a suíte JUnit/jqwik atual tem 37 testes (15 exemplos e 22 métodos de propriedades).
- **Mutações:** cinco alterações manuais em `Fraction.java`, cada uma aplicada a uma cópia isolada. "Detectou" significa que ao menos um teste da técnica revelou a violação.

| Alteração | Exemplos | jqwik | Randoop (semente 42) |
|-----------|----------|-------|----------------------|
| M1: `of` não move o sinal do denominador | detectou | detectou | detectou |
| M2: `of` não reduz por mdc | detectou | detectou | detectou |
| M3: `compareTo` por multiplicação em `long` | não | detectou (só P10d) | detectou |
| M4: overflow ignorado em `plus` e `times` | detectou | detectou (P13) | **não detectou** |
| M5: `hashCode` só do numerador | não | não | não (preserva o contrato) |

As alterações M1 a M4 foram reaplicadas em 06/10/2026 à suíte atual (com P15–P18) e continuam sendo detectadas pelos testes de exemplo e pelo jqwik (ver `tests.md`, seção 5.3).

O Randoop não detectou M4 porque nenhum dos contratos usados verifica o resultado aritmético. Esse resultado vale para esses contratos, sementes e tempo, e não prova que a ferramenta nunca o detectaria.

## Contribuições da equipe

Resumo baseado no histórico de commits do repositório (`git log`).

| Integrante | Contribuições |
|------------|---------------|
| Eduardo Hoffmann | Classe `Fraction`; testes de exemplo e propriedades PB-P1 a PB-P14; Maven, JaCoCo, Maven Wrapper e CI; scripts do Randoop; versão inicial da resenha, dos slides e do `tests.md` |
| Fernando Kunst | Configuração do Randoop para NPE com argumentos não nulos; experimentos de mutação em cópias isoladas (macOS, JDK 21); atualização do `tests.md` e da resenha com os resultados revisados |
| Lucas Mocelin | Propriedades PB-P15 a PB-P18 (`negate`, `abs`, `signum`, `reciprocal` e `doubleValue`, requisito R11); atualização do `tests.md`; execução da suíte em outra máquina (Windows 11, JDK 21) |

## Equipe

- Eduardo Hoffmann
- Lucas Mocelin
- Fernando Kunst

## Referências

FINK, G.; BISHOP, M. Property-based testing: a new approach to testing for assurance. **ACM SIGSOFT Software Engineering Notes**, New York, v. 22, n. 4, p. 74-80, jul. 1997. DOI: 10.1145/263244.263267. Disponível em: https://nob.cs.ucdavis.edu/bishop/papers/1997-sen. Acesso em: 2 out. 2026.

PACHECO, C.; LAHIRI, S. K.; ERNST, M. D.; BALL, T. Feedback-directed random test generation. *In*: INTERNATIONAL CONFERENCE ON SOFTWARE ENGINEERING, 29., 2007, Minneapolis. **Proceedings** [...]. 2007. p. 75-84. Disponível em: https://homes.cs.washington.edu/~mernst/pubs/feedback-testgen-icse2007.pdf. Acesso em: 2 out. 2026.
