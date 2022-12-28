/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*


class SleepQualityViewModel(private val nightId : Long, val database : SleepDatabaseDao) : ViewModel(){

    //update night sleep quality and navigate back to sleep tracker with updated text

    private val _navigateToSleepTracker = MutableLiveData<Boolean>()
    val navigateToSleepTracker : LiveData<Boolean>
        get() = _navigateToSleepTracker

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    fun onSetSleepQuality(quality : Int){

        //launch coroutine to get and update night
        uiScope.launch(){
            val night = getNightFromDatabase(nightId)
            night.sleepQuality = quality
            update(night)
            _navigateToSleepTracker.value = true
        }
    }

    private suspend fun getNightFromDatabase(nightId: Long): SleepNight{
            return withContext(Dispatchers.IO){
                database.get(nightId)!!
            }
    }

    private suspend fun update(night : SleepNight){
        withContext(Dispatchers.IO){
            database.update(night)
        }
    }

    fun doneNavigation(){
        _navigateToSleepTracker.value = false
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}