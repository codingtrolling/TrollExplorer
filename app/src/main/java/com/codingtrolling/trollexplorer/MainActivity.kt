package com.codingtrolling.trollexplorer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtrolling.trollexplorer.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentPath: File = File("/storage/emulated/0")
    private val fileAdapter = FileAdapter { navigateTo(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = fileAdapter

        loadFiles(currentPath)
    }

    private fun loadFiles(directory: File) {
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                directory.listFiles()?.sortedWith(
                    compareBy({ !it.isDirectory }, { it.name.lowercase() })
                ) ?: emptyList()
            }
            fileAdapter.submitList(files)
        }
    }

    private fun navigateTo(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(currentPath)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentPath.absolutePath != "/storage/emulated/0") {
            currentPath = currentPath.parentFile ?: currentPath
            loadFiles(currentPath)
        } else {
            super.onBackPressed()
        }
    }
}
