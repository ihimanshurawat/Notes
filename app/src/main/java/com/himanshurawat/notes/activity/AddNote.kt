package com.himanshurawat.notes.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.himanshurawat.notes.R
import com.himanshurawat.notes.databinding.ActivityAddNoteBinding
import com.himanshurawat.notes.db.entity.NoteEntity
import com.himanshurawat.notes.receiver.NotificationReceiver
import com.himanshurawat.notes.utils.Constant
import com.himanshurawat.notes.viewmodel.AddNoteViewModel
import com.himanshurawat.notes.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddNote : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var addNoteViewModel: AddNoteViewModel
    private lateinit var noteEntity: NoteEntity
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var userPref: SharedPreferences

    // Notification Variables
    private var yy: Int = 0
    private var mm: Int = 0
    private var dd: Int = 0
    private var hh: Int = 0
    private var mn: Int = 0

    // Note Id
    private var noteId: Long = -1L

    // Flags
    private var isNotificationSet = false
    private var noteEntityInitialized = false
    private var isDeleting = false

    // Observers
    private val observer: Observer<NoteEntity?> = Observer {
        if (it != null) {
            addNoteViewModel.setTitle(it.title)
            addNoteViewModel.setDescription(it.description)
            noteEntity = it
            noteEntityInitialized = true
            isNotificationSet = noteEntity.isNotificationSet
            if (isNotificationSet) {
                val calendar: Calendar = Calendar.getInstance()
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
        if (it != null) {
            noteId = it
            viewModel.getNoteById(noteId).observe(this, observer)
            addNoteViewModel.isFilled = true
            invalidateOptionsMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityAddNoteToolbar)

        // Setting Title and Enabling Home Up
        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)
        ab?.setDisplayHomeAsUpEnabled(true)

        userPref = application.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)

        // View Model
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        addNoteViewModel = ViewModelProvider(this)[AddNoteViewModel::class.java]

        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            addNoteViewModel.setDescription(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")
        } else if ("com.google.android.gms.actions.CREATE_NOTE" == intent.action && intent.type != null) {
            if (intent.extras != null) {
                addNoteViewModel.setTitle(resources.getString(R.string.self_note))
                if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                    addNoteViewModel.setDescription(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")
                }
            }
        }
        noteId = intent.getLongExtra(Constant.GET_NOTES, -1L)

        if (!addNoteViewModel.isFilled) {
            if (intent.hasExtra(Constant.GET_NOTES)) {
                viewModel.getNoteById(noteId).observe(this, observer)
                addNoteViewModel.isFilled = true
            }
        }

        // Persist Data when Configuration Changes
        addNoteViewModel.title.observe(this, Observer { text ->
            if (binding.activityAddNoteTitleEditText.text.toString() != text) {
                binding.activityAddNoteTitleEditText.setText(text)
            }
        })
        addNoteViewModel.description.observe(this, Observer { text ->
            if (binding.activityAddNoteDescriptionEditText.text.toString() != text) {
                binding.activityAddNoteDescriptionEditText.setText(text)
            }
        })

        binding.activityAddNoteNotificationChip.setOnCloseIconClickListener {
            removeChip()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.add_note_menu_save -> {
                if (noteId == -1L) {
                    if (updateDatabase()) {
                        displaySnackbar(binding.activityAddNoteRoot, getString(R.string.note_created))
                    }
                }
            }
            R.id.add_note_menu_delete -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.delete_note))
                    .setMessage(getString(R.string.sure_you_want_to_delete))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        if (::noteEntity.isInitialized) {
                            viewModel.deleteNote(noteEntity)
                        }
                        displayToast(getString(R.string.deleting))
                        isDeleting = true
                        if (isNotificationSet) {
                            deleteNotification()
                        }
                        finish()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            R.id.add_note_menu_notification -> {
                showDatePicker()
            }
            R.id.add_note_menu_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getSharedString(
                            binding.activityAddNoteTitleEditText.text.toString().trim(),
                            binding.activityAddNoteDescriptionEditText.text.toString().trim()
                        )
                    )
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Hide Delete From Menu If It's New Note
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (noteId == -1L) {
            menu.findItem(R.id.add_note_menu_delete)?.isVisible = false
            menu.findItem(R.id.add_note_menu_notification)?.isVisible = false
            menu.findItem(R.id.add_note_menu_share)?.isVisible = false
            menu.findItem(R.id.add_note_menu_save)?.isVisible = true
        } else {
            menu.findItem(R.id.add_note_menu_delete)?.isVisible = true
            menu.findItem(R.id.add_note_menu_notification)?.isVisible = true
            menu.findItem(R.id.add_note_menu_share)?.isVisible = true
            menu.findItem(R.id.add_note_menu_save)?.isVisible = false
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        if (noteId != -1L && !isDeleting) {
            updateDatabase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isDeleting) {
            title = binding.activityAddNoteTitleEditText.text.toString().trim()
            description = binding.activityAddNoteDescriptionEditText.text.toString().trim()
            if (intent.hasExtra(Constant.GET_NOTES)) {
                viewModel.getNoteById(noteId).removeObserver(observer)
            }
            addNoteViewModel.setTitle(title)
            addNoteViewModel.setDescription(description)
        }
    }

    private fun updateDatabase(): Boolean {
        title = binding.activityAddNoteTitleEditText.text.toString().trim()
        description = binding.activityAddNoteDescriptionEditText.text.toString().trim()

        if (title.isEmpty() && description.isNotEmpty()) {
            if (noteId == -1L) {
                noteId = 0
            }
            val preTitle = description.split(" ")
            val note: NoteEntity = if (isNotificationSet) {
                NoteEntity(
                    noteId,
                    preTitle[0],
                    description,
                    if (noteEntityInitialized) noteEntity.date else getSystemTimeInMillis(),
                    getNotificationTime(yy, mm, dd, hh, mn),
                    isNotificationSet
                )
            } else {
                NoteEntity(
                    noteId,
                    preTitle[0],
                    description,
                    if (noteEntityInitialized) noteEntity.date else getSystemTimeInMillis()
                )
            }
            viewModel.addNote(note).observe(this, noteIdObserver)
            return true
        } else if (title.isNotEmpty() || description.isNotEmpty()) {
            if (noteId == -1L) {
                noteId = 0
            }
            val note: NoteEntity = if (isNotificationSet) {
                NoteEntity(
                    noteId,
                    title,
                    description,
                    if (noteEntityInitialized) noteEntity.date else getSystemTimeInMillis(),
                    getNotificationTime(yy, mm, dd, hh, mn),
                    isNotificationSet
                )
            } else {
                NoteEntity(
                    noteId,
                    title,
                    description,
                    if (noteEntityInitialized) noteEntity.date else getSystemTimeInMillis()
                )
            }
            viewModel.addNote(note).observe(this, noteIdObserver)
            return true
        } else {
            displaySnackbar(binding.activityAddNoteRoot, getString(R.string.add_something_to_save))
        }
        return false
    }

    private fun displayToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun getSystemTimeInMillis(): Long {
        return System.currentTimeMillis()
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun setNotification() {
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, NotificationReceiver::class.java).apply {
            putExtra(Constant.NOTE_ID, noteId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            noteId.toInt(),
            intent,
            getPendingIntentFlags()
        )
        val triggerTime = getNotificationTime(yy, mm, dd, hh, mn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun deleteNotification() {
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, NotificationReceiver::class.java).apply {
            putExtra(Constant.NOTE_ID, noteId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            noteId.toInt(),
            intent,
            getPendingIntentFlags()
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getNotificationTime(year: Int, month: Int, day: Int, hour: Int, min: Int): Long {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, min, 0)
        return calendar.timeInMillis
    }

    private fun showDatePicker() {
        val year: Int
        val month: Int
        val day: Int

        if (yy == 0) {
            val calendar: Calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            year = yy
            month = mm
            day = dd
        }

        val datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val is24h = DateFormat.is24HourFormat(applicationContext)
        val hour: Int
        val minute: Int
        if (hh == 0 && mn == 0) {
            val calendar: Calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        } else {
            hour = hh
            minute = mn
        }

        val timePickerDialog = TimePickerDialog(this, this, hour, minute, is24h)
        timePickerDialog.show()
    }

    private fun createChip() {
        binding.activityAddNoteNotificationChip.visibility = View.VISIBLE
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(yy, mm, dd, hh, mn, 0)
        val currentTime = getSystemTimeInMillis()
        val notificationTime = calendar.timeInMillis

        binding.activityAddNoteNotificationChip.text = getDateTime(notificationTime)

        if (notificationTime > currentTime) {
            setNotification()
            val accentColor = ContextCompat.getColor(this, R.color.colorAccent)
            binding.activityAddNoteNotificationChip.setTextColor(accentColor)
            binding.activityAddNoteNotificationChip.closeIconTint = ColorStateList.valueOf(accentColor)
        } else {
            val dateColor = ContextCompat.getColor(this, R.color.colorDate)
            binding.activityAddNoteNotificationChip.setTextColor(dateColor)
            binding.activityAddNoteNotificationChip.closeIconTint = ColorStateList.valueOf(dateColor)
        }
    }

    private fun removeChip() {
        binding.activityAddNoteNotificationChip.visibility = View.GONE
        isNotificationSet = false
        deleteNotification()
        yy = 0
        mm = 0
        dd = 0
        hh = 0
        mn = 0
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hh = hourOfDay
        mn = minute

        if (isNotificationSet) {
            createChip()
            displaySnackbar(binding.activityAddNoteRoot, getString(R.string.notification_updated))
        } else {
            isNotificationSet = true
            createChip()
            displaySnackbar(binding.activityAddNoteRoot, getString(R.string.notification_set))
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        yy = year
        mm = month
        dd = dayOfMonth
        showTimePicker()
    }

    private fun getDateTime(timeInMillis: Long): String {
        val now = Date(timeInMillis)
        val is24h = DateFormat.is24HourFormat(applicationContext)
        val dateFormatter = if (is24h) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        }
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        return "$date ${getMonth(month)}, ${dateFormatter.format(now)}"
    }

    private fun getMonth(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "January"
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> ""
        }
    }

    private fun getSharedString(title: String, description: String): String {
        return when {
            title.isEmpty() && description.isNotEmpty() -> description
            title.isNotEmpty() && description.isEmpty() -> title
            title.isNotEmpty() && description.isNotEmpty() -> "$title - $description"
            else -> ""
        }
    }

    private fun displaySnackbar(view: View, string: String) {
        Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show()
    }
}

