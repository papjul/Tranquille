package fr.vinetos.tranquille.presentation.denylist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.vinetos.tranquille.App
import fr.vinetos.tranquille.CustomVerticalDivider
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.data.DenylistItem
import fr.vinetos.tranquille.data.YacbHolder
import fr.vinetos.tranquille.data.datasource.DenylistDataSource
import fr.vinetos.tranquille.utils.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class DenylistActivity : AppCompatActivity() {

    private val LOG: Logger = LoggerFactory.getLogger(DenylistActivity::class.java)

    private lateinit var denylistItemAdapter: DenylistItemAdapter
    private lateinit var denylistItemListView: RecyclerView
    private lateinit var denylistDataSource: DenylistDataSource

    private val settings = App.getSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denylist)

        denylistDataSource = YacbHolder.getDenylistDataSource()
        denylistItemAdapter = DenylistItemAdapter(denylistDataSource.getAll())

        denylistItemListView = findViewById(R.id.denylistItemsList)
        denylistItemListView.apply {
            adapter = denylistItemAdapter
            layoutManager = LinearLayoutManager(this@DenylistActivity)
            addItemDecoration(CustomVerticalDivider(this@DenylistActivity))
        }

        findViewById<FloatingActionButton>(R.id.fab).apply {
            visibility = FloatingActionButton.VISIBLE
            setOnClickListener {
                startActivity(Intent(this@DenylistActivity, EditDenylistItemActivity::class.java))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_denylist, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_block_denylisted)?.setChecked(settings.blockDenylisted)
        return super.onPrepareOptionsMenu(menu)
    }

    fun onBlockDenylistedChanged(item: MenuItem) {
        settings.blockDenylisted = !item.isChecked
    }

    fun onAddClicked(view: View?) {
        startActivity(EditDenylistItemActivity.getIntent(this, null, null))
    }

    private fun onItemClicked(denylistItem: DenylistItem) {
        startActivity(EditDenylistItemActivity.getIntent(this, denylistItem.id))
    }

    fun onExportDenylistClicked(item: MenuItem?) {
        val file = exportDenylist()
        if (file != null) {
            FileUtils.shareFile(this, file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportDenylist(): File? {
        val file = File(cacheDir, "Tranquille_backup.csv")
        try {
            if (!file.exists() && !file.createNewFile()) return null

            return null
            // FIXME
            /*FileWriter(file).use { writer ->
                if (DenylistImporterExporter().writeBackup(denylistDao.loadAll(), writer)) {
                    return file
                }
            }*/
        } catch (e: IOException) {
            LOG.warn("exportDenylist()", e)
        }

        return null
    }

    fun onImportDenylistClicked(item: MenuItem?) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")

        try {
            startActivityForResult(intent, REQUEST_CODE_IMPORT)
        } catch (e: ActivityNotFoundException) {
            LOG.warn("onImportDenylistClicked()", e)
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val REQUEST_CODE_IMPORT: Int = 1
    }

}