package com.himanshurawat.notes.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.provider.ContactsContract
import com.himanshurawat.notes.db.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNote(noteEntity: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY count DESC")
    fun allNotes(): LiveData<List<NoteEntity>>

    @Query("UPDATE notes SET count = count+1 WHERE id = :noteId ")
    fun updateCount(noteId: Long)


    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId:Long):LiveData<NoteEntity>

    @Query("SELECT * FROM notes WHERE title LIKE :searchQuery OR description LIKE :searchQuery OR date LIKE :searchQuery")
    fun searchNotes(searchQuery:String):LiveData<List<NoteEntity>>

    @Delete
    fun deleteNote(note: NoteEntity)

    @Update
    fun updateNote(note: NoteEntity)

}
