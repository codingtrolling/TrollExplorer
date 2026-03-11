package com.explorer.app

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Size
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

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = filteredFiles[position]
        holder.name.text = file.name
        
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))
        
        if (file.isDirectory) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_directions)
            val items = file.listFiles()?.size ?: 0
            holder.details.text = "$date | $items items"
        } else {
            val size = formatFileSize(file.length())
            holder.details.text = "$date | $size"

            // Thumbnail Logic
            if (isImageFile(file.path)) {
                val thumb = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(file.path), 64, 64
                )
                if (thumb != null) {
                    holder.icon.setImageBitmap(thumb)
                } else {
                    holder.icon.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                holder.icon.setImageResource(android.R.drawable.ic_menu_save)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(file) }
        holder.itemView.setOnLongClickListener { onItemLongClick(file); true }
    }

    private fun isImageFile(path: String): Boolean {
        val extensions = arrayOf(".jpg", ".jpeg", ".png", ".bmp", ".webp")
        return extensions.any { path.lowercase().endsWith(it) }
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun filter(query: String) {
        filteredFiles = if (query.isEmpty()) allFiles else allFiles.filter { it.name.contains(query, true) }.toTypedArray()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
        FileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

    override fun getItemCount() = filteredFiles.size
}
