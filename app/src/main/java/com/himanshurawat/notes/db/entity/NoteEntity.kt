package com.himanshurawat.notes.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull


@Entity(tableName = "notes")
data class NoteEntity(
        @PrimaryKey(autoGenerate = true)
        @NonNull
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