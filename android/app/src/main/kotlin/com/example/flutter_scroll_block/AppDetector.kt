package com.example.flutter_scroll_block

import android.view.accessibility.AccessibilityEvent

class AppDetector(private val settings: SettingsStore) : AccessibilityDetector {
    override fun onAccessibilityEvent(event: AccessibilityEvent): Boolean {
        val packageName = event.packageName.toString()
        val items = settings.getItemsForPackageId(packageName)

        if (items.isEmpty()) {
            return false
        }

        if (items.any { it.enabled }) {
            return true
        }

        return false
    }
}
