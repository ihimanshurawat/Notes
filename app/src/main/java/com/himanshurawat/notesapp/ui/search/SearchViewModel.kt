package com.himanshurawat.notesapp.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.himanshurawat.notesapp.data.database.entity.NoteEntity
import com.himanshurawat.notesapp.data.repository.NoteRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository.getInstance(application)

    fun getAllNotes(): LiveData<List<NoteEntity>> = repository.allNotes
}
