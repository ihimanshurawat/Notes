package com.himanshurawat.notes.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.himanshurawat.notes.R
import com.himanshurawat.notes.activity.AddNote
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.dao.NoteDao
import com.himanshurawat.notes.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(Constant.NOTE_ID, -1)
        if (noteId == -1L) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constant.NOTIFICATION_CHANNEL_ID,
                Constant.NOTES_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val database = NoteDatabase.getInstance(context)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val noteDao: NoteDao = database.getNoteDao()
                val noteEntity = noteDao.getNoteByIdForNotification(noteId)
                if (noteEntity != null) {
                    val notificationBuilder =
                        NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_ID)
                    notificationBuilder.setContentTitle(noteEntity.title)
                    notificationBuilder.setSmallIcon(R.drawable.square_notification)
                    notificationBuilder.setContentText(noteEntity.description)
                    notificationBuilder.color =
                        ContextCompat.getColor(context, R.color.colorPrimaryDark)

                    val notificationIntent = Intent(context.applicationContext, AddNote::class.java).apply {
                        putExtra(Constant.GET_NOTES, noteId)
                    }

                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }

                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context.applicationContext,
                        noteId.toInt(),
                        notificationIntent,
                        flags
                    )
                    notificationBuilder.setContentIntent(pendingIntent)
                    notificationBuilder.setAutoCancel(true)
                    val notification: Notification = notificationBuilder.build()
                    notificationManager.notify(noteId.toInt(), notification)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

