package com.himanshurawat.notesapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository = NoteRepository.getInstance(application)

    fun getNotes(): LiveData<List<NoteEntity>> = repository.allNotes

    fun addNote(note: NoteEntity, onComplete: ((Long) -> Unit)? = null): LiveData<Long> {
        val noteId = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.addNote(note)
            noteId.postValue(id)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(id)
            }
        }
        return noteId
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun getNoteById(id: Long): LiveData<NoteEntity> = repository.getNoteById(id)

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }
}
