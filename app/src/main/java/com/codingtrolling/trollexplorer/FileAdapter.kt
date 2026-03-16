package com.codingtrolling.trollexplorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codingtrolling.trollexplorer.databinding.ItemFileBinding
import java.io.File

class FileAdapter(private val onClick: (File) -> Unit) : 
    ListAdapter<File, FileAdapter.ViewHolder>(FileDiffCallback()) {

    inner class ViewHolder(val b: ItemFileBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = getItem(position)
        holder.b.itemName.text = file.name
        holder.b.itemIcon.setImageResource(
            if (file.isDirectory) android.R.drawable.ic_menu_directions 
            else android.R.drawable.ic_menu_help
        )
        holder.root.setOnClickListener { onClick(file) }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(old: File, new: File) = old.absolutePath == new.absolutePath
        override fun areContentsTheSame(old: File, new: File) = old == new
    }
}
