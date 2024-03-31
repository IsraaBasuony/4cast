package com.iti.a4cast

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.iti.a4cast.databinding.ActivityMainBinding
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.HomeUtils

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HomeUtils.changeLanguage(SettingsSharedPref.getInstance(this).getLanguagePref(),this)


        setSupportActionBar(binding.appBarMain.toolbar)
        binding.appBarMain.toolbar.setNavigationIcon(R.drawable.menu_icon)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.favouriteFragment, R.id.alertFragment, R.id.settingFragment
            ), binding.drawerLayout
        )

        findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)
        val color = ContextCompat.getColor(this, R.color.secondary)
        binding.appBarMain.toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener{
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                if (destination.id == R.id.favFragmentDetails || destination.id == R.id.settingFragment) {
                    this@MainActivity.binding.appBarMain.toolbar.visibility = View.GONE
                }else{
                    this@MainActivity.binding.appBarMain.toolbar.visibility = View.VISIBLE

                }
            }
    })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


}