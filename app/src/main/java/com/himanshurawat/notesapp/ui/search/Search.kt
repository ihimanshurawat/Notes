package com.himanshurawat.notesapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.himanshurawat.notesapp.R
import com.himanshurawat.notesapp.databinding.ActivitySearchBinding
import com.himanshurawat.notesapp.ui.add.AddNote
import com.himanshurawat.notesapp.ui.search.adapter.SearchItemAdapter
import com.himanshurawat.notesapp.util.Constant

class Search : AppCompatActivity(), SearchView.OnQueryTextListener, SearchItemAdapter.OnSearchItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchItemAdapter
    private lateinit var searchViewModel: SearchViewModel

    override fun onItemClick(id: Long) {
        val intent = Intent(this, AddNote::class.java).apply {
            putExtra(Constant.GET_NOTES, id)
        }
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

        adapter = SearchItemAdapter(this, arrayListOf(), this).apply {
            onFilterResultListener = { count, query ->
                if (query.isNotEmpty() && count == 0) {
                    binding.contentSearchLayout.contentSearchEmptyTextView.visibility = View.VISIBLE
                } else {
                    binding.contentSearchLayout.contentSearchEmptyTextView.visibility = View.GONE
                }
            }
        }

        binding.contentSearchLayout.contentSearchRecyclerView.apply {
            adapter = this@Search.adapter
            layoutManager = LinearLayoutManager(this@Search, LinearLayoutManager.VERTICAL, false)
        }

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
        adapter.filterSearch(newText ?: "")
        return true
    }
}
