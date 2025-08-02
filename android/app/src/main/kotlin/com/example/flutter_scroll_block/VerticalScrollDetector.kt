package com.example.flutter_scroll_block

import android.view.accessibility.AccessibilityEvent

class VerticalScrollDetector() : AccessibilityDetector {

    companion object {
        private const val TAG = "VerticalScrollDetector"
    }

    public override fun onAccessibilityEvent(event: AccessibilityEvent): Boolean {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (event.scrollDeltaY != 0) return true
        }
        return false
    }
}
