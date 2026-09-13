package com.himanshurawat.notes.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AddNoteViewModel: ViewModel() {

    val title:MutableLiveData<String> = MutableLiveData()
    val description:MutableLiveData<String> = MutableLiveData()
    var isFilled:Boolean

    init {
        title.value = ""
        description.value = ""
        isFilled = false
    }

    fun setTitle(titleText: String){
        title.value = titleText
    }

    fun setDescription(descriptionText: String){
        description.value = descriptionText
    }


}