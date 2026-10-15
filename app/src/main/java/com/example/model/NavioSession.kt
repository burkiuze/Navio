package com.example.model

data class NavioSession(
    val id: String,
    val title: String,
    val lastActivity: String,
    val modelName: String,
    val workspacePath: String,
    val permissionMode: String = "Ask before writes",
    val previewText: String = "",
    val messageCount: Int = 0,
    val status: String = "Idle"
)

enum class PermissionMode(val label: String, val description: String) {
    ASK_BEFORE_WRITES("Ask before writes", "Prompt before file modifications (Default)"),
    AUTO_APPROVE("Auto-approve", "Allow automatic workspace file changes"),
    READ_ONLY("Read-only", "Inspect and search only; reject writes")
}
