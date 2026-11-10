package io.github.deckers22.shoppinglist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountsTest {
    @Test
    fun formatsWithoutTrailingZeros() {
        assertEquals("", formatAmount(null))
        assertEquals("200", formatAmount(200.0))
        assertEquals("1.5", formatAmount(1.5))
        assertEquals("0.33", formatAmount(1.0 / 3))
        assertEquals("0", formatAmount(0.0))
        assertEquals("100", formatAmount(100.0))
    }

    @Test
    fun parsesDotAndComma() {
        assertEquals(1.5, parseAmount("1.5")!!, 0.0)
        assertEquals(1.5, parseAmount(" 1,5 ")!!, 0.0)
        assertNull(parseAmount(""))
        assertNull(parseAmount("abc"))
        assertNull(parseAmount("-2"))
    }

    @Test
    fun scalesToServings() {
        assertEquals(300.0, scaleAmount(200.0, 4, 6)!!, 1e-9)
        assertEquals(100.0, scaleAmount(200.0, 4, 2)!!, 1e-9)
        assertNull(scaleAmount(null, 4, 6))
    }

    @Test
    fun buildsQuantityLabel() {
        assertEquals("200 g", quantityLabel(200.0, "g"))
        assertEquals("2", quantityLabel(2.0, ""))
        assertEquals("", quantityLabel(null, " "))
    }
}
