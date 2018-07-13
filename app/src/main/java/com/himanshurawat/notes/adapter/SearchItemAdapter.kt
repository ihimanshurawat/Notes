package com.himanshurawat.notes.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity

class SearchItemAdapter(var searchItemList:List<NoteEntity>): RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder>(){

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

                if (items.title.toLowerCase().contains(getSearchString().toLowerCase()) ||
                        items.description.toLowerCase().contains(getSearchString().toLowerCase()) ||
                        items.date.toLowerCase().contains(getSearchString().toLowerCase())){
                        dataList.add(items)
                }
                if(dataList.size > 0){
                    filteredItemList = dataList;
                }
            }
            notifyDataSetChanged()

        }else{

            filteredItemList = arrayListOf()
            notifyDataSetChanged()
        }
    }






//    override fun getFilter(): Filter {
//
//        return object : Filter(){
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val charString = constraint.toString()
//                if(charString.isEmpty()){
//                    filteredItemList = searchItemList
//                    searchString = ""
//                }else {
//                    val filteredList = ArrayList<NoteEntity>()
//                    searchString = charString
//
//                    for (items in searchItemList) {
//                        if (items.title.toLowerCase().contains(charString.toLowerCase()) ||
//                                items.description.toLowerCase().contains(charString.toLowerCase()) ||
//                                items.date.toLowerCase().contains(charString.toLowerCase())) {
//                            filteredList.add(items)
//                        }
//                    }
//                    filteredItemList = filteredList
//                }
//
//                val filterResult = FilterResults()
//                filterResult.values = filteredItemList
//                return filterResult
//
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                if(results != null){
//                    filteredItemList = results.values as MutableList<NoteEntity>
//                    notifyDataSetChanged()
//                }
//            }
//
//        }
//
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {

        return SearchItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_item_view,parent,false))
    }

    override fun getItemCount(): Int {

        return filteredItemList.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val searchItem = filteredItemList[position]
        if(!searchString.equals("")) {
            holder.titleText.text = searchItem.title
            holder.dateText.text = searchItem.date
            holder.descriptionText.text = searchItem.description
        }

    }


    class SearchItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var titleText = itemView.findViewById<TextView>(R.id.note_item_view_title_text_view)
        var descriptionText = itemView.findViewById<TextView>(R.id.note_item_view_description_text_view)
        var dateText = itemView.findViewById<TextView>(R.id.note_item_view_date_text_view)
    }

    fun addSearchList(list:List<NoteEntity>){
        this.searchItemList = list
    }



}