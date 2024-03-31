package com.iti.a4cast.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.util.Constants
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ForecastDaoTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ForecastDatabase
    private lateinit var dao: ForecastDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), ForecastDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.forecastDao()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun getAllAlerts_InsertZero_ReturnEmpty() = runBlockingTest {
        val loaded = dao.getAllAlerts()

        launch {
            loaded.collectLatest {
                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it, CoreMatchers.`is`(emptyList()))
                cancel()
            }

        }
    }

    @Test
    fun getAllAlerts_Insert2Alert_ReturnSize2() = runTest {
        val alert1 = AlertModel(
            start = 123L,
            end = 123L,
            type = Constants.ALARM,
            latitude = 300.0,
            longitude = 300.0
        )
        val alert2 = AlertModel(
            start = 12L,
            end = 12L,
            type = Constants.ALARM,
            latitude = 300.0,
            longitude = 300.0
        )

        dao.insertAlert(alert1)
        dao.insertAlert(alert2)

        val loaded = dao.getAllAlerts()

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(2))
                cancel()
            }

        }

    }

    @Test
    fun getAllAlerts_Insert2Alert_Delete1Alert_ReturnSize1() = runTest {
        val alert1 = AlertModel(
            start = 123L,
            end = 123L,
            type = Constants.ALARM,
            latitude = 300.0,
            longitude = 300.0
        )
        val alert2 = AlertModel(
            start = 12L,
            end = 12L,
            type = Constants.ALARM,
            latitude = 300.0,
            longitude = 300.0
        )

        dao.insertAlert(alert1)
        dao.insertAlert(alert2)
        dao.deleteAlert(alert1)

        val loaded = dao.getAllAlerts()

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(1))
                cancel()
            }

        }

    }

    @Test
    fun getAlertByID_Insert1Alert() = runTest {
        val alert1 = AlertModel(
            start = 123L,
            end = 123L,
            type = Constants.ALARM,
            latitude = 300.0,
            longitude = 300.0
        )
        dao.insertAlert(alert1)

        val loaded = dao.getAlertByID(alert1.id)
        Assert.assertThat(loaded, CoreMatchers.`is`(alert1))
    }



@Test
fun getAllFavLocations_InsertZero_ReturnEmpty() = runBlockingTest {
    val loaded = dao.getAllFavLocations()

    launch {
        loaded.collectLatest {
            Assert.assertThat(it, CoreMatchers.notNullValue())
            Assert.assertThat(it, CoreMatchers.`is`(emptyList()))
            cancel()
        }

    }
}


    @Test
    fun getAllFavLocations_Insert2FavLocations_ReturnSize2() = runTest {
         val favLocation1 = FavLocation(latitude = 30.3, longitude = 30.3)
         val favLocation2 = FavLocation(latitude = 33.3, longitude = 33.3)

        dao.insertFavLocation(favLocation1)
        dao.insertFavLocation(favLocation2)

        val loaded = dao.getAllFavLocations()

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(2))
                cancel()
            }

        }

    }

    @Test
    fun getAllFavLocations_Insert2FavLocations_Delete1FavLocations_ReturnSize1() = runTest {

        val favLocation1 = FavLocation(latitude = 30.3, longitude = 30.3)
        val favLocation2 = FavLocation(latitude = 33.3, longitude = 33.3)

        dao.insertFavLocation(favLocation1)
        dao.insertFavLocation(favLocation2)
        dao.deleteFavLocation(favLocation1)

        val loaded = dao.getAllFavLocations()

        launch {
            loaded.collectLatest {

                Assert.assertThat(it, CoreMatchers.notNullValue())
                Assert.assertThat(it.size, CoreMatchers.`is`(1))
                cancel()
            }

        }

    }
}