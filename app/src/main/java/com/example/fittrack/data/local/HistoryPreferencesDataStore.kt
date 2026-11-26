package com.example.fittrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.historyPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "history_preferences"
)

enum class HistorySortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

data class HistoryPreferences(
    val sortOrder: HistorySortOrder = HistorySortOrder.NEWEST_FIRST,
    val showSetDetails: Boolean = true
)

class HistoryPreferencesDataStore(private val context: Context) {

    private object Keys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOW_SET_DETAILS = booleanPreferencesKey("show_set_details")
    }

    val preferences: Flow<HistoryPreferences> = context.historyPreferencesDataStore.data.map { prefs ->
        val sortValue = prefs[Keys.SORT_ORDER] ?: HistorySortOrder.NEWEST_FIRST.name
        val sortOrder = runCatching { HistorySortOrder.valueOf(sortValue) }
            .getOrDefault(HistorySortOrder.NEWEST_FIRST)
        val showDetails = prefs[Keys.SHOW_SET_DETAILS] ?: true
        HistoryPreferences(sortOrder = sortOrder, showSetDetails = showDetails)
    }

    suspend fun setSortOrder(order: HistorySortOrder) {
        context.historyPreferencesDataStore.edit { prefs ->
            prefs[Keys.SORT_ORDER] = order.name
        }
    }

    suspend fun setShowSetDetails(show: Boolean) {
        context.historyPreferencesDataStore.edit { prefs ->
            prefs[Keys.SHOW_SET_DETAILS] = show
        }
    }
}