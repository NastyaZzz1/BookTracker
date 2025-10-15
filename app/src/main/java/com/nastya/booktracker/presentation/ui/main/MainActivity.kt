package com.nastya.booktracker.presentation.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.nastya.booktracker.R
import com.nastya.booktracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.cvFragment) as NavHostFragment
        navController = navHostFragment.navController

        val builder = AppBarConfiguration.Builder(navController.graph)              // Строит конфигурацию, связывающую панель инструментов с графом навигации
        val appBarConfiguration = builder.build()

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.booksFragment) {
                binding.toolbar.navigationIcon = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {        // Элементы меню добавляются на панель инструментов
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {           // При щелчке на элемент происходит переход к цели
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }
}