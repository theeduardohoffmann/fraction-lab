# fraction-lab

[![CI](https://github.com/theeduardohoffmann/fraction-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/theeduardohoffmann/fraction-lab/actions/workflows/ci.yml)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
![JUnit 5](https://img.shields.io/badge/JUnit-5-green)
![jqwik](https://img.shields.io/badge/jqwik-1.9.3-orange)

Trabalho T1 de **Verificação e Validação** (PUCRS, Escola Politécnica, 2026/I).

O kata é `Fraction`: uma classe Java para números racionais (frações), imutável e sempre normalizada. Ela é testada com duas técnicas e os resultados são comparados na resenha crítica:

1. **Geração aleatória guiada por feedback com contratos como oráculo**, usando o [Randoop](https://randoop.github.io/randoop/) (base: Pacheco et al., 2007).
2. **Teste baseado em propriedades**, usando o [jqwik](https://jqwik.net/) (base conceitual: Fink e Bishop, 1997).

Também há um conjunto pequeno de **testes de exemplo** (partição de equivalência e valor limite) como baseline de comparação.

## O que é a classe `Fraction`

Representa `numerador/denominador` com `long`. Toda instância respeita:

- denominador > 0;
- fração irredutível (mdc = 1);
- zero representado como `0/1`.

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
│   └── literals.txt          valores `long` fornecidos ao gerador do Randoop
├── scripts/
│   └── run-randoop.sh         gera os testes com o Randoop
├── resenha/                   resenha crítica (resenha.pdf, com o HTML-fonte)
├── apresentacao/roteiro.md    roteiro da apresentação (6 min 30 s)
├── tests.md                   casos de teste documentados e matriz de rastreabilidade
├── .github/workflows/ci.yml   CI: build, testes e cobertura (GitHub Actions)
├── pom.xml                    build Maven (JUnit 5, jqwik, JaCoCo, perfil do Randoop)
└── README.md
```

`tests-randoop/` (testes gerados pelo Randoop) e `target/` (build) são criados na execução e não ficam no git.

## Pré-requisitos

- JDK 17 ou superior.
- Maven 3.9 ou superior.
- Para o Randoop: o arquivo `randoop-all-<versão>.jar`, baixado em <https://github.com/randoop/randoop/releases> (o projeto foi executado com a versão 4.3.4).
- Para rodar o script do Randoop no Windows: Git Bash (ou WSL).

## Como rodar os testes (JUnit 5 + jqwik)

```bash
mvn test
```

Roda os testes de exemplo (`EX-*`) e as propriedades do jqwik (`PB-*`). O jqwik imprime, para cada propriedade, o número de tentativas e a semente aleatória usada, o que permite reproduzir uma execução.

## Como rodar o Randoop

1. Gere os testes (tempo em segundos e semente são opcionais; padrão 60 e 42):

   ```bash
   RANDOOP_JAR=/caminho/para/randoop-all-4.3.4.jar scripts/run-randoop.sh 60 42
   ```

   O script compila o projeto, executa o Randoop sobre `br.pucrs.vv.fraction.Fraction` com as especificações de `randoop/fraction-specs.json` e os literais de `randoop/literals.txt`, e grava os testes em `tests-randoop/`.

2. Compile e rode os testes gerados, junto com os demais, ativando o perfil `randoop`:

   ```bash
   mvn -P randoop verify
   ```

**Contratos.** O Randoop já verifica seus contratos padrão (`equals` reflexivo, simétrico e transitivo; `hashCode` consistente; `hashCode` e `toString` sem exceção; sem NPE quando nenhum argumento era null). Os contratos adicionais do domínio são registrados em `randoop/fraction-specs.json` como pós-condições dos métodos:

| Contrato | Método | Condição |
|----------|--------|----------|
| C7 | `getDenominator()` | `result > 0` |
| C8 | `getNumerator()` | mdc(\|numerador\|, denominador) = 1 |
| C9 | `compareTo(other)` | `compareTo == 0` se e somente se `equals` |
| C10 | `toString()` | não vazio e `parse(toString())` igual ao original |

Para acrescentar um contrato, inclua uma nova entrada `post` no JSON e rode o script de novo. Registre em `tests.md` a versão do Randoop, o tempo, a semente e o número de testes gerados.

## Como ver a cobertura (JaCoCo)

```bash
mvn verify
```

O relatório HTML fica em `target/site/jacoco/index.html`. Com `mvn -P randoop verify`, a cobertura inclui também os testes gerados pelo Randoop.

## Casos de teste e rastreabilidade

Todos os casos estão documentados em [`tests.md`](tests.md), com ID, técnica, contrato/propriedade/requisito, entrada ou gerador, saída esperada e resultado obtido. O ID aparece no `@DisplayName`/`@Label` de cada teste (por exemplo, `PB-P8`, `EX-12`).

| Prefixo | Técnica | Onde está |
|---------|---------|-----------|
| `RD-Cn` | Randoop, contrato Cn como oráculo | `tests-randoop/` (gerado) e `randoop/fraction-specs.json` |
| `PB-Pn` | jqwik, propriedade Pn | `tests/.../FractionPropertiesTest.java` |
| `EX-nn` | Baseline: exemplos | `tests/.../FractionExampleTest.java` |

## Equipe

- Eduardo Hoffmann
- Lucas Mocelin
- Fernando Kunst

## Referências

PACHECO, C.; LAHIRI, S. K.; ERNST, M. D.; BALL, T. Feedback-directed random test generation. *In*: INTERNATIONAL CONFERENCE ON SOFTWARE ENGINEERING, 29., 2007, Minneapolis. **Proceedings** [...]. 2007. p. 75-84. Disponível em: https://homes.cs.washington.edu/~mernst/pubs/feedback-testgen-icse2007.pdf.

FINK, G.; BISHOP, M. Property-based testing: a new approach to testing for assurance. **ACM SIGSOFT Software Engineering Notes**, New York, v. 22, n. 4, p. 74-80, jul. 1997. DOI: 10.1145/263244.263267. Disponível em: https://nob.cs.ucdavis.edu/bishop/papers/1997-sen.
