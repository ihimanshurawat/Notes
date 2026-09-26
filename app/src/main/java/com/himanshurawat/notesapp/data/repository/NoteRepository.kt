package com.himanshurawat.notesapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.himanshurawat.notesapp.data.database.NoteDatabase
import com.himanshurawat.notesapp.data.database.dao.NoteDao
import com.himanshurawat.notesapp.data.database.entity.NoteEntity

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<NoteEntity>> = noteDao.allNotes()

    fun getNoteById(id: Long): LiveData<NoteEntity> = noteDao.getNoteById(id)

    fun searchNotes(query: String): LiveData<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun addNote(note: NoteEntity): Long = noteDao.addNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun getNoteByIdForNotification(id: Long): NoteEntity? =
        noteDao.getNoteByIdForNotification(id)

    suspend fun getAllNotesForReboot(): List<NoteEntity> =
        noteDao.getAllNotesForRebootReceiver()

    companion object {
        @Volatile
        private var INSTANCE: NoteRepository? = null

        fun getInstance(context: Context): NoteRepository {
            return INSTANCE ?: synchronized(this) {
                val database = NoteDatabase.getInstance(context)
                val instance = NoteRepository(database.getNoteDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
