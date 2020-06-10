package cs402.homework.project3

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_datetime.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Allows the user to set the date and time of which the notification for their "letter" will be
 * delivered to the device.
 * @author Payton Elsey
 */
class DateTime : AppCompatActivity() {

    private val messDB: MessageDB = MessageDB(this, null)
    private var cal: Calendar = Calendar.getInstance()      // Alterable calendar
    private var currCal: Calendar = Calendar.getInstance()  // Current date and time calendar
    private var date = ""
    private var time = ""
    private var timeSelected = false

    companion object {
        const val USER_NAME = "USER_NAME"       // Used to pass the user's email back and forth
        var FILE: ByteArray? = null              // Used to pass the attached image back and forth
    }

    /**
     * onCreate: Sets the default delivery time, adds the click listeners for the calendar and clock
     * as well as the send button. Also allows a user to click the back button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datetime)

        // Set the default delivery time to tomorrow this time
        date = getTomorrowDate()
        time = getCurrentTime()
        dateSelection.text = date
        timeSelection.text = time

        setCalendarListener()
        setClockListener()
        setSendListener()
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Sets the listener for the send button. First, it checks to make sure that the selected date
     * and time is in the future. Then, it asks the user "Are you sure you want to send?". If the
     * user clicks cancel, it goes back to activity. If the user clicks "Send", set up the
     * notification for this letter. If the date selected was in the past, don't let the user
     * send and let them know the selected time is not valid.
     */
    private fun setSendListener() {
        sendButton.setOnClickListener {
            val inPast = determineInPastCause()
            val invalidOutline = ContextCompat.getDrawable(this, R.drawable.back)
            dateSelection.setBackgroundResource(0)
            timeSelection.setBackgroundResource(0)

            // If the date and time selected are in the past, show the alert box.
            if (inPast == "none") {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage(resources.getString(R.string.popup_text))
                    .setCancelable(false)
                    .setPositiveButton("Send") { _, _ ->
                        // If the user hits 'Send', build the MessageModel and set the notification
                        val email = intent.getStringExtra(USER_NAME)!!
                        val msg = intent.getStringExtra("message")!!
                        val subj = intent.getStringExtra("subject")!!
                        val loc = intent.getStringExtra("location")!!
                        val file = FILE
                        val dt = "$date#$time"

                        val message = MessageModel(email, msg, subj, dt, loc, file)
                        val messID: Long = messDB.addMessage(message)

                        setNotification(messID, subj)

                        // Continue by moving to the success activity to let the user know it sent
                        val successIntent = Intent(this, Success::class.java)
                        successIntent.putExtra(Success.USER_NAME, intent.getStringExtra(USER_NAME))
                        successIntent.putExtra(Success.MSG_ID, messID)
                        startActivity(successIntent)
                        finish()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // If the user hits 'Cancel', close the alert box
                        dialog.cancel()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle(resources.getString(R.string.are_you_sure))
                alert.show()

            }
            // If the selected time was in the past, let the user know that either the date or time
            // were invalid.
            else {
                when (inPast) {
                    "date" -> {
                        dateSelection.background = invalidOutline
                        Toast.makeText(
                            this,
                            "Please select a date in the future.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    "time" -> {
                        timeSelection.background = invalidOutline
                        Toast.makeText(
                            this,
                            "Please select a time in the future.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Log.d("BSU", "Something went wrong")
                }
            }
        }
    }

    /**
     * Sets the listener for the calendar date selection. User can click either the calendar button
     * or the date itself to open the calendar selector.
     */
    private fun setCalendarListener() {
        // Set up the date picker to have tomorrow's date.
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        // Allow the user to click on the date itself to open the calendar
        dateSelection.setOnClickListener {
            calendar.performClick()
        }

        // Also allow the user to click the calendar icon to open the calendar
        calendar.setOnClickListener {
            val tomorrowDay = getTomorrowDate().split("/")[1].toInt()
            if (cal.get(Calendar.DATE) != tomorrowDay && !timeSelected)
                cal.add(Calendar.DATE, 1)   // Move to tomorrow
            DatePickerDialog(
                this,
                dateSetListener,
                // Set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DATE)
            ).show()
        }
    }

    /**
     * When the user selects a date from the date picker, update the UI to show the selected date.
     */
    private fun updateDateInView() {
        timeSelected = true
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        date = sdf.format(cal.time)
        dateSelection.text = date
    }

    /**
     * Sets the listener for the clock time selection. User can click either the clock button
     * or the time itself to open the clock selector.
     */
    private fun setClockListener() {
        // Set up the time picker
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR, hour)
                cal.set(Calendar.MINUTE, minute)
                val amPm: String = if (hour < 12) {
                    "AM"
                } else {
                    "PM"
                }
                var hr = cal.get(Calendar.HOUR)
                if (hr > 12) {
                    hr -= 12
                }
                if (hr == 0) {
                    hr = 12
                }
                updateTimeInView(hr.toString(), cal.get(Calendar.MINUTE).toString(), amPm)
            }

        // Allow the user to click on the selected time to open the time picker
        timeSelection.setOnClickListener {
            clock.performClick()
        }

        // Also allow the user to click on the clock icon to open the time picker
        clock.setOnClickListener {
            TimePickerDialog(
                this@DateTime,
                timeSetListener,
                // Set TimePickerDialog to point to today's date when it loads up
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    /**
     * When the user selects a time from the time picker, update the UI to show the selected time.
     */
    private fun updateTimeInView(hr: String, min: String, ampm: String) {
        var minute = min
        if (minute.toInt() < 10) {
            minute = "0$minute"
        }
        time = "$hr:$minute $ampm"
        timeSelection.text = time
    }

    /**
     * Calculates and returns tomorrow's date.
     * @return String, the date in month/day/year format
     */
    private fun getTomorrowDate(): String {
        currCal.add(Calendar.DATE, 1)   // Add one day to the current calendar
        val day = currCal.get(Calendar.DATE).toString()
        val month =
            (currCal.get(Calendar.MONTH) + 1).toString()    // Months are indexed starting at 0
        val year = currCal.get(Calendar.YEAR).toString()
        currCal.add(Calendar.DATE, -1)     // Put the calendar back to the current time
        return "$month/$day/$year"
    }

    /**
     * Gets the current time. Since clocks use 24-hour format and this app uses 12-hour/AM-PM format,
     * a conversion is necessary.
     * @return String, the time in hour:minute AM/PM format
     */
    private fun getCurrentTime(): String {
        var hour = currCal.get(Calendar.HOUR)
        if (hour > 12)
            hour -= 12
        if (hour == 0)
            hour = 12
        val hr = hour.toString()
        val minute = currCal.get(Calendar.MINUTE)
        var min = minute.toString()
        if (minute < 10) {
            min = "0$minute"
        }
        val amPm = currCal.get(Calendar.AM_PM)
        var ampm = ""
        if (amPm == 1) {
            ampm = "PM"
        } else if (amPm == 0) {
            ampm = "AM"
        }
        return "$hr:$min $ampm"
    }

    /**
     * Determine if the selected date and time are in the past. If it is in the past, determine
     * what the cause was: the selected date or the selected time. If it is not in the past, the
     * cause is "None".
     * @return String, the cause of the past date
     */
    private fun determineInPastCause(): String {
        // Split up the date to get the month, day, and year selected
        val splitDate = date.split("/")
        val month = splitDate[0]
        val day = splitDate[1]
        val year = splitDate[2]

        // Split up the time to get the hour, minute, and am/pm selected
        val splitTime = time.split(":", " ")
        val hour = splitTime[0]
        val minute = splitTime[1]
        val amPm = splitTime[2]

        // Get the current month, day, and year. Months are indexed starting at 0 so we add 1.
        val currMonth = currCal.get(Calendar.MONTH) + 1
        val currDay = currCal.get(Calendar.DATE)
        val currYear = currCal.get(Calendar.YEAR)

        // Get the current hour, minute, and am/pm
        val currHour = currCal.get(Calendar.HOUR)
        val currMin = currCal.get(Calendar.MINUTE)
        val currAmPm = currCal.get(Calendar.AM_PM)

        if (year.toInt() < currYear) {
            // The year is in the past
            return "date"
        } else if (year.toInt() == currYear) {
            if (month.toInt() < currMonth) {
                // This year was selected, but the month is in the past
                return "date"
            } else if (month.toInt() == currMonth) {
                if (day.toInt() < currDay) {
                    // This year and month was selected, but the day is in the past
                    return "date"
                } else if (day.toInt() == currDay) {
                    if (amPm == "PM" && currAmPm == 0) { // 0 is AM, 1 is PM
                        // Today was selected but it is currently PM and AM was selected
                        return "time"
                    } else if ((amPm == "PM" && currAmPm == 1) || (amPm == "AM" && currAmPm == 0)) {
                        if (hour.toInt() < currHour) {
                            // Today was selected and it's in the same AM/PM zone, but the hour is in the past
                            return "time"
                        } else if (hour.toInt() == currHour) {
                            if (minute.toInt() <= currMin) {
                                // Today at this hour was selected, but the minute is in the past
                                return "time"
                            }
                        }
                    }
                }
            }
        }

        // If none of the cases were true, the time selected was not in the past
        return "none"
    }

    /**
     * Set up the notification to be sent when the time arrives.
     * @param messId, Long, the message ID from the database that will be displayed when the time
     * arrives
     * @param subject, String, the subject of the message that was sent
     */
    @Suppress("DEPRECATION")
    private fun setNotification(messId: Long, subject: String) {
        // Set up the notification channel and notification builder
        val channelId = createNotificationChannel()
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
        mBuilder.setSmallIcon(R.drawable.arrow)
        val bitmap = resources.getDrawable(R.drawable.arrow_icon) as BitmapDrawable
        mBuilder.setLargeIcon(bitmap.bitmap)
        mBuilder.setContentTitle(resources.getString(R.string.you_have_letter))
        mBuilder.setContentText("Subject: $subject")
        mBuilder.setAutoCancel(true)

        // Set up the pending intent (the activity you will be taken to when you click on the
        // notification.
        val resultIntent =
            Intent(this, ViewLetter::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        ViewLetter.message_Id = messId
        val resultPendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)

        // Build the notification
        val notification: Notification = mBuilder.build()

        // Set up the notification publisher so when the time comes to send the notification, the
        // notification is shown on the device
        val notificationIntent = Intent(this, NotifPublisher::class.java)
        notificationIntent.putExtra(NotifPublisher.NOTIFICATION_ID, 333)
        notificationIntent.putExtra(NotifPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(
            this.applicationContext,
            333,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Set the alarm manager to send the notification to this device at the appropriate time
        val milli = getMilliseconds(messId)
        val futureToSend = System.currentTimeMillis() + milli
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureToSend, pendingIntent)
    }

    /**
     * Creates the channel for which notifications are sent through on the device.
     * @return String, channelId
     */
    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = resources.getString(R.string.app_name)
            val description = "Sendr Notification"
            val channelId = "${this.packageName}-$name"
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description
            channel.setShowBadge(false)

            val notificationManager = this.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)

            return channelId
        }
        return ""
    }

    /**
     * Get the milliseconds until delivery. The time that user specifies to send the letter is x
     * milliseconds on the clock. Return the current time in milliseconds minus x.
     * @param id, Long, the message ID to get the milliseconds of
     * @return Long, the milliseconds
     */
    private fun getMilliseconds(id: Long): Long {
        // Get the date and time that the message is set for
        val message: MessageModel = messDB.getMessage(id)!!
        val dateTime = message.datetime.replace("#", " ")

        // Using this date format, get the milliseconds from now this time is
        val sdf = SimpleDateFormat("M/d/yyyy h:mm a", Locale.US)
        try {
            val date: Date = sdf.parse(dateTime)!!
            return date.time - System.currentTimeMillis()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        // If something went wrong, return 0 long
        return 0L
    }
}
