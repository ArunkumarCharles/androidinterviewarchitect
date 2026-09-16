package com.sevvanam.android_interview_architect.core.model

/**
 * A sealed hierarchy representing operation outcome states (Success, Error, Loading).
 * Standard interview pattern to enforce robust error handling across data and domain layers.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable, val message: String? = exception.localizedMessage) : Result<Nothing>
    object Loading : Result<Nothing>
}
