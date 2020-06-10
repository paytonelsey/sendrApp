package cs402.homework.project3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.location_fragment.*

/**
 * Fragment for the location button on the Welcome Screen. When you click on the location symbol,
 * the device's current location is set and passed back to the activity that used the fragment.
 * @author Payton Elsey
 */
class LocationFragment : Fragment(), View.OnClickListener {

    private var toggle = 1                  // Toggles between the different views of this fragment
    private val permissionsCode = 123       // Permission to use location
    private var canUseLocation = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    /**
     * onCreate: sets the FusedLocationProviderClient variable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    /**
     * onCreateView: Inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.location_fragment, container, false)
        view.findViewById<ImageButton>(R.id.locationButton).setOnClickListener(this)
        return view
    }

    /**
     * Checks if the device has given permissions to access the fine location. If it hasn't,
     * request the permissions.
     */
    private fun permission() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(     // Method of Fragment
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionsCode
            )
        } else {
            canUseLocation = true
        }
    }

    /**
     * When the permission was requested, depending on the result of what the user said, set the
     * canUseLocation boolean to true if they granted permissions.
     * @param requestCode, Int
     * @param permissions, Array<String>
     * @param grantResults, IntArray
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionsCode) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
            ) {
                canUseLocation = true
            }
        }
    }

    /**
     * Once permissions and access are granted for location, proceed by getting the user's location.
     * Get the latitude and longitude and write it to the WelcomeScreen for use when sending the
     * letter.
     */
    private fun proceedWithLocation() {
        Log.d("BSU", "Location proceed")
        // Get the user's last location in latitude and longitude
        mFusedLocationClient.lastLocation.addOnCompleteListener(activity!!) { task ->
            val location: Location? = task.result
            if (location == null) {
                requestNewLocationData()
            } else {
                WelcomeScreen.LATITUDE = location.latitude.toString()
                WelcomeScreen.LONGITUDE = location.longitude.toString()
            }
        }
    }

    /**
     * When the location has changed or when this is the first time a location is being accessed,
     * request the location of the device.
     */
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    /**
     * When the location is gotten, send the data to the Welcome Screen activity.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            WelcomeScreen.LATITUDE = mLastLocation.latitude.toString()
            WelcomeScreen.LONGITUDE = mLastLocation.longitude.toString()
        }
    }

    /**
     * Check to see if the location is enabled on the user's device.
     * @return boolean, if the location is enabled
     */
    private fun isLocationEnabled(): Boolean {
        val loc = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return loc.isProviderEnabled(LocationManager.GPS_PROVIDER) || loc.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * When the user clicks on the location button, check permissions, get the device's location,
     * and update the fragment to let the user know the location was added.
     * @param v, View?
     */
    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        // If the user just clicked to add the location
        if (toggle == 1) {
            // Request permissions
            permission()
            if (canUseLocation && isLocationEnabled()) {
                proceedWithLocation()
            } else {
                Log.d("BSU", "Turn on GPS")
                Toast.makeText(context!!, "Turn on location", Toast.LENGTH_LONG).show()
            }
            // Update the view to provide visual feedback to the user
            locationButton.setImageDrawable(resources.getDrawable(R.drawable.location_added))
            locationText.text = resources.getText(R.string.location_added_text)
            toggle = 2
        }
        // Otherwise, if the location was already added and we want to remove the location
        else if (toggle == 2) {
            WelcomeScreen.LATITUDE = ""
            WelcomeScreen.LONGITUDE = ""
            locationButton.setImageDrawable(resources.getDrawable(R.drawable.location_icon))
            locationText.text = resources.getText(R.string.location_text)
            toggle = 1
        }
    }
}