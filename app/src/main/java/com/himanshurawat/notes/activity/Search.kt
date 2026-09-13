package com.himanshurawat.notes.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.himanshurawat.notes.R
import com.himanshurawat.notes.adapter.SearchItemAdapter
import com.himanshurawat.notes.databinding.ActivitySearchBinding
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.SearchViewModel

class Search : AppCompatActivity(), SearchView.OnQueryTextListener, SearchItemAdapter.OnSearchItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchItemAdapter
    private lateinit var searchViewModel: SearchViewModel

    override fun onItemClick(id: Long) {
        val intent = Intent(this, AddNote::class.java)
        intent.putExtra(Constant.GET_NOTES, id)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        adapter = SearchItemAdapter(this, arrayListOf(), this)

        // Setting Up Recycler View
        binding.contentSearchLayout.contentSearchRecyclerView.adapter = adapter
        binding.contentSearchLayout.contentSearchRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        searchViewModel.getAllNotes().observe(this, Observer { notes ->
            if (notes != null) {
                adapter.addSearchList(notes)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        if (menu != null) {
            val searchItem = menu.findItem(R.id.search_menu_search)
            val search = searchItem?.actionView as? SearchView
            search?.apply {
                isIconified = false
                setIconifiedByDefault(true)
                maxWidth = Integer.MAX_VALUE
                setOnQueryTextListener(this@Search)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            adapter.filterSearch(newText)
        }
        return true
    }
}

