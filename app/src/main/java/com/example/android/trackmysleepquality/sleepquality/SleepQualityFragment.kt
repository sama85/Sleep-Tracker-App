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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    private lateinit var binding : FragmentSleepQualityBinding
    private lateinit var viewModel : SleepQualityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_quality, container, false)

        //to get db instance
        val application = requireNotNull(this.activity).application

        //to create vm
        val database = SleepDatabase.getInstance(application).sleepDatabaseDao
        val args = SleepQualityFragmentArgs.fromBundle(arguments!!)
        val viewModelFactory = SleepQualityViewModelFactory(args.sleepNightKey, database)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SleepQualityViewModel::class.java)

        viewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer{
            if(it == true){
                this.findNavController().navigate(SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment())
                viewModel.doneNavigation()
            }
        })

        setOnclickListeners()
        return binding.root
    }

    private fun setOnclickListeners() {
        binding.qualityZeroImage.setOnClickListener{viewModel.onSetSleepQuality(0)}
        binding.qualityOneImage.setOnClickListener{viewModel.onSetSleepQuality(1)}
        binding.qualityTwoImage.setOnClickListener{viewModel.onSetSleepQuality(2)}
        binding.qualityThreeImage.setOnClickListener{viewModel.onSetSleepQuality(3)}
        binding.qualityFourImage.setOnClickListener{viewModel.onSetSleepQuality(4)}
        binding.qualityFiveImage.setOnClickListener {viewModel.onSetSleepQuality(5)}
    }
}
