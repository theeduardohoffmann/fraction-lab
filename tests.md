# Casos de teste – Fraction (T1 de Verificação e Validação, PUCRS 2026/I)

Este documento descreve os casos de teste do kata `Fraction` (números racionais) e liga cada um a uma técnica, a um contrato ou propriedade e a um requisito. A coluna **Resultado obtido** vem de execuções reais, descritas na seção 5; nenhum número deste arquivo é simulado.

- **Requisitos (R1…R15):** listados na seção 0 deste documento.
- **Técnica 1 – Randoop** (geração aleatória guiada por feedback, contratos como oráculo; Pacheco et al., 2007): IDs `RD-Cn`.
- **Técnica 2 – jqwik** (propriedades; Fink e Bishop, 1997, como inspiração conceitual): IDs `PB-Pn`.
- **Baseline** (partição de equivalência + valor limite): IDs `EX-nn`.

Arquivos: `tests/br/pucrs/vv/fraction/` (`FractionExampleTest`, `FractionPropertiesTest`), `randoop/fraction-specs.json` e `scripts/run-randoop.sh`.


---

## 0. Requisitos e contratos

| ID | Requisito de `Fraction` |
|----|-------------------------|
| R1 | Toda instância é normalizada: denominador > 0, mdc(\|n\|, d) = 1, zero é 0/1. |
| R2 | `of(n, 0)` lança `IllegalArgumentException`. |
| R3 | `of` lança `ArithmeticException` se a normalização causar overflow (ex.: `Long.MIN_VALUE`). |
| R4 | `parse` aceita `"a/b"` e `"a"`, com sinal opcional no numerador. |
| R5 | `parse` lança `IllegalArgumentException` para null, vazio, formato inválido ou denominador zero. |
| R6 | `getNumerator()` e `getDenominator()` devolvem os valores normalizados. |
| R7 | `plus`, `minus`, `times` calculam o resultado racional exato e normalizado. |
| R8 | `dividedBy` é exato; divisor zero lança `ArithmeticException`. |
| R9 | Argumento `null` em `plus/minus/times/dividedBy` lança `NullPointerException`. |
| R10 | Overflow aritmético lança `ArithmeticException`, nunca resultado errado silencioso. |
| R11 | `negate`, `abs`, `signum`, `doubleValue` corretos; `reciprocal` de zero lança `ArithmeticException`. |
| R12 | `compareTo` é uma ordem total coerente com o valor racional. |
| R13 | `equals` e `hashCode` por valor (1/2 e 2/4 são iguais, com o mesmo hash). |
| R14 | `toString()` devolve `"n/d"`, ou só `"n"` quando d = 1. |
| R15 | Objeto de valor imutável. |

Contratos do Randoop. Da **Figura 4 do artigo** (Pacheco et al., 2007): C1 `o.equals(o)` é verdadeiro; C5 `equals`, `hashCode` e `toString` não lançam exceção; C6 nenhum NPE quando nenhum argumento era null (o artigo lista também "sem `AssertionError`"). **C2, C3 e C4** (`equals` simétrico, `equals` transitivo, `equals` ⇒ mesmo `hashCode`) **não constam da Figura 4**: são contratos que a versão 4.3.4 da ferramenta verifica (classes `EqualsSymmetric`, `EqualsTransitive` e `EqualsHashcode` no jar). A versão 4.3.4 também aplica contratos próprios para `compareTo` (`compareTo-transitive`, `compareTo-equals` etc.). **C7–C10** são adicionais, registrados por nós: C7 denominador > 0; C8 mdc = 1 e zero é 0/1; C9 `compareTo == 0` ⇔ `equals`; C10 `toString` não vazio e `parse(toString())` igual ao original.

Nos scripts usados nos experimentos abaixo, C6 é configurado explicitamente com `--npe-on-non-null-input=ERROR`. A menção a `AssertionError` descreve o artigo de 2007; não significa que toda exceção não verificada seja classificada como falha pela configuração atual da ferramenta.

