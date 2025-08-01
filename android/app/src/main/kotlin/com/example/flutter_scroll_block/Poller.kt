package com.example.flutter_scroll_block

import android.os.Handler
import android.os.Looper

class Poller(private val intervalMillis: Long, private val callback: () -> Unit) {
    private var isPolling = false
    private val handler = Handler(Looper.getMainLooper())
    private val poller =
            object : Runnable {
                override fun run() {
                    callback()
                    handler.postDelayed(this, intervalMillis)
                }
            }

    fun start() {
        if (isPolling) return
        isPolling = true
        handler.post(poller)
    }

    fun stop() {
        if (!isPolling) return
        isPolling = false
        handler.removeCallbacks(poller)
    }
}
