package com.himanshurawat.notesapp.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.himanshurawat.notesapp.db.NoteDatabase
import com.himanshurawat.notesapp.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val database = NoteDatabase.getInstance(context)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val noteDao = database.getNoteDao()
                val notes = noteDao.getAllNotesForRebootReceiver()
                val currentTime = System.currentTimeMillis()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                for (note in notes) {
                    if (note.isNotificationSet && note.notification > currentTime) {
                        val noteIntent = Intent(context.applicationContext, NotificationReceiver::class.java).apply {
                            putExtra(Constant.NOTE_ID, note.id)
                        }

                        val notePendingIntent = PendingIntent.getBroadcast(
                            context.applicationContext,
                            note.id.toInt(),
                            noteIntent,
                            flags
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                note.notification,
                                notePendingIntent
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                note.notification,
                                notePendingIntent
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

