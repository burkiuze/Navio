package com.example.model

data class NavioModel(
    val id: String,
    val name: String,
    val providerId: String,
    val contextWindow: String,
    val isDefault: Boolean = false,
    val supportsTools: Boolean = true,
    val supportsStreaming: Boolean = true,
    val description: String = ""
)

data class NavioProvider(
    val id: String,
    val name: String,
    val protocol: String, // "Anthropic Messages API" or "OpenAI-compatible"
    val baseUrl: String,
    val isConfigured: Boolean,
    val maskedKey: String,
    val models: List<String>
)
