package cs402.homework.project3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_success.*

/**
 * Activity for displaying the success message when a letter is successfully sent to the notification
 * channel.
 */
class Success : AppCompatActivity() {

    private val messDB: MessageDB = MessageDB(this, null)

    companion object {
        const val USER_NAME = "USER_NAME"       // Used to pass the user's email back and forth
        const val MSG_ID = ""
    }

    /**
     * onCreate: Gets the message from the database and adds the home button listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        getMessageFromDB()
        addListener()
    }

    /**
     * Menu button for logging out or going to the user's account.
     * @param menu, Menu?
     * @return boolean, true
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_bar, menu)
        return true
    }

    /**
     * Options item selected for the menu button. IF the user selects to go to their account, display
     * that activity. If they select to log out, log them out.
     * @param item, MenuItem
     * @return boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_account -> {
                // Go to the user's account
                val accountIntent = Intent(this, Account::class.java)
                val email = intent.getStringExtra(WelcomeScreen.USER_NAME) as String
                accountIntent.putExtra(Account.EMAIL, email)
                startActivityForResult(accountIntent, WelcomeScreen.REQUEST_CODE)
                return true
            }
            R.id.action_logout -> {
                val logInIntent = Intent(this, LogIn::class.java)
                // When the user logs out, make sure the email stays typed in on the log in screen
                logInIntent.putExtra(
                    LogIn.USER_NAME,
                    intent.getStringExtra(WelcomeScreen.USER_NAME)
                )
                logInIntent.putExtra("returned", true)
                startActivity(logInIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Get the message from the database and display the information on the success activity.
     * Needs to display the subject line and the date and time of delivery.
     */
    private fun getMessageFromDB() {
        val id: Long = intent.getLongExtra(MSG_ID, 0)
        if (id != 0L) {
            Log.d("BSU", "Getting the message from the database")
            val mess: MessageModel? = messDB.getMessage(id)
            if (mess != null) {
                Log.d("BSU", "Message was found.")
                // If the message exists in the database, get the information
                replaceSubject.text = mess.subject
                val dateTime = mess.datetime.split("#")
                val date = dateTime[0]
                val time = dateTime[1]
                replaceDate.text = date
                val timeString = "at $time"
                replaceTime.text = timeString
            }
        }
    }

    /**
     * Add the on click listener for the "home button". When the user clicks restart, bring them
     * back to the welcome screen to make a new letter.
     */
    private fun addListener() {
        restartButton.setOnClickListener {
            val successIntent = Intent(this, WelcomeScreen::class.java)
            successIntent.putExtra(WelcomeScreen.USER_NAME, intent.getStringExtra(USER_NAME))
            startActivity(successIntent)
        }
    }

}