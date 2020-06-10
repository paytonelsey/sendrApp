package cs402.homework.project3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.upload_fragment.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

/**
 * Fragment for the upload button on the Welcome Screen and Account activity. When you click on the
 * upload symbol, the device's gallery opens for the user to select an image.
 * @author Payton Elsey
 */
class UploadFragment : Fragment(), View.OnClickListener {

    private var toggle = 1             // Toggles between the different displays for this fragment
    private var canUseFiles = false
    private val permissionsCode = 456       // Given file permissions
    private val imageCode = 1000            // Given the user selects a file

    /**
     * onCreateView: inflates the fragment layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.upload_fragment, container, false)
        view.findViewById<ImageButton>(R.id.uploadButton).setOnClickListener(this)
        return view
    }

    /**
     * Checks if the device has given permissions to access the external storage. If it hasn't,
     * request the permissions.
     */
    private fun permission() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions( // Method of Fragment
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permissionsCode
            )
        } else {
            canUseFiles = true
        }
    }

    /**
     * When the permission was requested, depending on the result of what the user said, proceed
     * with the file selection or display the toast.
     * @param requestCode, Int
     * @param permissions, Array<out String>
     * @param grantResults, IntArray
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionsCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission from popup granted
                    proceedWithFiles()
                } else {
                    // Permission from popup denied
                    Toast.makeText(context!!, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * When the user clicks on the upload button, check permissions, let the user select an image,
     * and update the fragment to let the user know the image was uploaded.
     * @param v, View?
     */
    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        // If the user just clicked to add the image
        if (toggle == 1) {
            permission()
            if (canUseFiles)
                proceedWithFiles()
            toggle = 2
        }
        // Otherwise, if the user wants to remove the image from the letter
        else if (toggle == 2) {
            val className = activity!!.localClassName.split(".")
            val name =
                className[className.size - 1] // Get the last one which should be the class name
            if (name == "WelcomeScreen") {
                DateTime.FILE = null
            } else if (name == "Account") {
                (activity as Account).setProfilePic(null)
            }
            uploadButton.setImageDrawable(resources.getDrawable(R.drawable.upload_icon))
            uploadText.text = resources.getText(R.string.upload_text)
            toggle = 1
        }
    }

    /**
     * Start the intent to let the user pick an image from their gallery.
     */
    private fun proceedWithFiles() {
        Log.d("BSU", "File proceed")
        // Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, imageCode)
    }

    /**
     * When the user selected an image, save that image to either the WelcomeScreen activity
     * or the Account activity depending on which one this fragment is from.
     * @param requestCode, Int
     * @param resultCode, Int
     * @param data, Intnet?
     */
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == imageCode) {
            val image = data?.data      // Get the image that was uploaded
            val bitmap: Bitmap?
            try {
                bitmap =
                    BitmapFactory.decodeStream(activity!!.contentResolver.openInputStream(image!!))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return
            }
            // Get which activity this fragment is currently being used in and save the image
            val className = activity!!.localClassName.split(".")
            val name =
                className[className.size - 1] // Get the last one which should be the class name
            if (name == "WelcomeScreen") {
                DateTime.FILE = getBitmapAsByteArray(bitmap!!)
            } else if (name == "Account") {
                (activity as Account).setProfilePic(bitmap)
            }
        }
        // Update the UI to let the user know the file was uploaded
        if (data != null) {
            uploadButton.setImageDrawable(resources.getDrawable(R.drawable.pink_check))
            uploadText.text = resources.getText(R.string.file_uploaded_text)
        } else {
            toggle = 1
        }
    }

    /**
     * Converts the given Bitmap to a ByteArray. The uploaded image is given as a bitmap, but SQLite
     * blob types take in a ByteArray, so a conversion is necessary.
     * @param bitmap, Bitmap, the image to convert
     * @return ByteArray?, the converted image
     */
    fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }
}