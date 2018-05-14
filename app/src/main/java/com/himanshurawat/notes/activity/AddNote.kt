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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)

        noteIntent = intent
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)

        if(intent.hasExtra(Constant.GET_NOTES)){
            viewModel.getNoteById(intent.
                    getLongExtra(Constant.GET_NOTES,-1)).
                    observe(this, Observer { note ->
                        titleEditText.setText(note?.title)
                        descriptionEditText.setText(note?.description)
                        if(note != null){
                            noteEntity = note
                        }

                    })
            viewModel.updateCount(intent.getLongExtra(Constant.GET_NOTES,-1))
        }else{
            titleEditText.requestFocus()
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputMethodManager.showSoftInput(titleEditText,InputMethodManager.SHOW_IMPLICIT)
        }

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.add_note_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean{

        when(item.itemId){
            R.id.save ->{
                if(titleEditText.text.toString() == "" && descriptionEditText.text.toString() != ""){
                    val note = NoteEntity(intent.getLongExtra(Constant.GET_NOTES,0),
                            descriptionEditText.text.trim()[0].toString(),
                            descriptionEditText.text.trim().toString(),
                            getDateTime(),
                            0)
                    viewModel.addNote(note)
                    finish()
                    return true
                }else if(titleEditText.text.toString() != "" || descriptionEditText.text.toString() != "") {
                    val note = NoteEntity(intent.getLongExtra(Constant.GET_NOTES,0),
                            titleEditText.text.trim().toString(),
                            descriptionEditText.text.trim().toString(),
                            getDateTime(),
                            0
                    )
                    viewModel.addNote(note)
                    finish()
                    return true
                }else if(titleEditText.text.toString() != "" && descriptionEditText.text.toString() == ""){
                    val note = NoteEntity(intent.getLongExtra(Constant.GET_NOTES,0),
                            titleEditText.text.trim().toString(),
                            "",
                            getDateTime(),
                            0
                    )
                    viewModel.addNote(note)
                    finish()
                    return true
                }

                else{
                    toast("Add Something to Note Down")
                }
            }
            R.id.delete ->{

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


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if(!intent.hasExtra(Constant.GET_NOTES)){
            menu.findItem(R.id.delete).isVisible = false
        }
        return true
    }

    private fun getDateTime():String {
        val now = Date()
        val dateFormatter = SimpleDateFormat("EEEE, d-M-y 'at' h:m a")
        return dateFormatter.format(now)

    }

}
