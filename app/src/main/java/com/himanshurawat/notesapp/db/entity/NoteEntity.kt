package com.himanshurawat.notesapp.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "notes")
data class NoteEntity(
        @PrimaryKey(autoGenerate = true)
        var id:Long = 0,
        @ColumnInfo(name = "title")
        var title:String = "",
        @ColumnInfo(name = "description")
        var description:String = "",
        @ColumnInfo(name = "date")
        var date: Long = 0,
        @ColumnInfo(name = "notification")
        var notification: Long = 0,
        @ColumnInfo(name = "is_notification_set")
        var isNotificationSet: Boolean = false)