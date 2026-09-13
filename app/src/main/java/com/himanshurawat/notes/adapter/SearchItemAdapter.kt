package com.himanshurawat.notes.adapter

import android.content.Context
import android.content.SharedPreferences
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import java.text.SimpleDateFormat
import java.util.*

class SearchItemAdapter(
    val context: Context,
    var searchItemList: List<NoteEntity>,
    val listener: OnSearchItemClickListener
) : RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder>() {

    var filteredItemList: List<NoteEntity> = emptyList()
    private var searchString: String = ""

    private val userPref: SharedPreferences =
        context.applicationContext.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)

    fun getSearchString(): String = searchString

    fun setSearchString(searchQuery: String) {
        searchString = searchQuery
    }

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        return SearchItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_view, parent, false)
        )
    }

    override fun getItemCount(): Int = filteredItemList.size

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION || pos >= filteredItemList.size) return
        val searchItem = filteredItemList[pos]
        holder.titleText.text = highlightText(searchString, searchItem.title)
        holder.descriptionText.text = highlightText(searchString, searchItem.description)
        holder.dateText.text = getDateTime(searchItem.date)
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < filteredItemList.size) {
                listener.onItemClick(filteredItemList[currentPos].id)
            }
        }
    }

    class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView = itemView.findViewById(R.id.note_item_view_title_text_view)
        var descriptionText: TextView = itemView.findViewById(R.id.note_item_view_description_text_view)
        var dateText: TextView = itemView.findViewById(R.id.note_item_view_date_text_view)
    }

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
                val spanStart = Math.min(start, originalText.length)
                val spanEnd = Math.min(start + search.length, originalText.length)
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

    // Returns Time String
    private fun getDateTime(timeInMillis: Long): String {
        val currentTime = System.currentTimeMillis()
        val now = Date(timeInMillis)
        val is24h = DateFormat.is24HourFormat(context)
        val dateFormatter = if (is24h) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        }

        if (timeInMillis < currentTime && timeInMillis >= (currentTime - Constant.TODAY)) {
            return "Today, " + dateFormatter.format(now)
        } else if (timeInMillis < (currentTime - Constant.TODAY) && timeInMillis >= (currentTime - Constant.YESTERDAY)) {
            return "Yesterday, " + dateFormatter.format(now)
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        return "$date ${getMonth(month)}, ${dateFormatter.format(now)}"
    }

    private fun getMonth(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "January"
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> ""
        }
    }
}