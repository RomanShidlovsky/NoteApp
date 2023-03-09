package by.bsuir.noteapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime

@Entity
data class Note(
    @PrimaryKey(autoGenerate = false)val id: Long,
    @ColumnInfo(name = "title")var title: String,
    @ColumnInfo(name = "description")var description: String,
    @ColumnInfo(name = "modification_time")var modificationTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "storage_option") var storageOptions: NoteAdapter.StorageOptions = NoteAdapter.StorageOptions.FileSystem
) : Serializable
