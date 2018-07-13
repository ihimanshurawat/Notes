package com.himanshurawat.notes.activity

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.himanshurawat.notes.R
import com.himanshurawat.notes.adapter.SearchItemAdapter
import com.himanshurawat.notes.viewmodel.SearchViewModel

import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.content_search.*

class Search : AppCompatActivity(), SearchView.OnQueryTextListener {

    lateinit var adapter: SearchItemAdapter
    lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar)

        adapter = SearchItemAdapter(arrayListOf())

        //Setting Up Recycler View
        content_search_recycler_view.adapter = adapter
        content_search_recycler_view.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false)

        searchViewModel = SearchViewModel(this.application)

        searchViewModel.getAllNotes().observe(this, Observer {
            if(it != null) {
                adapter.addSearchList(it)
                adapter.notifyDataSetChanged()
            }
        })


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)
        if(menu!=null) {
            val search: SearchView? = menu.findItem(R.id.searchIcon).actionView as SearchView
            if(search!=null) {
                search.isIconified = false
                search.setIconifiedByDefault(true)
                search.maxWidth = Integer.MAX_VALUE
                search.setOnQueryTextListener(this)
            }

        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        return true
    }


    override fun onQueryTextSubmit(query: String?): Boolean {

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            adapter.filterSearch(newText)
        }



        return true
    }



}
