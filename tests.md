# Casos de teste – Fraction (T1 de Verificação e Validação, PUCRS 2026/I)

Este documento descreve os casos de teste do kata `Fraction` (números racionais) e liga cada um a uma técnica, a um contrato ou propriedade e a um requisito. A coluna **Resultado obtido** fica em branco até a equipe executar de fato; nenhum número deste arquivo é simulado.

- **Requisitos (R1…R15):** listados na seção 0 deste documento.
- **Técnica 1 – Randoop** (geração aleatória guiada por feedback, contratos como oráculo; Pacheco et al., 2007): IDs `RD-Cn`.
- **Técnica 2 – jqwik** (propriedades; Fink e Bishop, 1997, como inspiração conceitual): IDs `PB-Pn`.
- **Baseline** (partição de equivalência + valor limite): IDs `EX-nn`.

Arquivos: `tests/br/pucrs/vv/fraction/` (`FractionExampleTest`, `FractionPropertiesTest`), `randoop/fraction-specs.json` e `scripts/run-randoop.sh`.

> Para os testes gerados pelo Randoop, registrar: versão do Randoop, `--time-limit`, `--randomseed`, número de testes gerados (regressão e erro-revelador). Para o jqwik, registrar a semente e `tries` (padrão 1000, a confirmar na execução) e o relatório de `Statistics` de PB-P13.

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

Contratos do Randoop: **C1–C6** são os contratos padrão (Pacheco et al., 2007): C1 `o.equals(o)`; C2 `equals` simétrico; C3 `equals` transitivo; C4 `equals` ⇒ mesmo `hashCode`; C5 `hashCode`/`toString` não lançam exceção; C6 sem NPE quando nenhum argumento era null. **C7–C10** são adicionais: C7 denominador > 0; C8 mdc = 1 e zero é 0/1; C9 `compareTo == 0` ⇔ `equals`; C10 `toString` não vazio e `parse(toString())` igual ao original.

Propriedades do jqwik: P1…P14, descritas na seção 2.

---

## 1. Técnica 1 – Randoop (contratos como oráculo)

| ID | Técnica | Contrato / Requisito | Entrada (gerador) | Saída esperada | Resultado obtido | Observações |
|----|---------|----------------------|-------------------|----------------|------------------|-------------|
| RD-C1 | Randoop | C1 / R13 | Sequências aleatórias de `of`, `parse` e operações | `o.equals(o)` é true | | Contrato padrão |
| RD-C2 | Randoop | C2 / R13 | idem (pares de objetos) | `equals` simétrico | | Contrato padrão |
| RD-C3 | Randoop | C3 / R13 | idem (trios de objetos) | `equals` transitivo | | Contrato padrão |
| RD-C4 | Randoop | C4 / R13 | idem | `equals` ⇒ mesmo `hashCode` | | Contrato padrão |
| RD-C5 | Randoop | C5 / R13, R14 | idem | `hashCode()` e `toString()` não lançam exceção | | Contrato padrão |
| RD-C6 | Randoop | C6 / R9 | idem, com e sem `null` | Nenhum NPE quando nenhum argumento era null | | Valida a decisão de projeto (NPE em argumento null) |
| RD-C7 | Randoop | C7 / R1 | idem | `getDenominator() > 0` | | Contrato adicional (`randoop/fraction-specs.json`) |
| RD-C8 | Randoop | C8 / R1 | idem | mdc(\|n\|, d) = 1; zero é 0/1 | | Contrato adicional |
| RD-C9 | Randoop | C9 / R12, R13 | idem | `compareTo == 0` ⇔ `equals` | | Contrato adicional |
| RD-C10 | Randoop | C10 / R4, R14 | idem | `toString()` não vazio e `parse(toString())` igual ao original | | Contrato adicional |

**Como o resultado deve ser lido:** o Randoop só reporta falha quando um contrato é violado. Sequências que lançam exceção (`ArithmeticException`, `IllegalArgumentException`) são tratadas conforme as opções de exceção da versão usada **[VERIFICAR no manual do Randoop e registrar o comportamento observado]**. Esse comportamento é o que a resenha discute (filtro de exceções do artigo 1, seção 2.4).

---

## 2. Técnica 2 – jqwik (propriedades)

Geradores "pequenos": n ∈ [−1000, 1000], d ∈ [1, 1000] (sem overflow). Geradores "largos": mistura de pequenos e `long` completo, com referência em `BigInteger`.

