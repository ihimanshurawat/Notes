package com.himanshurawat.notes.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*

import com.himanshurawat.notes.db.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(noteEntity: NoteEntity): Long

    @Query("SELECT * FROM notes")
    fun allNotes(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): LiveData<NoteEntity>

    @Query("SELECT * FROM notes WHERE title LIKE :searchQuery OR description LIKE :searchQuery OR date LIKE :searchQuery")
    fun searchNotes(searchQuery: String): LiveData<List<NoteEntity>>

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteByIdForNotification(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesForRebootReceiver(): List<NoteEntity>
}
