package com.kotori316.marker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void preservesWrappingAndInclusiveEndpoints() {
        Range range = new Range(0, 8);
        assertEquals(0, range.convert(0));
        assertEquals(8, range.convert(8));
        assertEquals(4, range.convert(12));
        assertEquals(4, range.convert(-4));
        assertEquals(3, range.convert(-5));
        assertEquals(8, range.convert(-8));
        assertEquals(0, range.convert(16));
        assertEquals(14, new Range(10, 18).convert(22));
    }

    @Test
    void hugeFiniteAnglesCannotRecurseOrEscapeTheRange() {
        Range range = new Range(0, 360);
        for (float angle : new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, 1.0e20f, -1.0e20f}) {
            float wrapped = range.convert(angle);
            assertTrue(Float.isFinite(wrapped) && wrapped >= 0 && wrapped <= 360);
        }
    }

    @Test
    void nonFiniteAnglesHaveASafeFallback() {
        Range range = new Range(10, 18);
        assertEquals(10, range.convert(Float.NaN));
        assertEquals(10, range.convert(Float.POSITIVE_INFINITY));
        assertEquals(10, range.convert(Float.NEGATIVE_INFINITY));
    }

    @Test
    void rejectsEmptyOrNonFiniteRanges() {
        assertThrows(IllegalArgumentException.class, () -> new Range(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Range(8, 0));
        assertThrows(IllegalArgumentException.class, () -> new Range(Float.NaN, 8));
        assertThrows(IllegalArgumentException.class, () -> new Range(0, Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Range(-Float.MAX_VALUE, Float.MAX_VALUE));
    }

}
