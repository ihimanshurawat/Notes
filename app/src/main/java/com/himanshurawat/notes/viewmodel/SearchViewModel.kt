package com.himanshurawat.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.entity.NoteEntity

class SearchViewModel(application: Application): AndroidViewModel(application){
    val noteDatabase:NoteDatabase

    init{
        noteDatabase = NoteDatabase.getInstance(this.getApplication())
    }

    //Get All the Item for Searching
    fun getAllNotes():LiveData<List<NoteEntity>>{

        return noteDatabase.getNoteDao().allNotes()
    }


}