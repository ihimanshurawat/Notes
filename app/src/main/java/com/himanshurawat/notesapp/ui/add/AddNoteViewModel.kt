package com.himanshurawat.notesapp.ui.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddNoteViewModel : ViewModel() {

    val title: MutableLiveData<String> = MutableLiveData("")
    val description: MutableLiveData<String> = MutableLiveData("")
    var isFilled: Boolean = false

    fun setTitle(titleText: String) {
        title.value = titleText
    }

    fun setDescription(descriptionText: String) {
        description.value = descriptionText
    }
}
