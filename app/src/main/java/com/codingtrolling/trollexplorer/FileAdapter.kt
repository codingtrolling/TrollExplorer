package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingtrolling.trollexplorer.databinding.ItemFileBinding
import java.io.File

class FileAdapter(private val onClick: (File) -> Unit) : 
    ListAdapter<File, FileAdapter.ViewHolder>(FileDiffCallback()) {

    inner class ViewHolder(val b: ItemFileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(file: File) {
            b.itemName.text = file.name
            val ext = file.extension.lowercase()

            if (file.isDirectory) {
                // Folder Icon
                b.itemIcon.setImageResource(android.R.drawable.ic_menu_directions)
            } else {
                when (ext) {
                    "apk" -> {
                        // Android Logo (Using system app icon as placeholder)
                        b.itemIcon.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                    "exe", "bat" -> {
                        // Windows Logos (Using management icon as placeholder)
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_manage)
                    }
                    "sh" -> {
                        // Linux Logo (Using info icon as placeholder)
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                    }
                    "pdf" -> {
                        // PDF Logo (Using edit icon as placeholder)
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    }
                    "png", "jpg", "jpeg", "webp", "gif" -> {
                        // Image Preview using Glide
                        Glide.with(b.itemIcon.context)
                            .load(file)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(b.itemIcon)
                    }
                    else -> {
                        // Default File Icon
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_help)
                    }
                }
            }

            b.root.setOnClickListener { onClick(file) }
            
            // Long click for "Trolling" actions (Delete/Rename/Info)
            b.root.setOnLongClickListener {
                Toast.makeText(b.root.context, "Selected: ${file.name}", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(old: File, new: File) = old.absolutePath == new.absolutePath
        override fun areContentsTheSame(old: File, new: File) = old == new
    }
}
