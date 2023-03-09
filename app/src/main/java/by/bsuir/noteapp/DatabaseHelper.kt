package by.bsuir.noteapp

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import androidx.room.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId



@Database(entities = [Note::class], version = 2, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class)
abstract class DatabaseHelper : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    DatabaseHelper::class.java,
                    "notes_db"
                )
                    .build()
            }
            return instance!!
        }
    }
}

class LocalDateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return if (value == null) null else LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note)

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("SELECT * FROM note")
    fun getAllNotes(): List<Note>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNoteById(id: Long): Note
}

