package com.himanshurawat.notes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.himanshurawat.notes.activity.AddNote
import com.himanshurawat.notes.adapter.NoteItemAdapter
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.NoteViewModel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private lateinit var noteViewModel: NoteViewModel

class MainActivity : AppCompatActivity(), NoteItemAdapter.OnItemClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this,AddNote::class.java))
        }

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        val noteAdapter = NoteItemAdapter(arrayListOf(),this)

        noteViewModel.getNotes().observe(this, Observer { it->
                if(it != null) {
                    noteAdapter.addNotes(it)
                    if(noteAdapter.itemCount>0){
                        poweredByImageView.visibility = View.GONE
                    }else{
                        poweredByImageView.visibility = View.VISIBLE
                    }
                }
        })

        noteRecyclerView.adapter = noteAdapter
        noteRecyclerView.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item)

    }

    override fun onNoteSelected(noteId: Long) {
        val intent = Intent(this,AddNote::class.java)
        intent.putExtra(Constant.GET_NOTES,noteId)
        startActivity(intent)
    }

}
