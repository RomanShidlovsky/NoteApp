package by.bsuir.noteapp

import android.content.Context
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import by.bsuir.noteapp.R
import by.bsuir.noteapp.Note
import kotlinx.android.synthetic.main.activity_edit_note.view.*
import kotlinx.android.synthetic.main.item_note.view.*
import kotlinx.coroutines.*
import java.io.*
import java.time.format.DateTimeFormatter
import java.util.*

class NoteAdapter(private val context: Context) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val notes = mutableListOf<Note>()
    private var filteredNotes = mutableListOf<Note>()
    private var listener: OnItemClickListener? = null
    private val databaseHelper = DatabaseHelper.getInstance(context)
    private val fileSystemHelper = FileSystemHelper(context)

    enum class StorageOptions {
        FileSystem,
        Database
    }

    init {
        loadNotes()
    }

    private fun loadNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val databaseNotes = databaseHelper.noteDao().getAllNotes()
            val fileSystemNotes = fileSystemHelper.getAllNotes()

            withContext(Dispatchers.Main) {
                notes.clear()
                notes.addAll(databaseNotes)
                notes.addAll(fileSystemNotes)
                filteredNotes.addAll(notes)
                notes.sortBy { it.modificationTime }
                notifyDataSetChanged()
            }
        }
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        this.notes.addAll(notes)
        notifyDataSetChanged()
    }

    fun filterNotes(query: String) {
        if (query.isEmpty()) {
            filteredNotes.clear()
            filteredNotes.addAll(notes)
        } else {
            filteredNotes.clear()
            notes.forEach{
                if (it.title.contains(query, true) || it.description.contains(query, true)) {
                    filteredNotes.add(it)
                }
            }
        }
        notifyDataSetChanged()
    }


    fun addNote(note: Note) {
        notes.add(note)
        onNoteChange()
        notifyItemInserted(notes.size - 1)
        CoroutineScope(Dispatchers.IO).launch {
            when (note.storageOptions) {
                StorageOptions.FileSystem -> fileSystemHelper.insertNote(note)
                StorageOptions.Database -> {
                    val noteDao = databaseHelper.noteDao()
                    noteDao.insertNote(note)
                }

            }
        }
    }

    private fun onNoteChange()
    {
        filteredNotes.clear()
        filteredNotes.addAll(notes)
    }

    fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
            onNoteChange()
            notifyItemChanged(index)
            CoroutineScope(Dispatchers.IO).launch {
                when (note.storageOptions) {
                    StorageOptions.FileSystem -> fileSystemHelper.updateNote(note)
                    StorageOptions.Database -> {
                        val noteDao = databaseHelper.noteDao()
                        noteDao.updateNote(note)
                    }
                }
            }

        }
    }

    fun deleteNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes.removeAt(index)
            onNoteChange()
            notifyItemRemoved(index)
            CoroutineScope(Dispatchers.IO).launch {
                when (note.storageOptions) {
                    StorageOptions.FileSystem -> fileSystemHelper.deleteNote(note)
                    StorageOptions.Database ->{
                        val noteDao = databaseHelper.noteDao()
                        noteDao.deleteNote(note)
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = filteredNotes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val popup = PopupMenu(context, holder.itemView, Gravity.END)
        popup.inflate(R.menu.popup_menu)
        popup.setForceShowIcon(true)
        holder.bind(filteredNotes[position], listener, popup)
    }

    interface OnItemClickListener {
        fun onItemClick(note: Note)
        fun onItemLongClick(note: Note, popupMenu: PopupMenu)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(note: Note, listener: OnItemClickListener?, popupMenu: PopupMenu) {
            itemView.title_text.text = note.title
            itemView.description_text.text = note.description
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            itemView.time_text.text = note.modificationTime.format(formatter)
            itemView.setOnClickListener {
                listener?.onItemClick(note)
            }
            itemView.setOnLongClickListener {
                listener?.onItemLongClick(note, popupMenu)
                true
            }

        }
    }
}