package com.himanshurawat.notes.utils

class Constant {

    companion object {
        const val GET_NOTES = "Get_Notes"
        const val DATABASE_NAME = "note_db"

        //Prefs
        const val USER_PREF = "userPref"
        const val IS_24_HOUR_FORMAT = "is24HourFormat"


        //Time Constants
        const val TODAY = 86399000L
        const val YESTERDAY = 172799000L

        //Notification
        const val NOTE_ID = "noteId"
        const val NOTIFICATION_CHANNEL_ID = "Notifications"
        const val NOTES_NOTIFICATION_CHANNEL = "Notes Notification"

        //Events
        const val ALARM_ICON_CLICKED = "alarm_clicked"
        const val ALARM_DATE_SET = "alarm_date_set"
        const val ALARM_TIME_SET = "alarm_time_set"
        const val ALARM_SET = "alarm_set"
        const val ALARM_TIME_UPDATED = "alarm_time_updated"

        const val DELETE_ICON_CLICKED = "delete_icon_clicked"
        const val DELETE_CANCELLED = "delete_cancelled"
        const val DELETE_CONFIRMED = "delete_confirmed"

        const val SAVING_EMPTY_NOTE = "saving_empty_note"
        const val SAVING_COMPLETE_NOTE = "saving_complete_note"
        const val SAVING_ONLY_TITLE_NOTE = "saving_only_title_note"
        const val SAVING_ONLY_DESCRIPTION_NOTE = "saving_only_description_note"
    }
}