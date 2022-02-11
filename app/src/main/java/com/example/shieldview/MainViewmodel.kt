package com.example.shieldview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewmodel : ViewModel() {

    var count: MutableLiveData<Int> = MutableLiveData(0)

    init {
        var cc = 0
        viewModelScope.launch {
            while (cc < 100) {
                cc++
                count.value = cc
                delay(1000)
            }
        }
    }
}