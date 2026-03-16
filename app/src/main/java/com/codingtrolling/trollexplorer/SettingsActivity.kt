package com.codingtrolling.trollexplorer

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.codingtrolling.trollexplorer.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("troll_prefs", Context.MODE_PRIVATE)

        supportActionBar?.apply {
            title = "Troll Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        // Load saved states
        binding.switchHiddenFiles.isChecked = prefs.getBoolean("show_hidden", false)
        binding.switchRootMode.isChecked = prefs.getBoolean("root_mode", false)

        // Save on change
        binding.switchHiddenFiles.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_hidden", isChecked).apply()
        }

        binding.switchRootMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("root_mode", isChecked).apply()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
