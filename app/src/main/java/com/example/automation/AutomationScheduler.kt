package com.example.automation

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.Automation
import com.example.data.ScheduleType
import java.util.concurrent.TimeUnit

/** Maps an [Automation]'s schedule onto WorkManager periodic work. */
object AutomationScheduler {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedule(context: Context, automation: Automation) {
        val wm = WorkManager.getInstance(context)
        if (!automation.enabled || automation.schedule.type == ScheduleType.MANUAL) {
            wm.cancelUniqueWork(periodicName(automation.id))
            return
        }
        val schedule = automation.schedule
        val (repeat, unit) = when (schedule.type) {
            ScheduleType.HOURLY -> schedule.intervalHours.toLong().coerceAtLeast(1) to TimeUnit.HOURS
            ScheduleType.DAILY -> 1L to TimeUnit.DAYS
            else -> 7L to TimeUnit.DAYS
        }
        val request = PeriodicWorkRequestBuilder<AutomationWorker>(repeat, unit)
            .setInitialDelay(schedule.initialDelayMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(networkConstraint)
            .setInputData(workDataOf(AutomationWorker.KEY_ID to automation.id))
            .addTag(TAG)
            .build()
        wm.enqueueUniquePeriodicWork(periodicName(automation.id), ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun runNow(context: Context, automationId: String) {
        val request = OneTimeWorkRequestBuilder<AutomationWorker>()
            .setConstraints(networkConstraint)
            .setInputData(workDataOf(AutomationWorker.KEY_ID to automationId))
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("automation-now-$automationId", ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context, automationId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(periodicName(automationId))
    }

    private fun periodicName(id: String) = "automation-$id"

    const val TAG = "navio-automation"
}
