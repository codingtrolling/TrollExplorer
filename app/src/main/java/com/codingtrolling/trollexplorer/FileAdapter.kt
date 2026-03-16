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
            
            when {
                file.isDirectory -> binding.fileIcon.setImageResource(R.drawable.ic_folder)
                nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || 
                nameLower.endsWith(".mp4") || nameLower.endsWith(".mkv") -> {
                    Glide.with(binding.fileIcon.context)
                        .load(file)
                        .centerCrop()
                        .placeholder(R.drawable.ic_file_default)
                        .into(binding.fileIcon)
                }
                nameLower.endsWith(".mcaddon") || nameLower.endsWith(".mcpack") -> {
                    binding.fileIcon.setImageResource(R.drawable.ic_minecraft)
                }
                nameLower.endsWith(".rbxl") || nameLower.endsWith(".rbxm") -> {
                    binding.fileIcon.setImageResource(R.drawable.ic_roblox)
                }
                else -> {
                    val iconRes = when {
                        nameLower.endsWith(".mp3") || nameLower.endsWith(".wav") -> R.drawable.ic_troll_audio
                        nameLower.endsWith(".apk") -> R.drawable.ic_android
                        nameLower.endsWith(".exe") || nameLower.endsWith(".msi") -> R.drawable.ic_exe
                        else -> R.drawable.ic_file_default
                    }
                    binding.fileIcon.setImageResource(iconRes)
                }
            }
            binding.root.setOnClickListener { onClick(file) }
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File) = oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File) = oldItem == newItem
    }
}
