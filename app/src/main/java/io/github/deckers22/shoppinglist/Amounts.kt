package io.github.deckers22.shoppinglist

import java.math.BigDecimal
import java.math.RoundingMode

/** "1.5", "200", or "" for no amount. At most two decimals. */
fun formatAmount(amount: Double?): String {
    if (amount == null) return ""
    val rounded = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
    return if (rounded.signum() == 0) "0" else rounded.toPlainString()
}

/** Accepts both "1.5" and "1,5". Returns null for blank or invalid input. */
fun parseAmount(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }

fun scaleAmount(amount: Double?, baseServings: Int, servings: Int): Double? =
    if (amount == null || baseServings <= 0) amount else amount * servings / baseServings

/** "200 g", "2", "g" or "". */
fun quantityLabel(amount: Double?, unit: String): String =
    listOf(formatAmount(amount), unit.trim()).filter { it.isNotEmpty() }.joinToString(" ")
