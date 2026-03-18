package com.thepriceisright.domain.model

sealed class Resource<out T> {

    data class Success<out T>(val data: T) : Resource<T>()

    data class Error<out T>(
        val message: String,
        val data: T? = null
    ) : Resource<T>()

    data class Loading<out T>(val data: T? = null) : Resource<T>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, data?.let(transform))
        is Loading -> Loading(data?.let(transform))
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> data
        is Loading -> data
    }
}
