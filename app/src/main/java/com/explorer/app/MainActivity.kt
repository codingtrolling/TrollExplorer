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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        updateList(currentPath)
    }

    private fun updateList(path: File) {
        try {
            val fileArray = path.listFiles()
            val files = fileArray?.toList() ?: emptyList()
            
            if (fileArray == null) {
                Toast.makeText(this, "Permission Denied or Path Invalid", Toast.LENGTH_LONG).show()
            }

            recyclerView.adapter = FileAdapter(files) { selectedFile ->
                if (selectedFile.isDirectory) {
                    currentPath = selectedFile
                    updateList(currentPath)
                } else {
                    Toast.makeText(this, "Opening: " + selectedFile.name, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (currentPath.path != "/storage/emulated/0" && currentPath.parentFile != null) {
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
            val icon: ImageView = view.findViewById(R.id.item_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.nameText.text = file.name
            holder.icon.setImageResource(if (file.isDirectory) android.R.drawable.ic_menu_directions else android.R.drawable.ic_menu_help)
            holder.itemView.setOnClickListener { onClick(file) }
        }

        override fun getItemCount(): Int = files.size
    }
}
