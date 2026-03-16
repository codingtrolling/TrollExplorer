package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codingtrolling.trollexplorer.databinding.ItemFileBinding
import java.io.File

class FileAdapter(private val onClick: (File) -> Unit) :
    ListAdapter<File, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.fileName.text = file.name
            
            val iconRes = when {
                file.isDirectory -> R.drawable.ic_folder
                file.name.endsWith(".java") || file.name.endsWith(".jar") -> R.drawable.ic_java
                file.name.endsWith(".py") -> R.drawable.ic_python
                file.name.endsWith(".exe") || file.name.endsWith(".bin") -> R.drawable.ic_exe
                file.name.endsWith(".iso") || file.name.endsWith(".img") -> R.drawable.ic_disk
                else -> R.drawable.ic_file_default
            }
            
            binding.fileIcon.setImageResource(iconRes)
            binding.root.setOnClickListener { onClick(file) }
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File) = oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File) = oldItem == newItem
    }
}
