package com.himanshurawat.notes.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import android.content.ClipData.Item



class NoteItemAdapter(var noteList:List<NoteEntity>,var listener: OnItemClickListener):
        RecyclerView.Adapter<NoteItemAdapter.NoteViewHolder>() {

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
        val note:NoteEntity? = noteList[position]
        if(note != null){
            holder.titleText.text = note.title
            holder.descriptionText.text = note.description
            holder.dateText.text = note.date
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

        fun bind(noteId: Long,listener:OnItemClickListener){
            itemView.setOnClickListener({
                listener.onNoteSelected(noteId)
            })
        }

    }



}