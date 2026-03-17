package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingtrolling.trollexplorer.databinding.ItemFileBinding
import java.io.File

class FileAdapter(
    private var files: List<File>,
    private val onClick: (File) -> Unit,
    private val onLongClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    fun updateData(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileName.text = file.name
        
        // Use a safe fallback for icons to prevent unresolved reference errors
        val iconRes = when {
            file.isDirectory -> R.drawable.ic_folder
            else -> R.drawable.ic_file_default
        }
        holder.binding.fileIcon.setImageResource(iconRes)

        holder.binding.root.setOnClickListener { onClick(file) }
        holder.binding.root.setOnLongClickListener { 
            onLongClick(file)
            true 
        }
    }

    override fun getItemCount() = files.size
}
