package com.himanshurawat.notesapp.ui.add

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.himanshurawat.notesapp.R
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.databinding.ActivityAddNoteBinding
import com.himanshurawat.notesapp.receiver.NotificationReceiver
import com.himanshurawat.notesapp.ui.main.NoteViewModel
import com.himanshurawat.notesapp.ui.util.DateTimeUtils
import com.himanshurawat.notesapp.util.Constant
import java.util.Calendar

class AddNote : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var viewModel: NoteViewModel
    private lateinit var addNoteViewModel: AddNoteViewModel
    private lateinit var noteEntity: NoteEntity
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var userPref: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDatePicker()
        } else {
            val snackbar = Snackbar.make(
                binding.activityAddNoteRoot,
                R.string.notification_permission_denied,
                Snackbar.LENGTH_LONG
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            ) {
                snackbar.setAction(R.string.action_settings) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
            }
            snackbar.show()
        }
    }

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
    private val observer: Observer<NoteEntity?> = Observer { note ->
        if (note != null) {
            addNoteViewModel.setTitle(note.title)
            addNoteViewModel.setDescription(note.description)
            noteEntity = note
            noteEntityInitialized = true
            isNotificationSet = note.isNotificationSet
            if (isNotificationSet) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = note.notification
                }
                yy = calendar.get(Calendar.YEAR)
                mm = calendar.get(Calendar.MONTH)
                dd = calendar.get(Calendar.DAY_OF_MONTH)
                hh = calendar.get(Calendar.HOUR_OF_DAY)
                mn = calendar.get(Calendar.MINUTE)
                createChip()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityAddNoteToolbar)

        val ab: ActionBar? = supportActionBar
        ab?.title = ""
        ab?.setHomeButtonEnabled(true)
        ab?.setDisplayHomeAsUpEnabled(true)

        userPref = application.getSharedPreferences(Constant.USER_PREF, Context.MODE_PRIVATE)

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
        addNoteViewModel.title.observe(this) { text ->
            if (binding.activityAddNoteTitleEditText.text.toString() != text) {
                binding.activityAddNoteTitleEditText.setText(text)
            }
        }
        addNoteViewModel.description.observe(this) { text ->
            if (binding.activityAddNoteDescriptionEditText.text.toString() != text) {
                binding.activityAddNoteDescriptionEditText.setText(text)
            }
        }

        binding.activityAddNoteNotificationChip.setOnCloseIconClickListener {
            removeChip()
        }
        binding.activityAddNoteNotificationChip.setOnClickListener {
            checkNotificationPermissionAndShowPicker()
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
                checkNotificationPermissionAndShowPicker()
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
            if (intent.hasExtra(Constant.GET_NOTES) && noteId != -1L) {
                viewModel.getNoteById(noteId).removeObserver(observer)
            }
            addNoteViewModel.setTitle(title)
            addNoteViewModel.setDescription(description)
        }
    }

    private fun updateDatabase(): Boolean {
        title = binding.activityAddNoteTitleEditText.text.toString().trim()
        description = binding.activityAddNoteDescriptionEditText.text.toString().trim()

        val hasTitle = title.isNotEmpty()
        val hasDescription = description.isNotEmpty()

        if (!hasTitle && !hasDescription) {
            displaySnackbar(binding.activityAddNoteRoot, getString(R.string.add_something_to_save))
            return false
        }

        val noteTitle = if (hasTitle) title else description.split(" ")[0]
        val currentNoteId = if (noteId == -1L) 0L else noteId

        val note = if (isNotificationSet) {
            NoteEntity(
                id = currentNoteId,
                title = noteTitle,
                description = description,
                date = if (noteEntityInitialized) noteEntity.date else System.currentTimeMillis(),
                notification = getNotificationTime(yy, mm, dd, hh, mn),
                isNotificationSet = true
            )
        } else {
            NoteEntity(
                id = currentNoteId,
                title = noteTitle,
                description = description,
                date = if (noteEntityInitialized) noteEntity.date else System.currentTimeMillis()
            )
        }

        viewModel.addNote(note) { newId ->
            noteId = newId
            if (isNotificationSet) {
                setNotification()
            }
            if (!addNoteViewModel.isFilled) {
                viewModel.getNoteById(noteId).observe(this, observer)
                addNoteViewModel.isFilled = true
            }
            invalidateOptionsMenu()
        }
        return true
    }

    private fun displayToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun setNotification() {
        if (noteId <= 0L) return
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e2: Exception) {
                // Safeguard against any unexpected exception
            }
        }
    }

    private fun deleteNotification() {
        if (noteId <= 0L) return
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
        val calendar = Calendar.getInstance().apply {
            set(year, month, day, hour, min, 0)
        }
        return calendar.timeInMillis
    }

    private fun checkNotificationPermissionAndShowPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showDatePicker()
                }
                else -> {
                    showNotificationExplainerDialog()
                }
            }
        } else {
            showDatePicker()
        }
    }

    private fun showNotificationExplainerDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_message)
            .setPositiveButton(R.string.notification_permission_continue) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(R.string.notification_permission_not_now, null)
            .show()
    }

    private fun showDatePicker() {
        val year: Int
        val month: Int
        val day: Int

        if (yy == 0) {
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            year = yy
            month = mm
            day = dd
        }

        DatePickerDialog(this, this, year, month, day).show()
    }

    private fun showTimePicker() {
        val is24h = DateFormat.is24HourFormat(applicationContext)
        val hour: Int
        val minute: Int
        if (hh == 0 && mn == 0) {
            val calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        } else {
            hour = hh
            minute = mn
        }

        TimePickerDialog(this, this, hour, minute, is24h).show()
    }

    private fun createChip() {
        binding.activityAddNoteNotificationChip.visibility = View.VISIBLE
        val calendar = Calendar.getInstance().apply {
            set(yy, mm, dd, hh, mn, 0)
        }
        val currentTime = System.currentTimeMillis()
        val notificationTime = calendar.timeInMillis

        binding.activityAddNoteNotificationChip.text = DateTimeUtils.formatDateTime(this, notificationTime)

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
