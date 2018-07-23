package com.himanshurawat.notes.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
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
    private var noteId:Long = -1L
    private lateinit var title: String
    private lateinit var description: String

    private val observer:Observer<NoteEntity?> = Observer {
        if (it != null) {
            addNoteViewModel.setTitle(it.title)
            addNoteViewModel.setDescription(it.description)
            noteEntity = it
        }
    }

    private val noteIdObserver: Observer<Long?> = Observer {
        if(it != null){
            noteId = it
            invalidateOptionsMenu()
        }
    }

    private lateinit var addNoteViewModel: AddNoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        setSupportActionBar(activity_add_note_toolbar)

        //Setting Title and Enabling Home Up
        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)
        ab?.setDisplayHomeAsUpEnabled(true)

        //Intent
        noteIntent = intent
        //View Model
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        addNoteViewModel = ViewModelProviders.of(this).get(AddNoteViewModel::class.java)
        noteId = noteIntent.getLongExtra(Constant.GET_NOTES,-1L)

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
            R.id.add_note_menu_save ->{
                if(noteId == -1L) {
                    //Setting NoteId to 0 to Auto Increment
                    noteId = 0
                    updateDatabase()
                }else{
                    updateDatabase()
                }
            }
            R.id.add_note_menu_delete ->{

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
            R.id.add_note_menu_notification ->{

                val userPref = application.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)
                val is24H = userPref.getBoolean(Constant.USER_PREF,false)

                val calendar = Calendar.getInstance()

                if(calendar != null) {

                    var yy = calendar.get(Calendar.YEAR)
                    var mm = calendar.get(Calendar.MONTH)
                    var dd = calendar.get(Calendar.DAY_OF_MONTH)
                    var hh = calendar.get(Calendar.HOUR_OF_DAY)
                    var mn = calendar.get(Calendar.MINUTE);

                    val datePickerDialog = DatePickerDialog(AddNote@ this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                        yy = year
                        mm = month
                        dd = dayOfMonth

                    },yy,mm,dd)

                    datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE,"OK",DialogInterface.OnClickListener { dialog, which ->
                        val timePickerDialog  = TimePickerDialog(AddNote@this,TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                            hh = hourOfDay
                            mn = minute


                        },hh,mn,is24H)
                        timePickerDialog.show()

                        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE,"OK",DialogInterface.OnClickListener { dialog, which ->
                            toast("Jingalala")
                        })


                    })

                    datePickerDialog.show()




                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Hide Delete From Menu If It's New Note
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if(noteId == -1L){
            menu.findItem(R.id.add_note_menu_delete).isVisible = false
            menu.findItem(R.id.add_note_menu_notification).isVisible = false
        }else{
            menu.findItem(R.id.add_note_menu_delete).isVisible = true
            menu.findItem(R.id.add_note_menu_notification).isVisible = true
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
            viewModel.getNoteById(noteId.toLong()).removeObserver {observer}
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


    private fun updateDatabase():Boolean{
        title = activity_add_note_title_edit_text.text.trim().toString()
        description = activity_add_note_description_edit_text.text.trim().toString()

        //When Title is Empty but Description Isn't
        if (title == "" && description != "") {

            //NoteEntity Object
            val preTitle = description.split(" ")

            val note = NoteEntity(noteId,preTitle[0],
                    description, getDateTime())

            viewModel.addNote(note).observe(this,noteIdObserver)
            toast("Note Updated")

            return true

            //When Title and Description are not Empty
        } else if (title != "" || description != "") {

            val note = NoteEntity(noteId, title,
                    description, getDateTime())

            viewModel.addNote(note).observe(this,noteIdObserver)
            toast("Note Updated")
            return true

            //When Title is Not Empty
        } else if (title != "" && description == "") {

            val note = NoteEntity(noteId, title,
                    "", getDateTime())

            viewModel.addNote(note).observe(this,noteIdObserver)
            toast("Note Updated")
            return true

        }

        //When Title and Description are Empty
        else {
            toast("Add Something to Save")
        }
        return true
    }


}
