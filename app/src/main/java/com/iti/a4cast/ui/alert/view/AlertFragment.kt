package com.iti.a4cast.ui.alert.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.iti.a4cast.R
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.repo.FavAndAlertRepo
import com.iti.a4cast.databinding.AlertDialogBinding
import com.iti.a4cast.databinding.FragmentAlertBinding
import com.iti.a4cast.ui.alert.AlertWorker
import com.iti.a4cast.ui.alert.viewmodel.AlertViewModel
import com.iti.a4cast.ui.alert.viewmodel.AlertViewModelFactory
import com.iti.a4cast.ui.map.view.MapActivity
import com.iti.a4cast.util.Constants
import com.iti.a4cast.util.HomeUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AlertFragment : Fragment() {
    lateinit var binding: FragmentAlertBinding
    private lateinit var customAlertDialogBinding: AlertDialogBinding
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var viewModel: AlertViewModel
    private lateinit var vmFactory: AlertViewModelFactory
    private lateinit var adapter: AlertAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vmFactory =
            AlertViewModelFactory(
                FavAndAlertRepo.getInstant(LocalDatasource.getInstance(requireContext()))
            )
        viewModel = ViewModelProvider(this, vmFactory)[AlertViewModel::class.java]

        adapter = AlertAdapter(requireContext()) {
            checkDeleteDialog(it)
        }
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        askPermissions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@AlertFragment.adapter
        }
        viewModel.getAllAlerts()

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alerts.collectLatest { res ->
                    when (res) {
                        is WeatherStatus.Loading -> {
                            Log.i("TAGTest", "onViewCreated: loading")
                            binding.imageViewEmptyList.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                        }

                        is WeatherStatus.Success -> {
                            if (res.data.isNotEmpty()) {
                                Log.i("TAGTest", "onViewCreated: success${res.data.size}")
                                adapter.submitList(res.data)
                                binding.progress.visibility = View.GONE
                                binding.imageViewEmptyList.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE

                            } else {
                                binding.progress.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.imageViewEmptyList.visibility = View.VISIBLE
                            }

                        }

                        else -> {
                            Log.i("TAGTest", "onViewCreated: faild")

                            binding.progress.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            binding.imageViewEmptyList.visibility = View.VISIBLE

                        }
                    }
                }
            }
        }
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        binding.btnAddFavLoc.setOnClickListener {

            if (HomeUtils.checkForInternet(requireContext())) {
                customAlertDialogBinding =
                    AlertDialogBinding.inflate(LayoutInflater.from(requireContext()), null, false)
                launchAlertDialog()
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.no_internet),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Setting", View.OnClickListener {
                        startActivityForResult(
                            Intent(
                                Settings.ACTION_SETTINGS
                            ), 0
                        );
                    }).show()
            }


        }

    }

    private fun launchAlertDialog() {
        val alertDialog = materialAlertDialogBuilder.setView(customAlertDialogBinding.root)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.rounded_corner, requireActivity().theme
                )
            ).setCancelable(false).show()
        setTimeAndDateInDialog()


        var startTime = Calendar.getInstance().timeInMillis
        val endCal = Calendar.getInstance()
        endCal.add(Calendar.DAY_OF_MONTH, 1)
        var endTime = endCal.timeInMillis

        customAlertDialogBinding.btnSetZone.setOnClickListener {
            var type: String = Constants.ALARM
            val id = if (customAlertDialogBinding.radioAlarm.isChecked) {

                saveToDatabase(startTime, endTime, Constants.ALARM)
            } else {
                type = Constants.NOTIFICATION
                saveToDatabase(startTime, endTime, Constants.NOTIFICATION)
            }

            scheduleWork(startTime, endTime, id)
            checkDisplayOverOtherAppPerm()
            with(Intent(requireContext(), MapActivity::class.java)) {
                /*putExtra(
                    SettingSharedPreferences.NAVIGATE_TO_MAP,
                    SettingSharedPreferences.ADD_T0_ALERTS_IN_THIS_LOCATION
                )*/
                putExtra(Constants.ID, id)
                putExtra(Constants.START, startTime)
                putExtra(Constants.END, endTime)
                putExtra(Constants.TYPE, type)
                startActivity(this)
            }
            alertDialog.dismiss()
        }
        customAlertDialogBinding.cardViewChooseStart.setOnClickListener {
            setAlarm(startTime) { currentTime ->
                startTime = currentTime
                customAlertDialogBinding.textStartTime.text =
                    "${HomeUtils.getTimeFormat(currentTime, "en")}"
                customAlertDialogBinding.textStartDate.text =
                    "${HomeUtils.getADateFormat(currentTime, "en")}"
            }
        }
        customAlertDialogBinding.cardViewChooseEnd.setOnClickListener {
            setAlarm(endTime) { currentTime ->
                endTime = currentTime
                customAlertDialogBinding.textEndTime.text =
                    "${HomeUtils.getTimeFormat(endTime, "en")}"
                customAlertDialogBinding.textEndDate.text =
                    "${HomeUtils.getADateFormat(endTime, "en")}"
            }
        }
        customAlertDialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun scheduleWork(startTime: Long, endTime: Long, tag: String) {

        val _Day_TIME_IN_MILLISECOND = 24 * 60 * 60 * 1000L
        val timeNow = Calendar.getInstance().timeInMillis

        val inputData = Data.Builder()
        inputData.putString(Constants.ID, tag)


        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val myWorkRequest: WorkRequest = if ((endTime - startTime) < _Day_TIME_IN_MILLISECOND) {
            Log.d("TAG", "scheduleWork: one")
            OneTimeWorkRequestBuilder<AlertWorker>().addTag(tag).setInitialDelay(
                startTime - timeNow, TimeUnit.MILLISECONDS
            ).setInputData(
                inputData = inputData.build()
            ).setConstraints(constraints).build()

        } else {

            WorkManager.getInstance(requireContext()).enqueue(
                OneTimeWorkRequestBuilder<AlertWorker>().addTag(tag).setInitialDelay(
                    startTime - timeNow, TimeUnit.MILLISECONDS
                ).setInputData(
                    inputData = inputData.build()
                ).setConstraints(constraints).build()
            )

            Log.d("TAG", "scheduleWork: periodic")

            PeriodicWorkRequest.Builder(
                AlertWorker::class.java, 24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS
            ).addTag(tag).setInputData(
                inputData = inputData.build()
            ).setConstraints(constraints).build()
        }
        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
    }


    private fun setAlarm(minTime: Long, callback: (Long) -> Unit) {
        val color = ResourcesCompat.getColor(resources, R.color.blue, requireActivity().theme)
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            val datePickerDialog = DatePickerDialog(
                requireContext(), R.style.DialogTheme, { _, year, month, day ->
                    this.set(Calendar.YEAR, year)
                    this.set(Calendar.MONTH, month)
                    this.set(Calendar.DAY_OF_MONTH, day)
                    val timePickerDialog = TimePickerDialog(
                        requireContext(), R.style.DialogTheme, { _, hour, minute ->
                            this.set(Calendar.HOUR_OF_DAY, hour)
                            this.set(Calendar.MINUTE, minute)
                            callback(this.timeInMillis)
                        }, this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE), false
                    )
                    timePickerDialog.show()
                    timePickerDialog.setCancelable(false)
                    timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
                    timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
                },

                this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH)

            )
            datePickerDialog.datePicker.minDate = minTime
            datePickerDialog.show()
            datePickerDialog.setCancelable(false)
            datePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
            datePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)

        }
    }

    private fun setTimeAndDateInDialog() {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        customAlertDialogBinding.textStartTime.text =
            "${HomeUtils.getTimeFormat(currentTime, "en")}"
        customAlertDialogBinding.textStartDate.text =
            "${HomeUtils.getADateFormat(currentTime, "en")}"
        val timeAfterOneHour = calendar.get(Calendar.HOUR_OF_DAY)
        calendar.set(Calendar.HOUR_OF_DAY, timeAfterOneHour + 2)
        customAlertDialogBinding.textEndDate.text =
            "${HomeUtils.getADateFormat(calendar.timeInMillis, "en")}"
        customAlertDialogBinding.textEndTime.text =
            "${HomeUtils.getTimeFormat(calendar.timeInMillis, "en")}"
    }

    private fun saveToDatabase(startTime: Long, endTime: Long, type: String): String {
        val alertEntity =
            AlertModel(
                start = startTime,
                end = endTime,
                type = type,
                latitude = 0.0,
                longitude = 0.0
            )
        viewModel.insertAlert(alertEntity)
        return alertEntity.id
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setShowWhenLocked(true)
            requireActivity().setTurnScreenOn(true)
        }
        if (!Settings.canDrawOverlays(requireActivity())) {
            checkDrawOverAppsPermissionsDialog()
        }

    }

    private fun checkDrawOverAppsPermissionsDialog() {
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.permission_request))
            .setCancelable(false)
            .setMessage(getString(R.string.please_allow_draw_over_apps_permission))
            .setPositiveButton(
                "Yes"
            ) { _, _ -> checkDisplayOverOtherAppPerm() }.setNegativeButton(
                "No"
            ) { _, _ -> errorNotGivingDrawOverAppsPermissions() }.show()
    }

    private fun checkDisplayOverOtherAppPerm() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireContext().packageName)
            )
            someActivityResultLauncher.launch(intent)
        }
    }

    private fun errorNotGivingDrawOverAppsPermissions() {
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.warning))
            .setCancelable(false).setMessage(
                getString(R.string.unfortunately_the_display_over_other_apps_permission_is_not_granted)
            ).setPositiveButton(android.R.string.ok) { _, _ -> }.show()
    }


    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (!Settings.canDrawOverlays(requireContext())) {

            }
        }

    private fun checkDeleteDialog(alertModel: AlertModel) {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.delete_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()


        val removeButton = dialog.findViewById<TextView>(R.id.remove)
        val cancelButton = dialog.findViewById<TextView>(R.id.cancel)


        removeButton.setOnClickListener {
            viewModel.deleteAlert(alertModel)
            dialog.dismiss()

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

}