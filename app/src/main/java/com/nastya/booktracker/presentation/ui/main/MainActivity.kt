package com.nastya.booktracker.presentation.ui.main

import android.Manifest
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
    private lateinit var navHostFragment: NavHostFragment
    private var toolbarMenu: Menu? = null

    private var isTimerRunning = false
    private var chronometer: Chronometer? = null
    private var pauseOffset: Long = 0

    private var currentDestinationId: Int = R.id.booksFragment

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

        if (savedInstanceState != null) {
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning", false)
            pauseOffset = savedInstanceState.getLong("pauseOffset", 0)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initViewModel()
        setupNavigation()
        setupToolbarMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        toolbarMenu = menu

        setupChronometer(menu)
        restoreTimerState(menu)
        updateMenuVisibility(currentDestinationId)

        return super.onCreateOptionsMenu(menu)
    }

    private fun initViewModel() {
        val dao = BookDatabase.getInstance(application).bookDao
        val viewModelFactory = AddBookEpubViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddBookEpubViewModel::class.java]
    }

    private fun setupNavigation() {
        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.cvFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestinationId = destination.id
            updateMenuVisibility(currentDestinationId)
        }

        val topLevelDestinations = setOf(
            R.id.booksFragment,
            R.id.calendarFragment,
            R.id.statFragment
        )

        val appBarConfiguration = AppBarConfiguration.Builder(topLevelDestinations).build()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupToolbarMenu() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addBookEpub -> {
                    checkPermissionAndOpenPicker()
                    true
                }
                R.id.settingsDialog -> {
                    openReaderSettings()
                    true
                }
                R.id.pause_timer -> {
                    toggleTimer(item)
                    true
                }
                R.id.bookNotesFragment -> {
                    openNotesScreen()
                    true
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    private fun openNotesScreen() {
        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.cvFragment)
            ?.childFragmentManager
            ?.fragments
            ?.firstOrNull()
        if (currentFragment is EpubReaderFragment) {
            val bookId = currentFragment.getBookId()

            val bundle = Bundle().apply {
                putLong("bookId", bookId)
            }
            navController.navigate(R.id.bookNotesFragment, bundle)
        }
    }

    private fun toggleTimer(item: MenuItem) {
        isTimerRunning = !isTimerRunning

        if (isTimerRunning) {
            item.setIcon(R.drawable.icon_pause_ticking)
            startTimer()
        } else {
            item.setIcon(R.drawable.icon_pause_start)
            stopTimer()
        }
    }

    private fun updateMenuVisibility(destinationId: Int) {
        when (destinationId) {
            R.id.epubReaderFragment -> {
                toolbarMenu?.findItem(R.id.settingsDialog)?.isVisible = true
                toolbarMenu?.findItem(R.id.pause_timer)?.isVisible = true
                toolbarMenu?.findItem(R.id.timer_layout)?.isVisible = true
                toolbarMenu?.findItem(R.id.bookNotesFragment)?.isVisible = true
                toolbarMenu?.findItem(R.id.favoriteBooksFragment)?.isVisible = false
                toolbarMenu?.findItem(R.id.addBookEpub)?.isVisible = false
                binding.toolbar.doOnPreDraw { hideSystemUi() }
                binding.bottomNavigationView.doOnPreDraw { hideSystemUi() }
            }
            else -> {
                toolbarMenu?.findItem(R.id.settingsDialog)?.isVisible = false
                toolbarMenu?.findItem(R.id.pause_timer)?.isVisible = false
                toolbarMenu?.findItem(R.id.timer_layout)?.isVisible = false
                toolbarMenu?.findItem(R.id.bookNotesFragment)?.isVisible = false
                toolbarMenu?.findItem(R.id.favoriteBooksFragment)?.isVisible = true
                toolbarMenu?.findItem(R.id.addBookEpub)?.isVisible = true
                showSystemUi()
            }
        }
    }

    private fun setupChronometer(menu: Menu?) {
        chronometer = Chronometer(this).apply {
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 16 }

            setTextColor(Color.WHITE)
            textSize = 18f

            text = getString(R.string.zero_time)
            base = SystemClock.elapsedRealtime() - pauseOffset

            setOnChronometerTickListener {
                val elapsed = (SystemClock.elapsedRealtime() - base) / 1000
                text = String.format("%02d:%02d", elapsed / 60, elapsed % 60)
            }

            if(isTimerRunning) {
                start()
            }
        }
        menu?.findItem(R.id.timer_layout)?.actionView = chronometer
    }

    private fun restoreTimerState(menu: Menu?) {
        val pauseMenuItem = menu?.findItem(R.id.pause_timer)

        if (isTimerRunning) {
            pauseMenuItem?.setIcon(R.drawable.icon_pause_ticking)
        } else {
            pauseMenuItem?.setIcon(R.drawable.icon_pause_start)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openReaderSettings() {
        val currentFragment =
            navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is EpubReaderFragment) {
            currentFragment.setSettingsBottomDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
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
        chronometer?.start()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pauseOffset = if (isTimerRunning && chronometer != null) {
            SystemClock.elapsedRealtime() - chronometer!!.base
        } else {
            pauseOffset
        }
        outState.putLong("pauseOffset", pauseOffset)
        outState.putBoolean("isTimerRunning", isTimerRunning)
    }
}