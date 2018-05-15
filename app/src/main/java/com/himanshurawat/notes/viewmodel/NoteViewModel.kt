package com.himanshurawat.notes.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.persistence.room.Room
import android.provider.ContactsContract
import android.support.annotation.NonNull
import android.util.Log
import com.himanshurawat.notes.db.NoteDatabase
import com.himanshurawat.notes.db.entity.NoteEntity
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class NoteViewModel constructor(application: Application):AndroidViewModel(application){


    //Database Reference
    private val database:NoteDatabase

    //Initializing the Database Variable
    init {
        database = NoteDatabase.getInstance(this.getApplication())
    }

    //ViewModel Function to Fetch All Data
    fun getNotes(): LiveData<List<NoteEntity>> {


        Log.i("Note","ASYNC Add Note Returning")
        return database.getNoteDao().allNotes()
    }

    //Function to Add Note to Database
    fun addNote(note: NoteEntity){

        //Using Anko for Async Operations
        doAsync {
            database.getNoteDao().addNote(note)
            Log.i("Note","ASYNC Add Note")
        }
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

    //Update Note Count
    fun updateCount(id: Long){
        doAsync {
            database.getNoteDao().updateCount(id)
        }
    }

}

