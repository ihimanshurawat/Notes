package com.himanshurawat.notesapp.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.himanshurawat.notesapp.data.database.entity.NoteEntity

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
