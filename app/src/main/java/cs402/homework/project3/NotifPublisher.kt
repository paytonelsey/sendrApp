package cs402.homework.project3

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Notification publisher for when the alarm manager alerts the device to display a notification.
 * Given the ID of the notification, notify the phone with said notification.
 * @author Payton Elsey (and StackOverflow)
 */
class NotifPublisher : BroadcastReceiver() {

    /**
     * onReceive: Gets the notification and notifies the manager with it.
     * @param context, Context
     * @param intent, Intent
     */
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        var NOTIFICATION_ID = "notification_id"
        var NOTIFICATION = "notification"
    }
}