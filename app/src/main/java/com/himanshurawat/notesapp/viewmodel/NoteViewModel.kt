package com.himanshurawat.notesapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.himanshurawat.notesapp.db.NoteDatabase
import com.himanshurawat.notesapp.db.entity.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // Database Reference
    private val database: NoteDatabase = NoteDatabase.getInstance(getApplication())

    // ViewModel Function to Fetch All Data
    fun getNotes(): LiveData<List<NoteEntity>> {
        return database.getNoteDao().allNotes()
    }

    // Function to Add Note to Database
    fun addNote(note: NoteEntity): LiveData<Long> {
        val noteId = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val id = database.getNoteDao().addNote(note)
            noteId.postValue(id)
        }
        return noteId
    }

    // Deleting a Note from Database
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.getNoteDao().deleteNote(note)
        }
    }

    // Get Note By id
    fun getNoteById(id: Long): LiveData<NoteEntity> {
        return database.getNoteDao().getNoteById(id)
    }

    // Update Note
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.getNoteDao().updateNote(note)
        }
    }
}

