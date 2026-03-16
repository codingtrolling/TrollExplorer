package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
            
            val nameLower = file.name.lowercase()
            val isImage = nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".webp")
            val isVideo = nameLower.endsWith(".mp4") || nameLower.endsWith(".mkv") || nameLower.endsWith(".webm")
                    nameLower.endsWith(".apk") -> R.drawable.ic_android

            if (isImage || isVideo) {
                // Load actual preview thumbnail
                Glide.with(binding.fileIcon.context)
                    .load(file)
                    .centerCrop()
                    .placeholder(R.drawable.ic_file_default)
                    .into(binding.fileIcon)
            } else {
                // Load static icons
                val iconRes = when {
                    file.isDirectory -> R.drawable.ic_folder
                    nameLower.endsWith(".java") || nameLower.endsWith(".jar") -> R.drawable.ic_java
                    nameLower.endsWith(".py") -> R.drawable.ic_python
                    nameLower.endsWith(".exe") || nameLower.endsWith(".bin") -> R.drawable.ic_exe
                    nameLower.endsWith(".iso") || nameLower.endsWith(".img") -> R.drawable.ic_disk_new
                    nameLower.endsWith(".mp3") || nameLower.endsWith(".wav") || nameLower.endsWith(".ogg") -> R.drawable.ic_troll_audio
                    nameLower.endsWith(".mp4") || nameLower.endsWith(".mkv") -> R.drawable.ic_video
                    nameLower.endsWith(".apk") -> R.drawable.ic_android
                    else -> R.drawable.ic_file_default
                }
                binding.fileIcon.setImageResource(iconRes)
            }
            
            binding.root.setOnClickListener { onClick(file) }
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File) = oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File) = oldItem == newItem
    }
}
