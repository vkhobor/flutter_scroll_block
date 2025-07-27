package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ScrollAccessibility : AccessibilityService() {

    private var currentIndex = 0
    private var startTime = 0
    private var endTime = 0

    // App Usage Info
    private var appPackageName = ""
    private var appScrollCount = 0
    private var appTimeSpent = 0
    private var appOpenCount = 0
    private var appScrollBlocked = 0

    private val supportedApps = listOf("com.instagram.android", "Youtube", "Linkedin", "Snapchat")

    override fun onServiceConnected() {
        super.onServiceConnected()

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
            // Detect Window Changes
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                // Update app status in memory
                // TODO
            }

            supportedApps.forEach {
                if (event.packageName == it) {
                    appPackageName = it

                    // Detect targeted content
                    val viewId = "${it}:id/${getBlockIdForApp(it)}"
                    val blockContent =
                            rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)

                    // Detect Scrolling
                    if (blockContent != null) {
                        if (blockContent.isNotEmpty() &&
                                        (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                                                event.eventType ==
                                                        AccessibilityEvent
                                                                .TYPE_WINDOW_STATE_CHANGED)
                        ) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                            Toast.makeText(
                                            this@ScrollAccessibility,
                                            "Feature Blocked",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    }
                }
            }
        }
    }

    private fun getBlockIdForApp(packageName: String): String {
        val blockIdMap =
                mapOf(
                        "com.instagram.android" to "clips_ufi_more_button_component",
                        "Youtube" to "youtube_block_id",
                        "Linkedin" to "linkedin_block_id",
                        "Snapchat" to "snapchat_block_id"
                )
        return blockIdMap[packageName] ?: "default_block_id"
    }

    override fun onInterrupt() {
        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}
