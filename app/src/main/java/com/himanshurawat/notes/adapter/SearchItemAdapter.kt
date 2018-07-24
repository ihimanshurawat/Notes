package com.himanshurawat.notes.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import java.text.Normalizer




class SearchItemAdapter(val context: Context,
                        var searchItemList:List<NoteEntity>,
                        val listener: OnSearchItemClickListener): RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder>(){

    var filteredItemList:List<NoteEntity>
    private var searchString: String



    init {
        filteredItemList = searchItemList
        searchString = ""
    }

    fun getSearchString():String {
        return searchString
    }
    fun setSearchString(searchQuery: String){
        searchString = searchQuery
    }


    fun filterSearch(query: String){

        setSearchString(query)

        if(getSearchString() != "" && getSearchString().trim() != ""){
            var dataList = mutableListOf<NoteEntity>()
            for(items in searchItemList){
                //Todo Fix Search
                if (items.title.toLowerCase().contains(getSearchString().toLowerCase()) ||
                        items.description.toLowerCase().contains(getSearchString().toLowerCase()) ||
                        items.date.toString().toLowerCase().contains(getSearchString().toLowerCase())){
                        dataList.add(items)
                }

                    filteredItemList = dataList;

            }
            notifyDataSetChanged()

        }else{

            filteredItemList = arrayListOf()
            notifyDataSetChanged()
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {

        return SearchItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_item_view,parent,false))
    }

    override fun getItemCount(): Int {

        return filteredItemList.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val searchItem = filteredItemList[holder.adapterPosition]
        if(!searchString.equals("")) {
            holder.titleText.text = highlightText(getSearchString(),searchItem.title)
            //Todo Fix Date
            holder.dateText.text = highlightText(getSearchString(),searchItem.date.toString())
            holder.descriptionText.text = highlightText(getSearchString(),searchItem.description)
            holder.item.setOnClickListener({
                listener.onItemClick(filteredItemList[holder.adapterPosition].id)
            })
        }

    }


    class SearchItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var titleText = itemView.findViewById<TextView>(R.id.note_item_view_title_text_view)
        var descriptionText = itemView.findViewById<TextView>(R.id.note_item_view_description_text_view)
        var dateText = itemView.findViewById<TextView>(R.id.note_item_view_date_text_view)
        var item = itemView
    }

    fun addSearchList(list:List<NoteEntity>){
        this.searchItemList = list
    }


    fun highlightText(search: String?, originalText: String): CharSequence {
        if (search != null && !search.equals("", ignoreCase = true)) {
            val normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD).toLowerCase()
            var start = normalizedText.indexOf(search)
            if (start < 0) {
                return originalText
            } else {
                val highlighted = SpannableString(originalText)
                while (start >= 0) {
                    val spanStart = Math.min(start, originalText.length)
                    val spanEnd = Math.min(start + search.length, originalText.length)
                    highlighted.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    start = normalizedText.indexOf(search, spanEnd)
                }
                return highlighted
            }
        }
        return originalText
    }


    interface OnSearchItemClickListener{
        fun onItemClick(id: Long)
    }


}