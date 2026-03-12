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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
        }

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

    override fun onBackPressed() {
        if (currentPath.absolutePath != "/storage/emulated/0") {
            currentPath = currentPath.parentFile ?: currentPath
            loadFiles(currentPath)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    class FileAdapter(private val onClick: (File) -> Unit) : 
        ListAdapter<File, FileAdapter.ViewHolder>(FileDiffCallback()) {

        inner class ViewHolder(val b: ItemFileBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = getItem(position)
            holder.b.itemName.text = file.name
            holder.b.itemIcon.setImageResource(
                if (file.isDirectory) android.R.drawable.ic_menu_directions 
                else android.R.drawable.ic_menu_help
            )
            holder.root.setOnClickListener { onClick(file) }
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(old: File, new: File) = old.absolutePath == new.absolutePath
        override fun areContentsTheSame(old: File, new: File) = old == new
    }
}
