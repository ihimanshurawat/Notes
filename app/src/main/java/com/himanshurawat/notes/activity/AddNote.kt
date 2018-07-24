package com.himanshurawat.notes.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.himanshurawat.notes.viewmodel.NoteViewModel
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.receiver.NotificationReceiver
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.AddNoteViewModel
import kotlinx.android.synthetic.main.activity_add_note.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class AddNote : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    private lateinit var viewModel: NoteViewModel
    private lateinit var noteIntent: Intent
    private lateinit var noteEntity: NoteEntity
    private var noteId:Long = -1L
    private lateinit var title: String
    private lateinit var description: String

    //Flag to Maintain Whether Item Is Begin Deleted or Not
    private var isDeleting = false

    //Notification Variables
    private var yy: Int = 0
    private var mm: Int = 0
    private var dd: Int = 0
    private var hh: Int = 0
    private var mn: Int = 0

    private var isNotificationSet = false

    private val observer:Observer<NoteEntity?> = Observer {
        if (it != null) {

            addNoteViewModel.setTitle(it.title)
            addNoteViewModel.setDescription(it.description)
            noteEntity = it
            isNotificationSet = noteEntity.isNotificationSet
            if(isNotificationSet){
                val calendar:Calendar = Calendar.getInstance()
                calendar.timeInMillis = noteEntity.notification
                yy = calendar.get(Calendar.YEAR)
                mm = calendar.get(Calendar.MONTH)
                dd = calendar.get(Calendar.DAY_OF_MONTH)
                hh = calendar.get(Calendar.HOUR_OF_DAY)
                mn = calendar.get(Calendar.MINUTE)
            }
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
        if(Intent.ACTION_SEND.equals(noteIntent.action) && noteIntent.type != null){
            toast("Working")
            addNoteViewModel.setDescription(noteIntent.getStringExtra(Intent.EXTRA_TEXT))
        }
        noteId = noteIntent.getLongExtra(Constant.GET_NOTES,-1L)



        if(!addNoteViewModel.isFilled){
            if(noteIntent.hasExtra(Constant.GET_NOTES)){
                viewModel.getNoteById(noteId).observe(this, observer)
                addNoteViewModel.isFilled = true
            }
        }


        //Persist Data when Configuration Changes
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
                    displayToast("Note Created")
                }else{
                    updateDatabase()
                    displayToast("Note Updated")
                }
            }
            R.id.add_note_menu_delete ->{

                //Alert Before Delete Using Anko
                alert("Sure you want to Delete?") {
                    title = "Delete Note"
                    yesButton {
                        viewModel.deleteNote(noteEntity)
                        displayToast("Deleting")
                        isDeleting = true
                        finish()
                    }
                    noButton {  }
                }.show()

            }
            R.id.add_note_menu_notification ->{
                showDatePicker()


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
        if(noteId != -1L && !isDeleting){
            updateDatabase()
            displayToast("Saving Changes")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!isDeleting) {
            title = activity_add_note_title_edit_text.text.trim().toString()
            description = activity_add_note_description_edit_text.text.trim().toString()
            if (noteIntent.hasExtra(Constant.GET_NOTES)) {
                viewModel.getNoteById(noteId).removeObserver { observer }
            }

            addNoteViewModel.setTitle(title)
            addNoteViewModel.setDescription(description)
        }

    }


    private fun updateDatabase():Boolean{
        title = activity_add_note_title_edit_text.text.trim().toString()
        description = activity_add_note_description_edit_text.text.trim().toString()

        //When Title is Empty but Description Isn't
        if (title == "" && description != "") {

            //NoteEntity Object
            val preTitle = description.split(" ")

            val note: NoteEntity
            if(isNotificationSet){
                note = NoteEntity(noteId,preTitle[0],
                        description, getSystemTimeInMillis(),getNotificationTime(yy,mm,dd,hh,mn),isNotificationSet)
            }else{
                note = NoteEntity(noteId,preTitle[0],
                        description, getSystemTimeInMillis())
            }




            viewModel.addNote(note).observe(this,noteIdObserver)

            return true

            //When Title and Description are not Empty
        } else if (title != "" || description != "") {


            val note:NoteEntity
            if(isNotificationSet){
                note = NoteEntity(noteId,title,description,getSystemTimeInMillis(),getNotificationTime(yy,mm,dd,hh,mn),isNotificationSet)
            }else{
                note = NoteEntity(noteId, title,
                        description, getSystemTimeInMillis())
            }

            viewModel.addNote(note).observe(this,noteIdObserver)

            return true

            //When Title is Not Empty
        } else if (title != "" && description == "") {

            val note:NoteEntity
            if(isNotificationSet){
                note = NoteEntity(noteId
                        ,title
                        ,""
                        ,getSystemTimeInMillis()
                        ,getNotificationTime(yy,mm,dd,hh,mn)
                        ,isNotificationSet)
            }else{
                note = NoteEntity(noteId, title,
                        "", getSystemTimeInMillis())
            }

            viewModel.addNote(note).observe(this,noteIdObserver)

            return true

        }

        //When Title and Description are Empty
        else {
            displayToast("Add Something to Save")
        }
        return true
    }

    private fun displayToast(string: String){
        toast(string)
    }

    private fun getSystemTimeInMillis(): Long{
        return System.currentTimeMillis()
    }

    private fun setNotification(){
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext,NotificationReceiver::class.java)
        intent.putExtra(Constant.NOTE_ID,noteId)
        val pendingIntent = PendingIntent.getBroadcast(this.applicationContext,noteId.toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC,getNotificationTime(yy,mm,dd,hh,mn),pendingIntent)
    }

    private fun getNotificationTime(year: Int,month: Int,day: Int, hour: Int,min: Int): Long{
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year,month,day,hour,min)
        return calendar.timeInMillis
    }

    private fun showDatePicker(){



        val year: Int
        val month: Int
        val day: Int


        if(yy == 0) {
            val calendar:Calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)

        }else{
            year = yy
            month = mm
            day = dd
        }

        val datePickerDialog = DatePickerDialog(AddNote@ this,this,year,month,day)

        datePickerDialog.show()

    }

    private fun showTimePicker(){

        val userPref:SharedPreferences = this.applicationContext.getSharedPreferences(Constant.USER_PREF,Context.MODE_PRIVATE)

        val is24h = userPref.getBoolean(Constant.IS_24_HOUR_FORMAT,false)

        val hour: Int
        val minute: Int
        if(hh == 0 || mm == 0){
            val calendar: Calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }else{
            hour = hh
            minute = mn
        }

        val timePickerDialog: TimePickerDialog = TimePickerDialog(AddNote@this,this,hour,minute,is24h)
        timePickerDialog.show()

    }



    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hh = hourOfDay
        mn = minute

        if(isNotificationSet){
            setNotification()
            displayToast("Notification Updated")
        }else if(!isNotificationSet){
            isNotificationSet = true
            setNotification()
            displayToast("Notification Set")
        }
    }

    //onDateSetListener
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        yy = year
        mm = month
        dd = dayOfMonth

        showTimePicker()

    }


}
