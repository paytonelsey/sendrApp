package cs402.homework.project3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent

/**
 * Main activity. Depending on the build configuration, starts up either the log in screen or the
 * welcome screen to make testing easier.
 * @author Payton Elsey
 */
class MainActivity : AppCompatActivity() {

    private val userDb = UserDB(this, null)

    /**
     * onCreate: determines which screen to load based on the Build Config value, IS_PRODUCTION.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.IS_PRODUCTION) {
            run()
        } else {
            signIn()
        }
    }

    /**
     * Run the login page for production variants of the app.
     */
    private fun run() {
        // TODO: Determine if the user should be auto logged in
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
    }

    /**
     * Automatically sign in the test account for debugging variants of the app.
     */
    private fun signIn() {
        val testEmail = "test@gmail.com"
        if (!userDb.emailUsed(testEmail)) {
            val userModel = UserModel(
                testEmail, "Test", "Account", "test123", null
            )
            userDb.addUser(userModel)
        }
        val intent = Intent(this, WelcomeScreen::class.java)
        // Open the welcome activity that displays the user's email address
        intent.putExtra(WelcomeScreen.USER_NAME, testEmail)
        startActivity(intent)
    }
}
