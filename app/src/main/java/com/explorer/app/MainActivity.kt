package com.explorer.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.explorer.app.databinding.ActivityMainBinding
import com.explorer.app.databinding.ItemFileBinding
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
        
        // 1. Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Setup RecyclerView with optimized settings
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
            setHasFixedSize(true) // Increases performance for large folders
        }

        // 3. Initial file load
        loadFiles(currentPath)
    }

    private fun loadFiles(directory: File) {
        // Run on the Main Scope but switch to IO for disk reading
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                try {
                    directory.listFiles()?.sortedWith(
                        compareBy({ !it.isDirectory }, { it.name.lowercase() })
                    ) ?: emptyList()
                } catch (e: Exception) {
                    emptyList<File>()
                }
            }
            
            // Submit the list to the adapter (handled by DiffUtil)
            fileAdapter.submitList(files)
        }
    }

    private fun navigateTo(file: File) {
        if (file.isDirectory) {
            currentPath = file
            loadFiles(currentPath)
        }
    }

    override fun onBackPressed() {
        // Custom back logic to navigate up folders instead of closing app
        if (currentPath.absolutePath != "/storage/emulated/0") {
            val parent = currentPath.parentFile
            if (parent != null) {
                currentPath = parent
                loadFiles(currentPath)
            }
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    // --- High Performance Adapter ---
    class FileAdapter(private val onClick: (File) -> Unit) : 
        ListAdapter<File, FileAdapter.ViewHolder>(FileDiffCallback()) {

        inner class ViewHolder(val b: ItemFileBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val b = ItemFileBinding.inflate(inflater, parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = getItem(position)
            holder.b.itemName.text = file.name
            
            // Set icons based on type
            val iconRes = if (file.isDirectory) {
                android.R.drawable.ic_menu_directions 
            } else {
                android.R.drawable.ic_menu_help
            }
            holder.b.itemIcon.setImageResource(iconRes)

            holder.root.setOnClickListener { onClick(file) }
        }
    }

    // --- Optimization Engine (DiffUtil) ---
    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(old: File, new: File): Boolean {
            return old.absolutePath == new.absolutePath
        }
        override fun areContentsTheSame(old: File, new: File): Boolean {
            // Checks if the file modified date or name changed
            return old == new && old.lastModified() == new.lastModified()
        }
    }
}