Propriedades do jqwik: P1…P14, descritas na seção 2.

---

## 1. Técnica 1 – Randoop (contratos como oráculo)

| ID | Técnica | Contrato / Requisito | Entrada (gerador) | Saída esperada | Resultado obtido | Observações |
|----|---------|----------------------|-------------------|----------------|------------------|-------------|
| RD-C1 | Randoop | C1 / R13 | Sequências aleatórias de `of`, `parse` e operações | `o.equals(o)` é true | Sem violação no código correto (seção 5.2) | Contrato padrão |
| RD-C2 | Randoop | C2 / R13 | idem (pares de objetos) | `equals` simétrico | Sem violação no código correto (seção 5.2) | Contrato padrão |
| RD-C3 | Randoop | C3 / R13 | idem (trios de objetos) | `equals` transitivo | Sem violação no código correto (seção 5.2) | Contrato padrão |
| RD-C4 | Randoop | C4 / R13 | idem | `equals` ⇒ mesmo `hashCode` | Sem violação no código correto (seção 5.2) | Contrato padrão |
| RD-C5 | Randoop | C5 / R13, R14 | idem | `hashCode()` e `toString()` não lançam exceção | Sem violação no código correto (seção 5.2) | Contrato padrão |
| RD-C6 | Randoop | C6 / R9 | idem, com e sem `null` | Nenhum NPE quando nenhum argumento era null | Sem violação no código correto (seção 5.2) | Valida a decisão de projeto (NPE em argumento null) |
| RD-C7 | Randoop | C7 / R1 | idem | `getDenominator() > 0` | Sem violação no código correto (seção 5.2) | Contrato adicional (`randoop/fraction-specs.json`) |
| RD-C8 | Randoop | C8 / R1 | idem | mdc(\|n\|, d) = 1; zero é 0/1 | Sem violação no código correto (seção 5.2) | Contrato adicional |
| RD-C9 | Randoop | C9 / R12, R13 | idem | `compareTo == 0` ⇔ `equals` | Sem violação no código correto (seção 5.2) | Contrato adicional |
| RD-C10 | Randoop | C10 / R4, R14 | idem | `toString()` não vazio e `parse(toString())` igual ao original | Sem violação no código correto (seção 5.2) | Contrato adicional |

**Como o resultado deve ser lido:** o Randoop só reporta falha quando um contrato é violado. Sequências que lançam exceção (`ArithmeticException`, `IllegalArgumentException`) são tratadas conforme as opções de exceção da versão usada **[VERIFICAR no manual do Randoop e registrar o comportamento observado]**. Esse comportamento é o que a resenha discute (filtro de exceções do artigo 1, seção 2.4).

---

## 2. Técnica 2 – jqwik (propriedades)

Geradores "pequenos": n ∈ [−1000, 1000], d ∈ [1, 1000] (sem overflow). Geradores "largos": mistura de pequenos e `long` completo, com referência em `BigInteger`.

