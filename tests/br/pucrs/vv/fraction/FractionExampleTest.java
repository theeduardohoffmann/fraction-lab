package br.pucrs.vv.fraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Baseline: testes de exemplo por partição de equivalência e valor limite
 * (IDs EX-nn em tests.md). Serve de contraste com Randoop e jqwik.
 */
class FractionExampleTest {

    @Test
    @DisplayName("EX-01 (R1): sinal do denominador é movido para o numerador")
    void ex01NegativeDenominator() {
        Fraction f = Fraction.of(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    @DisplayName("EX-02 (R1): redução por mdc")
    void ex02Reduction() {
        Fraction f = Fraction.of(6, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    @DisplayName("EX-03 (R1): zero é 0/1, qualquer que seja o denominador")
    void ex03Zero() {
        Fraction f = Fraction.of(0, -7);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    @DisplayName("EX-04 (R2): denominador zero lança IllegalArgumentException")
    void ex04ZeroDenominator() {
        assertThrows(IllegalArgumentException.class, () -> Fraction.of(1, 0));
    }

    @Test
    @DisplayName("EX-05 (R3): Long.MIN_VALUE irreduzível lança ArithmeticException")
    void ex05MinValue() {
        assertThrows(ArithmeticException.class, () -> Fraction.of(Long.MIN_VALUE, 3));
        assertThrows(ArithmeticException.class, () -> Fraction.of(1, Long.MIN_VALUE));
    }

    @Test
    @DisplayName("EX-06 (R3): Long.MIN_VALUE reduzível por 2 é aceito")
    void ex06MinValueReducible() {
        Fraction f = Fraction.of(Long.MIN_VALUE, 2);
        assertEquals(Long.MIN_VALUE / 2, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    @DisplayName("EX-07 (R4): parse de 'a/b', 'a' e sinal opcional")
    void ex07ParseValid() {
        assertEquals(Fraction.of(1, 2), Fraction.parse("1/2"));
        assertEquals(Fraction.of(-3, 1), Fraction.parse("-3"));
        assertEquals(Fraction.of(5, 3), Fraction.parse("+5/3"));
        assertEquals(Fraction.of(1, 2), Fraction.parse("2/4"));
    }

    @Test
    @DisplayName("EX-08 (R5): parse de entradas inválidas lança IllegalArgumentException")
    void ex08ParseInvalid() {
        for (String s : new String[] {null, "", " ", "abc", "1/", "/2", "1/-2", "1//2", "1/2/3", " 1/2", "1.5", "1/0"}) {
            assertThrows(IllegalArgumentException.class, () -> Fraction.parse(s), "entrada: " + s);
        }
    }

    @Test
    @DisplayName("EX-09 (R7): soma, subtração e produto básicos")
    void ex09Arithmetic() {
        Fraction a = Fraction.of(1, 2);
        Fraction b = Fraction.of(1, 3);
        assertEquals(Fraction.of(5, 6), a.plus(b));
        assertEquals(Fraction.of(1, 6), a.minus(b));
        assertEquals(Fraction.of(1, 6), a.times(b));
    }

    @Test
    @DisplayName("EX-10 (R8, R11): divisão, divisão por zero e recíproco de zero")
    void ex10Division() {
        assertEquals(Fraction.of(3, 2), Fraction.of(1, 2).dividedBy(Fraction.of(1, 3)));
        assertThrows(ArithmeticException.class, () -> Fraction.ONE.dividedBy(Fraction.ZERO));
        assertThrows(ArithmeticException.class, () -> Fraction.ZERO.reciprocal());
    }

    @Test
    @DisplayName("EX-11 (R9): argumento null lança NullPointerException")
    void ex11Null() {
        assertThrows(NullPointerException.class, () -> Fraction.ONE.plus(null));
        assertThrows(NullPointerException.class, () -> Fraction.ONE.dividedBy(null));
    }

    @Test
    @DisplayName("EX-12 (R10): overflow na soma e no produto é sinalizado")
    void ex12Overflow() {
        Fraction max = Fraction.of(Long.MAX_VALUE, 1);
        assertThrows(ArithmeticException.class, () -> max.plus(Fraction.ONE));
        assertThrows(ArithmeticException.class, () -> max.times(Fraction.of(2, 1)));
    }

    @Test
    @DisplayName("EX-13 (R11): negate, abs, signum e doubleValue")
    void ex13Unary() {
        Fraction f = Fraction.of(-3, 4);
        assertEquals(Fraction.of(3, 4), f.negate());
        assertEquals(Fraction.of(3, 4), f.abs());
        assertEquals(-1, f.signum());
        assertEquals(0, Fraction.ZERO.signum());
        assertEquals(-0.75, f.doubleValue(), 1e-12);
    }

    @Test
    @DisplayName("EX-14 (R12, R13): compareTo, equals e hashCode por valor")
    void ex14Value() {
        assertTrue(Fraction.of(1, 3).compareTo(Fraction.of(1, 2)) < 0);
        assertTrue(Fraction.of(-1, 2).compareTo(Fraction.of(-1, 3)) < 0);
        assertEquals(Fraction.of(1, 2), Fraction.of(2, 4));
        assertEquals(Fraction.of(1, 2).hashCode(), Fraction.of(2, 4).hashCode());
        assertNotEquals(Fraction.of(1, 2), Fraction.of(1, 3));
    }

    @Test
    @DisplayName("EX-15 (R14): toString")
    void ex15ToString() {
        assertEquals("3/4", Fraction.of(3, 4).toString());
        assertEquals("-3/4", Fraction.of(3, -4).toString());
        assertEquals("5", Fraction.of(10, 2).toString());
        assertEquals("0", Fraction.ZERO.toString());
    }
}
