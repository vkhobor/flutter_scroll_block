package com.example.flutter_scroll_block

import org.json.JSONArray
import org.json.JSONObject

class SettingsStore(private val sharedPreferences: android.content.SharedPreferences) {

    companion object {
        private const val TAG = "SettingsStore"
    }

    private val _items: MutableMap<String, ListItem> = mutableMapOf()

    val items: Map<String, ListItem>
        get() = _items.toMap()

    private val preferenceChangeListener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                android.util.Log.d("SharedPreferences", "Key changed: $key")
                when (key) {
                    "list_items" -> {
                        load()
                    }
                }
            }
    private var isListening = false

    public fun startListening() {
        if (!isListening) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
            isListening = true
            load()
        }
    }

    public fun stopListening() {
        if (isListening) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
            isListening = false
        }
    }

    public fun load() {
        _items.clear()
        val jsonString = sharedPreferences.getString("list_items", "[]") ?: "[]"
        android.util.Log.d(TAG, "Preference: $jsonString")
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val item = ListItem.fromJson(jsonObject)
            android.util.Log.d(TAG, "Adding: $item")
            _items[item.appid] = item
        }
    }
}

data class ListItem(val appid: String, val viewid: String, val enabled: Boolean) {
    companion object {
        fun fromJson(jsonObject: JSONObject): ListItem {
            return ListItem(
                    appid = jsonObject.getString("appid"),
                    viewid = jsonObject.getString("viewid"),
                    enabled = jsonObject.getBoolean("enabled")
            )
        }
    }
}
