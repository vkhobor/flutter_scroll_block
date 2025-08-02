package com.example.flutter_scroll_block

class Throttler(private val intervalMillis: Long) {
    private var lastExecutionTime = 0L

    fun throttle(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastExecutionTime >= intervalMillis) {
            lastExecutionTime = currentTime
            action()
        }
    }
}
