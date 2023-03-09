package by.bsuir.noteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_note.*
import java.util.*

class EditNoteActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTE = "extra_note"
    }

    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Заметки"

        var isEdit = false
        note = intent.getSerializableExtra(EXTRA_NOTE) as? Note
        if (note != null) {
            edit_title.setText(note!!.title)
            edit_description.setText(note!!.description)
            isEdit = true
        } else {
            note = Note(System.currentTimeMillis(), "", "" )
        }

        button_save.setOnClickListener {
            if (edit_title.text.toString().trim().isEmpty() || edit_description.text.toString()
                    .trim().isEmpty()
            ) {
                Toast.makeText(this, "Please insert a title and description", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val title = edit_title.text.toString()
            val description = edit_description.text.toString()

            val newNote = if (isEdit) {
                Note(note!!.id, title, description)
            } else {
                Note(UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE, title, description)
            }

            val intent = Intent()
            intent.putExtra(MainActivity.EXTRA_NOTE, newNote)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}