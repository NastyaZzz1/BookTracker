package com.nastya.booktracker.presentation.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nastya.booktracker.R
import com.nastya.booktracker.data.local.database.BookDatabase
import com.nastya.booktracker.databinding.ActivityMainBinding
import com.nastya.booktracker.presentation.ui.EpubRepository
import com.nastya.booktracker.presentation.ui.addBookEpub.AddBookEpubViewModel
import com.nastya.booktracker.presentation.ui.addBookEpub.AddBookEpubViewModelFactory
import com.nastya.booktracker.presentation.ui.epubReader.EpubReaderFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: AddBookEpubViewModel
    private var toolbarMenu: Menu? = null

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Разрешение не дано", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickEpubFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            this.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val epubRepository = EpubRepository(this)

            viewModel.addBookFromUri(this, uri, epubRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.cvFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.booksFragment) {
                binding.toolbar.navigationIcon = null
            }
            when (destination.id) {
                R.id.epubReaderFragment -> {
                    toolbarMenu?.findItem(R.id.settingsDialog)?.isVisible = true
                    binding.toolbar.doOnPreDraw { hideSystemUi() }
                    binding.bottomNavigationView.doOnPreDraw { hideSystemUi() }
                }
                else -> {
                    toolbarMenu?.findItem(R.id.settingsDialog)?.isVisible = false
                    showSystemUi()
                }
            }
        }

        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = AddBookEpubViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddBookEpubViewModel::class.java]

        val builder = AppBarConfiguration.Builder(navController.graph)
        val appBarConfiguration = builder.build()

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addBookEpub -> {
                    checkPermissionAndOpenPicker()
                    true
                }
                R.id.settingsDialog -> {
                    openReaderSettings(navHostFragment)
                    true
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        toolbarMenu = menu
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }

    private fun openReaderSettings(navHostFragment: NavHostFragment) {
        val currentFragment =
            navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is EpubReaderFragment) {
            currentFragment.setSettingsBottomDialog()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker()
            return
        }
        requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun openFilePicker() {
        pickEpubFile.launch(arrayOf("application/epub+zip"))
    }

    fun hideSystemUi() {
        binding.toolbar.animate()
            .translationY(-binding.toolbar.height.toFloat())
            .alpha(0f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.bottomNavigationView.animate()
            .translationY(binding.bottomNavigationView.height.toFloat())
            .alpha(0f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun showSystemUi() {
        binding.toolbar.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .start()
        binding.bottomNavigationView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .start()
    }
}