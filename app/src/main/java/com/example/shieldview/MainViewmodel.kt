package com.example.shieldview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewmodel : ViewModel() {

    var count: MutableLiveData<Double> = MutableLiveData(0.0)

    init {
        var cc = 0.0
        viewModelScope.launch {
            while (cc < 1.0) {
                cc += 0.01
                if (cc > 1.0) {
                    cc = 1.0
                }
                count.value = cc
                delay(500)
            }
        }
    }
}