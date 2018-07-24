package com.himanshurawat.notes.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.entity.NoteEntity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class NoteViewModel constructor(application: Application):AndroidViewModel(application){


    //Database Reference
    private val database:NoteDatabase = NoteDatabase.getInstance(this.getApplication())

    //ViewModel Function to Fetch All Data
    fun getNotes(): LiveData<List<NoteEntity>> {

        return database.getNoteDao().allNotes()
    }

    //Function to Add Note to Database
    fun addNote(note: NoteEntity): MutableLiveData<Long> {
        var noteId: MutableLiveData<Long> = MutableLiveData()
        //Using Anko for Async Operations
        doAsync {
            val id = database.getNoteDao().addNote(note)
            uiThread {
                noteId.value = id
            }
        }

        return noteId
    }

    //Deleting a Note from Database
    fun deleteNote(note: NoteEntity){

        doAsync {
            database.getNoteDao().deleteNote(note)
        }

    }

    //Get Note By id
    fun getNoteById(id: Long):LiveData<NoteEntity>{

        return database.getNoteDao().getNoteById(id)
    }

    //Update Note
    fun updateNote(note: NoteEntity){
        doAsync {
            database.getNoteDao().updateNote(note)
        }
    }


}

