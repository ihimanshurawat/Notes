package com.himanshurawat.notesapp.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.himanshurawat.notesapp.R
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.databinding.NoteItemViewBinding
import com.himanshurawat.notesapp.ui.util.DateTimeUtils

class NoteItemAdapter(
    val context: Context,
    var listener: OnItemClickListener
) : ListAdapter<NoteEntity, NoteItemAdapter.NoteViewHolder>(NoteDiffCallback) {

    private var recentlyDeletedItem: NoteEntity? = null
    private var recentlyDeletedItemPosition: Int = -1

    interface OnItemClickListener {
        fun onNoteSelected(noteId: Long)
        fun onItemSwiped(note: NoteEntity)
        fun showUndoSnackBar(note: NoteEntity?)
    }

    object NoteDiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION || pos >= currentList.size) return
        val note = getItem(pos) ?: return

        holder.binding.noteItemViewTitleTextView.text = note.title
        holder.binding.noteItemViewDescriptionTextView.text = note.description
        holder.binding.noteItemViewDateTextView.text = DateTimeUtils.formatDateTime(context, note.date)

        if (note.isNotificationSet) {
            val currentTime = System.currentTimeMillis()
            val notificationTime = note.notification

            holder.binding.noteItemViewClockImageView.visibility = View.VISIBLE
            holder.binding.noteItemViewNotificationTextView.visibility = View.VISIBLE

            val colorRes = if (notificationTime > currentTime) R.color.colorAccent else R.color.colorDate
            val color = ContextCompat.getColor(context, colorRes)

            holder.binding.noteItemViewClockImageView.setColorFilter(color)
            holder.binding.noteItemViewNotificationTextView.setTextColor(color)
            holder.binding.noteItemViewNotificationTextView.text =
                DateTimeUtils.formatDateTime(context, notificationTime)
        } else {
            holder.binding.noteItemViewClockImageView.visibility = View.INVISIBLE
            holder.binding.noteItemViewNotificationTextView.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < currentList.size) {
                listener.onNoteSelected(getItem(currentPos).id)
            }
        }
    }

    fun addNotes(notes: List<NoteEntity>?) {
        submitList(notes)
    }

    fun deleteItem(position: Int) {
        if (position in currentList.indices) {
            recentlyDeletedItem = getItem(position)
            recentlyDeletedItemPosition = position
            val note = getItem(position)
            listener.onItemSwiped(note)
            listener.showUndoSnackBar(recentlyDeletedItem)
        }
    }

    class NoteViewHolder(val binding: NoteItemViewBinding) : RecyclerView.ViewHolder(binding.root)
}
