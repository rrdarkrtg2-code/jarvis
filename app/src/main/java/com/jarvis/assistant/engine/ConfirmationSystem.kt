package com.jarvis.assistant.engine

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class ConfirmationRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val actionName: String,
    val description: String,
    val riskLevel: RiskLevel,
    val onConfirmed: suspend () -> String,
    val onRejected: suspend () -> String = { "Action cancelled." }
)

class ConfirmationSystem {

    fun determineRiskLevel(action: String): RiskLevel {
        val lower = action.lowercase()
        return when {
            lower.contains("call") ||
            lower.contains("send message") ||
            lower.contains("send sms") ||
            lower.contains("text ") ||
            lower.contains("delete") ||
            lower.contains("clear") ||
            lower.contains("reset") ||
            lower.contains("tap ") ||
            lower.contains("click ") -> RiskLevel.HIGH

            lower.contains("remind") ||
            lower.contains("set brightness") ||
            lower.contains("alarm") -> RiskLevel.MEDIUM

            else -> RiskLevel.LOW
        }
    }
}
