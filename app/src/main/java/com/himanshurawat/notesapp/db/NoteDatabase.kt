package com.himanshurawat.notesapp.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.himanshurawat.notesapp.db.dao.NoteDao
import com.himanshurawat.notesapp.db.entity.NoteEntity
import com.himanshurawat.notesapp.utils.Constant

@Database(entities = [(NoteEntity::class)],version = 1,exportSchema = false)
abstract class NoteDatabase: RoomDatabase(){

    abstract fun getNoteDao():NoteDao


    companion object {
        private var INSTANCE:NoteDatabase? = null

        fun getInstance(context: Context):NoteDatabase{
            if(INSTANCE == null){
                synchronized(NoteDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            NoteDatabase::class.java, Constant.DATABASE_NAME).build()
                }
            }
            return INSTANCE as NoteDatabase
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}