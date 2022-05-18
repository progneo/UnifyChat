package com.progcorp.unitedmessengers.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.ApiResult

abstract class DefaultViewModel : ViewModel() {
    private val _snackBarText = MutableLiveData<Event<String>>()
    val snackBarText: LiveData<Event<String>> = _snackBarText

    private val _dataLoading = MutableLiveData<Event<Boolean>>()
    val dataLoading: LiveData<Event<Boolean>> = _dataLoading

    protected fun <T> onResult(mutableLiveData: MutableLiveData<T>? = null, result: ApiResult<T>) {
        when (result) {
            is ApiResult.Loading -> _dataLoading.value = Event(true)

            is ApiResult.Error -> {
                _dataLoading.value = Event(false)
                result.msg?.let { _snackBarText.value = Event(it) }
            }

            is ApiResult.Success -> {
                _dataLoading.value = Event(false)
                result.data?.let { mutableLiveData?.value = it }
                result.msg?.let { _snackBarText.value = Event(it) }
            }
        }
    }
}