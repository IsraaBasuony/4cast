package com.iti.a4cast.data.repo

import com.iti.a4cast.data.fakes.FakeLocalDataSource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.util.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class FavAndAlertRepoTest {

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

    private val alerts = mutableListOf(alert1, alert2)
    private val favLocations = mutableListOf(favLocation1, favLocation2)

    private lateinit var fakeLocalDataSource: FakeLocalDataSource
    private lateinit var repo: FavAndAlertRepo

    @Before
    fun init() {
        fakeLocalDataSource = FakeLocalDataSource(alerts, favLocations)
        repo = FavAndAlertRepo.getInstant(fakeLocalDataSource)
    }


    @Test
    fun getAllAlerts_insertNewAlert_ListSize3() = runBlockingTest {

        val newAlert = AlertModel(
            start = 11L,
            end = 11L,
            type = Constants.NOTIFICATION,
            latitude = 11.1,
            longitude = 11.1
        )

        repo.insertAlert(newAlert)
        repo.getAllAlerts()

        launch {
            repo.getAllAlerts().collect {
                assertThat(it.size, `is`(3))
            }
        }
    }

    @Test
    fun deleteAlert_alertNotFound() = runBlockingTest {
        val alertToRemove = alerts.first()
        repo.deleteAlert(alertToRemove)
        repo.getAllAlerts().collect {
            val updatedAlerts = it
            assertThat(updatedAlerts.contains(alertToRemove), `is`(false))
        }

    }

    @Test
    fun insertAlert_alertAdded() = runBlockingTest {
        val newAlert = AlertModel(
            start = 123L,
            end = 123L,
            type = Constants.ALARM,
            latitude = 40.0,
            longitude = 40.0
        )
        repo.insertAlert(newAlert)
        repo.getAllAlerts().collect {
            val updatedAlerts = it
            assertThat(updatedAlerts.contains(newAlert), `is`(true))

        }
    }

    @Test
    fun getAlertByID_validID_returnCorrectAlert() {
        val alertRetrieved = alerts.last()
        val retrievedAlert = repo.getAlertByID(alertRetrieved.id)
        assertThat(retrievedAlert, `is`(alertRetrieved))
    }

    @Test
    fun getAllFavLocations_insertNewFavLocation_ListSize3() = runBlockingTest {

        val newFavLocation = FavLocation(latitude = 30.3, longitude = 30.3)

        repo.insertFavLocation(newFavLocation)
        repo.getAllFavLocations()

        launch {
            repo.getAllFavLocations().collect {
                assertThat(it.size, `is`(3))
            }
        }
    }

    @Test
    fun deleteFavLocation_locationNotFound() = runBlockingTest {
        val locationToRemove = favLocations.first()
        repo.deleteFavLocation(locationToRemove)
        repo.getAllFavLocations().collect {
            val updatedLocations = it
            assertThat(updatedLocations.contains(locationToRemove), `is`(false))
        }
    }

    @Test
    fun insertFavLocation_locationAdded() = runBlockingTest {
        val newLocation = FavLocation(latitude = 35.5, longitude = 35.5)
        repo.insertFavLocation(newLocation)
        repo.getAllFavLocations().collect {
            val updatedLocations = it
            assertThat(updatedLocations.contains(newLocation), `is`(true))
        }

    }

}