package com.iti.a4cast.ui.settings.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iti.a4cast.MainActivity
import com.iti.a4cast.MapActivity
import com.iti.a4cast.R
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentSettingBinding
import com.iti.a4cast.ui.home.viewmodel.HomeViewModel
import com.iti.a4cast.ui.home.viewmodel.HomeViewModelFactory
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.HomeUtils

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var vmFactory: HomeViewModelFactory
    private lateinit var viewModel: HomeViewModel

    private val settingSharedPref by lazy {
        SettingsSharedPref.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vmFactory =
            HomeViewModelFactory(
                ForecastRepo.getInstant(
                    ForecastRemoteDataSource.getInstance(),
                    SettingsSharedPref.getInstance(requireActivity().applicationContext)
                )
            )
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[HomeViewModel::class.java]


        setUpButtons()
        handleClicks()


    }

    private fun handleClicks() {
        binding.radioGroupChooseLanguage.setOnCheckedChangeListener { _, checked ->
            when (checked) {

                R.id.radio_button_Arabic -> {
                    HomeUtils.changeLanguage("ar", requireContext())
                    viewModel.setLanguage("ar")
                }

                R.id.radio_button_English -> {
                    HomeUtils.changeLanguage("en", requireContext())
                    viewModel.setLanguage("en")
                }
            }
            Log.i("Lang", "getLanguagVMe: ${viewModel.getLanguage()}")
            restartActivity()


        }
        binding.radioGroupLocation.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_GPS -> {
                    settingSharedPref.setLocationPref(SettingsSharedPref.GPS)
                    Toast.makeText(
                        requireContext(),
                        "Settings is changed Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                R.id.radio_button_map -> {
                    settingSharedPref.setLocationPref(SettingsSharedPref.MAP)
                    startActivity(Intent(requireActivity(), MapActivity::class.java))
                }
            }
        }
        binding.radioGroupWindSpeed.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_MPerSec -> {

                    settingSharedPref.setWindSpeedPref(
                        SettingsSharedPref.METER_PER_SECOND
                    )
                    Toast.makeText(
                        requireContext(), "Settings is changed Successfully", Toast.LENGTH_SHORT
                    ).show()
                }

                R.id.radio_button_MilePerHour -> {

                    settingSharedPref.setWindSpeedPref(
                        SettingsSharedPref.MILE_PER_HOUR
                    )

                    Toast.makeText(
                        requireContext(),
                        "Settings is changed Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.radioGroupTempDegree.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                R.id.radio_button_C -> {
                    settingSharedPref.setTempPref(
                        SettingsSharedPref.CELSIUS
                    )
                    Toast.makeText(
                        requireContext(),
                        "Settings is changed Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                R.id.radio_button_K -> {
                    settingSharedPref.setTempPref(SettingsSharedPref.KELVIN)
                    Toast.makeText(
                        requireContext(),
                        "Settings is changed Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                R.id.radio_button_F -> {
                    settingSharedPref.setTempPref(
                        SettingsSharedPref.FAHRENHEIT
                    )
                    Toast.makeText(
                        requireContext(),
                        "Settings is changed Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    private fun setUpButtons() {
        when (settingSharedPref.getLanguagePref()) {
            SettingsSharedPref.ENGLIGH -> binding.radioButtonEnglish.toggle()
            SettingsSharedPref.ARABIC -> binding.radioButtonArabic.toggle()
        }


        when (settingSharedPref.getWindSpeedPref()) {
            SettingsSharedPref.METER_PER_SECOND -> binding.radioButtonMPerSec.toggle()
            SettingsSharedPref.MILE_PER_HOUR -> binding.radioButtonMilePerHour.toggle()
        }


        when (settingSharedPref.getLocationPref()) {
            SettingsSharedPref.GPS -> binding.radioButtonGPS.toggle()
            SettingsSharedPref.MAP -> binding.radioButtonMap.toggle()
        }

        when (settingSharedPref.getTempPref()) {
            SettingsSharedPref.CELSIUS -> binding.radioButtonC.toggle()
            SettingsSharedPref.KELVIN -> binding.radioButtonK.toggle()
            SettingsSharedPref.FAHRENHEIT -> binding.radioButtonF.toggle()
        }
    }

    private fun restartActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireActivity().finish()
        startActivity(intent)
    }
}