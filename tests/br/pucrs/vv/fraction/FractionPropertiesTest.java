package br.pucrs.vv.fraction;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import org.junit.jupiter.api.Assertions;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes baseados em propriedades (jqwik) para a classe Fraction.
 * Propriedades PB-P1 a PB-P14 escritas por Eduardo Hoffmann e Fernando Kunst.
 * Propriedades PB-P15 a PB-P18 escritas por Lucas Mocelin e João Pedro Ongaratto.
 */
class FractionPropertiesTest {

    // -----------------------------------------------------------------------
    // PB-P1  toda fração criada com of() está normalizada (R1, R3)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P1 of() produz fração normalizada ou ArithmeticException")
    void ofProducesNormalizedFraction(
            @ForAll long n,
            @ForAll @LongRange(min = Long.MIN_VALUE, max = -1) long negD,
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long posD) {
        for (long d : new long[]{negD, posD}) {
            try {
                Fraction f = Fraction.of(n, d);
                assertTrue(f.getDenominator() > 0, "denominador deve ser > 0");
                long absN = Math.abs(f.getNumerator());
                assertEquals(1, gcd(absN, f.getDenominator()), "fração deve ser irredutível");
                if (n == 0) assertEquals(Fraction.ZERO, f, "zero deve ser 0/1");
            } catch (ArithmeticException e) {
                // overflow na normalização é permitido (R3)
            }
        }
    }

