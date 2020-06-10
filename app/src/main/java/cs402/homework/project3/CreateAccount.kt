package cs402.homework.project3

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_create_account.*
import java.io.ByteArrayOutputStream

/**
 * Allows someone to create an account for sendr. Asks for a first and last name, an email address,
 * and a password. Once the account is created, it signs you in as that created user.
 * @author Payton Elsey
 */
class CreateAccount : AppCompatActivity() {

    private val db: UserDB = UserDB(this, null)

    companion object {
        const val USER_NAME = "USER_NAME"       // Used to pass user's email back and forth
    }

    /**
     * onCreate: Sets the sign up button listener and the listener for the option to log in.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        signUp()
        signInOption()
        returnFromIntent()
    }

    /**
     * When the user clicks "Sign Up", if the account is valid, it is created and they are signed
     * in. Resets the text field backgrounds to remove the "error" outlines when an account creation
     * is invalid.
     */
    private fun signUp() {
        signUpButton.setOnClickListener {
            firstNameTextField.setBackgroundResource(0)     // Clear out pink outlines
            lastNameTextField.setBackgroundResource(0)
            emailTextField.setBackgroundResource(0)
            passwordTextField.setBackgroundResource(0)
            confirmTextField.setBackgroundResource(0)

            if (valid() && createUser()) {
                val email = emailTextField.text
                // Start a welcome activity displaying the user's email
                val welcomeIntent = Intent(this, WelcomeScreen::class.java)
                welcomeIntent.putExtra(WelcomeScreen.USER_NAME, "$email")
                startActivity(welcomeIntent)
            }
        }
    }

    /**
     * Determines if the account being created is valid or not. The first and last name have to be
     * at least 2 characters, the email must be at least 7 characters and contain an '@' and a '.',
     * and the password must be at least 6 characters and match the "confirm password" entry. If
     * any of these conditions is false, return false, otherwise it is valid and return true.
     * @return valid, boolean, if it is valid
     */
    private fun valid(): Boolean {
        val firstName = firstNameTextField.text.toString()
        val lastName = lastNameTextField.text.toString()
        val email = emailTextField.text.toString()
        val password = passwordTextField.text.toString()
        val confirmPassword = confirmTextField.text.toString()

        // Outline on text boxes that are invalid
        val invalidOutline = ContextCompat.getDrawable(this, R.drawable.back)

        var valid = true

        // First and last name must be at least 2 characters
        if (firstName.length < 2) {
            firstNameTextField.background = invalidOutline
            valid = false
        }
        if (lastName.length < 2) {
            lastNameTextField.background = invalidOutline
            valid = false
        }
        // Email must be at least 7 characters and contain an @ and .
        if (email.length < 7 || !email.contains("@") || !email.contains(".")) {
            emailTextField.background = invalidOutline
            valid = false
        }
        // Passwords must be at least 6 characters
        if (password.length < 6) {
            passwordTextField.background = invalidOutline
            valid = false
        }
        // Passwords must match
        if (password != confirmPassword || confirmPassword.length < 6) {
            confirmTextField.background = invalidOutline
            valid = false
        }
        return valid
    }

    /**
     * Create the user in the database if it can be created. Accounts are unique based on email
     * address so if the account already exists, return false (do not log them in), otherwise,
     * create the account and return true.
     * @return logIn, boolean, if the creation was successful
     */
    private fun createUser(): Boolean {
        // Create the UserModel for this account creation
        val newUser = UserModel()
        newUser.email = emailTextField.text.toString()
        newUser.firstName = firstNameTextField.text.toString()
        newUser.lastName = lastNameTextField.text.toString()
        newUser.password = passwordTextField.text.toString()
        val icon = BitmapFactory.decodeResource(resources, R.drawable.default_profile_pic)
        newUser.profilePic = getBitmapAsByteArray(icon)     // Set to default image

        var logIn = false

        // If the account doesn't already exists in the database, add the user and set login to true
        if (!db.userExists(newUser)) {
            db.addUser(newUser)
            logIn = true
        }
        // Otherwise, let the user know that the account already exists with this email
        else {
            accountExists.visibility = View.VISIBLE
            accountExists.setText(R.string.account_exists)
        }
        return logIn
    }

    /**
     * Converts the given Bitmap to a ByteArray. The icon for the default profile picture is given
     * as a bitmap, but SQLite blob types take in a ByteArray, so a conversion is necessary.
     * @param bitmap, Bitmap, the image to convert
     * @return ByteArray?, the converted image
     */
    private fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Set the onclick listener for the link that allows a user to jump back to the login page.
     * This is so if they try to create an account and realize that they already have an account,
     * they can jump back and log in.
     */
    private fun signInOption() {
        logInLink.setOnClickListener {
            val logInIntent = Intent(this, LogIn::class.java)
            // If the user clicks "Sign in", start a Log In activity, filling out the email
            logInIntent.putExtra(LogIn.USER_NAME, emailTextField.text.toString())
            logInIntent.putExtra("returned", true)
            startActivity(logInIntent)
        }
    }

    /**
     * returnFromIntent: this is so jumping between the login page and the sign up page propagates
     * the typed email address. So, if a user types in their email address to login page and
     * realizes they have to sign up, they don't have to retype their email address.
     */
    private fun returnFromIntent() {
        var returned = false
        returned = intent.getBooleanExtra("returned", returned)

        if (returned) {
            // If we are returning from the log in page, fill out the email with what the user
            // may have typed.
            val email: String = intent.getStringExtra("USER_NAME") as String
            emailTextField.setText(email)
        }
    }
}