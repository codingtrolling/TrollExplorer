package com.explorer.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var currentPath: File = File("/storage/emulated/0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState: Bundle?)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        updateList(currentPath)
    }

    private fun updateList(path: File) {
        val files = path.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        recyclerView.adapter = FileAdapter(files) { selectedFile ->
            if (selectedFile.isDirectory) {
                currentPath = selectedFile
                updateList(currentPath)
            } else {
                Toast.makeText(this, "Opening: ${selectedFile.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (currentPath.parentFile != null && currentPath.path != "/storage/emulated/0") {
            currentPath = currentPath.parentFile!!
            updateList(currentPath)
        } else {
            super.onBackPressed()
        }
    }

    class FileAdapter(
        private val files: List<File>,
        private val onClick: (File) -> Unit
    ) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.item_name)
            val detailText: TextView = view.findViewById(R.id.item_details)
            val icon: ImageView = view.findViewById(R.id.item_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.nameText.text = file.name
            holder.detailText.text = if (file.isDirectory) "Folder" else "${file.length() / 1024} KB"
            
            // Basic icon logic
            val iconRes = if (file.isDirectory) {
                android.R.drawable.ic_menu_archive
            } else {
                android.R.drawable.ic_menu_report_image
            }
            holder.icon.setImageResource(iconRes)

            holder.itemView.setOnClickListener { onClick(file) }
        }

        override fun getItemCount() = files.size
    }
}
