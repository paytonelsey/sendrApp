package cs402.homework.project3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_log_in.emailTextField
import kotlinx.android.synthetic.main.activity_log_in.passwordTextField

/**
 * Allows someone to log into an account already created. Asks for an email address and matching
 * password. If the account information matches, the user is logged in.
 * @author Payton Elsey
 */
class LogIn : AppCompatActivity() {

    private val db: UserDB = UserDB(this, null)

    companion object {
        const val USER_NAME = "USER_NAME"       // Used for passing the email back and forth
    }

    /**
     * onCreate: Sets up the login button listener and the listener to bring the user to the create
     * an account page.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        logIn()
        createAccountOption()
        returnFromIntent()
    }

    /**
     * Sets the listener for logging into the app. Resets the text field backgrounds to remove the
     * "error" outlines when a login attempt is invalid.
     */
    private fun logIn() {
        logInButton.setOnClickListener {
            emailTextField.setBackgroundResource(0)     // Remove the outline from incorrect fields
            passwordTextField.setBackgroundResource(0)

            if (valid()) {       // First, check that the email and password are "valid"
                val emailAddress = emailTextField.text
                // Open the welcome activity that displays the user's email address
                val welcomeIntent = Intent(this, WelcomeScreen::class.java)
                welcomeIntent.putExtra(WelcomeScreen.USER_NAME, "$emailAddress")
                startActivity(welcomeIntent)
            }
        }
    }

    /**
     * returnFromIntent: this is so jumping between the login page and the sign up page propagates
     * the typed email address. So, if a user types in their email address to create account page and
     * realizes they have to log in, they don't have to retype their email address.
     */
    private fun returnFromIntent() {
        var returned = false
        returned = intent.getBooleanExtra("returned", returned)

        // If this activity is returning from the welcome screen, fill out the email that was just
        // signed in.
        if (returned) {
            val email: String = intent.getStringExtra("USER_NAME") as String
            emailTextField.setText(email)
        }
    }

    /**
     * Set the onclick listener for the link that allows a user to jump to the create account page.
     * This is so if they try to log in and realize that they already have to create an account,
     * they can move to the appropriate activity.
     */
    private fun createAccountOption() {
        signUpLink.setOnClickListener {
            // When the user clicks "Sign up" start the Create Account activity
            val createAccountIntent = Intent(this, CreateAccount::class.java)
            createAccountIntent.putExtra(CreateAccount.USER_NAME, emailTextField.text.toString())
            createAccountIntent.putExtra("returned", true)
            startActivity(createAccountIntent)
        }
    }

    /**
     * Determines if a login attempt is valid. If the password matches the email, it is valid. If
     * not, it is invalid.
     * @return boolean, if the login is valid
     */
    private fun valid(): Boolean {
        val email = emailTextField.text.toString()
        val password = passwordTextField.text.toString()
        val logInUser = UserModel()
        logInUser.email = email
        logInUser.password = password

        var valid = true

        // Check if the account is in the database
        if (!db.userExists(logInUser)) {
            invalidDesign()
            valid = false
        } else {
            val existingUser: UserModel? = db.getUser(email)
            // Check if the email and password match
            if (existingUser != null && logInUser.password != existingUser.password) {
                invalidDesign()
                valid = false
            }
        }

        return valid
    }

    /**
     * The invalid account design. When a login attempt is invalid, highlight both fields and let
     * the user know that one of the fields they typed in is invalid.
     */
    private fun invalidDesign() {
        // This is the thin pink line that outlines text boxes that are invalid
        val invalidOutline = ContextCompat.getDrawable(this, R.drawable.back)

        emailTextField.background = invalidOutline
        passwordTextField.background = invalidOutline
        accountDoesNotExist.visibility = View.VISIBLE
        accountDoesNotExist.setText(R.string.account_does_not_exist)
    }
}