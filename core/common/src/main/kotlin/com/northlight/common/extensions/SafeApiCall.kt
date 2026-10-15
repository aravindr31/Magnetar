package com.northlight.common.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import com.northlight.common.result.Result

/**
 * Safely executes a suspending block and wraps the result in [Result].
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    block: suspend () -> T
): Result<T> = withContext(dispatcher) {
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}
