package com.iti.a4cast

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.iti.a4cast.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){
   private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var tittleToolbar: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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