| ID | Técnica | Propriedade / Requisito | Gerador | Saída esperada | Resultado obtido | Observações |
|----|---------|-------------------------|---------|----------------|------------------|-------------|
| PB-P1 | jqwik | P1 / R1, R3 | n qualquer `long`, d ≠ 0 qualquer | d > 0, mdc = 1, valor preservado; `ArithmeticException` só se houver `Long.MIN_VALUE` | Passou (1000 tentativas) | Faixa completa |
| PB-P2 | jqwik | P2 / R13 | a, b ≠ 0, k ≠ 0 pequenos | `of(a,b).equals(of(ka,kb))`, hashCodes iguais | Passou (1000 tentativas) | |
| PB-P3 | jqwik | P3 / R4, R14 | frações largas | `parse(toString(f))` = f | Passou (1000 tentativas) | |
| PB-P4 | jqwik | P4 / R7 | pequenas | `a+b = b+a`, `a×b = b×a` | Passou (1000 tentativas) | |
| PB-P5 | jqwik | P5 / R7 | pequenas | associatividade de + e × | Passou (1000 tentativas) | |
| PB-P6 | jqwik | P6 / R7 | pequenas | `f+0 = f`, `f×1 = f` | Passou (1000 tentativas) | |
| PB-P7 | jqwik | P7 / R7, R11 | pequenas | `f + (−f) = 0` | Passou (1000 tentativas) | |
| PB-P8 | jqwik | P8 / R11 | pequenas, f ≠ 0 | `f × f⁻¹ = 1` | Passou (1000 tentativas) | |
| PB-P9 | jqwik | P9 / R7 | pequenas | `a(b+c) = ab + ac` | Passou (1000 tentativas) | |
| PB-P10a | jqwik | P10 / R12 | pequenas | `sgn(a.cmp(b)) = −sgn(b.cmp(a))` | Passou (1000 tentativas) | |
| PB-P10b | jqwik | P10 / R12 | pequenas | transitividade de `compareTo` | Passou (1000 tentativas) | |
| PB-P10c | jqwik | P10 / R12 | pequenas | `sgn(a.cmp(b)) = (a−b).signum()` | Passou (1000 tentativas) | |
| PB-P10d | jqwik | P10 / R12 | largas | `compareTo` = referência `BigInteger` | Passou (1000 tentativas) | Existe para pegar overflow no `compareTo`, que P10a–c (faixa pequena) não vê |
| PB-P11 | jqwik | P11 / R8 | pequenas, b ≠ 0 | `(a÷b)×b = a` | Passou (1000 tentativas) | |
| PB-P12 | jqwik | P12 / R2, R8, R11 | pequenas; n qualquer | `÷0` e `reciprocal(0)` → `ArithmeticException`; `of(n,0)` → `IllegalArgumentException` | Passou (1000 tentativas) | |
| PB-P13 | jqwik | P13 / R3, R10 | largas | `+ − × ÷` exatos ou `ArithmeticException`, nunca valor errado | Passou (1000 tentativas) | Usar `Statistics` para registrar a proporção com/sem overflow |
| PB-P14a | jqwik | P14 / R5 | `String` arbitrária | aceita só `[+-]?[0-9]+(/[0-9]+)?`; senão `IllegalArgumentException` | Passou (1000 tentativas) | |
| PB-P14b | jqwik | P14 / R4, R5 | "n/d" com n, d ≥ 0 quaisquer | `parse` = `of(n,d)`; d = 0 → `IllegalArgumentException` | Passou (1000 tentativas) | |

---

## 3. Baseline – partição de equivalência e valor limite

| ID | Técnica | Requisito | Entrada | Saída esperada | Resultado obtido | Observações |
|----|---------|-----------|---------|----------------|------------------|-------------|
| EX-01 | Exemplo | R1 | `of(1,-2)` | −1/2 | Passou | denominador negativo |
| EX-02 | Exemplo | R1 | `of(6,4)` | 3/2 | Passou | redução |
| EX-03 | Exemplo | R1 | `of(0,-7)` | 0/1 | Passou | zero |
| EX-04 | Exemplo | R2 | `of(1,0)` | `IllegalArgumentException` | Passou | |
| EX-05 | Exemplo | R3 | `of(MIN,3)`, `of(1,MIN)` | `ArithmeticException` | Passou | limite |
| EX-06 | Exemplo | R3 | `of(MIN,2)` | −2⁶²/1 | Passou | limite reduzível |
| EX-07 | Exemplo | R4 | "1/2", "-3", "+5/3", "2/4" | 1/2, −3, 5/3, 1/2 | Passou | |
| EX-08 | Exemplo | R5 | null, "", " ", "abc", "1/", "/2", "1/-2", "1//2", "1/2/3", " 1/2", "1.5", "1/0" | `IllegalArgumentException` | Passou | |
| EX-09 | Exemplo | R7 | 1/2 e 1/3 | 5/6, 1/6, 1/6 | Passou | |
| EX-10 | Exemplo | R8, R11 | `÷0`, `reciprocal(0)` | `ArithmeticException` | Passou | |
| EX-11 | Exemplo | R9 | `plus(null)`, `dividedBy(null)` | `NullPointerException` | Passou | |
| EX-12 | Exemplo | R10 | `MAX + 1`, `MAX × 2` | `ArithmeticException` | Passou | |
| EX-13 | Exemplo | R11 | −3/4 | `negate`, `abs`, `signum`, `doubleValue` | Passou | |
| EX-14 | Exemplo | R12, R13 | pares de frações | ordem, igualdade e hash por valor | Passou | |
| EX-15 | Exemplo | R14 | 3/4, −3/4, 10/2, 0 | "3/4", "-3/4", "5", "0" | Passou | |

