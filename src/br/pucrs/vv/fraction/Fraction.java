package br.pucrs.vv.fraction;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Número racional imutável, mantido sempre na forma normalizada.
 *
 * <h2>Invariantes (requisito R1)</h2>
 * <ul>
 *   <li>{@code getDenominator() > 0};</li>
 *   <li>{@code mdc(|numerador|, denominador) == 1};</li>
 *   <li>zero é representado como {@code 0/1};</li>
 *   <li>o numerador nunca é {@link Long#MIN_VALUE} (decisão de projeto: assim
 *       {@link #negate()} e {@link #abs()} nunca causam overflow).</li>
 * </ul>
 *
 * <h2>Contratos de exceção</h2>
 * <ul>
 *   <li>{@code of(n, 0)}: {@link IllegalArgumentException} (R2);</li>
 *   <li>{@code of}: {@link ArithmeticException} se a normalização exigir
 *       {@link Long#MIN_VALUE} como numerador ou negar um denominador
 *       {@code Long.MIN_VALUE} (R3);</li>
 *   <li>{@code parse}: {@link IllegalArgumentException} para {@code null},
 *       vazio, formato inválido ou denominador zero (R5). Um texto bem formado
 *       cuja normalização estoura (ex.: "-9223372036854775808") lança
 *       {@link ArithmeticException}, como em {@code of};</li>
 *   <li>{@code plus/minus/times/dividedBy/compareTo} com argumento {@code null}:
 *       {@link NullPointerException} (R9). Decisão deliberada: compatível com o
 *       contrato do Randoop, que só trata NPE como falha quando nenhum
 *       argumento era null;</li>
 *   <li>overflow em operações aritméticas: {@link ArithmeticException} (R10),
 *       nunca resultado errado silencioso. A implementação é conservadora:
 *       pode lançar mesmo quando o resultado reduzido caberia em {@code long},
 *       se um valor intermediário estourar;</li>
 *   <li>{@code dividedBy(zero)} e {@code reciprocal()} de zero:
 *       {@link ArithmeticException} (R8, R11).</li>
 * </ul>
 */
public final class Fraction implements Comparable<Fraction> {

    /** Zero (0/1). */
    public static final Fraction ZERO = new Fraction(0, 1);
    /** Um (1/1). */
    public static final Fraction ONE = new Fraction(1, 1);

    private final long numerator;
    private final long denominator;

    /** Construtor privado: só recebe valores já normalizados. */
    private Fraction(long numerator, long denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    // ------------------------------------------------------------------
    // Fábricas
    // ------------------------------------------------------------------

    /**
     * Cria a fração {@code numerator/denominator} normalizada (R1).
     *
     * @throws IllegalArgumentException se {@code denominator == 0} (R2)
     * @throws ArithmeticException se a normalização causar overflow (R3)
     */
    public static Fraction of(long numerator, long denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("denominador zero");
        }
        if (numerator == 0) {
            return ZERO;
        }
        // Long.MIN_VALUE não pode ser negado/abs. Se ambos forem pares, reduzir
        // por 2 elimina o problema quando o outro valor também for par.
        while (((numerator | denominator) & 1L) == 0
                && (numerator == Long.MIN_VALUE || denominator == Long.MIN_VALUE)) {
            numerator /= 2;
            denominator /= 2;
        }
        if (numerator == Long.MIN_VALUE || denominator == Long.MIN_VALUE) {
            throw new ArithmeticException("overflow na normalização");
        }
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        long g = gcd(Math.abs(numerator), denominator);
        return new Fraction(numerator / g, denominator / g);
    }

    /**
     * Interpreta {@code "a/b"} ou {@code "a"}, com sinal opcional ({@code +}
     * ou {@code -}) apenas no numerador (R4). Não admite espaços.
     *
     * @throws IllegalArgumentException para null, vazio, formato inválido,
     *         número fora do intervalo de {@code long} ou denominador zero (R5)
     */
    public static Fraction parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("texto nulo");
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("texto vazio");
        }
        if (!text.matches("[+-]?[0-9]+(/[0-9]+)?")) {
            throw new IllegalArgumentException("formato inválido: " + text);
        }
        int slash = text.indexOf('/');
        try {
            if (slash < 0) {
                return of(Long.parseLong(text), 1);
            }
            long n = Long.parseLong(text.substring(0, slash));
            long d = Long.parseLong(text.substring(slash + 1));
            return of(n, d);
        } catch (NumberFormatException e) { // subclasse de IllegalArgumentException
            throw new IllegalArgumentException("número fora do intervalo: " + text, e);
        }
    }

    // ------------------------------------------------------------------
    // Acesso
    // ------------------------------------------------------------------

    /** Numerador normalizado (carrega o sinal). */
    public long getNumerator() {
        return numerator;
    }

    /** Denominador normalizado, sempre {@code > 0}. */
    public long getDenominator() {
        return denominator;
    }

    // ------------------------------------------------------------------
    // Aritmética
    // ------------------------------------------------------------------

    /**
     * Soma exata.
     *
     * @throws NullPointerException se {@code o == null} (R9)
     * @throws ArithmeticException em overflow (R10)
     */
    public Fraction plus(Fraction o) {
        Objects.requireNonNull(o, "o");
        long g = gcd(denominator, o.denominator);
        long d1 = denominator / g;
        long d2 = o.denominator / g;
        long n = Math.addExact(Math.multiplyExact(numerator, d2),
                               Math.multiplyExact(o.numerator, d1));
        long d = Math.multiplyExact(d1, o.denominator);
        return of(n, d);
    }

    /** Subtração exata ({@code this + (-o)}). */
    public Fraction minus(Fraction o) {
        Objects.requireNonNull(o, "o");
        return plus(o.negate());
    }

    /**
     * Produto exato, com redução cruzada para evitar overflow desnecessário.
     *
     * @throws NullPointerException se {@code o == null} (R9)
     * @throws ArithmeticException em overflow (R10)
     */
    public Fraction times(Fraction o) {
        Objects.requireNonNull(o, "o");
        if (numerator == 0 || o.numerator == 0) {
            return ZERO;
        }
        long g1 = gcd(Math.abs(numerator), o.denominator);
        long g2 = gcd(Math.abs(o.numerator), denominator);
        long n = Math.multiplyExact(numerator / g1, o.numerator / g2);
        long d = Math.multiplyExact(denominator / g2, o.denominator / g1);
        return of(n, d);
    }

    /**
     * Divisão exata.
     *
     * @throws NullPointerException se {@code o == null} (R9)
     * @throws ArithmeticException se {@code o} for zero (R8) ou em overflow
     */
    public Fraction dividedBy(Fraction o) {
        Objects.requireNonNull(o, "o");
        if (o.numerator == 0) {
            throw new ArithmeticException("divisão por zero");
        }
        return times(o.reciprocal());
    }

    /** Simétrico aditivo. Seguro: o numerador nunca é {@code Long.MIN_VALUE}. */
    public Fraction negate() {
        return numerator == 0 ? this : new Fraction(-numerator, denominator);
    }

    /**
     * Inverso multiplicativo.
     *
     * @throws ArithmeticException se esta fração for zero (R11)
     */
    public Fraction reciprocal() {
        if (numerator == 0) {
            throw new ArithmeticException("recíproco de zero");
        }
        return of(denominator, numerator); // of() reposiciona o sinal
    }

    /** Valor absoluto. */
    public Fraction abs() {
        return numerator < 0 ? negate() : this;
    }

    /** Sinal: -1, 0 ou 1. */
    public int signum() {
        return Long.signum(numerator);
    }

    /** Aproximação em ponto flutuante (pode perder precisão). */
    public double doubleValue() {
        return (double) numerator / (double) denominator;
    }

    // ------------------------------------------------------------------
    // Object / Comparable
    // ------------------------------------------------------------------

    /**
     * Ordem total coerente com o valor racional (R12). Usa {@link BigInteger}
     * na multiplicação cruzada, portanto não estoura.
     *
     * @throws NullPointerException se {@code o == null}
     */
    @Override
    public int compareTo(Fraction o) {
        Objects.requireNonNull(o, "o");
        BigInteger left = BigInteger.valueOf(numerator).multiply(BigInteger.valueOf(o.denominator));
        BigInteger right = BigInteger.valueOf(o.numerator).multiply(BigInteger.valueOf(denominator));
        return left.compareTo(right);
    }

    /** Igualdade por valor; como a forma é normalizada, basta comparar os campos (R13). */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Fraction)) {
            return false;
        }
        Fraction f = (Fraction) obj;
        return numerator == f.numerator && denominator == f.denominator;
    }

    @Override
    public int hashCode() {
        return 31 * Long.hashCode(numerator) + Long.hashCode(denominator);
    }

    /** {@code "n/d"}, ou apenas {@code "n"} quando {@code d == 1} (R14). */
    @Override
    public String toString() {
        return denominator == 1 ? Long.toString(numerator) : numerator + "/" + denominator;
    }

    // ------------------------------------------------------------------
    // Auxiliar
    // ------------------------------------------------------------------

    /** MDC de dois valores não negativos (algoritmo de Euclides); {@code gcd(0, b) = b}. */
    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}
