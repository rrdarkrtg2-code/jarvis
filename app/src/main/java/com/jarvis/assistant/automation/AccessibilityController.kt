package com.jarvis.assistant.automation

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityController {

    private var serviceRef: AccessibilityService? = null

    fun setServiceInstance(service: AccessibilityService?) {
        serviceRef = service
    }

    fun isServiceEnabled(): Boolean = serviceRef != null

    fun performGlobal(action: Int): Boolean {
        val service = serviceRef ?: return false
        return service.performGlobalAction(action)
    }

    fun goHome(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_HOME)
    fun goBack(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_BACK)
    fun showRecents(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_RECENTS)
    fun takeScreenshot(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)

    fun scroll(forward: Boolean): Boolean {
        val service = serviceRef ?: return false
        val root = service.rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root) ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return scrollableNode.performAction(action)
    }

    fun clickByText(text: String): String {
        val service = serviceRef ?: return "Accessibility automation is not enabled. Please enable J.A.R.V.I.S. in Accessibility Settings."
        val root = service.rootInActiveWindow ?: return "Could not access the current screen content."

        // Safety check: Avoid clicking in known sensitive or banking / authentication packages
        val packageName = root.packageName?.toString()?.lowercase() ?: ""
        if (isSensitivePackage(packageName)) {
            return "Action blocked by safety policy: J.A.R.V.I.S. does not automate actions inside sensitive financial, banking, or credential screens."
        }

        val matchingNodes = root.findAccessibilityNodeInfosByText(text)
        if (matchingNodes.isNullOrEmpty()) {
            return "Could not find any button or element with text '$text' on screen."
        }

        // Find the most clickable node
        for (node in matchingNodes) {
            var current: AccessibilityNodeInfo? = node
            while (current != null) {
                if (current.isClickable) {
                    current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return "Tapped '$text'."
                }
                current = current.parent
            }
        }

        // If not directly clickable, attempt click on first matching node
        matchingNodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return "Tapped '$text'."
    }

    fun readVisibleScreen(): String {
        val service = serviceRef ?: return "Accessibility service is not enabled."
        val root = service.rootInActiveWindow ?: return "Unable to read current screen content."

        val packageName = root.packageName?.toString()?.lowercase() ?: ""
        if (isSensitivePackage(packageName)) {
            return "Screen content hidden for privacy (detected sensitive application)."
        }

        val textCollector = mutableListOf<String>()
        collectText(root, textCollector)

        if (textCollector.isEmpty()) {
            return "The current screen does not contain accessible readable text."
        }

        val summary = textCollector.distinct().take(15).joinToString("; ")
        return "On screen: $summary"
    }

    private fun collectText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty() && text.length > 1 && !node.isPassword) {
            list.add(text)
        } else if (!desc.isNullOrEmpty() && desc.length > 1 && !node.isPassword) {
            list.add(desc)
        }

        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), list)
        }
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val result = findScrollableNode(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun isSensitivePackage(pkg: String): Boolean {
        val sensitiveKeywords = listOf("bank", "paytm", "gpay", "phonepe", "authenticator", "password", "wallet", "binance", "crypto", "cred")
        return sensitiveKeywords.any { pkg.contains(it) }
    }
}
