package fr.smo.chess.core.utils


fun <T> Boolean.ifTrue(lambda: (Unit) -> T?): T? = if (this) lambda.invoke(Unit) else null
