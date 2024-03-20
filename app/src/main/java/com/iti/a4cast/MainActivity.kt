package com.iti.a4cast

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.ActivityMainBinding
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.ui.settings.viewmodel.SettingsViewModel
import com.iti.a4cast.ui.settings.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(){
   private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var tittleToolbar: TextView
    private lateinit var toolbar: Toolbar

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        settingsViewModelFactory =
            SettingsViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance(), SettingsSharedPref.getInstance(this)))
        settingsViewModel = ViewModelProvider(this, settingsViewModelFactory)[SettingsViewModel::class.java]



        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {
               settingsViewModel.language.collect {
                    val primaryLocale: Locale = this@MainActivity.resources.configuration.locales[0]
                    val locale: String = primaryLocale.language
                    if(locale != it) {
                        Locale.setDefault(Locale(it))
                        this@MainActivity.resources.configuration.setLocale(Locale(it))
                        resources.updateConfiguration(this@MainActivity.resources.configuration, this@MainActivity.resources.displayMetrics)
                    }
                }

            }
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.favouriteFragment, R.id.alertFragment, R.id.settingFragment
            ), binding.drawerLayout
        )
         //appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

       findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, appBarConfiguration)

        binding.navigationView.setupWithNavController(navController)
        val color = ContextCompat.getColor(this, R.color.secondary)
        binding.appBarMain.toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }



}