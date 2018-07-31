package com.himanshurawat.notes.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

            context.toast("Reboot Completed")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            doAsync {
                val noteDatabase = NoteDatabase.getInstance(context)
                val noteDao = noteDatabase.getNoteDao()
                val notes = noteDao.getAllNotesForRebootReceiver()
                uiThread {
                    for(note in notes){
                        if(note.isNotificationSet){

                            val noteIntent = Intent(context.applicationContext
                                    ,NotificationReceiver::class.java)

                            noteIntent.putExtra(Constant.NOTE_ID,note.id)

                            val notePendingIntent = PendingIntent.getBroadcast(context.applicationContext
                                    ,note.id.toInt()
                                    ,noteIntent,PendingIntent.FLAG_UPDATE_CURRENT)
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = note.notification

                            val yy = calendar.get(Calendar.YEAR)
                            val mm = calendar.get(Calendar.MONTH)
                            val dd = calendar.get(Calendar.DATE)
                            val hh = calendar.get(Calendar.HOUR_OF_DAY)
                            val mn = calendar.get(Calendar.MINUTE)

                            alarmManager.set(AlarmManager.RTC
                                    ,getNotificationTime(yy,mm,dd,hh,mn)
                                    ,notePendingIntent)
                        }
                    }
                }
            }



    }


    private fun getNotificationTime(year: Int,month: Int,day: Int, hour: Int,min: Int): Long{
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year,month,day,hour,min)
        return calendar.timeInMillis
    }


}
