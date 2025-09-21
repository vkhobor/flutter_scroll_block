package com.example.flutter_scroll_block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ScreenDetector(
        private val settings: SettingsStore,
        private val accessibilityService: AccessibilityService
) : AccessibilityDetector {
    private var screen: String? = null
    private var shouldImmediateBlock: Boolean = false

    override fun onAccessibilityEvent(event: AccessibilityEvent): Boolean {
        val items = settings.getItemsForPackageId(event.packageName.toString())
        
        // Reset immediate block flag
        shouldImmediateBlock = false

        for (setting in items.filter { it.enabled }) {
            val viewId = "${event.packageName}:id/${setting.viewid}"
            val blockContent =
                    accessibilityService.rootInActiveWindow?.findAccessibilityNodeInfosByViewId(
                            viewId
                    )

            if (blockContent != null && blockContent.isNotEmpty()) {
                screen = setting.viewid
                // Check if this specific setting has immediate block enabled
                if (setting.immediateBlock) {
                    shouldImmediateBlock = true
                }
                return true
            }
        }

        return false
    }

    public fun getScreen(): String {
        return screen ?: ""
    }
    
    public fun shouldBlockImmediately(): Boolean {
        return shouldImmediateBlock
    }
}
