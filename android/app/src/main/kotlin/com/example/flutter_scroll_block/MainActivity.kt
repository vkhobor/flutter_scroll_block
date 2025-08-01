package com.example.flutter_scroll_block

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    companion object {
        var cachedFlutterEngine: FlutterEngine? = null
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        cachedFlutterEngine = flutterEngine
    }

    override fun onDestroy() {
        super.onDestroy()
        cachedFlutterEngine = null
    }
}
