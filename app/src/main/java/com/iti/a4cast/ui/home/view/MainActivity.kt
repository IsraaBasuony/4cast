package com.iti.a4cast.ui.home.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.iti.a4cast.HomeFragment
import com.iti.a4cast.AlertFragment
import com.iti.a4cast.FavouriteFragment
import com.iti.a4cast.R
import com.iti.a4cast.SettingFragment
import com.iti.a4cast.databinding.ActivityMainBinding


/*class MainActivity : AppCompatActivity() {

    lateinit var viewModel: HomeViewModel
    lateinit var vmFactory: HomeViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vmFactory = HomeViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance()))
        viewModel = ViewModelProvider(this, vmFactory).get(HomeViewModel::class.java)

    }
}*/
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var tittleToolbar: TextView
    private lateinit var toolbar: Toolbar
    private var _binding:ActivityMainBinding?=null
    private val binding get() = _binding!!
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding?.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        toolbar = findViewById(R.id.toolbar)
        tittleToolbar = findViewById(R.id.title_toolbar)
        title = ""
        setSupportActionBar(toolbar)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /// Default Navigation bar Tab Selected
        replaceFragment(HomeFragment())
        binding.navigationView.setCheckedItem(R.id.nav_home)
        tittleToolbar.text = getString(R.string.app_name)
        binding.navigationView.setBackgroundColor(getColor(R.color.primary))
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
                tittleToolbar.text = getString(R.string.app_name)

            }

            R.id.nav_Settings -> {
                replaceFragment(SettingFragment())
                tittleToolbar.text = getString(R.string.settings)
            }

            R.id.nav_favorite -> {
                replaceFragment(FavouriteFragment())
                tittleToolbar.text = getString(R.string.favorite)
            }

            R.id.nav_alerts -> {
                replaceFragment(AlertFragment())
                tittleToolbar.text = getString(R.string.alerts)
            }

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun onBackPressedMethod() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

}