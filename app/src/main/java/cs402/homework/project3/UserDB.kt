package cs402.homework.project3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Database methods for the users SQLite file.
 * @author Payton Elsey
 */
class UserDB(context: Context, factory: SQLiteDatabase.CursorFactory?) :

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
                COLUMN_EMAIL + " TEXT PRIMARY KEY," +
                COLUMN_NAME_FIRST + " TEXT, " +
                COLUMN_NAME_LAST + " TEXT, " +
                COLUMN_PIC + " BLOB, " +
                COLUMN_PASS + " TEXT" + ")"
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
     * Insert a user into the database.
     * @param user, UserModel
     */
    fun addUser(user: UserModel) {
        val values = ContentValues()
        values.put(COLUMN_EMAIL, user.email)
        values.put(COLUMN_NAME_FIRST, user.firstName)
        values.put(COLUMN_NAME_LAST, user.lastName)
        values.put(COLUMN_PASS, user.password)
        values.put(COLUMN_PIC, user.profilePic)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    /**
     * Determines if the user already exists in the database.
     * @param, user, UserModel
     * @return boolean, if it exists
     */
    fun userExists(user: UserModel): Boolean {
        val db = this.writableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ? "
        db.rawQuery(selectQuery, arrayOf(user.email)).use {
            if (it.moveToFirst()) {
                return true
            }
        }
        return false
    }

    /**
     * Determines if the email is already being used.
     * @param email, String
     * @return boolean, if it's being used or not
     */
    fun emailUsed(email: String): Boolean {
        val db = this.writableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ? "
        db.rawQuery(selectQuery, arrayOf(email)).use {
            if (it.moveToFirst()) {
                return true
            }
        }
        return false
    }

    /**
     * Get a user from the database.
     * @param email, String, the unique email to look up
     * @return UserModel? or null if it could not be located
     */
    fun getUser(email: String): UserModel? {
        val db = this.readableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?"
        db.rawQuery(selectQuery, arrayOf(email)).use {
            if (it.moveToFirst()) {
                val result = UserModel()
                result.email = it.getString(it.getColumnIndex(COLUMN_EMAIL))
                result.firstName = it.getString(it.getColumnIndex(COLUMN_NAME_FIRST))
                result.lastName = it.getString(it.getColumnIndex(COLUMN_NAME_LAST))
                result.password = it.getString(it.getColumnIndex(COLUMN_PASS))
                result.profilePic = it.getBlob(it.getColumnIndex(COLUMN_PIC))
                return result
            }
        }
        return null
    }

    /**
     * Update this user's profile picture in the database.
     * @param pic, ByteArray
     * @param email, String
     */
    fun updatePic(pic: ByteArray, email: String) {
        val db = this.readableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_PIC, pic)
        val whereClause = "$COLUMN_EMAIL = ?"
        val whereArgs = arrayOf(email)
        db.update(TABLE_NAME, cv, whereClause, whereArgs)
    }

    /**
     * Update this user's first and last name in the database
     * @param first, String
     * @param last, String
     * @param email, String
     */
    fun updateName(first: String, last: String, email: String) {
        val db = this.readableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_NAME_LAST, last)
        cv.put(COLUMN_NAME_FIRST, first)
        val whereClause = "$COLUMN_EMAIL = ?"
        val whereArgs = arrayOf(email)
        db.update(TABLE_NAME, cv, whereClause, whereArgs)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "sendr.db"
        const val TABLE_NAME = "users"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_NAME_FIRST = "firstName"
        const val COLUMN_NAME_LAST = "lastName"
        const val COLUMN_PASS = "password"
        const val COLUMN_PIC = "profilePic"
    }
}