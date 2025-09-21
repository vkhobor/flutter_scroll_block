package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.content.res.Resources
import android.os.SystemClock
import android.preference.PreferenceManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ScrollAccessibility : AccessibilityService() {

    companion object {
        private const val TAG = "ScrollAccessibility"
        private const val POLLING_INTERVAL = 1_000L // 10 seconds
    }

    private lateinit var settingsStore: SettingsStore
    private lateinit var poller: Poller

    private lateinit var scrollDetector: VerticalScrollDetector
    private lateinit var appDetector: AppDetector
    private lateinit var screenDetector: ScreenDetector

    private val throttler = Throttler(3_000L)

    private var currentPackageId = ""

    override fun onServiceConnected() {
        super.onServiceConnected()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        settingsStore = SettingsStore(sharedPreferences)
        settingsStore.startListening()

        poller =
                Poller(POLLING_INTERVAL) {
                    val settings = settingsStore.getItemsForPackageId(currentPackageId)
                    if (settings.isNotEmpty() && settings.any { it.enabled && it.usePolling }) {
                        onPoll()
                    }
                }

        val height = Resources.getSystem().displayMetrics.heightPixels
        android.util.Log.d(TAG, "height: $height")

        scrollDetector = VerticalScrollDetector()
        screenDetector = ScreenDetector(settingsStore, this)
        appDetector = AppDetector(settingsStore)

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

    fun isAfterBootDelay(): Boolean {
        val elapsedMillis = SystemClock.elapsedRealtime()
        val sevenMinutesMillis = 7 * 60 * 1000L

        if (elapsedMillis >= sevenMinutesMillis) {
            return true
        } else {
            return false
        }
    }

    fun block() {
        val hasImmediateBlock = settingsStore.hasImmediateBlockForPackage(currentPackageId)
        
        if (!hasImmediateBlock && !isAfterBootDelay()) return

        throttler.throttle {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this@ScrollAccessibility, "Feature Blocked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            settingsStore.startListening()

            if (!appDetector.onAccessibilityEvent(event)) {
                poller.stop()
                previousScreen = null
                isFirstPoll = true
                return
            }
            currentPackageId = it.packageName.toString()

            if (!screenDetector.onAccessibilityEvent(event)) {
                poller.stop()
                previousScreen = null
                isFirstPoll = true
                return
            }
            if (currentScreenId != screenDetector.getScreen()) {
                previousScreen = null
                isFirstPoll = true
            }
            poller.start()

            if (!scrollDetector.onAccessibilityEvent(event)) return

            poller.stop()
            block()
        }
    }

    private var currentScreenId: String? = null
    private var previousScreen: String? = null
    private var isFirstPoll = true

    fun onPoll() {
        if (isFirstPoll) {
            isFirstPoll = false
            return
        }

        val settings = settingsStore.getItemsForPackageId(currentPackageId)
        if (settings.isEmpty() || settings.all { !it.enabled }) {
            return
        }

        val builder = StringBuilder()
        traverseTree(rootInActiveWindow, builder)

        val text = builder.toString()
        if (previousScreen != null) {
            val diff = previousScreen != text

            if (diff) {
                block()
                previousScreen = null
            }
        }
        previousScreen = text
    }

    fun traverseTree(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        builder.append(node.viewIdResourceName).append(node.text).append(node.className).append("|")

        for (i in 0 until node.childCount) {
            traverseTree(node.getChild(i), builder)
        }
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
