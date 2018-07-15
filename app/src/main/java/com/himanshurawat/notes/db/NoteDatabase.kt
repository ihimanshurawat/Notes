package com.himanshurawat.notes.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.himanshurawat.notes.db.dao.NoteDao
import com.himanshurawat.notes.db.entity.NoteEntity

@Database(entities = [(NoteEntity::class)],version = 1,exportSchema = false)
abstract class NoteDatabase: RoomDatabase(){

    abstract fun getNoteDao():NoteDao


    companion object {
        private var INSTANCE:NoteDatabase? = null

        fun getInstance(context: Context):NoteDatabase{
            if(INSTANCE == null){
                synchronized(NoteDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            NoteDatabase::class.java,"note_db").build()
                }
            }
            return INSTANCE as NoteDatabase
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}