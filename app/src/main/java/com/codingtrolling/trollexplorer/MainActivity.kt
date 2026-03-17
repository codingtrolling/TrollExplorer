package com.codingtrolling.trollexplorer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtrolling.trollexplorer.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var showHidden: Boolean = false
    private var fileObserver: FileObserver? = null

    private val binderListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BRANDING: Organization Identity
        Toast.makeText(this, "CodingTrolling Limited - Personalization En Cours", Toast.LENGTH_LONG).show()

        Shizuku.addBinderReceivedListener(binderListener)
        initializeUI()
        
        if (checkPermissions()) {
            loadFiles(currentPath)
        } else {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            loadFiles(currentPath)
            startObserving(currentPath)
        }
    }

    override fun onPause() {
        super.onPause()
        stopObserving()
    }

    private fun initializeUI() {
        setupRecyclerView()
        
        // Header Trolling: Show Device Info on Click
        binding.headerBar.setOnClickListener {
            val deviceInfo = "Model: ${Build.MODEL} | SDK: ${Build.VERSION.SDK_INT}"
            Toast.makeText(this, deviceInfo, Toast.LENGTH_SHORT).show()
        }

        binding.btnSettings.setOnClickListener {
            showHidden = !showHidden
            Toast.makeText(this, "Hidden Files: ${if(showHidden) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
            loadFiles(currentPath)
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
        adapter = FileAdapter(emptyList(), { onFileClick(it) }, { onFileLongClick(it) })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // @REMEMBER: Added FileObserver to detect changes in real-time
    private fun startObserving(path: File) {
        stopObserving()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileObserver = object : FileObserver(path, CREATE or DELETE or MODIFY or MOVED_FROM or MOVED_TO) {
                override fun onEvent(event: Int, pathName: String?) {
                    runOnUiThread { loadFiles(currentPath) }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            fileObserver = object : FileObserver(path.absolutePath, ALL_EVENTS) {
                override fun onEvent(event: Int, pathName: String?) {
                    runOnUiThread { loadFiles(currentPath) }
                }
            }
        }
        fileObserver?.startWatching()
    }

    private fun stopObserving() {
        fileObserver?.stopWatching()
        fileObserver = null
    }

    private fun loadFiles(directory: File) {
        val filesList = directory.listFiles() ?: emptyArray()
        
        val filtered = if (showHidden) filesList.toList() else filesList.filter { !it.name.startsWith(".") }
        val sorted = filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        
        adapter.updateData(sorted)
        currentPath = directory
        binding.tvCurrentPath.text = directory.absolutePath
        updateStorageStats()
    }

    private fun filterFiles(query: String?) {
        val files = currentPath.listFiles()?.toList() ?: emptyList()
        val filtered = if (query.isNullOrBlank()) files else files.filter { it.name.contains(query, ignoreCase = true) }
        adapter.updateData(filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
    }

    private fun onFileClick(file: File) {
        if (file.isDirectory) {
            loadFiles(file)
            startObserving(file)
        } else {
            Toast.makeText(this, "Opening: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFileLongClick(file: File) {
        if (Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku Ready for ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStorageStats() {
        try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val available = stat.blockSizeLong * stat.availableBlocksLong
            val total = stat.blockSizeLong * stat.blockCountLong
            val df = DecimalFormat("#.##")
            val freeGB = df.format(available.toDouble() / (1024 * 1024 * 1024))
            supportActionBar?.subtitle = "Free: ${freeGB} GB"
        } catch (e: Exception) {
            Log.e("TrollExplorer", "Storage stats failed", e)
        }
    }

    private fun checkShizukuStatus() {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1001)
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        }
    }

    override fun onBackPressed() {
        val root = Environment.getExternalStorageDirectory().absolutePath
        if (currentPath.absolutePath != root) {
            val parent = currentPath.parentFile ?: Environment.getExternalStorageDirectory()
            loadFiles(parent)
            startObserving(parent)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        stopObserving()
        super.onDestroy()
    }
}
