package com.codingtrolling.trollexplorer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtrolling.trollexplorer.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sdcard = Environment.getExternalStorageDirectory()
    private var currentPath: File = sdcard

    private val fileAdapter = FileAdapter { navigateTo(it) }
    private var originalFileList: List<File> = emptyList()

    private val shizukuListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Shizuku Authorized!", Toast.LENGTH_SHORT).show()
            loadFiles(currentPath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = fileAdapter

        checkStoragePermissions()
        setupShizuku()

        loadFiles(currentPath)
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupShizuku() {
        try {
            Shizuku.addRequestPermissionResultListener(shizukuListener)
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(1001)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFiles(directory: File) {
        val prefs = getSharedPreferences("troll_prefs", android.content.Context.MODE_PRIVATE)
        val showHidden = prefs.getBoolean("show_hidden", false)
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                directory.listFiles()?.filter { 
                    if (!showHidden) !it.name.startsWith(".") else true 
                }?.sortedWith(
                    compareBy({ !it.isDirectory }, { it.name.lowercase() })
                ) ?: emptyList()
            }
            originalFileList = files
            fileAdapter.submitList(files)
            supportActionBar?.title = if (directory.absolutePath == sdcard.absolutePath) "Internal Storage" else directory.name
            supportActionBar?.subtitle = directory.absolutePath
        }
    }

            originalFileList = files
            fileAdapter.submitList(files)

            supportActionBar?.title = if (directory.absolutePath == sdcard.absolutePath) "Internal Storage" else directory.name
            supportActionBar?.subtitle = directory.absolutePath
        }
    }

    private fun navigateTo(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(currentPath)
        } else {
            Toast.makeText(this, "Opening: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goBack() {
        val parent = currentPath.parentFile
        if (parent != null && currentPath.absolutePath != "/") {
            currentPath = parent
            loadFiles(currentPath)
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrBlank()) {
                    originalFileList
                } else {
                    originalFileList.filter { it.name.contains(newText, ignoreCase = true) }
                }
                fileAdapter.submitList(filtered)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                goBack()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentPath.absolutePath != sdcard.absolutePath && currentPath.absolutePath != "/") {
            goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFiles(currentPath)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuListener)
    }
}
