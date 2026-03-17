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
        when {
            file.isDirectory -> {
                holder.binding.ivFileIcon.setImageResource(R.drawable.ic_folder)
                holder.binding.ivFileIcon.clearColorFilter()
            }
            /* Android & System */
            ext == "apk" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_android)
            ext in listOf("dex", "so", "bin", "elf") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_exe)
            ext == "firmware" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_firmware)
            
            /* CodingTrolling Dev (Kotlin, Java, Python, Scripts) */
            ext in listOf("kt", "kts") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_code)
            ext == "java" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_java)
            ext == "py" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_python)
            ext in listOf("js", "ts", "php", "sh") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_script)
            ext in listOf("asm", "s") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_assembly)
            
            /* Minecraft & Gaming */
            ext in listOf("mcpack", "mcaddon") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_minecraft)
            ext == "mcworld" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_mc_world)
            ext == "rbxl" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_roblox)
            
            /* Data & Configs */
            ext in listOf("json", "xml", "yml", "yaml") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_config)
            ext in listOf("db", "sqlite") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_database)
            ext == "log" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_log)
            
            /* Media & Documents */
            ext in listOf("zip", "rar", "7z") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_archive)
            ext in listOf("jpg", "png", "webp") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_vector)
            ext in listOf("mp4", "mkv") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_video)
            ext in listOf("mp3", "wav", "ogg") -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_troll_audio)
            ext == "pdf" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_pdf)
            ext == "md" -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_markdown)
            
            /* Default */
            else -> holder.binding.ivFileIcon.setImageResource(R.drawable.ic_file_default)
        }
        // Optional: Remove global tint if you want to see the original PNG colors
        holder.binding.ivFileIcon.clearColorFilter()
        holder.binding.fileIcon.setImageResource(iconRes)

        holder.binding.root.setOnClickListener { onClick(file) }
        holder.binding.root.setOnLongClickListener { 
            onLongClick(file)
            true 
        }
    }

    override fun getItemCount() = files.size
}
