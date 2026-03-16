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

            // --- ICON LOGIC ---
            if (file.isDirectory) {
                // Folder Icon
                b.itemIcon.setImageResource(android.R.drawable.ic_menu_directions)
            } else {
                when (ext) {
                    // JAVA / JAR = Coffee Icon (Using 'today' calendar icon as placeholder)
                    "java", "jar", "class" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_today)
                    }
                    // PYTHON = Snake Icon (Using 'send' paper plane as placeholder)
                    "py", "pyw", "pyc" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_send)
                    }
                    // ISO / IMG = CD Icon (Using 'save' floppy as placeholder)
                    "iso", "img", "bin" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_save)
                    }
                    // APK = Android Logo
                    "apk" -> {
                        b.itemIcon.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                    // WINDOWS = EXE / BAT
                    "exe", "bat", "msi" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_manage)
                    }
                    // LINUX = SH
                    "sh" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                    }
                    // PDF = Adobe
                    "pdf" -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    }
                    // IMAGES = Previews
                    "png", "jpg", "jpeg", "webp", "gif" -> {
                        Glide.with(b.itemIcon.context)
                            .load(file)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(b.itemIcon)
                    }
                    // DEFAULT
                    else -> {
                        b.itemIcon.setImageResource(android.R.drawable.ic_menu_help)
                    }
                }
            }

            // --- ACTIONS ---
            b.root.setOnClickListener { onClick(file) }
            
            b.root.setOnLongClickListener {
                val info = "Type: ${if(file.isDirectory) "Folder" else "File"}\nSize: ${file.length() / 1024} KB"
                Toast.makeText(b.root.context, info, Toast.LENGTH_LONG).show()
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
