package com.progcorp.unitedmessengers.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.Result

abstract class DefaultViewModel : ViewModel() {
    private val _snackBarText = MutableLiveData<Event<String>>()
    val snackBarText: LiveData<Event<String>> = _snackBarText

    private val _dataLoading = MutableLiveData<Event<Boolean>>()
    val dataLoading: LiveData<Event<Boolean>> = _dataLoading

    protected fun <T> onResult(mutableLiveData: MutableLiveData<T>? = null, result: Result<T>) {
        when (result) {
            is Result.Loading -> _dataLoading.value = Event(true)

            is Result.Error -> {
                _dataLoading.value = Event(false)
                result.msg?.let { _snackBarText.value = Event(it) }
            }

            is Result.Success -> {
                _dataLoading.value = Event(false)
                result.data?.let { mutableLiveData?.value = it }
                result.msg?.let { _snackBarText.value = Event(it) }
            }
        }
    }
}