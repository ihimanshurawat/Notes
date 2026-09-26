package com.himanshurawat.notesapp.ui.search.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.himanshurawat.notesapp.R
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.databinding.NoteItemViewBinding
import com.himanshurawat.notesapp.ui.util.DateTimeUtils

class SearchItemAdapter(
    val context: Context,
    var searchItemList: List<NoteEntity>,
    val listener: OnSearchItemClickListener
) : RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder>() {

    var filteredItemList: List<NoteEntity> = emptyList()
    private var searchString: String = ""
    var onFilterResultListener: ((resultCount: Int, query: String) -> Unit)? = null

    fun getSearchString(): String = searchString

    fun filterSearch(query: String) {
        val trimmed = query.trim()
        searchString = trimmed

        if (trimmed.isNotEmpty()) {
            val dataList = mutableListOf<NoteEntity>()
            for (item in searchItemList) {
                if (item.title.contains(trimmed, ignoreCase = true) ||
                    item.description.contains(trimmed, ignoreCase = true)
                ) {
                    dataList.add(item)
                }
            }
            filteredItemList = dataList
        } else {
            filteredItemList = emptyList()
        }
        notifyDataSetChanged()
        onFilterResultListener?.invoke(filteredItemList.size, searchString)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val binding = NoteItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchItemViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredItemList.size

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION || pos >= filteredItemList.size) return
        val searchItem = filteredItemList[pos]

        holder.binding.noteItemViewTitleTextView.text = highlightText(searchString, searchItem.title)
        holder.binding.noteItemViewDescriptionTextView.text = highlightText(searchString, searchItem.description)
        holder.binding.noteItemViewDateTextView.text = DateTimeUtils.formatDateTime(context, searchItem.date)

        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < filteredItemList.size) {
                listener.onItemClick(filteredItemList[currentPos].id)
            }
        }
    }

    class SearchItemViewHolder(val binding: NoteItemViewBinding) : RecyclerView.ViewHolder(binding.root)

    fun addSearchList(list: List<NoteEntity>) {
        this.searchItemList = list
        if (searchString.isNotEmpty()) {
            filterSearch(searchString)
        } else {
            notifyDataSetChanged()
        }
    }

    fun highlightText(search: String?, originalText: String): CharSequence {
        if (!search.isNullOrEmpty()) {
            val highlighted = SpannableString(originalText)
            var start = originalText.indexOf(search, 0, ignoreCase = true)
            while (start >= 0) {
                val spanStart = start.coerceAtMost(originalText.length)
                val spanEnd = (start + search.length).coerceAtMost(originalText.length)
                highlighted.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                    spanStart,
                    spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                start = originalText.indexOf(search, spanEnd, ignoreCase = true)
            }
            return highlighted
        }
        return originalText
    }

    interface OnSearchItemClickListener {
        fun onItemClick(id: Long)
    }
}
