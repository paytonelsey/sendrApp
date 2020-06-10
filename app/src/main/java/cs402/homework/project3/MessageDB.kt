package cs402.homework.project3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Database methods for the messages SQLite file.
 * @author Payton Elsey
 */
class MessageDB(context: Context, factory: SQLiteDatabase.CursorFactory?) :

    SQLiteOpenHelper(
        context, DATABASE_NAME,
        factory, DATABASE_VERSION
    ) {

    /**
     * onCreate: creates the SQLite database table.
     * @param db, SQLiteDatabase
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " +
                TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_SUBJECT + " TEXT, " +
                COLUMN_DT + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_FILES + " BLOB" + ")"
                )

        db.execSQL(createTable)
    }

    /**
     * onUpgrade: If the table exists, drop it and start a new table.
     * @param db, SQLiteDatabase
     * @param oldVersion, int
     * @param newVersion, int
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Insert a message into the database.
     * @param message, MessageModel
     * @return Long, id, the unique ID of the inserted message
     */
    fun addMessage(message: MessageModel): Long {
        val values = ContentValues()
        values.put(COLUMN_EMAIL, message.email)
        values.put(COLUMN_MESSAGE, message.message)
        values.put(COLUMN_SUBJECT, message.subject)
        values.put(COLUMN_DT, message.datetime)
        values.put(COLUMN_LOCATION, message.location)
        values.put(COLUMN_FILES, message.files)
        val db = this.writableDatabase
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    /**
     * Get a message from the database.
     * @param id, Long, the unique ID given to the message when inserted
     * @return MessageModel? or null if it could not be located
     */
    fun getMessage(id: Long): MessageModel? {
        val idStr = id.toString()
        val db = this.readableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        db.rawQuery(selectQuery, arrayOf(idStr)).use {
            if (it.moveToFirst()) {
                val result = MessageModel()
                result.message = it.getString(it.getColumnIndex(COLUMN_MESSAGE))
                result.email = it.getString(it.getColumnIndex(COLUMN_EMAIL))
                result.datetime = it.getString(it.getColumnIndex(COLUMN_DT))
                result.files = it.getBlob(it.getColumnIndex(COLUMN_FILES))
                result.location = it.getString(it.getColumnIndex(COLUMN_LOCATION))
                result.subject = it.getString(it.getColumnIndex(COLUMN_SUBJECT))
                return result
            }
        }
        return null
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "messageSendr.db"
        const val TABLE_NAME = "messages"
        const val COLUMN_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_DT = "sendDateTime"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_FILES = "file"
        const val COLUMN_SUBJECT = "subject"
    }
}