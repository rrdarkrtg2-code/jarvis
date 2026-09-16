package com.jarvis.assistant

import com.jarvis.assistant.engine.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CalculatorTest {

    @Test
    fun testBasicArithmetic() {
        assertEquals("200", CalculatorEngine.evaluate("25 times 8"))
        assertEquals("25", CalculatorEngine.evaluate("100 divided by 4"))
        assertEquals("30", CalculatorEngine.evaluate("10 plus 20"))
        assertEquals("15", CalculatorEngine.evaluate("45 minus 30"))
    }

    @Test
    fun testPercentageCalculations() {
        assertEquals("100", CalculatorEngine.evaluate("20 percent of 500"))
        assertEquals("100", CalculatorEngine.evaluate("20% of 500"))
        assertEquals("100", CalculatorEngine.evaluate("500 ka 20 percent"))
    }

    @Test
    fun testHinglishPhrases() {
        assertEquals("200", CalculatorEngine.evaluate("25 guna 8"))
        assertEquals("25", CalculatorEngine.evaluate("100 bhaag 4"))
    }

    @Test
    fun testNonMathQueries() {
        assertEquals(null, CalculatorEngine.evaluate("Open YouTube"))
        assertEquals(null, CalculatorEngine.evaluate("What is the weather"))
    }
}
