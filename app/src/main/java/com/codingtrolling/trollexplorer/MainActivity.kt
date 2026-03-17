package com.codingtrolling.trollexplorer

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtrolling.trollexplorer.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FileAdapter
    private var currentPath: File = Environment.getExternalStorageDirectory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadFiles(currentPath)
    }

    private fun setupRecyclerView() {
        // Correcting the 3-parameter constructor: files, onClick, onLongClick
        adapter = FileAdapter(
            emptyList(),
            { file -> onFileClick(file) },
            { file -> onFileLongClick(file) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadFiles(directory: File) {
        val files = directory.listFiles()?.toList() ?: emptyList()
        // Using updateData() instead of submitList()
        adapter.updateData(files)
        currentPath = directory
    }

    private fun onFileClick(file: File) {
        if (file.isDirectory) {
            loadFiles(file)
        }
    }

    private fun onFileLongClick(file: File) {
        // Handle long click (e.g., show properties or delete)
    }

    override fun onBackPressed() {
        if (currentPath != Environment.getExternalStorageDirectory()) {
            loadFiles(currentPath.parentFile ?: Environment.getExternalStorageDirectory())
        } else {
            super.onBackPressed()
        }
    }
}
