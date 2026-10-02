package br.pucrs.vv.fraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.statistics.Statistics;

/**
 * Testes baseados em propriedades com jqwik (IDs PB-Pn em tests.md).
 *
 * <p>P4–P9, P11 e P10a–c usam geradores pequenos (|n| ≤ 1000, 1 ≤ d ≤ 1000),
 * para que não haja overflow e a propriedade algébrica seja a única coisa
 * sendo testada. O overflow é tratado à parte em P1, P10d e P13, com a faixa
 * completa de {@code long} e um oráculo em {@link BigInteger}.
 */
class FractionPropertiesTest {

    // ------------------------------------------------------------------
    // Geradores
    // ------------------------------------------------------------------

    @Provide
    Arbitrary<Fraction> small() {
        return Combinators.combine(
                Arbitraries.longs().between(-1000, 1000),
                Arbitraries.longs().between(1, 1000))
            .as(Fraction::of);
    }

    @Provide
    Arbitrary<Fraction> smallNonZero() {
        return small().filter(f -> f.signum() != 0);
    }

    @Provide
    Arbitrary<Long> smallNum() {
        return Arbitraries.longs().between(-1000, 1000);
    }

    @Provide
    Arbitrary<Long> smallNonZeroLong() {
        return Arbitraries.longs().between(-1000, 1000).filter(x -> x != 0);
    }

    @Provide
    Arbitrary<Long> nonZeroLong() {
        return Arbitraries.longs().filter(x -> x != 0);
    }

    /**
     * Mistura de frações pequenas e frações com numerador/denominador em toda a
     * faixa de long (o jqwik inclui Long.MIN_VALUE/MAX_VALUE como casos limite).
     * A mistura garante que haja casos com e sem overflow.
     */
    @Provide
    Arbitrary<Fraction> wide() {
        Arbitrary<Fraction> raw = Combinators.combine(
                Arbitraries.longs(),
                Arbitraries.longs().filter(d -> d != 0))
            .as((n, d) -> {
                try {
                    return Fraction.of(n, d);
                } catch (ArithmeticException e) {
                    return Fraction.ONE; // não representável (Long.MIN_VALUE): substitui por 1
                }
            });
        return Arbitraries.oneOf(small(), raw);
    }

    // ------------------------------------------------------------------
    // Oráculo de referência (BigInteger)
    // ------------------------------------------------------------------

    private static boolean sameValue(BigInteger n1, BigInteger d1, Fraction f) {
        return n1.multiply(BigInteger.valueOf(f.getDenominator()))
                .equals(BigInteger.valueOf(f.getNumerator()).multiply(d1));
    }

    private static BigInteger bn(Fraction f) {
        return BigInteger.valueOf(f.getNumerator());
    }

    private static BigInteger bd(Fraction f) {
        return BigInteger.valueOf(f.getDenominator());
    }

    // ------------------------------------------------------------------
    // P1–P3
    // ------------------------------------------------------------------

    @Property
    @Label("PB-P1 (R1,R3): of(n,d) é normalizada e exata; só estoura com Long.MIN_VALUE")
    void pbP1_normalization(@ForAll("anyLong") long n, @ForAll("nonZeroLong") long d) {
        Fraction f;
        try {
            f = Fraction.of(n, d);
        } catch (ArithmeticException e) {
            assertTrue(n == Long.MIN_VALUE || d == Long.MIN_VALUE,
                    "ArithmeticException só é esperada com Long.MIN_VALUE: " + n + "/" + d);
            return;
        }
        assertTrue(f.getDenominator() > 0);
        assertEquals(BigInteger.ONE, bn(f).abs().gcd(bd(f)), "mdc deve ser 1: " + f);
        assertTrue(sameValue(BigInteger.valueOf(n), BigInteger.valueOf(d), f),
                "valor racional preservado: " + n + "/" + d + " -> " + f);
    }

    @Provide
    Arbitrary<Long> anyLong() {
        return Arbitraries.longs();
    }