---

## 4. Matriz de rastreabilidade

| Requisito | Randoop (contrato) | jqwik (propriedade) | Exemplo |
|-----------|--------------------|---------------------|---------|
| R1 normalização | RD-C7, RD-C8 | PB-P1 | EX-01…03 |
| R2 `of(n,0)` | | PB-P12 | EX-04 |
| R3 overflow em `of` | | PB-P1, PB-P13 | EX-05, EX-06 |
| R4 `parse` válido | RD-C10 | PB-P3, PB-P14b | EX-07 |
| R5 `parse` inválido | | PB-P14a, PB-P14b | EX-08 |
| R7 `+ − ×` | | PB-P4…P7, P9 | EX-09 |
| R8 `÷` | | PB-P11, PB-P12 | EX-10 |
| R9 NPE | RD-C6 | | EX-11 |
| R10 overflow | | PB-P13 | EX-12 |
| R11 unários, recíproco | | PB-P7, PB-P8, PB-P12 | EX-10, EX-13 |
| R12 ordem | RD-C9 | PB-P10a…d | EX-14 |
| R13 igualdade/hash | RD-C1…C5 | PB-P2 | EX-14 |
| R14 `toString` | RD-C5, RD-C10 | PB-P3 | EX-15 |
| R6, R15 | (cobertos indiretamente) | | |

---

## 5. Execuções e resultados

### 5.1 Ambiente e parâmetros

Os resultados abaixo são da execução de 02/10/2026 sobre o commit `94c00e7`, com a configuração corrigida do Randoop. Substituem os números da execução anterior.

| Item | Valor |
|------|-------|
| Plataforma | macOS, JDK 21 compilando com `release 17`, Maven integrado ao IntelliJ |
| Bibliotecas | JUnit 5.11.4, jqwik 1.9.3, JaCoCo 0.8.12 |
| Randoop | 4.3.4, `--time-limit=60`, `--randomseed=42`, `--npe-on-non-null-input=ERROR`, contratos de `randoop/fraction-specs.json` e literais de `randoop/literals.txt` |
| jqwik | 18 métodos de propriedades; sementes efetivamente usadas registradas nos logs |
| Execução | Cópias isoladas, uma mutação por cópia, com até duas execuções independentes em paralelo |

A semente 42 refere-se ao Randoop. O argumento `-Djqwik.seed=42` usado na automação não fixou as sementes do jqwik nesta configuração; para reproduzir as propriedades, devem ser consultadas as sementes registradas nos logs.

### 5.2 Código correto (baseline, M0)

| Técnica | Resultado |
|---------|-----------|
| Exemplos (EX-01…15) | 15 de 15 passaram |
| jqwik (P1–P14, distribuídas em 18 métodos) | 18 de 18 passaram |
| Randoop | 1.467 testes de regressão gerados e executados, sem falhas; 0 testes reveladores de erro |
| Todos juntos (`mvn -P randoop verify`) | 1.500 testes, 0 falhas e 0 erros |

O Randoop limita a geração por tempo. Assim, a quantidade de testes varia com o ambiente e a carga da máquina, mesmo usando a mesma semente. Esses resultados correspondem à execução registrada acima e não demonstram ausência de defeitos.

### 5.3 Mutações manuais em `Fraction.java`

