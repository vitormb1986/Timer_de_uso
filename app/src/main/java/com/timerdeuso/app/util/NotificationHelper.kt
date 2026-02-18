package com.timerdeuso.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timerdeuso.app.R
import com.timerdeuso.app.receiver.AlarmActionReceiver
import com.timerdeuso.app.ui.main.MainActivity

object NotificationHelper {

    const val CHANNEL_SERVICE = "channel_service"
    const val CHANNEL_WARNING = "channel_warning"
    const val CHANNEL_ALARM = "channel_alarm"

    const val NOTIFICATION_SERVICE_ID = 1
    private const val NOTIFICATION_WARNING_BASE = 1000
    private const val NOTIFICATION_ALARM_BASE = 2000

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "Serviço de Monitoramento",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificação persistente do monitoramento em background"
            setShowBadge(false)
        }

        val warningChannel = NotificationChannel(
            CHANNEL_WARNING,
            "Avisos de Tempo",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Aviso quando 80% do tempo limite foi atingido"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200)
        }

        // Delete and recreate alarm channel to ensure sound setting is applied on existing installs
        manager.deleteNotificationChannel(CHANNEL_ALARM)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarmes de Limite",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarme quando o tempo limite foi atingido"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(alarmSoundUri, alarmAudioAttributes)
        }

        manager.createNotificationChannels(listOf(serviceChannel, warningChannel, alarmChannel))
    }

    fun buildServiceNotification(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle("Timer de Uso")
            .setContentText("Monitorando apps em uso...")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun showWarningNotification(context: Context, packageName: String, appName: String, minutesUsed: Int, limitMinutes: Int) {
        val notificationId = NOTIFICATION_WARNING_BASE + packageName.hashCode().and(0xFFF)

        val notification = NotificationCompat.Builder(context, CHANNEL_WARNING)
            .setContentTitle("Aviso: $appName")
            .setContentText("$minutesUsed min de $limitMinutes min usados (80%)")
            .setSmallIcon(R.drawable.ic_warning)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 200))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) { }
    }

    fun showAlarmNotification(context: Context, packageName: String, appName: String, limitMinutes: Int) {
        val notificationId = NOTIFICATION_ALARM_BASE + packageName.hashCode().and(0xFFF)

        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = "com.timerdeuso.ACTION_SNOOZE"
            putExtra("package_name", packageName)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notificationId, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val silenceIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = "com.timerdeuso.ACTION_SILENCE"
            putExtra("package_name", packageName)
        }
        val silencePending = PendingIntent.getBroadcast(
            context, notificationId + 10000, silenceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeMinutes = PrefsManager.getSnoozeMinutes(context)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setContentTitle("Limite atingido: $appName")
            .setContentText("Você usou $appName por $limitMinutes minutos!")
            .setSmallIcon(R.drawable.ic_alarm)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(alarmSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_snooze, "Adiar ($snoozeMinutes min)", snoozePending)
            .addAction(R.drawable.ic_silence, "Silenciar hoje", silencePending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) { }
    }

    fun dismissAlarmNotification(context: Context, packageName: String) {
        val notificationId = NOTIFICATION_ALARM_BASE + packageName.hashCode().and(0xFFF)
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
