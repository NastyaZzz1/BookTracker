package com.nastya.booktracker.presentation.ui.main

import android.Manifest
import android.R.string
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Chronometer
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
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

    private var isTimerRunning = false
    private var chronometer: Chronometer? = null
    private var pauseOffset: Long = 0

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

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
                    toolbarMenu?.findItem(R.id.pause_timer)?.isVisible = true
                    toolbarMenu?.findItem(R.id.timer_layout)?.isVisible = true
                    toolbarMenu?.findItem(R.id.favoriteBooksFragment)?.isVisible = false
                    toolbarMenu?.findItem(R.id.addBookEpub)?.isVisible = false
                    binding.toolbar.doOnPreDraw { hideSystemUi() }
                    binding.bottomNavigationView.doOnPreDraw { hideSystemUi() }
                }
                else -> {
                    toolbarMenu?.findItem(R.id.settingsDialog)?.isVisible = false
                    toolbarMenu?.findItem(R.id.pause_timer)?.isVisible = false
                    toolbarMenu?.findItem(R.id.timer_layout)?.isVisible = false
                    toolbarMenu?.findItem(R.id.favoriteBooksFragment)?.isVisible = true
                    toolbarMenu?.findItem(R.id.addBookEpub)?.isVisible = true
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
                R.id.pause_timer -> {
                    isTimerRunning = !isTimerRunning

                    if (isTimerRunning) {
                        item.setIcon(R.drawable.icon_pause_ticking)
                        startTimer()
                    } else {
                        item.setIcon(R.drawable.icon_pause_start)
                        stopTimer()
                    }
                    true
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        toolbarMenu = menu
        menu?.findItem(R.id.settingsDialog)?.isVisible = false
        menu?.findItem(R.id.pause_timer)?.isVisible = false
        menu?.findItem(R.id.timer_layout)?.isVisible = false

        chronometer = Chronometer(this).apply {
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 16 }

            setTextColor(Color.WHITE)
            textSize = 18f
            text = getString(R.string.zero_time)
            base = SystemClock.elapsedRealtime()

            setOnChronometerTickListener {
                val elapsed = (SystemClock.elapsedRealtime() - base) / 1000
                text = String.format("%02d:%02d", elapsed / 60, elapsed % 60)
            }
        }
        menu?.findItem(R.id.timer_layout)?.actionView = chronometer
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

    private fun startTimer() {
        chronometer?.apply {
            base = when {
                (pauseOffset > 0) -> SystemClock.elapsedRealtime() - pauseOffset
                else -> SystemClock.elapsedRealtime()
            }
            start()
        }
    }

    private fun stopTimer() {
        chronometer?.apply {
            stop()
            pauseOffset = SystemClock.elapsedRealtime() - base
        }
    }

    fun resetTimerFromReader(): Long {
        return chronometer?.let {
            val elapsedTime = if(isTimerRunning) {
                SystemClock.elapsedRealtime() - it.base
            } else {
                pauseOffset
            }

            it.stop()
            it.base = SystemClock.elapsedRealtime()
            it.text = getString(R.string.zero_time)
            pauseOffset = 0
            isTimerRunning = false

            toolbarMenu?.findItem(R.id.pause_timer)?.setIcon(R.drawable.icon_pause_start)

            elapsedTime
        } ?: 0L
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