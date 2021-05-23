/** Application main ViewModel. Contains logic to get feed posts, save result in live data
and return result to activity through observables. If error is caught shows it to activity through
live data also. */

package com.example.sportogram.Viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sportogram.Models.Post
import com.example.sportogram.Repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    application: Application
) : AndroidViewModel(application) {

    private val postLiveData: MutableLiveData<List<Post>> = MutableLiveData()
    private val errorLiveData: MutableLiveData<String> = MutableLiveData()

    fun getFeed(url: String) {
        viewModelScope.launch {
            try {
                val result = networkRepository.getFeed(url)
                if (result.isSuccessful) {
                    postLiveData.postValue(result.body())
                } else {
                    errorLiveData.postValue(result.errorBody().toString())
                }
            } catch (e: Exception) {
                errorLiveData.postValue(e.message)
            }
        }
    }

    fun getPostLiveData(): MutableLiveData<List<Post>> {
        return postLiveData
    }

    fun getErrorLiveData(): MutableLiveData<String> {
        return errorLiveData
    }
}