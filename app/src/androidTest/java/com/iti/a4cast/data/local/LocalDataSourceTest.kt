package com.iti.a4cast.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.filters.MediumTest
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.util.Constants
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
class LocalDataSourceTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ForecastDatabase
    private lateinit var localDataSource: LocalDatasource

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ForecastDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        localDataSource = LocalDatasource.getInstance(
            database.forecastDao()
        )
    }


    @After
    fun closeDb() {
        database.close()
        database.clearAllTables()
    }

    private val alert1 = AlertModel(
        start = 123L,
        end = 123L,
        type = Constants.ALARM,
        latitude = 300.0,
        longitude = 300.0
    )
    private val alert2 = AlertModel(
        start = 12L,
        end = 12L,
        type = Constants.ALARM,
        latitude = 300.0,
        longitude = 300.0
    )
    private val favLocation1 = FavLocation(latitude = 30.3, longitude = 30.3)
    private val favLocation2 = FavLocation(latitude = 33.3, longitude = 33.3)


    @Test
    fun getAllFavLocations_NoInsert_ReturnEmpty() = runTest {
        val loaded = localDataSource.getAllFavLocations()
        launch {
            loaded.collectLatest {
                Assert.assertThat(it, `is`(emptyList()))
                cancel()
            }
        }
    }

    @Test
    fun getAllFavLocations_Insert2FavLocations_ListSize2() = runTest {
        localDataSource.insertFavLocation(favLocation1)
        localDataSource.insertFavLocation(favLocation2)

        var result: MutableList<AlertModel> = mutableListOf()

        launch {
            localDataSource.getAllAlerts().collect {
                result = it.toMutableList()
                cancel()
            }
            assertThat(result.size, `is`(2))
        }

    }

    @Test
    fun getAllFavLocations_Insert2FavLocations_delete1Fav_ListSize1() = runTest {
        localDataSource.insertFavLocation(favLocation1)
        localDataSource.insertFavLocation(favLocation2)
        localDataSource.deleteFavLocation(favLocation1)

        var result: MutableList<AlertModel> = mutableListOf()

        launch {
            localDataSource.getAllAlerts().collect {
                result = it.toMutableList()
                cancel()
            }
            assertThat(result.size, `is`(1))
        }
    }


    @Test
    fun getAllAlerts_NoInsert_ReturnEmpty() = runTest {
        val loaded = localDataSource.getAllAlerts()
        launch {
            loaded.collectLatest {
                Assert.assertThat(it, `is`(emptyList()))
                cancel()
            }
        }
    }

    @Test
    fun getAllAlerts_Insert2Alert_ListSize2() = runTest {
        localDataSource.insertAlert(alert1)
        localDataSource.insertAlert(alert2)

        var result: MutableList<AlertModel> = mutableListOf()

        launch {
            localDataSource.getAllAlerts().collect {
                result = it.toMutableList()
                cancel()
            }
            assertThat(result.size, `is`(2))
        }

    }

    @Test
    fun getAllAlerts_Insert2Alert_delete1Alert_ListSize1() = runTest {
        localDataSource.insertAlert(alert1)
        localDataSource.insertAlert(alert2)
        localDataSource.deleteAlert(alert1)

        var result: MutableList<AlertModel> = mutableListOf()

        launch {
            localDataSource.getAllAlerts().collect {
                result = it.toMutableList()
                cancel()
            }
            assertThat(result.size, `is`(1))
        }

    }


    @Test
    fun getAlertByID_InsertAlert() = runTest {
        localDataSource.insertAlert(alert1)
        val loadedAlert = localDataSource.getAlertByID(alert1.id)
        assertThat(loadedAlert.id, `is`(alert1.id))

    }



}