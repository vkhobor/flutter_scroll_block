package com.example.flutter_scroll_block

import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray
import org.json.JSONObject

class SettingsStore(
        private val sharedPreferences: android.content.SharedPreferences,
        private val onItemsChanged: (() -> Unit)? = null
) {

    companion object {
        private const val TAG = "SettingsStore"
    }

    private var methodChannel: MethodChannel? = null
    private val _items: MutableMap<String, MutableList<ListItem>> = mutableMapOf()

    public fun getItemsForPackageId(packageId: String): List<ListItem> {
        return _items[packageId] ?: emptyList()
    }

    private fun onItemsChanged() {
        onItemsChanged?.invoke()
        invokeFlutterMethodChannel()
    }

    private fun invokeFlutterMethodChannel() {
        if (methodChannel != null) {
            methodChannel!!.invokeMethod(Channels.Settings.ON_CHANGE_METHOD, null)
        } else {
            if (MainActivity.cachedFlutterEngine != null) {
                methodChannel =
                        MethodChannel(
                                MainActivity.cachedFlutterEngine!!.dartExecutor.binaryMessenger,
                                Channels.Settings.CHANNEL_NAME
                        )
                methodChannel!!.invokeMethod(Channels.Settings.ON_CHANGE_METHOD, null)
            }
        }
    }

    private val preferenceChangeListener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                android.util.Log.d("SharedPreferences", "Key changed: $key")
                when (key) {
                    "list_items" -> {
                        load()
                        onItemsChanged()
                    }
                }
            }
    private var isListening = false

    public fun startListening() {
        if (!isListening) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
            isListening = true
            load()
            onItemsChanged()
        }
    }

    public fun stopListening() {
        if (isListening) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
            isListening = false
        }
    }

    public fun setAllOff() {
        _items.forEach { (_, items) ->
            for (i in items.indices) {
                items[i] = items[i].copy(enabled = false)
            }
        }
        save()
        onItemsChanged()
    }

    public fun setAllOn() {
        _items.forEach { (_, items) ->
            for (i in items.indices) {
                items[i] = items[i].copy(enabled = true)
            }
        }
        save()
        onItemsChanged()
    }

    private fun save() {
        val jsonArray = JSONArray()
        _items.values.forEach { items -> items.forEach { item -> jsonArray.put(item.toJson()) } }
        sharedPreferences.edit().putString("list_items", jsonArray.toString()).apply()
    }

    public fun isAnyOn(): Boolean {
        return _items.values.any { it.any { it.enabled } }
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

            if (_items.containsKey(item.appid)) {
                _items[item.appid]!!.add(item)
            } else {
                _items[item.appid] = mutableListOf(item)
            }
        }
    }
}

data class ListItem(
        val appid: String,
        val viewid: String,
        val enabled: Boolean,
        val usePolling: Boolean,
        val immediateBlock: Boolean
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): ListItem {
            return ListItem(
                    appid = jsonObject.getString("appid"),
                    viewid = jsonObject.getString("viewid"),
                    enabled = jsonObject.getBoolean("enabled"),
                    usePolling = jsonObject.getBoolean("usePolling"),
                    immediateBlock = jsonObject.optBoolean("immediateBlock", false)
            )
        }
    }

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("appid", appid)
        jsonObject.put("viewid", viewid)
        jsonObject.put("enabled", enabled)
        jsonObject.put("usePolling", usePolling)
        jsonObject.put("immediateBlock", immediateBlock)
        return jsonObject
    }
}