| ID | Técnica | Propriedade / Requisito | Gerador | Saída esperada | Resultado obtido | Observações |
|----|---------|-------------------------|---------|----------------|------------------|-------------|
| PB-P1 | jqwik | P1 / R1, R3 | n qualquer `long`, d ≠ 0 qualquer | d > 0, mdc = 1, valor preservado; `ArithmeticException` só se houver `Long.MIN_VALUE` | | Faixa completa |
| PB-P2 | jqwik | P2 / R13 | a, b ≠ 0, k ≠ 0 pequenos | `of(a,b).equals(of(ka,kb))`, hashCodes iguais | | |
| PB-P3 | jqwik | P3 / R4, R14 | frações largas | `parse(toString(f))` = f | | |
| PB-P4 | jqwik | P4 / R7 | pequenas | `a+b = b+a`, `a×b = b×a` | | |
| PB-P5 | jqwik | P5 / R7 | pequenas | associatividade de + e × | | |
| PB-P6 | jqwik | P6 / R7 | pequenas | `f+0 = f`, `f×1 = f` | | |
| PB-P7 | jqwik | P7 / R7, R11 | pequenas | `f + (−f) = 0` | | |
| PB-P8 | jqwik | P8 / R11 | pequenas, f ≠ 0 | `f × f⁻¹ = 1` | | |
| PB-P9 | jqwik | P9 / R7 | pequenas | `a(b+c) = ab + ac` | | |
| PB-P10a | jqwik | P10 / R12 | pequenas | `sgn(a.cmp(b)) = −sgn(b.cmp(a))` | | |
| PB-P10b | jqwik | P10 / R12 | pequenas | transitividade de `compareTo` | | |
| PB-P10c | jqwik | P10 / R12 | pequenas | `sgn(a.cmp(b)) = (a−b).signum()` | | |
| PB-P10d | jqwik | P10 / R12 | largas | `compareTo` = referência `BigInteger` | | Existe para pegar overflow no `compareTo`, que P10a–c (faixa pequena) não vê |
| PB-P11 | jqwik | P11 / R8 | pequenas, b ≠ 0 | `(a÷b)×b = a` | | |
| PB-P12 | jqwik | P12 / R2, R8, R11 | pequenas; n qualquer | `÷0` e `reciprocal(0)` → `ArithmeticException`; `of(n,0)` → `IllegalArgumentException` | | |
| PB-P13 | jqwik | P13 / R3, R10 | largas | `+ − × ÷` exatos ou `ArithmeticException`, nunca valor errado | | Usar `Statistics` para registrar a proporção com/sem overflow |
| PB-P14a | jqwik | P14 / R5 | `String` arbitrária | aceita só `[+-]?[0-9]+(/[0-9]+)?`; senão `IllegalArgumentException` | | |
| PB-P14b | jqwik | P14 / R4, R5 | "n/d" com n, d ≥ 0 quaisquer | `parse` = `of(n,d)`; d = 0 → `IllegalArgumentException` | | |

---

## 3. Baseline – partição de equivalência e valor limite

| ID | Técnica | Requisito | Entrada | Saída esperada | Resultado obtido | Observações |
|----|---------|-----------|---------|----------------|------------------|-------------|
| EX-01 | Exemplo | R1 | `of(1,-2)` | −1/2 | | denominador negativo |
| EX-02 | Exemplo | R1 | `of(6,4)` | 3/2 | | redução |
| EX-03 | Exemplo | R1 | `of(0,-7)` | 0/1 | | zero |
| EX-04 | Exemplo | R2 | `of(1,0)` | `IllegalArgumentException` | | |
| EX-05 | Exemplo | R3 | `of(MIN,3)`, `of(1,MIN)` | `ArithmeticException` | | limite |
| EX-06 | Exemplo | R3 | `of(MIN,2)` | −2⁶²/1 | | limite reduzível |
| EX-07 | Exemplo | R4 | "1/2", "-3", "+5/3", "2/4" | 1/2, −3, 5/3, 1/2 | | |
| EX-08 | Exemplo | R5 | null, "", " ", "abc", "1/", "/2", "1/-2", "1//2", "1/2/3", " 1/2", "1.5", "1/0" | `IllegalArgumentException` | | |
| EX-09 | Exemplo | R7 | 1/2 e 1/3 | 5/6, 1/6, 1/6 | | |
| EX-10 | Exemplo | R8, R11 | `÷0`, `reciprocal(0)` | `ArithmeticException` | | |
| EX-11 | Exemplo | R9 | `plus(null)`, `dividedBy(null)` | `NullPointerException` | | |
| EX-12 | Exemplo | R10 | `MAX + 1`, `MAX × 2` | `ArithmeticException` | | |
| EX-13 | Exemplo | R11 | −3/4 | `negate`, `abs`, `signum`, `doubleValue` | | |
| EX-14 | Exemplo | R12, R13 | pares de frações | ordem, igualdade e hash por valor | | |
| EX-15 | Exemplo | R14 | 3/4, −3/4, 10/2, 0 | "3/4", "-3/4", "5", "0" | | |

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
