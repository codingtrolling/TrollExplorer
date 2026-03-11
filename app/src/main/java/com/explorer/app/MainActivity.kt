package com.explorer.app

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rikka.shizuku.Shizuku
import java.io.*
import java.util.zip.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pathView: TextView
    private lateinit var storageBar: ProgressBar
    private lateinit var storageText: TextView
    
    private var currentPath: String = Environment.getExternalStorageDirectory().path
    private val userRoot = Environment.getExternalStorageDirectory().path
    private var showHidden: Boolean = false
    private var currentSort: Int = 0 // 0: Name, 1: Size

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, result ->
        if (result == PackageManager.PERMISSION_GRANTED) loadDirectory(currentPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        pathView = findViewById(R.id.path_view)
        storageBar = findViewById(R.id.storage_bar)
        storageText = findViewById(R.id.storage_text)
        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)
        val searchView: SearchView = findViewById(R.id.search_view)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        Shizuku.addRequestPermissionResultListener(permissionListener)

        checkPermissions()
        loadDirectory(currentPath)
        updateStorageInfo()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                (recyclerView.adapter as? FileAdapter)?.filter(q ?: "")
                return true
            }
        })

        fabAdd.setOnClickListener { showNewFolderDialog() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentPath != "/" && currentPath != userRoot) {
                    val parent = File(currentPath).parent ?: "/"
                    loadDirectory(parent)
                } else finish()
            }
        })
    }

    private fun loadDirectory(path: String) {
        currentPath = path
        pathView.text = path
        val filesList = mutableListOf<File>()

        if (path.startsWith(userRoot)) {
            // NORMAL MODE
            val dir = File(path)
            val files = dir.listFiles()?.toMutableList() ?: mutableListOf()
            filesList.addAll(files)
        } else {
            // SHIZUKU MODE
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                try {
                    val process = Shizuku.newProcess(arrayOf("ls", "-1", "-F", path), null, null)
                    BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                        val name = if (line.endsWith("/") || line.endsWith("@")) line.dropLast(1) else line
                        filesList.add(File(path, name))
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Shizuku Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Shizuku.requestPermission(0)
            }
        }

        if (!showHidden) filesList.retainAll { !it.name.startsWith(".") }
        filesList.sortWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        recyclerView.adapter = FileAdapter(filesList.toTypedArray(), 
            { f -> if (f.isDirectory || !f.extension.contains(".")) loadDirectory(f.absolutePath) else openFile(f) },
            { f -> showOptions(f) }
        )
    }

    private fun showOptions(file: File) {
        val isZip = file.extension.lowercase() == "zip"
        val options = arrayOf(if (isZip) "Extract" else "Compress", "Rename", "Delete")
        AlertDialog.Builder(this).setTitle(file.name).setItems(options) { _, w ->
            when (w) {
                0 -> { if (isZip) unzipFile(file) else zipFile(file); updateStorageInfo() }
                1 -> showRenameDialog(file)
                2 -> if (file.deleteRecursively()) { loadDirectory(currentPath); updateStorageInfo() }
            }
        }.show()
    }

    private fun zipFile(src: File) {
        try {
            ZipOutputStream(FileOutputStream(File(src.parent, src.name + ".zip"))).use { zos ->
                zos.putNextEntry(ZipEntry(src.name))
                src.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
            loadDirectory(currentPath)
        } catch (e: Exception) { Toast.makeText(this, "Zip Failed", Toast.LENGTH_SHORT).show() }
    }

    private fun unzipFile(zip: File) {
        try {
            val outDir = File(zip.parent, zip.nameWithoutExtension)
            if (!outDir.exists()) outDir.mkdir()
            ZipInputStream(zip.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    FileOutputStream(File(outDir, entry.name)).use { zis.copyTo(it) }
                    entry = zis.nextEntry
                }
            }
            loadDirectory(currentPath)
        } catch (e: Exception) { Toast.makeText(this, "Unzip Failed", Toast.LENGTH_SHORT).show() }
    }

    private fun updateStorageInfo() {
        val stat = StatFs(userRoot)
        val total = stat.blockCountLong * stat.blockSizeLong
        val avail = stat.availableBlocksLong * stat.blockSizeLong
        val used = total - avail
        val pct = if (total > 0) (used * 100 / total).toInt() else 0
        storageBar.progress = pct
        storageText.text = "Storage: $pct% used (${used/1024/1024}MB / ${total/1024/1024}MB)"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Root (/)").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, 2, 1, "Home").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, 3, 2, "Show Hidden").setCheckable(true).isChecked = showHidden
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> loadDirectory("/")
            2 -> loadDirectory(userRoot)
            3 -> { showHidden = !showHidden; item.isChecked = showHidden; loadDirectory(currentPath) }
        }
        return true
    }

    private fun checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:" + packageName)
                })
            }
        }
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Open with..."))
    }

    private fun showNewFolderDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this).setTitle("New Folder").setView(input).setPositiveButton("Create") { _, _ ->
            if (File(currentPath, input.text.toString()).mkdir()) loadDirectory(currentPath)
        }.show()
    }

    private fun showRenameDialog(file: File) {
        val input = EditText(this).apply { setText(file.name) }
        AlertDialog.Builder(this).setTitle("Rename").setView(input).setPositiveButton("OK") { _, _ ->
            if (file.renameTo(File(file.parent, input.text.toString()))) loadDirectory(currentPath)
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }
}
