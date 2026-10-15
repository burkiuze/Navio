package com.example.data

import java.util.Calendar
import java.util.Locale

enum class Role { USER, ASSISTANT }

data class Source(val title: String, val url: String)

data class ChatMessage(
    val id: String,
    val role: Role,
    val content: String,
    val sources: List<Source> = emptyList(),
    /** Automations the assistant created while answering this message. */
    val createdAutomationIds: List<String> = emptyList(),
    val isError: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ScheduleType(val id: String) {
    HOURLY("hourly"),
    DAILY("daily"),
    WEEKLY("weekly"),
    MANUAL("manual");

    companion object {
        fun from(id: String?): ScheduleType = entries.firstOrNull { it.id == id } ?: MANUAL
    }
}

data class Schedule(
    val type: ScheduleType,
    /** Hours between runs for [ScheduleType.HOURLY]. */
    val intervalHours: Int = 1,
    val hour: Int = 9,
    val minute: Int = 0,
    /** 1 = Monday … 7 = Sunday, for [ScheduleType.WEEKLY]. */
    val weekday: Int = 1
) {
    fun describe(): String {
        val time = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
        return when (type) {
            ScheduleType.HOURLY -> if (intervalHours <= 1) "Her saat" else "Her $intervalHours saatte bir"
            ScheduleType.DAILY -> "Her gün $time"
            ScheduleType.WEEKLY -> "Her ${WEEKDAYS_TR[(weekday - 1).coerceIn(0, 6)]} $time"
            ScheduleType.MANUAL -> "Elle çalıştırılır"
        }
    }

    /** Milliseconds from [now] until the next run, used as WorkManager's initial delay. */
    fun initialDelayMillis(now: Calendar = Calendar.getInstance()): Long {
        if (type == ScheduleType.HOURLY || type == ScheduleType.MANUAL) return 0L
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (type == ScheduleType.WEEKLY) {
            // Calendar: SUNDAY = 1 … SATURDAY = 7; ours: MONDAY = 1 … SUNDAY = 7.
            val target = if (weekday == 7) Calendar.SUNDAY else weekday + 1
            val diff = (target - next.get(Calendar.DAY_OF_WEEK) + 7) % 7
            next.add(Calendar.DAY_OF_YEAR, diff)
            if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 7)
        } else if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }

    companion object {
        val WEEKDAYS_TR = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
    }
}

data class AutomationRun(
    val id: String,
    val startedAt: Long,
    val output: String,
    val sources: List<Source> = emptyList(),
    val success: Boolean = true
)

data class Automation(
    val id: String,
    val name: String,
    /** Instruction the agent executes on every run. */
    val prompt: String,
    val schedule: Schedule,
    val useWebSearch: Boolean = true,
    val enabled: Boolean = true,
    val runs: List<AutomationRun> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val lastRun: AutomationRun? get() = runs.firstOrNull()
}