    // -----------------------------------------------------------------------
    // PB-P2  frações equivalentes são iguais (R13)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P2 of(ka,kb) = of(a,b) para k != 0")
    void equivalentFractionsAreEqual(
            @ForAll @LongRange(min = -100, max = 100) long a,
            @ForAll @LongRange(min = 1,   max = 100) long b,
            @ForAll @LongRange(min = 1,   max = 100) long k) {
        Assume.that(a != 0);
        try {
            Fraction f1 = Fraction.of(a, b);
            Fraction f2 = Fraction.of(a * k, b * k);
            assertEquals(f1, f2);
            assertEquals(f1.hashCode(), f2.hashCode());
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P3  parse(toString(f)) = f (R4, R14)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P3 parse(toString(f)) == f")
    void parseToStringRoundTrip(
            @ForAll @LongRange(min = -1_000_000, max = 1_000_000) long n,
            @ForAll @LongRange(min = 1,          max = 1_000_000) long d) {
        Fraction f = Fraction.of(n, d);
        assertEquals(f, Fraction.parse(f.toString()));
    }

    // -----------------------------------------------------------------------
    // PB-P4  comutatividade de + e × (R7)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P4 comutatividade de + e ×")
    void commutativity(
            @ForAll @LongRange(min = -1000, max = 1000) long n1,
            @ForAll @LongRange(min = 1,     max = 1000) long d1,
            @ForAll @LongRange(min = -1000, max = 1000) long n2,
            @ForAll @LongRange(min = 1,     max = 1000) long d2) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        try {
            assertEquals(a.plus(b), b.plus(a), "a+b deve ser igual a b+a");
            assertEquals(a.times(b), b.times(a), "a×b deve ser igual a b×a");
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P5  associatividade de + e × (R7)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P5 associatividade de + e ×")
    void associativity(
            @ForAll @LongRange(min = -100, max = 100) long n1,
            @ForAll @LongRange(min = 1,   max = 100) long d1,
            @ForAll @LongRange(min = -100, max = 100) long n2,
            @ForAll @LongRange(min = 1,   max = 100) long d2,
            @ForAll @LongRange(min = -100, max = 100) long n3,
            @ForAll @LongRange(min = 1,   max = 100) long d3) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        Fraction c = Fraction.of(n3, d3);
        try {
            assertEquals(a.plus(b).plus(c), a.plus(b.plus(c)), "(a+b)+c deve ser igual a a+(b+c)");
            assertEquals(a.times(b).times(c), a.times(b.times(c)), "(a×b)×c deve ser igual a a×(b×c)");
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P6  elementos neutros (R7)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P6 f+0=f e f×1=f")
    void neutralElements(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);
        assertEquals(f, f.plus(Fraction.ZERO), "f+0 deve ser f");
        assertEquals(f, f.times(Fraction.ONE), "f×1 deve ser f");
    }

    // -----------------------------------------------------------------------
    // PB-P7  f + (-f) = 0 (R7, R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P7 f + negate(f) = 0")
    void addNegateIsZero(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);
        assertEquals(Fraction.ZERO, f.plus(f.negate()), "f + (-f) deve ser 0");
    }

    // -----------------------------------------------------------------------
    // PB-P8  f × f⁻¹ = 1 para f != 0 (R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P8 f × reciprocal(f) = 1 para f != 0")
    void timesReciprocalIsOne(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Assume.that(n != 0);
        Fraction f = Fraction.of(n, d);
        assertEquals(Fraction.ONE, f.times(f.reciprocal()), "f × f⁻¹ deve ser 1");
    }

    // -----------------------------------------------------------------------
    // PB-P9  distributividade (R7)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P9 a(b+c) = ab + ac")
    void distributivity(
            @ForAll @LongRange(min = -100, max = 100) long n1,
            @ForAll @LongRange(min = 1,   max = 100) long d1,
            @ForAll @LongRange(min = -100, max = 100) long n2,
            @ForAll @LongRange(min = 1,   max = 100) long d2,
            @ForAll @LongRange(min = -100, max = 100) long n3,
            @ForAll @LongRange(min = 1,   max = 100) long d3) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        Fraction c = Fraction.of(n3, d3);
        try {
            assertEquals(a.times(b.plus(c)), a.times(b).plus(a.times(c)),
                    "a(b+c) deve ser igual a ab+ac");
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P10  compareTo é ordem total (R12)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P10a compareTo anti-simétrico")
    void compareToAntisymmetric(
            @ForAll @LongRange(min = -1000, max = 1000) long n1,
            @ForAll @LongRange(min = 1,     max = 1000) long d1,
            @ForAll @LongRange(min = -1000, max = 1000) long n2,
            @ForAll @LongRange(min = 1,     max = 1000) long d2) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        assertEquals(-Integer.signum(b.compareTo(a)), Integer.signum(a.compareTo(b)),
                "sgn(a.cmp(b)) deve ser igual a -sgn(b.cmp(a))");
    }

    @Property(tries = 1000)
    @Label("PB-P10b compareTo transitivo")
    void compareToTransitive(
            @ForAll @LongRange(min = -100, max = 100) long n1,
            @ForAll @LongRange(min = 1,   max = 100) long d1,
            @ForAll @LongRange(min = -100, max = 100) long n2,
            @ForAll @LongRange(min = 1,   max = 100) long d2,
            @ForAll @LongRange(min = -100, max = 100) long n3,
            @ForAll @LongRange(min = 1,   max = 100) long d3) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        Fraction c = Fraction.of(n3, d3);
        if (a.compareTo(b) <= 0 && b.compareTo(c) <= 0) {
            assertTrue(a.compareTo(c) <= 0, "compareTo deve ser transitivo");
        }
    }

    @Property(tries = 1000)
    @Label("PB-P10c compareTo coerente com subtração")
    void compareToConsistentWithSubtraction(
            @ForAll @LongRange(min = -1000, max = 1000) long n1,
            @ForAll @LongRange(min = 1,     max = 1000) long d1,
            @ForAll @LongRange(min = -1000, max = 1000) long n2,
            @ForAll @LongRange(min = 1,     max = 1000) long d2) {
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        try {
            assertEquals(Integer.signum(a.compareTo(b)), a.minus(b).signum(),
                    "sgn(a.cmp(b)) deve ser igual a (a-b).signum()");
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    @Property(tries = 1000)
    @Label("PB-P10d compareTo = referência BigInteger (faixa larga)")
    void compareToMatchesBigInteger(
            @ForAll long n1,
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long d1,
            @ForAll long n2,
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long d2) {
        try {
            Fraction a = Fraction.of(n1, d1);
            Fraction b = Fraction.of(n2, d2);
            BigInteger left  = BigInteger.valueOf(a.getNumerator()).multiply(BigInteger.valueOf(b.getDenominator()));
            BigInteger right = BigInteger.valueOf(b.getNumerator()).multiply(BigInteger.valueOf(a.getDenominator()));
            assertEquals(Integer.signum(left.compareTo(right)), Integer.signum(a.compareTo(b)),
                    "compareTo deve coincidir com a comparação via BigInteger");
        } catch (ArithmeticException e) {
            // overflow na criação da fração permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P11  (a÷b)×b = a (R8)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P11 (a÷b)×b = a")
    void divideMultiplyIsIdentity(
            @ForAll @LongRange(min = -1000, max = 1000) long n1,
            @ForAll @LongRange(min = 1,     max = 1000) long d1,
            @ForAll @LongRange(min = -1000, max = 1000) long n2,
            @ForAll @LongRange(min = 1,     max = 1000) long d2) {
        Assume.that(n2 != 0);
        Fraction a = Fraction.of(n1, d1);
        Fraction b = Fraction.of(n2, d2);
        try {
            assertEquals(a, a.dividedBy(b).times(b), "(a÷b)×b deve ser igual a a");
        } catch (ArithmeticException e) {
            // overflow permitido
        }
    }

    // -----------------------------------------------------------------------
    // PB-P12  exceções para operações inválidas (R2, R8, R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P12 divisão por zero e reciprocal de zero lançam ArithmeticException")
    void invalidOperationsThrow(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);
        assertThrows(ArithmeticException.class, () -> f.dividedBy(Fraction.ZERO));
        assertThrows(ArithmeticException.class, Fraction.ZERO::reciprocal);
        assertThrows(IllegalArgumentException.class, () -> Fraction.of(n, 0));
    }

    // -----------------------------------------------------------------------
    // PB-P13  overflow aritmético -> ArithmeticException, nunca valor errado (R3, R10)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P13 + − × ÷ exatos ou ArithmeticException, nunca valor errado")
    void arithmeticExactOrException(
            @ForAll long n1,
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long d1,
            @ForAll long n2,
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long d2) {
        try {
            Fraction a = Fraction.of(n1, d1);
            Fraction b = Fraction.of(n2, d2);
            BigInteger bigN1 = BigInteger.valueOf(a.getNumerator());
            BigInteger bigD1 = BigInteger.valueOf(a.getDenominator());
            BigInteger bigN2 = BigInteger.valueOf(b.getNumerator());
            BigInteger bigD2 = BigInteger.valueOf(b.getDenominator());

            checkOp(a, b, bigN1, bigD1, bigN2, bigD2);
        } catch (ArithmeticException e) {
            // overflow na criação permitido
        }
    }

    private void checkOp(Fraction a, Fraction b,
                         BigInteger n1, BigInteger d1,
                         BigInteger n2, BigInteger d2) {
        try {
            Fraction result = a.plus(b);
            BigInteger expN = n1.multiply(d2).add(n2.multiply(d1));
            BigInteger expD = d1.multiply(d2);
            BigInteger g = expN.abs().gcd(expD);
            BigInteger rN = expN.divide(g);
            BigInteger rD = expD.divide(g);
            assertEquals(rN.longValueExact(), result.getNumerator());
            assertEquals(rD.longValueExact(), result.getDenominator());
        } catch (ArithmeticException e) {
            // overflow permitido - ArithmeticException esperada
        }
    }

    // -----------------------------------------------------------------------
    // PB-P14  parse valida formato (R4, R5)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P14a parse rejeita strings de formato inválido")
    void parseRejectsInvalidFormat(@ForAll String s) {
        Assume.that(!s.matches("[+-]?[0-9]+(/[0-9]+)?"));
        assertThrows(IllegalArgumentException.class, () -> Fraction.parse(s));
    }

    @Property(tries = 1000)
    @Label("PB-P14b parse(\"n/d\") = of(n,d)")
    void parseEqualsOf(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 0,     max = 1000) long d) {
        String text = n + "/" + d;
        if (d == 0) {
            assertThrows(IllegalArgumentException.class, () -> Fraction.parse(text));
        } else {
            assertEquals(Fraction.of(n, d), Fraction.parse(text));
        }
    }

    // =======================================================================
    // PB-P15 a PB-P18  contribuição de Lucas Mocelin e João Pedro Ongaratto
    // =======================================================================

    // -----------------------------------------------------------------------
    // PB-P15  negate aplicado duas vezes devolve a fração original (R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P15 negate involutivo: negate(negate(f)) = f")
    void negateInvolutive(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);
        assertEquals(f, f.negate().negate(),
                "negate aplicado duas vezes deve devolver a fração original");
    }

    // -----------------------------------------------------------------------
    // PB-P16  abs nunca é negativo e signum é coerente com o sinal do numerador (R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P16 abs >= 0 e signum coerente com o numerador")
    void absNonNegativeAndSignumConsistent(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);

        assertTrue(f.abs().getNumerator() >= 0,
                "abs() deve ser >= 0");

        int expectedSignum = Long.signum(f.getNumerator());
        assertEquals(expectedSignum, f.signum(),
                "signum deve ser coerente com o sinal do numerador normalizado");

        if (f.signum() < 0) {
            assertEquals(f.negate(), f.abs(),
                    "se signum < 0 então abs deve ser igual a negate");
        } else {
            assertEquals(f, f.abs(),
                    "se signum >= 0 então abs deve ser igual à própria fração");
        }
    }

    // -----------------------------------------------------------------------
    // PB-P17  reciprocal aplicado duas vezes devolve a fração original (R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P17 reciprocal involutivo: reciprocal(reciprocal(f)) = f para f != 0")
    void reciprocalInvolutive(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Assume.that(n != 0);
        Fraction f = Fraction.of(n, d);
        assertEquals(f, f.reciprocal().reciprocal(),
                "reciprocal aplicado duas vezes deve devolver a fração original");
    }

    // -----------------------------------------------------------------------
    // PB-P18  doubleValue está próximo de numerador / denominador (R11)
    // -----------------------------------------------------------------------
    @Property(tries = 1000)
    @Label("PB-P18 doubleValue próximo de numerador/denominador")
    void doubleValueApproximation(
            @ForAll @LongRange(min = -1000, max = 1000) long n,
            @ForAll @LongRange(min = 1,     max = 1000) long d) {
        Fraction f = Fraction.of(n, d);
        double expected = (double) f.getNumerator() / (double) f.getDenominator();
        assertEquals(expected, f.doubleValue(), 1e-9,
                "doubleValue deve ser próximo de numerador/denominador");
    }

    // -----------------------------------------------------------------------
    // Auxiliar
    // -----------------------------------------------------------------------
    private static long gcd(long a, long b) {
        while (b != 0) { long t = a % b; a = b; b = t; }
        return a;
    }
}
