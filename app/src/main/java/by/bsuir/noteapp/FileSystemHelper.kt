package by.bsuir.noteapp

import android.content.Context
import java.io.*
import java.util.*

class FileSystemHelper(val context: Context) : NoteDao {
    private val fileName = "notes.txt"

    override fun insertNote(note: Note) {
        val notes = getAllNotes().toMutableList()
        notes.add(note)
        writeNotesToFile(notes)
    }

    override fun updateNote(note: Note) {
        val notes = getAllNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
        }
        writeNotesToFile(notes)
    }

    override fun deleteNote(note: Note) {
        val notes = getAllNotes().toMutableList()
        notes.removeIf { it.id == note.id }
        writeNotesToFile(notes)
    }

    override fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val ois = ObjectInputStream(FileInputStream(file))
                notes.addAll(ois.readObject() as List<Note>)
                ois.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return notes
    }

    override fun getNoteById(id: Long): Note {
        val notes = getAllNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        return notes[index]
    }

    private fun writeNotesToFile(notes: List<Note>) {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            val oos = ObjectOutputStream(FileOutputStream(file))
            oos.writeObject(ArrayList(notes))
            oos.flush()
            oos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
