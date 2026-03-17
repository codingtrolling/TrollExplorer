package com.codingtrolling.trollexplorer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtrolling.trollexplorer.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()

    private val binderListener = Shizuku.OnBinderReceivedListener {
        checkShizukuPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Shizuku.addBinderReceivedListener(binderListener)

        if (checkPermissions()) {
            setupUI()
        } else {
            requestPermissions()
        }
    }

    private fun checkShizukuPermission() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(1001)
            }
        }
    }

    private fun setupUI() {
        setupRecyclerView()
        loadFiles(currentPath)

        // Verifying IDs from activity_main.xml
        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "TrollSettings...", Toast.LENGTH_SHORT).show()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterFiles(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = FileAdapter(
            emptyList(),
            { file -> onFileClick(file) },
            { file -> onFileLongClick(file) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadFiles(directory: File) {
        val files = directory.listFiles()?.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        ) ?: emptyList()
        
        adapter.updateData(files)
        currentPath = directory
        binding.tvCurrentPath.text = directory.absolutePath
    }

    private fun filterFiles(query: String?) {
        val allFiles = currentPath.listFiles()?.toList() ?: emptyList()
        val filtered = if (query.isNullOrBlank()) {
            allFiles
        } else {
            allFiles.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
    }

    private fun onFileClick(file: File) {
        if (file.isDirectory) {
            loadFiles(file)
        } else {
            Toast.makeText(this, "Trolling: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFileLongClick(file: File) {
        // Shizuku-powered actions
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        }
    }

    override fun onBackPressed() {
        val root = Environment.getExternalStorageDirectory().absolutePath
        if (currentPath.absolutePath != root) {
            loadFiles(currentPath.parentFile ?: Environment.getExternalStorageDirectory())
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        super.onDestroy()
    }
}
