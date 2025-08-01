package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.preference.PreferenceManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ScrollAccessibility : AccessibilityService() {

    companion object {
        private const val TAG = "ScrollAccessibility"
        private const val POLLING_INTERVAL = 10_000L // 10 seconds
    }

    private lateinit var settingsStore: SettingsStore
    private lateinit var poller: Poller

    private var currentPackageId = ""

    override fun onServiceConnected() {
        super.onServiceConnected()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        settingsStore = SettingsStore(sharedPreferences)
        settingsStore.startListening()

        poller =
                Poller(POLLING_INTERVAL) {
                    val settings = settingsStore.getItemsForPackageId(currentPackageId)
                    if (settings.isNotEmpty() && settings.any { it.enabled }) {
                        findBlocked(settings)
                    }
                }

        // Create a notification channel (required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "scroll_accessibility_service"
            val channelName = "Scroll Accessibility Service"
            val channel =
                    android.app.NotificationChannel(
                            channelId,
                            channelName,
                            android.app.NotificationManager.IMPORTANCE_LOW
                    )
            val notificationManager =
                    getSystemService(android.content.Context.NOTIFICATION_SERVICE) as
                            android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification =
                android.app.Notification.Builder(this, "scroll_accessibility_service")
                        .setContentTitle("Scroll Accessibility Service")
                        .setContentText("Service is running")
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        ) // Ensure a valid small icon is set
                        .build()

        // Start the service in the foreground
        startForeground(1, notification)
    }

    fun findBlocked(settings: List<ListItem>) {
        for (setting in settings) {
            val viewId = "${setting.appid}:id/${setting.viewid}"
            android.util.Log.d(TAG, "onView: $rootInActiveWindow")

            val blockContent = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)
            android.util.Log.d(TAG, "onView: $blockContent")

            // Detect Scrolling
            if (blockContent != null && blockContent.isNotEmpty()) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this@ScrollAccessibility, "Feature Blocked", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            settingsStore.startListening()

            // android.util.Log.d(TAG, "Event: $event")
            currentPackageId = it.packageName.toString()

            val settings = settingsStore.getItemsForPackageId(currentPackageId)
            if (settings.isEmpty() || settings.all { !it.enabled }) {
                poller.stop()
                return
            }
            poller.start()

            findBlocked(settings)
        }
    }

    fun onPoll() {
        val settings = settingsStore.getItemsForPackageId(currentPackageId)
        if (settings.isEmpty() || settings.all { !it.enabled }) {
            return
        }
        findBlocked(settings)
    }

    override fun onInterrupt() {
        settingsStore.stopListening()
        poller.stop()
        // stopForeground(true)
    }

    override fun onDestroy() {
        settingsStore.stopListening()
        poller.stop()
        stopForeground(true)
        super.onDestroy()
    }
}
