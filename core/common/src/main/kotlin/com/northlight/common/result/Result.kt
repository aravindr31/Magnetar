package com.northlight.common.result

/**
 * A sealed class representing the result of an operation.
 * Used across all modules as the standard result wrapper.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, String) -> Unit): Result<T> {
    if (this is Result.Error) action(exception, message)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
    is Result.Loading -> Result.Loading
}

fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error   -> throw exception
    is Result.Loading -> error("Result is still loading")
}

