package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingtrolling.trollexplorer.databinding.ItemFileBinding
import java.io.File

class FileAdapter(
    private val files: List<File>,
    private val onClick: (File) -> Unit,
    private val onLongClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileName.text = file.name
        
        val nameLower = file.name.lowercase()
        
        // Comprehensive Icon Mapping
        when {
            file.isDirectory -> holder.binding.fileIcon.setImageResource(R.drawable.ic_folder)
            nameLower.endsWith(".zip") || nameLower.endsWith(".rar") || nameLower.endsWith(".7z") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_archive)
            nameLower.endsWith(".pdf") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_doc)
            nameLower.endsWith(".mp3") || nameLower.endsWith(".wav") || nameLower.endsWith(".ogg") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_music)
            nameLower.endsWith(".exe") || nameLower.endsWith(".msi") || nameLower.endsWith(".apk") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_executable)
            nameLower.endsWith(".iso") || nameLower.endsWith(".img") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_disk)
            nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") -> 
                holder.binding.fileIcon.setImageResource(R.drawable.ic_image)
            else -> holder.binding.fileIcon.setImageResource(R.drawable.ic_file_default)
        }

        holder.binding.root.setOnClickListener { onClick(file) }
        holder.binding.root.setOnLongClickListener { 
            onLongClick(file)
            true 
        }
    }

    override fun getItemCount() = files.size
}
