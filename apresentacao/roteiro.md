# Roteiro da apresentação (6 min 30 s, 8 slides)

Tema: **Verificação de uma classe de frações com Randoop e jqwik.**
Divisão: Eduardo (slides 1–3), Lucas (slides 4–5), Fernando (slides 6–8). Cada pessoa fala cerca de 2 min.
Todos os números vêm de `tests.md`, seção 5. Os slides estão em `apresentacao.pptx`, com a fala de cada um nas notas do orador.

| # | Tempo | Quem | Slide |
|---|-------|------|-------|
| 1 | 0:30 | Eduardo | Problema e objetivo |
| 2 | 0:50 | Eduardo | O kata: `Fraction` |
| 3 | 0:50 | Eduardo | As duas técnicas e o baseline |
| 4 | 1:00 | Lucas | Randoop: contratos como oráculo |
| 5 | 1:00 | Lucas | jqwik: propriedades |
| 6 | 1:10 | Fernando | Resultados: o que cada técnica detectou |
| 7 | 0:40 | Fernando | Discussão: custo, filtro de exceções e ameaças |
| 8 | 0:30 | Fernando | Lições aprendidas |

---

## Slide 1 – Problema e objetivo (0:30, Eduardo)
**Na tela:** título, equipe, "Como testar o mesmo código com duas técnicas e comparar?"
**Fala:** "Nosso trabalho aplica duas técnicas de teste a um mesmo problema, uma classe de frações em Java, e compara o que cada uma encontra. A pergunta é: o que um oráculo genérico enxerga que propriedades do domínio não enxergam, e o contrário?"

## Slide 2 – O kata `Fraction` (0:50, Eduardo)
**Na tela:** invariantes (denominador > 0, irredutível, zero = 0/1), operações, overflow → `ArithmeticException`, Java 17 + Maven + GitHub Actions.
**Fala:** "A `Fraction` é imutável e sempre normalizada. Tem aritmética exata e sinaliza overflow com exceção, nunca com resultado errado. Escolhemos esse kata porque tem invariantes fortes, uma álgebra conhecida e um risco numérico claro. Uma decisão de projeto: argumento nulo lança `NullPointerException`, compatível com o contrato do Randoop."

## Slide 3 – As técnicas e o baseline (0:50, Eduardo)
**Na tela:** linha do tempo: 1997, Fink e Bishop (propriedades) → 2007, Pacheco et al. (Randoop); faixa de atenção sobre o jqwik; baseline com 15 exemplos.
**Fala:** "Em 1997, Fink e Bishop propõem testar a partir de propriedades formais, com oráculo gerado da especificação. Em 2007, Pacheco e colegas propõem gerar testes de forma aleatória guiada por feedback, com contratos genéricos como oráculo. Dez anos separam os artigos: o oráculo ficou genérico e a geração de dados ficou automática. Um cuidado: o jqwik não implementa a técnica de Fink e Bishop, mas usa a mesma ideia de propriedade como especificação. Como contraste, 15 testes de exemplo."

## Slide 4 – Randoop (1:00, Lucas)
**Na tela:** contratos C1–C6 (padrão) e C7–C10 (nossos); JSON de pós-condições; literais; 60 s, semente 42.
**Fala:** "O Randoop já verifica contratos como `equals` reflexivo e `hashCode` consistente. Nós registramos quatro contratos do domínio em JSON: denominador positivo, irredutibilidade, `compareTo` coerente com `equals` e ida e volta de `parse`. Também demos valores variados ao gerador. Aprendemos que um oráculo precisa ser validado: nossa primeira versão do contrato de ida e volta lançava exceção e fez o Randoop abortar."

## Slide 5 – jqwik (1:00, Lucas)
**Na tela:** 14 propriedades (P1–P14), geradores pequenos e largos, referência em `BigInteger`, P13.
**Fala:** "Escrevemos 14 propriedades: normalização, comutatividade, associatividade, inversos, distributividade, ordem total, divisão e exceções. As algébricas usam faixa pequena para não misturar com overflow. O overflow tem propriedades próprias, com a faixa completa de `long` e `BigInteger` como referência: o resultado é exato ou `ArithmeticException`. No P13, 86% das operações não estouraram e 14% lançaram exceção, e nenhuma devolveu valor errado."

## Slide 6 – Resultados (1:10, Fernando)
**Na tela:** Tabela 1 da resenha (M1–M5 × Exemplos / jqwik / Randoop) e o baseline: 15 + 18 + 884 testes, 0 falhas.
**Fala:** "No código correto tudo passou: 917 testes e nenhuma violação. Depois injetamos cinco defeitos. O defeito de overflow, M4, só foi detectado por exemplo e pelo jqwik; o Randoop não achou em nenhuma das quatro sementes, porque nenhum contrato fala de aritmética. Já o defeito no `compareTo` com multiplicação em `long` mostra o contrário: as propriedades de faixa pequena não viram, e só a de faixa larga viu. O M5, `hashCode` só do numerador, ninguém detecta: é um mutante equivalente em relação aos contratos."

## Slide 7 – Discussão (0:40, Fernando)
**Na tela:** três cartões (custo de especificação, filtro de exceções, ameaças à validade) e a síntese: nenhuma técnica cobre sozinha todos os defeitos.
**Fala:** "O Randoop custou quatro entradas JSON; as propriedades custaram geradores, faixas e um oráculo exato. O filtro de exceções do artigo trata exceção como entrada inválida, mas em `Fraction` divisão por zero e overflow são comportamento especificado; por isso o Randoop explora pouco essa região e só as propriedades confirmam a exceção exigida. Ameaças: defeitos injetados à mão, poucos, uma só classe, poucas sementes."

## Slide 8 – Lições aprendidas (0:30, Fernando)
**Na tela:** 3 itens.
**Fala:** "Um: as técnicas se complementam. Dois: as entradas importam tanto quanto o oráculo. Três: valide o oráculo com defeitos injetados, senão um contrato inócuo passa despercebido. O repositório tem tudo, com README, `tests.md` e CI. Obrigado."

---

## Antes de apresentar
- Ensaiar com cronômetro; o total previsto é 6:30, com folga de 30 s dentro do limite de 7 min.
- Deixar a Tabela 1 (resenha) e o resumo de `tests.md`, seção 5, abertos para eventuais perguntas.
- Se perguntarem "por que o Randoop não achou M4?", a resposta está no slide 6 e na resenha, seção 4.
