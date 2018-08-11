package com.himanshurawat.notes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import com.himanshurawat.notes.activity.AddNote
import com.himanshurawat.notes.activity.Search
import com.himanshurawat.notes.adapter.NoteItemAdapter
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.NoteViewModel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*



private lateinit var noteViewModel: NoteViewModel

private lateinit var userPref: SharedPreferences

private lateinit var firebaseAnalytics: FirebaseAnalytics

class MainActivity : AppCompatActivity(), NoteItemAdapter.OnItemClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        userPref = application.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //Check Whether User Uses 24H format
        val is24H = DateFormat.is24HourFormat(applicationContext)
        userPref.edit().putBoolean(Constant.IS_24_HOUR_FORMAT,is24H).apply()



        fab.setOnClickListener { _ ->

            firebaseAnalytics.logEvent(Constant.ADD_NOTE,null)

            startActivity(Intent(this,AddNote::class.java))
        }

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        val noteAdapter = NoteItemAdapter(this,arrayListOf(),this)

        noteViewModel.getNotes().observe(this, Observer { it->
                if(it != null) {
                    noteAdapter.addNotes(it)
                    if(noteAdapter.itemCount>0){
                        content_main_empty_notes_image_view.visibility = View.GONE
                    }else{
                        content_main_empty_notes_image_view.visibility = View.VISIBLE
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
        when(item.itemId){

            R.id.search ->{

                firebaseAnalytics.logEvent(Constant.SEARCH_ICON_CLICKED,null)
                startActivity(Intent(this@MainActivity, Search::class.java))
            }
        }
        return true;

    }

    override fun onNoteSelected(noteId: Long) {

        val intent = Intent(this,AddNote::class.java)
        intent.putExtra(Constant.GET_NOTES,noteId)
        startActivity(intent)

    }


}
