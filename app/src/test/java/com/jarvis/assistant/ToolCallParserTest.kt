package com.jarvis.assistant

import com.jarvis.assistant.ai.ToolCallParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ToolCallParserTest {

    @Test
    fun testExtractMarkdownJsonToolCall() {
        val aiResponse = """
            Certainly. I will launch YouTube for you.
            ```json
            {"tool": "open_app", "arguments": {"app_name": "YouTube"}}
            ```
        """.trimIndent()

        val toolCall = ToolCallParser.extractToolCall(aiResponse)
        assertNotNull(toolCall)
        assertEquals("open_app", toolCall?.name)
        assertEquals("YouTube", toolCall?.arguments?.get("app_name"))
    }

    @Test
    fun testExtractRawJsonToolCall() {
        val aiResponse = """{"tool": "toggle_flashlight", "arguments": {"enable": true}}"""
        val toolCall = ToolCallParser.extractToolCall(aiResponse)
        assertNotNull(toolCall)
        assertEquals("toggle_flashlight", toolCall?.name)
        assertEquals(true, toolCall?.arguments?.get("enable"))
    }
}
