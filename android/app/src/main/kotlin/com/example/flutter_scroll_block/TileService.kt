package com.example.flutter_scroll_block

import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class MyQSTileService : TileService() {
    companion object {
        private const val TAG = "MyQSTileService"
    }

    private val settingsStore: SettingsStore by lazy {
        SettingsStore(
                PreferenceManager.getDefaultSharedPreferences(this),
                onItemsChanged = { updateFromSettings() }
        )
    }
    private var on = false
    private var isListening = false

    override fun onTileAdded() {
        super.onTileAdded()
        settingsStore.startListening()
    }

    override fun onStartListening() {
        super.onStartListening()
        android.util.Log.d(TAG, "onStartListening")
        isListening = true
        settingsStore.load()
        settingsStore.startListening()
    }

    private fun updateFromSettings() {
        if (!isListening) return
        android.util.Log.d(TAG, "updateFromSettings")

        val anyOn = settingsStore.isAnyOn()
        android.util.Log.d(TAG, "anyOn: $anyOn")

        if (anyOn) {
            on = true
        } else {
            on = false
        }

        updateTile()
    }

    override fun onStopListening() {
        android.util.Log.d(TAG, "onStopListening")
        isListening = false
        settingsStore.stopListening()

        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        if (!on) {
            settingsStore.setAllOn()
        } else {
            settingsStore.setAllOff()
        }
    }

    private fun updateTile() {
        val tile = qsTile
        tile?.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile?.updateTile()
    }

    override fun onTileRemoved() {
        settingsStore.stopListening()
        super.onTileRemoved()
    }
}
