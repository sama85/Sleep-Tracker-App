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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    //data encapsulation pattern
    private val _tonight = MutableLiveData<SleepNight?>()
    val tonight: LiveData<SleepNight?>
        get() = _tonight

    val nights = database.getAllNights()

    private var _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality : LiveData<SleepNight?>
        get() = _navigateToSleepQuality


    //WHY DO WE NEED A JOB?
    private var viewModelJob = Job()

    //coroutines in ui scope will run in main thread
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val IOScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        initializeTonight()
    }

    //uses a coroutine as it performs db operation
    //WHY USE MAIN THREAD?
    private fun initializeTonight() {
        uiScope.launch {
            _tonight.value = getTonightFromDatabase()
        }
    }

    //WHY CREATE ANOTHER COROUTINE IN IO THREAD?
    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.startTimeMilli != night?.endTimeMilli)
                night = null
            night
        }
    }

    //create night and insert to db
    fun onStartTracking() {


        var night = SleepNight()
        uiScope.launch {
            //database.insert(night)
            //how does the suspend function in uiScope coroutine executes in another thread?
            //does it block this thread and processor excutes it in IO thread or wait for IO thread to be
            //processed then executes?
            //processor executes one thread at a time?

             insert(night)
            //_tonight.value = night
            _tonight.value = getTonightFromDatabase()
        }
    }

        //db operation in coroutine that runs on thread other than ui
        private suspend fun insert(night: SleepNight) {
                withContext(Dispatchers.IO){
                        database.insert(night)
                }
        }

    //update end time of tonight and in db
    fun onStopTracking() {
        uiScope.launch {
            //EXPLAIN LINE?
            val night = _tonight.value ?: return@launch
            night.endTimeMilli = System.currentTimeMillis()
            update(night)
            _navigateToSleepQuality.value = night
        }
    }

    fun doneNavigation(){
        _navigateToSleepQuality.value = null
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            _tonight.value = null
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    val nightsString = Transformations.map(nights, { nights ->
        formatNights(nights, application.resources)

    })

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}

