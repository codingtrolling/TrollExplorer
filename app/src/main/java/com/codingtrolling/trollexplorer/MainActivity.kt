package com.codingtrolling.trollexplorer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
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
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()
    
    // Shizuku Request Code
    private val SHIZUKU_CODE = 1002

    // Listener for Shizuku binder connection
    private val binderListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BRANDING: Using CodingTrolling Navy (#0b0f1a) and Blue (#3b82f6)
        window.statusBarColor = Color.parseColor("#0b0f1a")
        binding.root.setBackgroundColor(Color.parseColor("#0b0f1a"))
        binding.headerBar.setBackgroundColor(Color.parseColor("#161e2d"))
        binding.tvBrand.setTextColor(Color.parseColor("#3b82f6")) // Pure Blue branding

        setupRecyclerView()
        setupListeners()
        
        // Register Shizuku Listener
        Shizuku.addBinderReceivedListener(binderListener)
        
        if (checkPermissions()) loadFiles(currentPath) else requestPermissions()
    }

    private fun checkShizukuStatus() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(SHIZUKU_CODE)
            } else {
                // Subtle Blue highlight when active, no neon green
                binding.tvBrand.shadowLayer = 10f
            }
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterFiles(newText)
                return true
            }
        })

        binding.tvBrand.setOnClickListener {
            loadFiles(Environment.getExternalStorageDirectory())
        }
    }

    private fun setupRecyclerView() {
        adapter = FileAdapter(emptyList(), { onFileClick(it) }, { onFileLongClick(it) })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun onFileLongClick(file: File) {
        if (Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku Ready: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Shizuku Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFiles(directory: File) {
        val files = directory.listFiles() ?: emptyArray()
        adapter.updateData(files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
        currentPath = directory
        binding.tvCurrentPath.text = directory.absolutePath
        binding.tvCurrentPath.setTextColor(Color.parseColor("#e2e8f0")) // Slate text
        updateStorageStats()
    }

    private fun updateStorageStats() {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val free = (stat.blockSizeLong * stat.availableBlocksLong).toDouble() / (1024 * 1024 * 1024)
        // Set storage info in the Titan header
        binding.tvStorageInfo.text = "Free: ${DecimalFormat("#.##").format(free)} GB"
        binding.tvStorageInfo.setTextColor(Color.parseColor("#94a3b8"))
    }

    private fun checkPermissions(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
        Environment.isExternalStorageManager() else 
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        }
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        super.onDestroy()
    }
}
