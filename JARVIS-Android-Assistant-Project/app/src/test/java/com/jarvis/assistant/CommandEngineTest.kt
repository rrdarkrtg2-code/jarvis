package com.jarvis.assistant

import com.jarvis.assistant.engine.CommandPattern
import com.jarvis.assistant.engine.LocalCommandEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandEngineTest {

    private val engine = LocalCommandEngine()

    @Test
    fun testBatteryCommands() {
        assertEquals(CommandPattern.GetBattery, engine.parseCommand("battery"))
        assertEquals(CommandPattern.GetBattery, engine.parseCommand("what is my battery percentage"))
        assertEquals(CommandPattern.GetBattery, engine.parseCommand("Jarvis battery kitni hai"))
    }

    @Test
    fun testFlashlightCommands() {
        assertEquals(CommandPattern.ToggleFlashlight(true), engine.parseCommand("turn on flashlight"))
        assertEquals(CommandPattern.ToggleFlashlight(false), engine.parseCommand("turn off flashlight"))
        assertEquals(CommandPattern.ToggleFlashlight(true), engine.parseCommand("torch chalu karo"))
        assertEquals(CommandPattern.ToggleFlashlight(false), engine.parseCommand("torch band karo"))
    }

    @Test
    fun testAppLaunchCommands() {
        val cmd1 = engine.parseCommand("open YouTube") as? CommandPattern.OpenApp
        assertEquals("youtube", cmd1?.appName)

        val cmd2 = engine.parseCommand("WhatsApp kholo") as? CommandPattern.OpenApp
        assertEquals("whatsapp", cmd2?.appName)
    }

    @Test
    fun testMemoryCommands() {
        val cmd = engine.parseCommand("remember that my minecraft project is Sub-Terra") as? CommandPattern.StoreMemory
        assertTrue(cmd?.content?.contains("minecraft project is sub-terra") == true)
    }

    @Test
    fun testNavigationCommands() {
        assertEquals(CommandPattern.GoHome, engine.parseCommand("go home"))
        assertEquals(CommandPattern.GoBack, engine.parseCommand("go back"))
        assertEquals(CommandPattern.ShowRecentApps, engine.parseCommand("show recent apps"))
        assertEquals(CommandPattern.TakeScreenshot, engine.parseCommand("take screenshot"))
    }
}
