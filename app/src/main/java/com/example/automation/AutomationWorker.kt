package com.example.automation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AutomationRun
import com.example.data.NavioAgent
import com.example.data.NavioStore

/** Runs one automation through the agent, stores the result and posts a notification. */
class AutomationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val store = NavioStore.get(applicationContext)
        val automation = store.automation(id)
        if (automation == null) {
            AutomationScheduler.cancel(applicationContext, id)
            return Result.failure()
        }
        val run = NavioAgent(applicationContext).runAutomation(automation)
        store.addRun(id, run)
        notify(automation.name, run, id.hashCode())
        return Result.success()
    }

    @SuppressLint("MissingPermission") // checked at the top of the function
    private fun notify(title: String, run: AutomationRun, notificationId: Int) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_COMPUTER, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = run.output.replace(Regex("[#*`>_]"), "").trim()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_navio)
            .setContentTitle(if (run.success) title else "$title — hata")
            .setContentText(text.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text.take(1500)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val KEY_ID = "automation_id"
        private const val CHANNEL_ID = "navio_automations"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Otomasyonlar", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }
        }
    }
}
