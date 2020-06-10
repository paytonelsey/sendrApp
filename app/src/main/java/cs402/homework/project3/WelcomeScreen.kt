package cs402.homework.project3

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

/**
 * Welcome Screen activity for drafting a new letter. Allows the user to write a letter to their
 * future self by providing the option for a subject, greeting, body, signature, and the potential
 * for an uploaded image and the addition of the device's current location.
 * @author Payton Elsey
 */
class WelcomeScreen : AppCompatActivity() {

    private val userDB: UserDB = UserDB(this, null)
    private val apiKey = "7deee3e342373e37f24eee5ded4eefe552c3def"      // For Geocodio API

    companion object {
        const val USER_NAME = "USER_NAME"       // Used to pass the user's email back and forth
        const val REQUEST_CODE = 1
        var FIRST = "FIRST_NAME"
        var LAST = "LAST_NAME"
        var LONGITUDE = ""
        var LATITUDE = ""
    }

    /**
     * onCreate: adds the location and upload fragments to the view, sets the generated greeting
     * to have the user's first name in it, and sets the listener for the next button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val locFragment = LocationFragment()
        val upFragment = UploadFragment()

        val name: String = userDB.getUser(intent.getStringExtra(USER_NAME)!!)!!.firstName
        val greetingStr = "Dear $name,"
        editGreeting.hint = greetingStr

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.uploadFragment, upFragment)
            .addToBackStack("main")
            .commit()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.locationFragment, locFragment)
            .addToBackStack("main")
            .commit()

        setMessageListener()
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
                val email = intent.getStringExtra(USER_NAME) as String
                accountIntent.putExtra(Account.EMAIL, email)
                startActivityForResult(accountIntent, REQUEST_CODE)
                return true
            }
            R.id.action_logout -> {
                val logInIntent = Intent(this, LogIn::class.java)
                // When the user logs out, make sure the email stays typed in on the log in screen
                logInIntent.putExtra(LogIn.USER_NAME, intent.getStringExtra(USER_NAME))
                logInIntent.putExtra("returned", true)
                startActivity(logInIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * When the user is finished updating their account, update the view to have the new first name
     * and update the database to reflect these changes.
     * @param requestCode, Int
     * @param resultCode, Int
     * @param data, Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val firstName = data!!.getStringExtra(FIRST)!!
            val lastName = data.getStringExtra(LAST)!!
            editGreeting.hint = "Dear $firstName,"
            userDB.updateName(firstName, lastName, intent.getStringExtra(USER_NAME)!!)
        }
    }

    /**
     * Sets the listener for the continue button. Once the user is done drafting the letter, they
     * are taken to the activity to set the delivery date and time.
     */
    private fun setMessageListener() {
        continueButton.setOnClickListener {
            val locationLong = LONGITUDE
            val locationLat = LATITUDE

            val email = intent.getStringExtra(USER_NAME) as String
            val msg: String = getMessage(
                editGreeting.text.toString(),
                editMessage.text.toString(),
                editSignature.text.toString()
            )

            // Get location if the user specified to add it
            var location = ""
            if (locationLong != "" && locationLat != "") {
                location = getLocationFromCoords(locationLong, locationLat)
            }

            // Check if subject is filled out
            var subject = editSubject.text.toString()
            if (subject == "")
                subject = "A Letter From Past You"

            // Start the date and time selection activity
            val dateTimeIntent = Intent(this, DateTime::class.java)
            dateTimeIntent.putExtra(DateTime.USER_NAME, email)
            dateTimeIntent.putExtra("message", msg)
            dateTimeIntent.putExtra("location", location)
            dateTimeIntent.putExtra("subject", subject)
            startActivity(dateTimeIntent)
        }
    }

    /**
     * Get the message ready to be inserted into the database. Concatenate the message together with
     * the delimiter [#].
     * @TODO: Concatenate the message with \n\n so I don't have to parse the message later.
     * @param greeting, String
     * @param body, String
     * @param signature, String
     * @return String, the formatted message
     */
    private fun getMessage(greeting: String, body: String, signature: String): String {
        val name: String = userDB.getUser(intent.getStringExtra(USER_NAME)!!)!!.firstName
        var greet = greeting
        var sig = signature
        if (greet == "")
            greet = "Dear $name,"
        if (sig == "")
            sig = "Sincerely, Your Past Self"
        val delimiter = "[#]"
        return "$greet $delimiter $body $delimiter $sig"
    }

    /**
     * Using the geocod.io API, get the location of the user in a formatted address. If, for some
     * reason, the API doesn't work, just use Geocoder for Android.
     * @param longitude: String
     * @param latitude: String
     * @return String, location as an address
     */
    private fun getLocationFromCoords(longitude: String, latitude: String): String {
        // Convert the values to doubles
        val lat: Double
        val long: Double
        try {
            lat = latitude.toDouble()
            long = longitude.toDouble()
            Log.d("BSU", "$lat and $long")
        } catch (e: Exception) {
            Log.d("BSU", "Trouble converting lat and long to doubles")
            return "N/A"
        }

        // Make API call
        val url = "https://api.geocod.io/v1.4/reverse?q=$lat,$long&api_key=$apiKey"
        var json = "unset"
        // Try to get the data from the API
        Executors.newSingleThreadExecutor().execute {
            json = URL(url).readText()
        }
        // If the API fails (it should not) just use Geocoder
        val address: String
        Log.d("BSU", json)
        address = if (json != "unset") {
            val results = Response(json).data
            val firstRes = results!![0]
            firstRes.getAddr()!!
        } else {
            val coder = Geocoder(this)
            val response = coder.getFromLocation(lat, long, 1)[0]
            response.getAddressLine(0)
        }

        return address
    }

    /**
     * Convert the json results to a JSONObject.
     * @param json, String
     * @author Payton Elsey (and StackOverflow)
     */
    class Response(json: String) : JSONObject(json) {
        val data = this.optJSONArray("results")
            ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } }
            ?.map { GetAddress(it.toString()) }
    }

    /**
     * Get the address from the JSONObject.
     * @param json, String
     * @author Payton Elsey (and StackOverflow)
     */
    class GetAddress(json: String) : JSONObject(json) {
        private val address: String? = this.optString("formatted_address")

        fun getAddr(): String? {
            return address
        }
    }
}