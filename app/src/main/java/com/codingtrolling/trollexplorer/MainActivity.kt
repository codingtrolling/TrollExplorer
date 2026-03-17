package com.codingtrolling.trollexplorer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
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

/**
 * GIGA POMEGRANATE EDITION - CODINGTROLLING LIMITED
 * Integrated: Shizuku, FileObserver, StorageStats, Custom Web-Theming
 */
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

        // Apply CodingTrolling Web Palette (#0b0f1a = Deep Navy, #161e2d = Card Background)
        window.statusBarColor = Color.parseColor("#0b0f1a")
        binding.root.setBackgroundColor(Color.parseColor("#0b0f1a"))
        binding.headerBar.setBackgroundColor(Color.parseColor("#161e2d"))

        Toast.makeText(this, "Personalization En Cours: GIGA POMEGRANATE", Toast.LENGTH_LONG).show()

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
            updateStorageStats()
            checkShizukuStatus()
        }
    }

    override fun onPause() {
        super.onPause()
        stopObserving()
    }

    private fun initializeUI() {
        setupRecyclerView()
        
        // Settings Button: Hidden File Toggle with Branded Toast
        binding.btnSettings.setOnClickListener {
            showHidden = !showHidden
            Toast.makeText(this, "TrollView: ${if(showHidden) "Secrets Revealed" else "Ghost Mode"}", Toast.LENGTH_SHORT).show()
            loadFiles(currentPath)
        }

        // Search Engine: Real-time filtering by name and extension
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterFiles(newText)
                return true
            }
        })

        // Header Action: Return to Storage Root
        binding.tvCurrentPath.setOnClickListener {
            loadFiles(Environment.getExternalStorageDirectory())
        }
    }

    private fun setupRecyclerView() {
        adapter = FileAdapter(emptyList(), { onFileClick(it) }, { onFileLongClick(it) })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

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
            fileObserver = object : FileObserver(path.absolutePath) {
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
        
        // Syncing with CodingTrolling Slate Text Color
        binding.tvCurrentPath.text = directory.absolutePath
        binding.tvCurrentPath.setTextColor(Color.parseColor("#e2e8f0"))
    }

    private fun filterFiles(query: String?) {
        val files = currentPath.listFiles()?.toList() ?: emptyList()
        val filtered = if (query.isNullOrBlank()) files else files.filter { 
            it.name.contains(query, ignoreCase = true) || it.extension.contains(query, ignoreCase = true) 
        }
        adapter.updateData(filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
    }

    private fun onFileClick(file: File) {
        if (file.isDirectory) {
            loadFiles(file)
            startObserving(file)
        } else {
            val sdf = SimpleDateFormat("HH:mm | dd/MM/yy", Locale.getDefault())
            Toast.makeText(this, "Size: ${file.length() / 1024}KB | ${sdf.format(Date(file.lastModified()))}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFileLongClick(file: File) {
        if (Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku Exec Ready: ${file.name}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Shizuku Inactive - Cannot TROLL", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStorageStats() {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val available = (stat.blockSizeLong * stat.availableBlocksLong).toDouble() / (1024 * 1024 * 1024)
        val df = DecimalFormat("#.##")
        supportActionBar?.subtitle = "Storage Available: ${df.format(available)} GB"
    }

    private fun checkShizukuStatus() {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1001)
        }
    }

    private fun checkPermissions(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
        Environment.isExternalStorageManager() else 
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        }
    }

    override fun onBackPressed() {
        val root = Environment.getExternalStorageDirectory().absolutePath
        if (currentPath.absolutePath != root) {
            val parent = currentPath.parentFile ?: Environment.getExternalStorageDirectory()
            loadFiles(parent)
            startObserving(parent)
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        stopObserving()
        super.onDestroy()
    }
}
