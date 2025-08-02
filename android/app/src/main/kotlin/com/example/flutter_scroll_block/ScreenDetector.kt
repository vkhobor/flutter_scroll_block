package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ScreenDetector(
        private val settings: SettingsStore,
        private val accessibilityService: AccessibilityService
) : AccessibilityDetector {
    private var screen: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent): Boolean {
        val items = settings.getItemsForPackageId(event.packageName.toString())

        for (setting in items.filter { it.enabled }) {
            val viewId = "${event.packageName}:id/${setting.viewid}"
            val blockContent =
                    accessibilityService.rootInActiveWindow?.findAccessibilityNodeInfosByViewId(
                            viewId
                    )

            if (blockContent != null && blockContent.isNotEmpty()) {
                screen = setting.viewid
                return true
            }
        }

        return false
    }

    public fun getScreen(): String {
        return screen ?: ""
    }
}
