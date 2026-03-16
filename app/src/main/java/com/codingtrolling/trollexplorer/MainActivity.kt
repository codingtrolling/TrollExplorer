package com.codingtrolling.trollexplorer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private var currentPath: File = File("/storage/emulated/0")
    private val fileAdapter = FileAdapter { navigateTo(it) }

    // Shizuku permission result listener
    private val shizukuListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Shizuku Authorized!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Shizuku Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = fileAdapter

        // Permissions & Services
        checkStoragePermissions()
        setupShizuku()
        
        loadFiles(currentPath)
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
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
            } else {
                Toast.makeText(this, "Shizuku not running", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFiles(directory: File) {
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                directory.listFiles()?.sortedWith(
                    compareBy({ !it.isDirectory }, { it.name.lowercase() })
                ) ?: emptyList()
            }
            
            if (files.isEmpty() && directory.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Access Denied or Empty Folder", Toast.LENGTH_SHORT).show()
                }
            }
            
            fileAdapter.submitList(files)
            supportActionBar?.title = directory.name.ifEmpty { "Root" }
            supportActionBar?.subtitle = directory.absolutePath
        }
    }

    private fun navigateTo(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(currentPath)
        } else {
            Toast.makeText(this, "File: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goBack() {
        val rootPath = "/storage/emulated/0"
        if (currentPath.absolutePath != rootPath && currentPath.absolutePath != "/") {
            currentPath = currentPath.parentFile ?: File("/")
            loadFiles(currentPath)
        } else {
            finish()
        }
    }

    // Handles the Action Bar (top) back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            goBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Handles the physical/gesture back button
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val rootPath = "/storage/emulated/0"
        if (currentPath.absolutePath != rootPath && currentPath.absolutePath != "/") {
            goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuListener)
    }
}
