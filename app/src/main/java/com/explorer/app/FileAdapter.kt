package com.explorer.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private var allFiles: Array<File>, 
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var filteredFiles = allFiles

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        val details: TextView = view.findViewById(R.id.item_details)
        val icon: ImageView = view.findViewById(R.id.item_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = filteredFiles[position]
        holder.name.text = file.name
        
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(file.lastModified()))
        
        if (file.isDirectory || !file.name.contains(".")) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_directions)
            holder.details.text = "$date | Folder"
        } else {
            holder.icon.setImageResource(android.R.drawable.ic_menu_save)
            val size = if (file.length() > 0) "${file.length() / 1024} KB" else "0 KB"
            holder.details.text = "$date | $size"
        }

        holder.itemView.setOnClickListener { onItemClick(file) }
        holder.itemView.setOnLongClickListener { onItemLongClick(file); true }
    }

    override fun getItemCount() = filteredFiles.size

    fun filter(query: String) {
        filteredFiles = if (query.isEmpty()) allFiles 
        else allFiles.filter { it.name.contains(query, true) }.toTypedArray()
        notifyDataSetChanged()
    }
}
