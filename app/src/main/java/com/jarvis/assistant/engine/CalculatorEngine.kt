package com.jarvis.assistant.engine

import java.text.DecimalFormat

object CalculatorEngine {

    private val decimalFormat = DecimalFormat("#.######")

    fun evaluate(query: String): String? {
        val cleaned = query.lowercase().trim()
            .replace("jarvis", "")
            .replace("what is", "")
            .replace("calculate", "")
            .replace("solve", "")
            .replace("kitna hota hai", "")
            .replace("kitna hoga", "")
            .trim()

        // 1. Percentage check: "20 percent of 500" or "20% of 500" or "500 ka 20 percent"
        val percentRegex1 = Regex("""(\d+(?:\.\d+)?)\s*(?:percent|%)\s*(?:of|ka)\s*(\d+(?:\.\d+)?)""")
        percentRegex1.find(cleaned)?.let {
            val p = it.groupValues[1].toDoubleOrNull() ?: return null
            val total = it.groupValues[2].toDoubleOrNull() ?: return null
            val result = (p / 100.0) * total
            return decimalFormat.format(result)
        }

        val percentRegex2 = Regex("""(\d+(?:\.\d+)?)\s*(?:ka)\s*(\d+(?:\.\d+)?)\s*(?:percent|%)""")
        percentRegex2.find(cleaned)?.let {
            val total = it.groupValues[1].toDoubleOrNull() ?: return null
            val p = it.groupValues[2].toDoubleOrNull() ?: return null
            val result = (p / 100.0) * total
            return decimalFormat.format(result)
        }

        // 2. Normalize spoken math terms to standard operators
        var expr = cleaned
            .replace("plus", "+")
            .replace("add", "+")
            .replace("minus", "-")
            .replace("subtract", "-")
            .replace("times", "*")
            .replace("multiplied by", "*")
            .replace("multiply", "*")
            .replace("guna", "*")
            .replace("into", "*")
            .replace("x", "*")
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("divide", "/")
            .replace("bhaag", "/")
            .replace("over", "/")
            .replace("power", "^")
            .replace("raised to", "^")

        // Keep only numbers, operators, dots, and parens
        expr = expr.filter { it in "0123456789+-*/^(). " }.trim()
        if (expr.isEmpty() || !expr.any { it in "+-*/^" }) {
            return null
        }

        return try {
            val res = evaluateSimpleExpression(expr)
            decimalFormat.format(res)
        } catch (e: Exception) {
            null
        }
    }

    private fun evaluateSimpleExpression(expr: String): Double {
        // Tokenize
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            if (ch.isWhitespace()) {
                i++
                continue
            }
            if (ch in "+-*/^()") {
                tokens.add(ch.toString())
                i++
            } else if (ch.isDigit() || ch == '.') {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
            } else {
                i++
            }
        }

        if (tokens.isEmpty()) throw IllegalArgumentException("Empty expression")

        // Parse with simple recursive descent or shunting-yard
        return evaluateTokens(tokens)
    }

    private fun evaluateTokens(tokens: List<String>): Double {
        // Binary operation fallback for simple expressions like "25 * 8" or "100 / 4"
        val values = ArrayDeque<Double>()
        val ops = ArrayDeque<Char>()

        fun applyOp(op: Char, b: Double, a: Double): Double {
            return when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> {
                    if (b == 0.0) throw ArithmeticException("Division by zero")
                    a / b
                }
                '^' -> Math.pow(a, b)
                else -> 0.0
            }
        }

        fun precedence(op: Char): Int {
            return when (op) {
                '+', '-' -> 1
                '*', '/' -> 2
                '^' -> 3
                else -> -1
            }
        }

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            val num = token.toDoubleOrNull()
            if (num != null) {
                values.addLast(num)
            } else if (token == "(") {
                ops.addLast('(')
            } else if (token == ")") {
                while (ops.isNotEmpty() && ops.last() != '(') {
                    values.addLast(applyOp(ops.removeLast(), values.removeLast(), values.removeLast()))
                }
                if (ops.isNotEmpty() && ops.last() == '(') {
                    ops.removeLast()
                }
            } else if (token.length == 1 && token[0] in "+-*/^") {
                val op = token[0]
                while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(op)) {
                    values.addLast(applyOp(ops.removeLast(), values.removeLast(), values.removeLast()))
                }
                ops.addLast(op)
            }
            i++
        }

        while (ops.isNotEmpty()) {
            values.addLast(applyOp(ops.removeLast(), values.removeLast(), values.removeLast()))
        }

        return if (values.isNotEmpty()) values.last() else throw IllegalArgumentException("Invalid expression")
    }
}
