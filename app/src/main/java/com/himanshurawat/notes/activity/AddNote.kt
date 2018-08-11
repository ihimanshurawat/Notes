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
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.gms.actions.NoteIntents
import com.google.firebase.analytics.FirebaseAnalytics
import com.himanshurawat.notes.viewmodel.NoteViewModel
import com.himanshurawat.notes.R
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.receiver.NotificationReceiver
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.AddNoteViewModel
import kotlinx.android.synthetic.main.activity_add_note.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class AddNote : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    //Lateinits
    private lateinit var viewModel: NoteViewModel
    private lateinit var addNoteViewModel: AddNoteViewModel
    private lateinit var noteEntity: NoteEntity
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var userPref: SharedPreferences
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //Variables
    //Notification
    private var yy: Int = 0
    private var mm: Int = 0
    private var dd: Int = 0
    private var hh: Int = 0
    private var mn: Int = 0
    //Note Id
    private var noteId:Long = -1L

    //Flags
    private var isNotificationSet = false
    private var noteEntityInitialized = false
    private var isDeleting = false


    //Observers
    private val observer:Observer<NoteEntity?> = Observer {
        if (it != null) {

            addNoteViewModel.setTitle(it.title)
            addNoteViewModel.setDescription(it.description)
            noteEntity = it
            noteEntityInitialized = true
            isNotificationSet = noteEntity.isNotificationSet
            if(isNotificationSet){
                val calendar:Calendar = Calendar.getInstance()
                calendar.timeInMillis = noteEntity.notification
                yy = calendar.get(Calendar.YEAR)
                mm = calendar.get(Calendar.MONTH)
                dd = calendar.get(Calendar.DAY_OF_MONTH)
                hh = calendar.get(Calendar.HOUR_OF_DAY)
                mn = calendar.get(Calendar.MINUTE)
                createChip()
            }
        }
    }

    private val noteIdObserver: Observer<Long?> = Observer {
        if(it != null){
            noteId = it
            viewModel.getNoteById(noteId).observe(this, observer)
            addNoteViewModel.isFilled = true
            invalidateOptionsMenu()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        setSupportActionBar(activity_add_note_toolbar)

        //Setting Title and Enabling Home Up
        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)
        ab?.setDisplayHomeAsUpEnabled(true)

        userPref = application.getSharedPreferences(Constant.USER_PREF,Context.MODE_PRIVATE)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //View Model
        viewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        addNoteViewModel = ViewModelProviders.of(this).get(AddNoteViewModel::class.java)

        if(Intent.ACTION_SEND.equals(intent.action) && intent.type != null){
            addNoteViewModel.setDescription(intent.getStringExtra(Intent.EXTRA_TEXT))
        }else if(NoteIntents.ACTION_CREATE_NOTE.equals(intent.action)&& intent.type != null){
            if(intent.extras !=null) {

                addNoteViewModel.setTitle(resources.getString(R.string.self_note))

                if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                    addNoteViewModel.setDescription(intent.getStringExtra(Intent.EXTRA_TEXT))
                }

            }
        }
        noteId = intent.getLongExtra(Constant.GET_NOTES,-1L)



        if(!addNoteViewModel.isFilled){
            if(intent.hasExtra(Constant.GET_NOTES)){
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


        activity_add_note_notification_chip_view.setOnDeleteClicked {
            removeChip()
        }

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

                    if(updateDatabase()) {
                        //Note Created Snackbar
                        displaySnackbar(activity_add_note_root, getString(R.string.note_created))
                    }
                    //Snackbar.make(activity_add_note_root,"Note Created", Snackbar.LENGTH_SHORT).show()
                   //displayToast("Note Created")
                }
            }
            R.id.add_note_menu_delete ->{

                firebaseAnalytics.logEvent(Constant.DELETE_ICON_CLICKED,null)

                //Alert Before Delete Using Anko
                alert(getString(R.string.sure_you_want_to_delete)) {
                    title = getString(R.string.delete_note)
                    yesButton {
                        viewModel.deleteNote(noteEntity)
                        //Deleting Snackbar
                        displayToast(getString(R.string.deleting))
                        //displayToast(getString(R.string.deleting))
                        isDeleting = true
                        if(isNotificationSet){
                            deleteNotification()
                        }
                        firebaseAnalytics.logEvent(Constant.DELETE_CONFIRMED,null)
                        finish()
                    }
                    noButton {
                        firebaseAnalytics.logEvent(Constant.DELETE_CANCELLED,null)
                    }
                }.show()

            }
            R.id.add_note_menu_notification ->{
                firebaseAnalytics.logEvent(Constant.ALARM_ICON_CLICKED,null)

                showDatePicker()
            }
            R.id.add_note_menu_share ->{
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT,getSharedString(activity_add_note_title_edit_text.text.trim().toString()
                        ,activity_add_note_description_edit_text.text.trim().toString()))
                shareIntent.type = "text/plain"
                startActivity(shareIntent)
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
            menu.findItem(R.id.add_note_menu_share).isVisible = false
            menu.findItem(R.id.add_note_menu_save).isVisible = true
        }else{
            menu.findItem(R.id.add_note_menu_delete).isVisible = true
            menu.findItem(R.id.add_note_menu_notification).isVisible = true
            menu.findItem(R.id.add_note_menu_share).isVisible = true
            menu.findItem(R.id.add_note_menu_save).isVisible = false
        }
        return true
    }



    override fun onStop() {
        super.onStop()
        if(noteId != -1L && !isDeleting){
            updateDatabase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!isDeleting) {
            title = activity_add_note_title_edit_text.text.trim().toString()
            description = activity_add_note_description_edit_text.text.trim().toString()
            if (intent.hasExtra(Constant.GET_NOTES)) {
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

            if(noteId == -1L){
                noteId = 0
            }


            //NoteEntity Object
            val preTitle = description.split(" ")

            val note: NoteEntity = if(isNotificationSet){
                NoteEntity(noteId,preTitle[0]
                        ,description
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis()
                        ,getNotificationTime(yy,mm,dd,hh,mn)
                        ,isNotificationSet)
            }else{
                NoteEntity(noteId
                        ,preTitle[0]
                        ,description
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis())
            }




            viewModel.addNote(note).observe(this,noteIdObserver)

            firebaseAnalytics.logEvent(Constant.SAVING_ONLY_DESCRIPTION_NOTE,null)

            return true

            //When Title and Description are not Empty
        } else if (title != "" || description != "") {

            if(noteId == -1L){
                noteId = 0
            }

            val note:NoteEntity = if(isNotificationSet){
                NoteEntity(noteId
                        ,title
                        ,description
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis()
                        ,getNotificationTime(yy,mm,dd,hh,mn)
                        ,isNotificationSet)
            }else{
                NoteEntity(noteId
                        ,title
                        ,description
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis())
            }

            viewModel.addNote(note).observe(this,noteIdObserver)

            firebaseAnalytics.logEvent(Constant.SAVING_COMPLETE_NOTE,null)

            return true

            //When Title is Not Empty
        } else if (title != "" && description == "") {

            if(noteId == -1L){
                noteId = 0
            }

            val note:NoteEntity = if(isNotificationSet){
                NoteEntity(noteId
                        ,title
                        ,""
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis()
                        ,getNotificationTime(yy,mm,dd,hh,mn)
                        ,isNotificationSet)
            }else{
                NoteEntity(noteId
                        ,title
                        ,""
                        ,if(noteEntityInitialized)noteEntity.date else getSystemTimeInMillis())
            }

            viewModel.addNote(note).observe(this,noteIdObserver)

            firebaseAnalytics.logEvent(Constant.SAVING_ONLY_TITLE_NOTE,null)

            return true

        }

        //When Title and Description are Empty
        else {
            //Add Something to Save Snackbar
            displaySnackbar(activity_add_note_root,getString(R.string.add_something_to_save))
            firebaseAnalytics.logEvent(Constant.SAVING_EMPTY_NOTE,null)
            //displayToast("Add Something to Save")
        }
        return false
    }

    private fun displayToast(string: String){
        toast(string)
        val bundle = Bundle()
        bundle.putString(Constant.TOAST_STRING,string)
        firebaseAnalytics.logEvent(Constant.DISPLAY_TOAST,bundle)
    }

    private fun getSystemTimeInMillis(): Long{
        return System.currentTimeMillis()
    }

    private fun setNotification(){
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext,NotificationReceiver::class.java)
        intent.putExtra(Constant.NOTE_ID,noteId)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext,noteId.toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC,getNotificationTime(yy,mm,dd,hh,mn),pendingIntent)
    }

    private fun deleteNotification(){
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext,NotificationReceiver::class.java)
        intent.putExtra(Constant.NOTE_ID,noteId)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext,noteId.toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        firebaseAnalytics.logEvent(Constant.ALARM_CANCELLED,null)
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


    private fun createChip(){
        activity_add_note_notification_chip_view.visibility = View.VISIBLE
        val calendar:Calendar = Calendar.getInstance()
        calendar.set(yy,mm,dd,hh,mn)
        val currentTime = getSystemTimeInMillis()
        val notificationTime = calendar.timeInMillis

        firebaseAnalytics.logEvent(Constant.CHIP_CREATED,null)

        //Set Notification Only When Selected Time is ahead of Current Time
        if(notificationTime > currentTime){
            setNotification()
            activity_add_note_notification_chip_view.label  = getDateTime(notificationTime)
            activity_add_note_notification_chip_view.setLabelColor(ContextCompat.getColor(this,R.color.colorAccent))
            activity_add_note_notification_chip_view.setDeleteIconColor(ContextCompat.getColor(this,R.color.colorAccent))
            firebaseAnalytics.logEvent(Constant.CHIP_TIME_AHEAD,null)
        }else{
            activity_add_note_notification_chip_view.label  = getDateTime(notificationTime)
            activity_add_note_notification_chip_view.setLabelColor(ContextCompat.getColor(this,R.color.colorDate))
            activity_add_note_notification_chip_view.setDeleteIconColor(ContextCompat.getColor(this,R.color.colorDate))
            firebaseAnalytics.logEvent(Constant.CHIP_TIME_BEHIND,null)
        }


    }

    private fun removeChip(){
        activity_add_note_notification_chip_view.visibility = View.GONE
        isNotificationSet = false
        deleteNotification()
        yy = 0
        mm = 0
        dd = 0
        hh = 0
        mn = 0

        firebaseAnalytics.logEvent(Constant.CHIP_CANCELLED,null)
    }




    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hh = hourOfDay
        mn = minute

        firebaseAnalytics.logEvent(Constant.ALARM_TIME_SET,null)

        if(isNotificationSet){
            createChip()
            //Notification Updated
            displaySnackbar(activity_add_note_root,getString(R.string.notification_updated))

            firebaseAnalytics.logEvent(Constant.ALARM_TIME_UPDATED,null)

            //displayToast("Notification Updated")
        }else if(!isNotificationSet){
            isNotificationSet = true
            createChip()
            displaySnackbar(activity_add_note_root,getString(R.string.notification_set))

            firebaseAnalytics.logEvent(Constant.ALARM_SET,null)

            //displayToast(getString(R.string.notification_set))
        }

    }

    //onDateSetListener
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        yy = year
        mm = month
        dd = dayOfMonth

        firebaseAnalytics.logEvent(Constant.ALARM_DATE_SET,null)

        showTimePicker()

    }

    //Returns Time String
    private fun getDateTime(timeInMillis: Long):String {
        val now = Date(timeInMillis)
        lateinit var dateFormatter: SimpleDateFormat
        if(userPref.getBoolean(Constant.IS_24_HOUR_FORMAT,false)){
            dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        }else {
            dateFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        }
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

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


    private fun getSharedString(title: String, description: String): String{
        if(title == "" && description != ""){
            return description
        }else if(title != "" && description == ""){
            return title
        }else if(title != "" && description != ""){
            return "$title - $description"
        }
        return ""
    }

    private fun displaySnackbar(view: View,string: String){
        Snackbar.make(view,string,Snackbar.LENGTH_SHORT).show()
        val bundle = Bundle()
        bundle.putString(Constant.SNACK_BAR_STRING,string)
        firebaseAnalytics.logEvent(Constant.DISPLAY_SNACK_BAR,bundle)
    }



}