Foram avaliadas cinco mutações: quatro introduzem defeitos e M5 preserva o contrato de igualdade/hashCode. Cada mutação foi aplicada isoladamente a uma cópia do código correto.

Critério: "detectou" = ao menos um teste da técnica revelou uma violação causada pela mutação. Para o Randoop, a geração terminou normalmente e os testes reveladores de erro foram executados. Parâmetros: semente 42 e 60 segundos.

| ID | Alteração aplicada | Exemplos (EX) | jqwik (PB) | Randoop: testes reveladores de erro | Testes de regressão do Randoop |
|----|-------------------|---------------|------------|-----------------------------------|-------------------------------|
| M1 | `of` não move o sinal do denominador para o numerador | Detectou: EX-01 | Detectou: P1, P2, P3, P11 | Detectou: 18 testes falharam | 1.441 passaram |
| M2 | `of` não divide numerador e denominador pelo mdc | Detectou: EX-02, EX-07, EX-14, EX-15 | Detectou: P1, P2, P5, P9, P11 | Detectou: 16 testes falharam | 1.401 passaram |
| M3 | `compareTo` usa multiplicação cruzada em `long`, sujeita a overflow | Não detectou | Detectou: P10d | Detectou: 4 testes falharam | 1.458 passaram |
| M4 | `plus` e `times` usam operadores aritméticos no lugar de `addExact` e `multiplyExact` | Detectou: EX-12 | Detectou: P13 | Não detectou: 0 testes reveladores de erro | 1.466 passaram |
| M5 | `hashCode` usa somente o numerador | Não detectou | Não detectou | Não detectou: 0 testes reveladores de erro | 1.473 passaram |

Em M1, P3 detectou o problema por uma `IllegalArgumentException`: a representação produzida, como `1/-2`, foi rejeitada por `parse`. As demais propriedades indicadas falharam por asserção.

M5 preserva o contrato: frações iguais continuam produzindo o mesmo hash. A alteração permite colisões adicionais entre valores diferentes, o que não viola esse contrato. Portanto, não deve ser contada como um defeito não detectado.

Os testes de regressão do Randoop registram comportamentos observados na versão usada para geração, inclusive na versão mutada. Passarem não significa que essa versão esteja correta; a detecção contabilizada aqui vem dos testes reveladores de erro.

### 5.4 Variação do Randoop entre sementes (60 s cada)

Testes reveladores de erro que falharam / testes de regressão que passaram:

| Mutação | Semente 1 | Semente 2 | Semente 3 |
|---------|-----------|-----------|-----------|
| M1 | 74 / 1.231 | 202 / 1.805 | 30 / 1.484 |
| M4 | 0 / 1.238 | 0 / 1.718 | 0 / 1.606 |

O Randoop detectou M1 em todas as sementes testadas (1, 2, 3 e 42) e não detectou M4 nessas execuções. Isso não permite concluir que a ferramenta nunca detectaria M4 com outros contratos, entradas ou parâmetros.

Foram preservados os logs de compilação, geração e execução, os relatórios de testes, os testes gerados e o código exato de cada mutação. O conjunto foi consolidado no relatório `RELATORIO.md` e no pacote `evidencias-mutacoes.zip`, mantidos separadamente do repositório nesta revisão.

### 5.5 Dificuldades registradas
- A primeira versão da pós-condição C10 chamava `parse` sobre texto malformado (o que acontece em M1) e o Randoop **abortou** com "Failure executing expression method". Corrigimos com uma guarda de formato antes de chamar `parse`.
- Numa execução exploratória de 20 s e sem literais, o Randoop não detectou M1: o gerador usava quase só `of(1, 1)`. Passamos a fornecer literais variados (`randoop/literals.txt`), incluindo valores negativos e `Long.MAX_VALUE`/`Long.MIN_VALUE`.

- Na execução manual de M1, a geração foi interrompida ao carregar `RegressionTest0`. Nas cópias isoladas com Java 21, as 12 gerações terminaram normalmente. Como o caminho e o contexto de execução também mudaram, não foi isolada a causa do erro original.
