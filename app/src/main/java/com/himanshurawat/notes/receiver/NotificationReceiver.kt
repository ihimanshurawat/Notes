package com.himanshurawat.notes.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.himanshurawat.notes.R
import com.himanshurawat.notes.activity.AddNote
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.dao.NoteDao
import com.himanshurawat.notes.utils.Constant
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getLongExtra(Constant.NOTE_ID,-1)

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(Constant.NOTIFICATION_CHANNEL_ID
                    , Constant.NOTES_NOTIFICATION_CHANNEL
                    , NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(context,Constant.NOTIFICATION_CHANNEL_ID)

        val database: NoteDatabase = NoteDatabase.getInstance(context)

        doAsync {
            val noteDao: NoteDao = database.getNoteDao()
            val noteEntity = noteDao.getNoteByIdForNotification(noteId)
            uiThread {
                notificationBuilder.setContentTitle(noteEntity.title)
                notificationBuilder.setSmallIcon(R.drawable.square_notification)
                notificationBuilder.setContentText(noteEntity.description)
                notificationBuilder.color = ContextCompat.getColor(context,R.color.colorPrimaryDark)
                val notificationIntent = Intent(context.applicationContext, AddNote::class.java)
                notificationIntent.putExtra(Constant.GET_NOTES,noteId)
                val pendingIntent :PendingIntent = PendingIntent.getActivity(context.applicationContext
                        ,noteId.toInt()
                        ,notificationIntent
                        ,PendingIntent.FLAG_ONE_SHOT)
                notificationBuilder.setContentIntent(pendingIntent)
                notificationBuilder.setAutoCancel(true)
                val notification:Notification = notificationBuilder.build()
                notificationManager.notify(noteId.toInt(),notification)
            }
        }

    }
}
