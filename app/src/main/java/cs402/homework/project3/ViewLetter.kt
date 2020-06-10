package cs402.homework.project3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_letter.*
import java.sql.SQLException
import kotlin.system.exitProcess

/**
 * Activity for viewing the delivered letter. This is the activity that gets started when the
 * notification for a letter delivery is clicked and displays everything on screen.
 * @author Payton Elsey
 */
class ViewLetter : AppCompatActivity() {

    private val messDB: MessageDB = MessageDB(this, null)

    companion object {
        var message_Id = 0L
    }

    /**
     * onCreate: Sets all of the information from the letter to be displayed and the listener for
     * the exit button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_letter)

        setFields()
        setExitListener()
    }

    /**
     * Sets all of the fields for this view: the message (greeting, body, and signature), the
     * location if one was added, and an uploaded image if one was added.
     */
    private fun setFields() {
        // Get the message to display
        val message: MessageModel = messDB.getMessage(message_Id)!!
        // Parse the message so it displays correctly
        val parsedMessage = parseMessage(message.message)
        letterView.text = parsedMessage
        if (message.location != "") {
            // If the location was added, set the location text
            locationView.text = message.location
        } else {
            // Otherwise, hide all of the location elements
            locationImage.visibility = View.GONE
            locationView.visibility = View.GONE
            locationSeparate.visibility = View.GONE
        }
        if (message.files != null) {
            // If an image was added, set the picture bitmap
            val picture = message.files as ByteArray
            pictureView.setImageBitmap(getByteArrayAsBitmap(picture))
        } else {
            // Otherwise, hide all of the image elements
            pictureView.visibility = View.GONE
            photoImage.visibility = View.GONE
            imageSeparate.visibility = View.GONE
        }
    }

    /**
     * The message is stored in the database as greeting [#] body [#] signature, so parse the
     * message to display it correctly.
     * @TODO: Just store it in the database as greeting\n\nbody\n\nsignature.
     * @param message, String
     * @return String, the parsed message
     */
    private fun parseMessage(message: String): String {
        val arrayMess = message.split("[#]")
        val greeting = arrayMess[0].trim()
        val body = arrayMess[1].trim()
        val signature = arrayMess[2].trim()
        return "$greeting\n\n$body\n\n$signature\n"
    }

    /**
     * Used to convert the byte array to a bitmap. This is because the blob data object for SQLite
     * stores images as a byte array but Android Kotlin displays images as bitmaps. This converts
     * the file from the database to make it display-able on the app.
     * @param img, ByteArray the image to convert
     * @return bitmap, Bitmap?, the image as a bitmap
     */
    private fun getByteArrayAsBitmap(img: ByteArray): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            bitmap = BitmapFactory.decodeByteArray(img, 0, img.size, options)
        } catch (e: SQLException) {
        }
        return bitmap
    }

    /**
     * Sets the listener for exiting after reading the message.
     */
    private fun setExitListener() {
        homeButton.setOnClickListener {
            finish()
            exitProcess(0)
        }
    }
}