package com.himanshurawat.notes.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.himanshurawat.notes.viewmodel.NoteViewModel
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import kotlinx.android.synthetic.main.activity_add_note.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddNote : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var noteIntent: Intent
    private lateinit var noteEntity: NoteEntity
    private var noteId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        //Setting Title and Enabling Home Up
        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)

        //Intent
        noteIntent = intent
        //View Model
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)

        //Check Whether its Editing or New Note
        if(intent.hasExtra(Constant.GET_NOTES)){
            noteId = intent.getLongExtra(Constant.GET_NOTES,-1)

            viewModel.getNoteById(noteId).observe(this, Observer { note ->

                        titleEditText.setText(note?.title)
                        descriptionEditText.setText(note?.description)

                        if(note != null){
                            //Note Object
                            noteEntity = note
                        }

                    })
            viewModel.updateCount(noteId)
        }else{

        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.add_note_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean{

        when(item.itemId){
            R.id.save ->{
                val title = titleEditText.text.trim().toString()
                val description = descriptionEditText.text.trim().toString()

                if(title == "" && description != ""){
                    //NoteEntity Object
                    val note = NoteEntity(noteId,description[0].toString(),
                            description, getDateTime(), 0)

                    viewModel.addNote(note)
                    finish()
                    return true

                }else if(title != "" || description != "") {

                    val note = NoteEntity(noteId,title,
                            description, getDateTime(), 0)

                    viewModel.addNote(note)
                    finish()
                    return true
                }else
                    if(title != "" && description == ""){

                    val note = NoteEntity(noteId, title,
                            "", getDateTime(),0)

                    viewModel.addNote(note)
                    finish()
                    return true

                    }

                else{
                        toast("Add Something to Note Down")
                }
            }
            R.id.delete ->{
                //Alert Before Delete Using Anko
                alert("Sure you want to Delete?") {
                    title = "Delete Note"
                    yesButton {
                        viewModel.deleteNote(noteEntity)
                        toast("Deleting")
                        finish()
                    }
                    noButton {  }
                }.show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Hide Delete From Menu If It's New Note
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if(!intent.hasExtra(Constant.GET_NOTES)){
            menu.findItem(R.id.delete).isVisible = false
        }
        return true
    }

    //Returns Time String
    private fun getDateTime():String {
        val now = Date()
        val dateFormatter = SimpleDateFormat("EE, d-M-y 'at' h:m a")
        return dateFormatter.format(now)

    }

}
