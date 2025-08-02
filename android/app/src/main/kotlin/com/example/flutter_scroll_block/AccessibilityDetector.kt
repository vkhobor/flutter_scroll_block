package com.example.flutter_scroll_block

import android.view.accessibility.AccessibilityEvent

interface AccessibilityDetector {
    fun onAccessibilityEvent(event: AccessibilityEvent): Boolean {
        return false
    }
}