    @Property
    @Label("PB-P2 (R13): of(a,b) = of(k*a,k*b) e hashCodes iguais")
    void pbP2_valueEquality(@ForAll("smallNum") long a, @ForAll("smallNonZeroLong") long b,
                            @ForAll("smallNonZeroLong") long k) {
        Fraction x = Fraction.of(a, b);
        Fraction y = Fraction.of(k * a, k * b);
        assertEquals(x, y);
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Property
    @Label("PB-P3 (R4,R14): parse(toString(f)) = f")
    void pbP3_roundTrip(@ForAll("wide") Fraction f) {
        assertEquals(f, Fraction.parse(f.toString()));
    }

    // ------------------------------------------------------------------
    // P4–P9, P11 (faixa pequena: sem overflow)
    // ------------------------------------------------------------------

    @Property
    @Label("PB-P4 (R7): comutatividade de + e ×")
    void pbP4_commutativity(@ForAll("small") Fraction a, @ForAll("small") Fraction b) {
        assertEquals(a.plus(b), b.plus(a));
        assertEquals(a.times(b), b.times(a));
    }

    @Property
    @Label("PB-P5 (R7): associatividade de + e ×")
    void pbP5_associativity(@ForAll("small") Fraction a, @ForAll("small") Fraction b,
                            @ForAll("small") Fraction c) {
        assertEquals(a.plus(b).plus(c), a.plus(b.plus(c)));
        assertEquals(a.times(b).times(c), a.times(b.times(c)));
    }

    @Property
    @Label("PB-P6 (R7): elementos neutros 0 (soma) e 1 (produto)")
    void pbP6_identity(@ForAll("small") Fraction f) {
        assertEquals(f, f.plus(Fraction.ZERO));
        assertEquals(f, f.times(Fraction.ONE));
    }

    @Property
    @Label("PB-P7 (R7,R11): inverso aditivo")
    void pbP7_additiveInverse(@ForAll("small") Fraction f) {
        assertEquals(Fraction.ZERO, f.plus(f.negate()));
    }

    @Property
    @Label("PB-P8 (R11): inverso multiplicativo")
    void pbP8_multiplicativeInverse(@ForAll("smallNonZero") Fraction f) {
        assertEquals(Fraction.ONE, f.times(f.reciprocal()));
    }

    @Property
    @Label("PB-P9 (R7): distributividade")
    void pbP9_distributivity(@ForAll("small") Fraction a, @ForAll("small") Fraction b,
                             @ForAll("small") Fraction c) {
        assertEquals(a.times(b).plus(a.times(c)), a.times(b.plus(c)));
    }

    // ------------------------------------------------------------------
    // P10 (ordem total)
    // ------------------------------------------------------------------

    @Property
    @Label("PB-P10a (R12): antissimetria de compareTo")
    void pbP10a_antisymmetry(@ForAll("small") Fraction a, @ForAll("small") Fraction b) {
        assertEquals(Integer.signum(a.compareTo(b)), -Integer.signum(b.compareTo(a)));
    }

    @Property
    @Label("PB-P10b (R12): transitividade de compareTo")
    void pbP10b_transitivity(@ForAll("small") Fraction a, @ForAll("small") Fraction b,
                             @ForAll("small") Fraction c) {
        if (a.compareTo(b) <= 0 && b.compareTo(c) <= 0) {
            assertTrue(a.compareTo(c) <= 0);
        }
    }

    @Property
    @Label("PB-P10c (R12): compareTo coerente com minus().signum()")
    void pbP10c_consistentWithMinus(@ForAll("small") Fraction a, @ForAll("small") Fraction b) {
        assertEquals(Integer.signum(a.compareTo(b)), a.minus(b).signum());
    }

    @Property
    @Label("PB-P10d (R12): compareTo correto em toda a faixa de long (referência BigInteger)")
    void pbP10d_compareWideRange(@ForAll("wide") Fraction a, @ForAll("wide") Fraction b) {
        BigInteger left = bn(a).multiply(bd(b));
        BigInteger right = bn(b).multiply(bd(a));
        assertEquals(left.compareTo(right), Integer.signum(a.compareTo(b)));
    }

    // ------------------------------------------------------------------
    // P11, P12
    // ------------------------------------------------------------------

    @Property
    @Label("PB-P11 (R8): (a / b) * b = a, b != 0")
    void pbP11_division(@ForAll("small") Fraction a, @ForAll("smallNonZero") Fraction b) {
        assertEquals(a, a.dividedBy(b).times(b));
    }

    @Property
    @Label("PB-P12 (R2,R8,R11): exceções de divisão por zero e denominador zero")
    void pbP12_exceptions(@ForAll("small") Fraction f, @ForAll("anyLong") long n) {
        assertThrows(ArithmeticException.class, () -> f.dividedBy(Fraction.ZERO));
        assertThrows(ArithmeticException.class, () -> Fraction.ZERO.reciprocal());
        assertThrows(IllegalArgumentException.class, () -> Fraction.of(n, 0));
    }

    // ------------------------------------------------------------------
    // P13 (overflow sinalizado) e P14 (parse)
    // ------------------------------------------------------------------

    @Property
    @Label("PB-P13 (R3,R10): em toda a faixa, o resultado é exato ou ArithmeticException")
    void pbP13_overflowSignalled(@ForAll("wide") Fraction a, @ForAll("wide") Fraction b) {
        check(a, b, "plus", () -> a.plus(b),
                bn(a).multiply(bd(b)).add(bn(b).multiply(bd(a))), bd(a).multiply(bd(b)));
        check(a, b, "minus", () -> a.minus(b),
                bn(a).multiply(bd(b)).subtract(bn(b).multiply(bd(a))), bd(a).multiply(bd(b)));
        check(a, b, "times", () -> a.times(b), bn(a).multiply(bn(b)), bd(a).multiply(bd(b)));
        if (b.signum() != 0) {
            BigInteger n = bn(a).multiply(bd(b));
            BigInteger d = bd(a).multiply(bn(b));
            check(a, b, "dividedBy", () -> a.dividedBy(b), d.signum() < 0 ? n.negate() : n, d.abs());
        }
    }

    private static void check(Fraction a, Fraction b, String op,
                              java.util.function.Supplier<Fraction> action,
                              BigInteger expectedN, BigInteger expectedD) {
        try {
            Fraction r = action.get();
            Statistics.collect("sem overflow");
            assertTrue(sameValue(expectedN, expectedD, r),
                    op + " devolveu valor errado em silêncio: " + a + " " + op + " " + b + " = " + r);
        } catch (ArithmeticException e) {
            Statistics.collect("ArithmeticException");
        }
    }

    @Property
    @Label("PB-P14a (R5): parse de texto arbitrário aceita só o formato válido")
    void pbP14a_parseArbitraryText(@ForAll String s) {
        try {
            Fraction f = Fraction.parse(s);
            assertTrue(s.matches("[+-]?[0-9]+(/[0-9]+)?"), "aceitou formato inválido: " + s);
            assertEquals(f, Fraction.parse(f.toString()));
        } catch (IllegalArgumentException e) {
            // esperado para texto inválido
        } catch (ArithmeticException e) {
            assertTrue(s.matches("[+-]?[0-9]+(/[0-9]+)?"), "ArithmeticException em texto inválido: " + s);
        }
    }

    @Property
    @Label("PB-P14b (R4,R5): parse de 'n/d' bem formado equivale a of(n,d)")
    void pbP14b_parseWellFormed(@ForAll("anyLong") long n, @ForAll("nonNegLong") long d) {
        String text = n + "/" + d;
        if (d == 0) {
            assertThrows(IllegalArgumentException.class, () -> Fraction.parse(text));
            return;
        }
        try {
            assertEquals(Fraction.of(n, d), Fraction.parse(text));
        } catch (ArithmeticException e) {
            assertThrows(ArithmeticException.class, () -> Fraction.of(n, d));
        }
    }

    @Provide
    Arbitrary<Long> nonNegLong() {
        return Arbitraries.longs().between(0, Long.MAX_VALUE);
    }
}
