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

        val forecastRepo = ForecastRepo.getInstant(
            ForecastRemoteDataSource.getInstance(),
            SettingsSharedPref.getInstance(applicationContext)
        )
        val repo =
            FavAndAlertRepo.getInstant(LocalDatasource.getInstance(context = applicationContext))

        val context = applicationContext
        val id = inputData.getString(Constants.ID)

        return withContext(Dispatchers.IO) {
            if (id != null) {
                try {
                    val alertModel = repo.getAlertByID(id)
                    val response = forecastRepo.getForecastWeather(
                        alertModel.latitude,
                        alertModel.longitude,
                        "en"
                    )
                    response.collectLatest {
                        val alerts = it?.alerts
                        if (alerts != null) {

                            val alertsMeeage: String = buildString {
                                for (alert in alerts) {
                                    append(alert.event)
                                    append(" ")
                                }
                            }
                            when (alertModel.type) {
                                Constants.NOTIFICATION -> sendNotification(context, alertsMeeage)
                                Constants.ALARM -> createAlarm(context, alertsMeeage)

                            }
                        } else {

                            when (alertModel.type) {

                                Constants.ALARM -> runBlocking {
                                    createAlarm(
                                        context,
                                        context.getString(R.string.weather_is_fine)
                                    )
                                }

                                Constants.NOTIFICATION -> sendNotification(
                                    context,
                                    context.getString(R.string.weather_is_fine)
                                )
                            }
                        }

                        removeFromDataBaseAndWorker(repo, alertModel, context)
                        Result.success()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure()
                }
            }
            Result.failure()
        }
    }

    private suspend fun removeFromDataBaseAndWorker(
        repo: FavAndAlertRepo,
        alertModel: AlertModel,
        appContext: Context
    ) {

        val Day_TIME_IN_MILLISECOND = 24 * 60 * 60 * 1000L
        val now = Calendar.getInstance().timeInMillis
        if ((  alertModel.end - now) < Day_TIME_IN_MILLISECOND) {
            WorkManager.getInstance(appContext).cancelAllWorkByTag(  alertModel.id)
            repo.deleteAlert(  alertModel)
        }

    }

    private val LAYOUT_FLAG_TYPYE =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

    private suspend fun createAlarm(context: Context, message: String) {
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.fure)

        val view: View =
            LayoutInflater.from(context).inflate(R.layout.alarm_dialog, null, false)
        val dismissBtn = view.findViewById<Button>(R.id.btn_dismiss)
        val textView = view.findViewById<TextView>(R.id.textViewDescribtionMessage)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            LAYOUT_FLAG_TYPYE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        withContext(Dispatchers.Main) {
            windowManager.addView(view, layoutParams)
            view.visibility = View.VISIBLE
            textView.text = message
        }

        mediaPlayer.start()
        mediaPlayer.isLooping = true
        dismissBtn.setOnClickListener {
            mediaPlayer.start()
            mediaPlayer.release()
            windowManager.removeView(view)
        }
    }


}