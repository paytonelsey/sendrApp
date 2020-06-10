package cs402.homework.project3

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_account.*
import java.sql.SQLException

/**
 * Activity for the user's account information. Displays their profile picture, first name, last
 * name, and email address. Allows them to change all of that information except the email address.
 * @author Payton Elsey
 */
class Account : AppCompatActivity() {

    private val db: UserDB = UserDB(this, null)

    companion object {
        const val EMAIL = "EMAIL"       // Used to pass the user's email back and forth
    }

    /**
     * onCreate: sets up the account view, adds the action listener, adds the upload fragment
     * to allow a user to change their profile picture.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        accountInfo()
        okay()

        // Fragment to upload a photo
        val upFragment = UploadFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.uploadFragment_acct, upFragment, "UPTAG")
            .addToBackStack("main")
            .commit()
    }

    /**
     * Sets all of the UI elements for the user's account. Using the intent extra, EMAIL, get the
     * user's information out of the database and display the first and last name as well as the
     * email and a profile picture. If the user doesn't have a profile picture set, use the default
     * profile picture.
     */
    private fun accountInfo() {
        val email: String = intent.getStringExtra(EMAIL) as String
        val user: UserModel? = db.getUser(email)
        // Fill out the user's information displayed on the account page
        if (user != null) {
            firstNameUser.setText(user.firstName)
            lastNameUser.setText(user.lastName)
            emailUser.text = user.email
            if (user.profilePic != null) {
                val bitmap = getByteArrayAsBitmap(user.profilePic!!)
                profilePic.setImageBitmap(bitmap)
            } else {
                val profile =
                    BitmapFactory.decodeResource(resources, R.drawable.default_profile_pic)
                profilePic.setImageBitmap(profile)
            }
        }
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
     * Set the button listener for the check button on this activity. When the user clicks this
     * button, the information that was changed (first name, last name, and/or profile pic) is
     * passed back to the Welcome Screen activity to get saved.
     */
    private fun okay() {
        okayButton.setOnClickListener {
            val welcomeIntent = Intent(this, WelcomeScreen::class.java)
            // Pass the changed data back to the Welcome Screen to get updated in the database
            welcomeIntent.putExtra(WelcomeScreen.FIRST, firstNameUser.text.toString())
            welcomeIntent.putExtra(WelcomeScreen.LAST, lastNameUser.text.toString())
            setResult(Activity.RESULT_OK, welcomeIntent)
            finish()
        }
    }

    /**
     * Set's the user's profile picture to the given bitmap image.
     * @param img, Bitmap? the new profile picture
     */
    fun setProfilePic(img: Bitmap?) {
        val fm: FragmentManager = supportFragmentManager
        val fragment: UploadFragment = fm.findFragmentByTag("UPTAG") as UploadFragment

        var pic: Bitmap? = img

        if (img == null) {
            // Set the user's profile picture back to default
            pic = BitmapFactory.decodeResource(
                resources,
                R.drawable.default_profile_pic
            )
        }
        // Get the minimum size to crop it to to make it a square
        val size: Int
        size = if (pic!!.height >= pic.width) {
            pic.width
        } else {
            pic.height
        }
        val cropped: Bitmap = Bitmap.createBitmap(pic, 0, 0, size, size)

        // Set the image view to this bitmap
        profilePic.setImageBitmap(cropped)
        // Convert it to a byte array
        val bytes = fragment.getBitmapAsByteArray(cropped)
        // Update the database
        db.updatePic(bytes!!, intent.getStringExtra(EMAIL)!!)
    }
}