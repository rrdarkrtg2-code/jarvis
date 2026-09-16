package com.jarvis.assistant

import com.jarvis.assistant.engine.ConfirmationSystem
import com.jarvis.assistant.engine.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskClassificationTest {

    private val confirmationSystem = ConfirmationSystem()

    @Test
    fun testRiskLevels() {
        assertEquals(RiskLevel.LOW, confirmationSystem.determineRiskLevel("open YouTube"))
        assertEquals(RiskLevel.LOW, confirmationSystem.determineRiskLevel("get battery"))

        assertEquals(RiskLevel.MEDIUM, confirmationSystem.determineRiskLevel("create reminder"))

        assertEquals(RiskLevel.HIGH, confirmationSystem.determineRiskLevel("call Rahul"))
        assertEquals(RiskLevel.HIGH, confirmationSystem.determineRiskLevel("send message to Rahul"))
        assertEquals(RiskLevel.HIGH, confirmationSystem.determineRiskLevel("delete all memories"))
        assertEquals(RiskLevel.HIGH, confirmationSystem.determineRiskLevel("tap Continue"))
    }
}
