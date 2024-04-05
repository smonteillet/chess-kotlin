package fr.smo.chess.core.utils

sealed class Outcome<out T, out ERR : OutcomeError> {

    fun <U> map(mapFn: (T) -> U): Outcome<U, ERR> = when (this) {
        is Success -> Success(mapFn(value))
        is Failure -> this
    }

    fun orNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun orThrow() : T = when (this) {
        is Success -> value
        is Failure -> throw IllegalStateException(error.toString())
    }
}

// it is not possible to put this directly in Outcome
fun <E : OutcomeError, T, U> Outcome<T, E>.flatMap(f: (T) -> Outcome<U, E>): Outcome<U, E> =
        when (this) {
            is Success -> f(value)
            is Failure -> this
        }

data class Success<T>(val value: T) : Outcome<T, Nothing>()
data class Failure<ERR : OutcomeError>(val error :ERR) : Outcome<Nothing, ERR>()

interface OutcomeError {
    val message : String
}

