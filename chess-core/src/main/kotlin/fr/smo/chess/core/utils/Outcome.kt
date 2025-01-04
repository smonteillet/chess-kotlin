package fr.smo.chess.core.utils

sealed class Outcome<out ERR : OutcomeError, out T> {

    fun <U> map(mapFn: (T) -> U): Outcome<ERR, U> = when (this) {
        is Success -> Success(mapFn(value))
        is Failure -> this
    }

    fun orNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun orThrow(exceptionSupplier: (ERR) -> Exception = { IllegalStateException(it.toString()) }): T = when (this) {
        is Success -> value
        is Failure -> throw exceptionSupplier.invoke(error)
    }
}

// it is not possible to put this directly in Outcome
fun <E : OutcomeError, T, U> Outcome<E, T>.flatMap(f: (T) -> Outcome<E, U>): Outcome<E, U> =
    when (this) {
        is Success -> f(value)
        is Failure -> this
    }

data class Success<T>(val value: T) : Outcome<Nothing, T>()
data class Failure<ERR : OutcomeError>(val error: ERR) : Outcome<ERR, Nothing>()

interface OutcomeError {
    val message: String
}

