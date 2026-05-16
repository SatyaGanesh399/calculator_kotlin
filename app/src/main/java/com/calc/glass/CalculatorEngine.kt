package com.calc.glass

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Stateless calculator engine. Holds the current display string, an optional
 * pending operand, and the operator awaiting a right-hand side.
 *
 * Behavior matches a typical phone calculator:
 *  - Tapping an operator after another operator replaces it.
 *  - Tapping "=" repeatedly re-applies the last op with the last operand.
 *  - "%" converts the current value to current / 100 (or, if a pending op
 *    exists, takes a percentage of the left operand).
 *  - "±" flips the sign of the current entry.
 *  - "⌫" deletes the last digit; clears to "0" when only one char remains.
 *  - "C" resets everything.
 *
 * Numbers are computed with BigDecimal to avoid floating-point drift.
 */
data class CalcState(
    val display: String = "0",
    val expression: String = "",
    private val accumulator: BigDecimal? = null,
    private val pendingOp: Op? = null,
    private val justEvaluated: Boolean = false,
    private val freshEntry: Boolean = true,
    private val lastOp: Op? = null,
    private val lastOperand: BigDecimal? = null
) {
    fun input(action: Action): CalcState = when (action) {
        is Action.Digit  -> onDigit(action.d)
        Action.Dot       -> onDot()
        Action.Clear     -> CalcState()
        Action.Backspace -> onBackspace()
        Action.PlusMinus -> onPlusMinus()
        Action.Percent   -> onPercent()
        is Action.Operator -> onOperator(action.op)
        Action.Equals    -> onEquals()
    }

    private fun onDigit(d: Int): CalcState {
        if (justEvaluated) {
            return copy(
                display = d.toString(),
                expression = "",
                accumulator = null,
                pendingOp = null,
                justEvaluated = false,
                freshEntry = false
            )
        }
        if (freshEntry || display == "0") {
            return copy(display = d.toString(), freshEntry = false)
        }
        if (display.replace("-", "").replace(".", "").length >= 12) return this
        return copy(display = display + d.toString())
    }

    private fun onDot(): CalcState {
        if (justEvaluated) {
            return copy(
                display = "0.",
                expression = "",
                accumulator = null,
                pendingOp = null,
                justEvaluated = false,
                freshEntry = false
            )
        }
        if (freshEntry) return copy(display = "0.", freshEntry = false)
        if (display.contains(".")) return this
        return copy(display = "$display.")
    }

    private fun onBackspace(): CalcState {
        if (justEvaluated || freshEntry) return this
        val d = display
        val next = when {
            d.length <= 1 -> "0"
            d.length == 2 && d.startsWith("-") -> "0"
            else -> d.dropLast(1)
        }
        return copy(display = next, freshEntry = next == "0")
    }

    private fun onPlusMinus(): CalcState {
        if (display == "0") return this
        val next = if (display.startsWith("-")) display.drop(1) else "-$display"
        return copy(display = next, justEvaluated = false)
    }

    private fun onPercent(): CalcState {
        val current = display.toBigDecimalOrNull() ?: return this
        // If we have an accumulator and pending op, percent acts as
        // "x% of accumulator". Otherwise it's just current/100.
        val result = if (accumulator != null && pendingOp != null) {
            accumulator.multiply(current).divide(BigDecimal(100), MC)
        } else {
            current.divide(BigDecimal(100), MC)
        }
        return copy(display = result.prettyString(), freshEntry = false)
    }

    private fun onOperator(op: Op): CalcState {
        val current = display.toBigDecimalOrNull() ?: return this

        // If user just tapped an operator (no new entry yet), swap it.
        if (freshEntry && pendingOp != null && accumulator != null) {
            return copy(pendingOp = op, expression = "${accumulator.prettyString()} ${op.glyph}")
        }

        val newAcc = if (accumulator == null || pendingOp == null) {
            current
        } else {
            apply(accumulator, current, pendingOp) ?: return copy(display = "Error")
        }
        return copy(
            display = newAcc.prettyString(),
            expression = "${newAcc.prettyString()} ${op.glyph}",
            accumulator = newAcc,
            pendingOp = op,
            justEvaluated = false,
            freshEntry = true
        )
    }

    private fun onEquals(): CalcState {
        // Repeat last operation if user mashes "=" after a result.
        if (justEvaluated && lastOp != null && lastOperand != null) {
            val current = display.toBigDecimalOrNull() ?: return this
            val r = apply(current, lastOperand, lastOp) ?: return copy(display = "Error")
            return copy(
                display = r.prettyString(),
                expression = "",
                justEvaluated = true,
                freshEntry = true
            )
        }
        val current = display.toBigDecimalOrNull() ?: return this
        val op = pendingOp ?: return this
        val acc = accumulator ?: return this
        val r = apply(acc, current, op) ?: return copy(display = "Error", expression = "")
        return copy(
            display = r.prettyString(),
            expression = "",
            accumulator = null,
            pendingOp = null,
            justEvaluated = true,
            freshEntry = true,
            lastOp = op,
            lastOperand = current
        )
    }

    private fun apply(a: BigDecimal, b: BigDecimal, op: Op): BigDecimal? = try {
        when (op) {
            Op.Add -> a.add(b, MC)
            Op.Sub -> a.subtract(b, MC)
            Op.Mul -> a.multiply(b, MC)
            Op.Div -> if (b.signum() == 0) null else a.divide(b, MC)
        }
    } catch (_: ArithmeticException) { null }

    companion object {
        private val MC = MathContext(16, RoundingMode.HALF_EVEN)
    }
}

enum class Op(val glyph: String) {
    Add("+"), Sub("−"), Mul("×"), Div("÷")
}

sealed interface Action {
    data class Digit(val d: Int) : Action
    data object Dot : Action
    data object Clear : Action
    data object Backspace : Action
    data object PlusMinus : Action
    data object Percent : Action
    data class Operator(val op: Op) : Action
    data object Equals : Action
}

/** Format a BigDecimal as a calculator-friendly string. */
internal fun BigDecimal.prettyString(): String {
    val stripped = stripTrailingZeros()
    val plain = if (stripped.scale() < 0) {
        stripped.setScale(0).toPlainString()
    } else {
        stripped.toPlainString()
    }
    // Cap visible length: switch to scientific if it would overflow the display.
    return if (plain.replace("-", "").replace(".", "").length > 12) {
        toEngineering(stripped)
    } else plain
}

private fun toEngineering(value: BigDecimal): String {
    // Compact scientific notation, e.g. "1.2345e9"
    val s = value.round(MathContext(10, RoundingMode.HALF_EVEN)).toString()
    return s.replace("E", "e")
}
