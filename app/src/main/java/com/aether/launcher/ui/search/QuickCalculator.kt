package com.aether.launcher.ui.search

/** Tiny shunting-yard calculator so typing "12*7+3" in the drawer search bar just works, like a real launcher search. */
object QuickCalculator {
    private val expressionPattern = Regex("""^[\s0-9+\-*/().]+$""")

    fun tryEvaluate(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || !expressionPattern.matches(trimmed) || trimmed.none { it.isDigit() }) return null
        return runCatching { evaluate(trimmed) }.getOrNull()
    }

    private fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        val output = ArrayDeque<Double>()
        val ops = ArrayDeque<Char>()

        fun apply() {
            val b = output.removeLast(); val a = output.removeLast()
            output.addLast(
                when (ops.removeLast()) {
                    '+' -> a + b; '-' -> a - b; '*' -> a * b; '/' -> a / b
                    else -> throw IllegalStateException()
                }
            )
        }

        fun precedence(c: Char) = if (c == '+' || c == '-') 1 else 2

        for (token in tokens) {
            when {
                token == "(" -> ops.addLast('(')
                token == ")" -> {
                    while (ops.isNotEmpty() && ops.last() != '(') apply()
                    if (ops.isNotEmpty()) ops.removeLast()
                }
                token.length == 1 && token[0] in "+-*/" -> {
                    while (ops.isNotEmpty() && ops.last() != '(' && precedence(ops.last()) >= precedence(token[0])) apply()
                    ops.addLast(token[0])
                }
                else -> output.addLast(token.toDouble())
            }
        }
        while (ops.isNotEmpty()) apply()
        return output.single()
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens += expr.substring(start, i)
                }
                c in "+-*/()" -> { tokens += c.toString(); i++ }
                else -> i++
            }
        }
        return tokens
    }
}
