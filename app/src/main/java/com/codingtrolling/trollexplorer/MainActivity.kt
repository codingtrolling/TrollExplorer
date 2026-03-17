package com.codingtrolling.trollexplorer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
    
    // REQUEST CODE for Shizuku Permission
    private val SHIZUKU_CODE = 1002

    private val binderListener = Shizuku.OnBinderReceivedListener {
        checkShizukuPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Branding & Colors (CodingTrolling #0b0f1a)
        window.statusBarColor = Color.parseColor("#0b0f1a")
        binding.root.setBackgroundColor(Color.parseColor("#0b0f1a"))
        binding.headerBar.setBackgroundColor(Color.parseColor("#161e2d"))

        setupRecyclerView()
        setupListeners()
        
        // Initialize Shizuku
        Shizuku.addBinderReceivedListener(binderListener)
        
        if (checkPermissions()) loadFiles(currentPath) else requestPermissions()
    }

    private fun checkShizukuPermission() {
        if (Shizuku.isPreV11()) return
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            binding.tvBrand.setTextColor(Color.parseColor("#22c55e")) // Giga-Green Active
        } else {
            Shizuku.requestPermission(SHIZUKU_CODE)
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
    }

    private fun setupRecyclerView() {
        adapter = FileAdapter(emptyList(), { onFileClick(it) }, { onFileLongClick(it) })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun onFileLongClick(file: File) {
        if (Shizuku.pingBinder()) {
            // This is where the Titan logic happens. 
            // We can now run 'rm -rf' or 'cp' using Shizuku shell.
            val cmd = "ls -l ${file.absolutePath}"
            Toast.makeText(this, "Shizuku Exec: ${file.name}", Toast.LENGTH_SHORT).show()
            // executeShellCommand(cmd) <- This would be your next step
        } else {
            Toast.makeText(this, "Shizuku Service Not Found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFiles(directory: File) {
        val files = directory.listFiles() ?: emptyArray()
        adapter.updateData(files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
        currentPath = directory
        binding.tvCurrentPath.text = directory.absolutePath
        updateStorageStats()
    }

    private fun updateStorageStats() {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val free = (stat.blockSizeLong * stat.availableBlocksLong).toDouble() / (1024 * 1024 * 1024)
        supportActionBar?.subtitle = "Free: ${DecimalFormat("#.##").format(free)} GB"
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
