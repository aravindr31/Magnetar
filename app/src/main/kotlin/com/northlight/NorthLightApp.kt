package com.northlight

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp

private val Application.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "northlight_prefs")

/**
 * Application class — entry point for Hilt dependency injection.
 */
@HiltAndroidApp
class NorthLightApp : Application()
