package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.preference.PreferenceManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ScrollAccessibility : AccessibilityService() {

    private lateinit var settingsStore: SettingsStore

    override fun onServiceConnected() {
        super.onServiceConnected()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        settingsStore = SettingsStore(sharedPreferences)
        settingsStore.startListening()

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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            settingsStore.startListening()

            if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                return
            }

            val setting = settingsStore.items[it.packageName]
            if (setting == null || !setting.enabled) return

            // Detect targeted content
            val viewId = "${setting.appid}:id/${setting.viewid}"
            val blockContent = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)

            // Detect Scrolling
            if (blockContent != null && blockContent.isNotEmpty()) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this@ScrollAccessibility, "Feature Blocked", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onInterrupt() {
        settingsStore.stopListening()
        // stopForeground(true)
    }

    override fun onDestroy() {
        settingsStore.stopListening()
        stopForeground(true)
        super.onDestroy()
    }
}
