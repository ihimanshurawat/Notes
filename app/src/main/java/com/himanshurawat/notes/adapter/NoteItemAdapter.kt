package com.himanshurawat.notes.adapter

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import java.text.SimpleDateFormat
import java.util.*


class NoteItemAdapter(val context: Context,var noteList:List<NoteEntity>,var listener: OnItemClickListener):
        RecyclerView.Adapter<NoteItemAdapter.NoteViewHolder>() {

    private val userPref: SharedPreferences = context.applicationContext.getSharedPreferences(Constant.USER_PREF,Context.MODE_PRIVATE)

    interface OnItemClickListener{
        fun onNoteSelected(noteId: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {

        return NoteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_item_view,parent,false))
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note:NoteEntity? = noteList[holder.adapterPosition]
        if(note != null){
            holder.titleText.text = note.title
            holder.descriptionText.text = note.description
            holder.dateText.text = getDateTime(note.date)
            if(note.isNotificationSet){
                val currentTime = System.currentTimeMillis()
                val notificationTime = note.notification

                holder.clockImage.visibility = View.VISIBLE
                holder.notificationText.visibility = View.VISIBLE

                if(notificationTime > currentTime){
                    holder.clockImage.setColorFilter(ContextCompat.getColor(context,R.color.colorAccent))
                    holder.notificationText.setTextColor(ContextCompat.getColor(context,R.color.colorAccent))
                    holder.notificationText.text = getNotificationDate(notificationTime)
                }else{
                    holder.clockImage.setColorFilter(ContextCompat.getColor(context,R.color.colorDate))
                    holder.notificationText.setTextColor(ContextCompat.getColor(context,R.color.colorDate))
                    holder.notificationText.text = getNotificationDate(notificationTime)
                }
            }else{
                holder.clockImage.visibility = View.INVISIBLE
                holder.notificationText.visibility = View.INVISIBLE
            }
            holder.bind(note.id,listener)
        }
    }

    fun addNotes(note:List<NoteEntity>){
        this.noteList = note
        notifyDataSetChanged()
    }


    class NoteViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var titleText = itemView.findViewById<TextView>(R.id.note_item_view_title_text_view)
        var descriptionText = itemView.findViewById<TextView>(R.id.note_item_view_description_text_view)
        var dateText = itemView.findViewById<TextView>(R.id.note_item_view_date_text_view)
        var clockImage = itemView.findViewById<ImageView>(R.id.note_item_view_clock_image_view)
        var notificationText = itemView.findViewById<TextView>(R.id.note_item_view_notification_text_view)

        fun bind(noteId: Long,listener:OnItemClickListener){
            itemView.setOnClickListener({
                listener.onNoteSelected(noteId)
            })
        }

    }




    //Returns Time String
    private fun getDateTime(timeInMillis: Long):String {
        val now = Date(timeInMillis)
        lateinit var dateFormatter: SimpleDateFormat
        if(userPref.getBoolean(Constant.IS_24_HOUR_FORMAT,false)){
            dateFormatter = SimpleDateFormat("HH:mm",Locale.getDefault())
        }else {
            dateFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        }

//        if(timeInMillis < currentTime && timeInMillis >= (currentTime - Constant.TODAY)){
//            return "Today, "+dateFormatter.format(now)
//        }else if(timeInMillis < (currentTime-Constant.TODAY)&& timeInMillis >= (currentTime-Constant.YESTERDAY)){
//            return "Yesterday, "+dateFormatter.format(now)
//        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        return "$date ${getMonth(month)}, ${dateFormatter.format(now)}"
    }


    private fun getNotificationDate(timeInMillis: Long): String{
        val now = Date(timeInMillis)
        lateinit var dateFormatter: SimpleDateFormat
        if(userPref.getBoolean(Constant.IS_24_HOUR_FORMAT,false)){
            dateFormatter = SimpleDateFormat("HH:mm",Locale.getDefault())
        }else {
            dateFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        return "$date ${getMonth(month)}, ${dateFormatter.format(now)}"
    }

    private fun getMonth(month: Int): String{
        when(month){
            Calendar.JANUARY ->{
                return "January"
            }
            Calendar.FEBRUARY ->{
                return "February"
            }
            Calendar.MARCH ->{
                return "March"
            }
            Calendar.APRIL ->{
                return "April"
            }
            Calendar.MAY ->{
                return "May"
            }
            Calendar.JUNE ->{
                return "June"
            }
            Calendar.JULY ->{
                return "July"
            }
            Calendar.AUGUST ->{
                return "August"
            }
            Calendar.SEPTEMBER ->{
                return "September"
            }
            Calendar.OCTOBER ->{
                return "October"
            }
            Calendar.NOVEMBER ->{
                return "November"
            }
            Calendar.DECEMBER ->{
                return "December"
            }
        }
        return ""
    }




}