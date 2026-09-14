package com.himanshurawat.notesapp
 
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
import com.himanshurawat.notesapp.activity.AddNote
import com.himanshurawat.notesapp.activity.Search
import com.himanshurawat.notesapp.adapter.NoteItemAdapter
import com.himanshurawat.notesapp.adapter.SwipeDeleteCallback
import com.himanshurawat.notesapp.databinding.ActivityMainBinding
import com.himanshurawat.notesapp.db.entity.NoteEntity
import com.himanshurawat.notesapp.utils.Constant
import com.himanshurawat.notesapp.viewmodel.NoteViewModel

class MainActivity : AppCompatActivity(), NoteItemAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var userPref: SharedPreferences

    override fun showUndoSnackBar(note: NoteEntity?) {
        val snackbar: Snackbar = Snackbar.make(binding.root, R.string.deleting, Snackbar.LENGTH_LONG)
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
        val noteAdapter = NoteItemAdapter(this, arrayListOf(), this)

        noteViewModel.getNotes().observe(this, Observer { notes ->
            if (notes != null) {
                noteAdapter.addNotes(notes)
                if (noteAdapter.itemCount > 0) {
                    binding.contentMainLayout.contentMainEmptyNotesImageView.visibility = View.GONE
                } else {
                    binding.contentMainLayout.contentMainEmptyNotesImageView.visibility = View.VISIBLE
                }
            }
        })

        binding.contentMainLayout.noteRecyclerView.adapter = noteAdapter
        binding.contentMainLayout.noteRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        val itemTouchHelper = ItemTouchHelper(SwipeDeleteCallback(noteAdapter))
        itemTouchHelper.attachToRecyclerView(binding.contentMainLayout.noteRecyclerView)
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
        val intent = Intent(this, AddNote::class.java)
        intent.putExtra(Constant.GET_NOTES, noteId)
        startActivity(intent)
    }

    override fun onItemSwiped(note: NoteEntity) {
        noteViewModel.deleteNote(note)
    }
}

