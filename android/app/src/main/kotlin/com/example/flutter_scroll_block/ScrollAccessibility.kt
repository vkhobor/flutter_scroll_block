package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.time.LocalTime
import kotlin.math.max

class ScrollAccessibility : AccessibilityService() {
    private val appUsageList = mutableListOf<Map<String, Any>>()

    private var appStatus =
            mapOf("Instagram" to true, "Youtube" to true, "Linkedin" to true, "Snapchat" to true)

    private var isInstagramDisabled = true
    private var isYoutubeDisabled = true
    private var isLinkedinDisabled = true
    private var isSnapchatDisabled = true

    companion object {
        private const val MIN_VALID_SCROLL_COUNT = 3
        private const val MIN_VALID_TIME_SPENT = 5
    }

    private var currentIndex = 0
    private var startTime = 0
    private var endTime = 0

    // App Usage Info
    private var appPackageName = ""
    private var appScrollCount = 0
    private var appTimeSpent = 0
    private var appOpenCount = 0
    private var appScrollBlocked = 0

    private val supportedApps = listOf("Instagram", "Youtube", "Linkedin", "Snapchat")

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
                appStatus =
                        mapOf(
                                "Instagram" to isInstagramDisabled,
                                "Youtube" to isYoutubeDisabled,
                                "Linkedin" to isLinkedinDisabled,
                                "Snapchat" to isSnapchatDisabled
                        )

                if (isValidAppUsage()) {
                    // Calculate App Usage
                    endTime = LocalTime.now().toSecondOfDay()
                    appTimeSpent = max(0, endTime - startTime)
                    appOpenCount++

                    // Save App Usage in DB
                    saveAppUsage()
                }
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
                            if (!appStatus[it]!!) {
                                // Start Scrolling time
                                if (startTime == 0) {
                                    startTime = LocalTime.now().toSecondOfDay()
                                }

                                // Detect single scroll per content
                                if (currentIndex != event.fromIndex) {
                                    appScrollCount++
                                    currentIndex = event.fromIndex
                                }
                            } else {
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
    }

    private fun isValidAppUsage(): Boolean {
        val currentTime = LocalTime.now().toSecondOfDay()
        val isValidTimeSpent = startTime != 0 && ((currentTime - startTime) >= MIN_VALID_TIME_SPENT)
        val isValidScrollCount = appScrollCount >= MIN_VALID_SCROLL_COUNT

        return appPackageName.isNotEmpty() && (isValidTimeSpent || isValidScrollCount)
    }

    private fun saveAppUsage() {

        val appUsage =
                mapOf(
                        "packageName" to appPackageName,
                        "scrollCount" to appScrollCount,
                        "timeSpent" to appTimeSpent,
                        "appOpenCount" to appOpenCount,
                        "scrollsBlocked" to appScrollBlocked
                )

        appUsageList.add(appUsage)
        resetAppUsage()
    }

    private fun getBlockIdForApp(packageName: String): String {
        val blockIdMap =
                mapOf(
                        "Instagram" to "instagram_block_id",
                        "Youtube" to "youtube_block_id",
                        "Linkedin" to "linkedin_block_id",
                        "Snapchat" to "snapchat_block_id"
                )
        return blockIdMap[packageName] ?: "default_block_id"
    }

    private fun resetAppUsage() {
        appPackageName = ""
        appScrollCount = 0
        appTimeSpent = 0
        appOpenCount = 0
        appScrollBlocked = 0

        startTime = 0
        endTime = 0
    }

    override fun onInterrupt() {
        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}
