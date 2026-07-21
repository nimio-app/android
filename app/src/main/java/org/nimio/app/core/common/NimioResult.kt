package org.nimio.app.core.common

sealed interface NimioResult<out T> {
    data class Success<T>(val value: T) : NimioResult<T>
    data class Error(val throwable: Throwable) : NimioResult<Nothing>
}

