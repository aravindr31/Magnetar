package com.northlight.ui.navigation

/**
 * Central route definitions for the app navigation graph.
 * All feature modules reference these constants.
 */
object Routes {
    const val BROWSE = "browse"
    const val SEARCH = "search"
    const val DETAILS = "details/{movieId}"
    const val PLAYER = "player/{movieId}/{torrentHash}"
    const val SETTINGS = "settings"

    fun details(movieId: Int) = "details/$movieId"
    fun player(movieId: Int, torrentHash: String) = "player/$movieId/$torrentHash"
}

