package com.iti.a4cast.ui.alert

import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.iti.a4cast.R
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.FavAndAlertRepo
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlertWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {


}