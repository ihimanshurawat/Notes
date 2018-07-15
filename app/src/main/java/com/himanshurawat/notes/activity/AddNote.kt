package com.himanshurawat.notes.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import com.himanshurawat.notes.viewmodel.NoteViewModel
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.AddNoteViewModel
import kotlinx.android.synthetic.main.activity_add_note.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*

class AddNote : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var noteIntent: Intent
    private lateinit var noteEntity: NoteEntity
    private var noteId: Long = -1
    private lateinit var title: String
    private lateinit var description: String

    private val observer:Observer<NoteEntity?> = Observer {
        if (it != null) {
            addNoteViewModel.setTitle(it.title)
            addNoteViewModel.setDescription(it.description)
            noteEntity = it
        }
    }

    private lateinit var addNoteViewModel: AddNoteViewModel

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
        addNoteViewModel = ViewModelProviders.of(this).get(AddNoteViewModel::class.java)
        noteId = noteIntent.getLongExtra(Constant.GET_NOTES,0)

        if(!addNoteViewModel.isFilled){
            if(noteIntent.hasExtra(Constant.GET_NOTES)){
                viewModel.getNoteById(noteId).observe(this, observer)
                addNoteViewModel.isFilled = true
            }
        }


        addNoteViewModel.title.observe(this, Observer { text ->
            activity_add_note_title_edit_text.setText(text)
        })
        addNoteViewModel.description.observe(this, Observer { text ->
            activity_add_note_description_edit_text.setText(text)
        })


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.add_note_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean{

        when(item.itemId){
            R.id.save ->{
                title = activity_add_note_title_edit_text.text.trim().toString()
                description = activity_add_note_description_edit_text.text.trim().toString()

                //When Title is Empty but Description Isn't
                if(title == "" && description != ""){

                    //NoteEntity Object
                    val preTitle = description.split(" ")

                    val note = NoteEntity(noteId,preTitle[0],
                            description, getDateTime())

                    viewModel.addNote(note)
                    finish()
                    return true

                //When Title and Description are not Empty
                }else if(title != "" || description != "") {

                    val note = NoteEntity(noteId,title,
                            description, getDateTime())

                    viewModel.addNote(note)
                    finish()
                    return true

                //When Title is Not Empty
                }else if(title != "" && description == ""){

                    val note = NoteEntity(noteId, title,
                            "", getDateTime())

                    viewModel.addNote(note)
                    finish()
                    return true

                    }

                //When Title and Description are Empty
                else{
                        toast("Add Something to Save")
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


    override fun onStop() {
        super.onStop()
        if(noteIntent.hasExtra(Constant.GET_NOTES)){

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        title = activity_add_note_title_edit_text.text.trim().toString()
        description = activity_add_note_description_edit_text.text.trim().toString()
        if(noteIntent.hasExtra(Constant.GET_NOTES)) {
            viewModel.getNoteById(noteId).removeObserver {observer}
        }

        addNoteViewModel.setTitle(title)
        addNoteViewModel.setDescription(description)
    }



    //Returns Time String
    private fun getDateTime():String {
        val now = Date()
        val dateFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)
        return "$date ${getMonth(month)}, ${dateFormatter.format(now)}"
    }


    private fun getMonth(month: Int): String{
        when(month){
            Calendar.JANUARY ->{
                return "January"
            }
            Calendar.FEBRUARY ->{
                return "February"
            }
            Calendar.MARCH ->{
                return "March"
            }
            Calendar.APRIL ->{
                return "April"
            }
            Calendar.MAY ->{
                return "May"
            }
            Calendar.JUNE ->{
                return "June"
            }
            Calendar.JULY ->{
                return "July"
            }
            Calendar.AUGUST ->{
                return "August"
            }
            Calendar.SEPTEMBER ->{
                return "September"
            }
            Calendar.OCTOBER ->{
                return "October"
            }
            Calendar.NOVEMBER ->{
                return "November"
            }
            Calendar.DECEMBER ->{
                return "December"
            }
        }
        return ""
    }


}
