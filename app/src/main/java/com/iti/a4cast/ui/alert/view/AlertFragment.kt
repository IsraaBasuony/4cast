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

}