package com.himanshurawat.notesapp.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.himanshurawat.notesapp.R
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.databinding.ActivityMainBinding
import com.himanshurawat.notesapp.ui.add.AddNote
import com.himanshurawat.notesapp.ui.main.adapter.NoteItemAdapter
import com.himanshurawat.notesapp.ui.main.adapter.SwipeDeleteCallback
import com.himanshurawat.notesapp.ui.search.Search
import com.himanshurawat.notesapp.util.Constant

class MainActivity : AppCompatActivity(), NoteItemAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var userPref: SharedPreferences
    private lateinit var noteAdapter: NoteItemAdapter

    override fun showUndoSnackBar(note: NoteEntity?) {
        val snackbar = Snackbar.make(binding.root, R.string.deleting, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.undo) {
            if (note != null) {
                noteViewModel.addNote(note)
            }
        }
        snackbar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        userPref = application.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)

        // Check Whether User Uses 24H format
        val is24H = DateFormat.is24HourFormat(applicationContext)
        userPref.edit().putBoolean(Constant.IS_24_HOUR_FORMAT, is24H).apply()

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddNote::class.java))
        }

        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        noteAdapter = NoteItemAdapter(this, this)

        binding.contentMainLayout.noteRecyclerView.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        val itemTouchHelper = ItemTouchHelper(SwipeDeleteCallback(noteAdapter))
        itemTouchHelper.attachToRecyclerView(binding.contentMainLayout.noteRecyclerView)

        noteViewModel.getNotes().observe(this, Observer { notes ->
            noteAdapter.submitList(notes)
            if (notes.isNullOrEmpty()) {
                binding.contentMainLayout.contentMainEmptyState.visibility = View.VISIBLE
            } else {
                binding.contentMainLayout.contentMainEmptyState.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                startActivity(Intent(this@MainActivity, Search::class.java))
            }
        }
        return true
    }

    override fun onNoteSelected(noteId: Long) {
        val intent = Intent(this, AddNote::class.java).apply {
            putExtra(Constant.GET_NOTES, noteId)
        }
        startActivity(intent)
    }

    override fun onItemSwiped(note: NoteEntity) {
        noteViewModel.deleteNote(note)
    }
}
