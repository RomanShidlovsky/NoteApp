package by.bsuir.noteapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var sharedPref: SharedPreferences
    private lateinit var searchView: SearchView
    private var savedQuery : String? = null

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1
        const val REQUEST_CODE_EDIT_NOTE = 2
        const val EXTRA_NOTE = "extra_note"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Заметки"

        sharedPref = getPreferences(Context.MODE_PRIVATE)
        recyclerView = findViewById(R.id.recycler_view)

        searchView = findViewById(R.id.search_view)
        if (savedInstanceState != null) {
            savedQuery = savedInstanceState.getString("search_query")
        }

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, EditNoteActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE, null)
        }

        adapter = NoteAdapter(applicationContext)

        adapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
                intent.putExtra(EXTRA_NOTE, note)
                startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE, null)
            }

            override fun onItemLongClick(note: Note, popupMenu: PopupMenu) {
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete_note -> {
                            adapter.deleteNote(note)
                        }
                        R.id.edit_note -> {
                            val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
                            intent.putExtra(EXTRA_NOTE, note)
                            startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE, null)
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(EXTRA_NOTE)) {
            val note = data.getSerializableExtra(EXTRA_NOTE) as Note

            when (requestCode) {
                REQUEST_CODE_ADD_NOTE -> {
                    when (getSelectedOption()) {
                        0 -> note.storageOptions = NoteAdapter.StorageOptions.FileSystem
                        1 -> note.storageOptions = NoteAdapter.StorageOptions.Database
                    }
                    adapter.addNote(note)
                }
                REQUEST_CODE_EDIT_NOTE -> adapter.updateNote(note)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu?.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView
        if (savedQuery != null) {
            searchView.setQuery(savedQuery, false)
            searchView.requestFocus()
            adapter.filterNotes(savedQuery.toString())
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filterNotes(newText)
                return true
            }
        })
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.options_menu_item -> {
                val selectedOption = getSelectedOption()
                val dialog = RadioOptionsDialog(this, selectedOption)
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("search_query", searchView.query.toString())
    }

    private fun getSelectedOption(): Int {
        val sharedPref = getSharedPreferences("storage_option", Context.MODE_PRIVATE)
        return sharedPref.getInt("storage_option", 0)
    }

    private fun saveSelectedOption(selectedOption: Int) {
        val sharedPref = getSharedPreferences("storage_option", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("storage_option", selectedOption)
        editor.apply()
    }

    inner class RadioOptionsDialog(
        context: Context,
        private val selectedOption: Int
    ) : AlertDialog(context) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_radio_options)

            val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
            val radioButton1 = findViewById<RadioButton>(R.id.radio_option_1)
            val radioButton2 = findViewById<RadioButton>(R.id.radio_option_2)

            when (selectedOption) {
                0 -> radioButton1.isChecked = true
                1 -> radioButton2.isChecked = true
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_option_1 -> saveSelectedOption(0)
                    R.id.radio_option_2 -> saveSelectedOption(1)
                }
            }
        }
    }
}


