package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.PieceType.*
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue

object PGN {

    private const val KING_SIDE_CASTLE_NOTATION = "O-O"
    private const val QUEEN_SIDE_CASTLE_NOTATION = "O-O-O"
    private const val BLACK_WIN_PGN = "0-1"
    private const val WHITE_WIN_PGN = "1-0"
    private const val DRAWN_PGN = "1/2-1/2"
    private const val UNKNOWN_RESULT_PGN = "*"

    private val PGN_MOVE_REGEX = "(?<originPiece>[NBQRK])?(?<originFileAdditionalInfo>[a-h])?(?<originRankAdditionalInfo>[1-8])?x?(?<destinationSquare>[a-h][1-8])(=(?<promotion>[NBQR]))?((?<check>\\+)|(?<checkmate>#))?".toRegex()
    private val PGN_MOVE_NUMBER_REGEX = "(([0-9]*)\\.( )?)".toRegex()
    private val PGN_COMMENT_BRACKET_REGEX = "\\[.*]".toRegex()
    private val PGN_COMMENT_CURLY_BRACKET_REGEX = "\\{.*}".toRegex()



    fun exportPGN(game: Game): String {
        val pgnHistory = game.history.moves.mapIndexed { index, move ->
            val indexStr = if (index % 2 == 0) "${index / 2 + 1}. " else ""
            return@mapIndexed indexStr + moveToPgn(move, game.history.moves.size == index + 1)
        }.joinToString(" ")
        val pgnOutcome = when (game.status) {
            Status.BLACK_WIN -> BLACK_WIN_PGN
            Status.WHITE_WIN -> WHITE_WIN_PGN
            Status.DRAW -> DRAWN_PGN
            else -> ""
        }
        return "$pgnHistory $pgnOutcome"
    }

    fun import(pgn: String): Game {
        return pgn.trim()
            .replace(PGN_COMMENT_BRACKET_REGEX, "")
            .replace(PGN_COMMENT_CURLY_BRACKET_REGEX, "")
            .replace("\n", " ")
            .split(" ")
            .map { it.trim().replace(PGN_MOVE_NUMBER_REGEX, "") }
            .filter { it.isNotBlank() }
            .fold(GameFactory.createStandardGame()) { currentGame, pgnMove -> applyPGNMove(currentGame, pgnMove) }

    }

    private fun applyPGNMove(game: Game, pgnMoveStr: String): Game {
        val color = game.sideToMove
        importKingSideCastleMoveIfNecessary(game, pgnMoveStr)?.let { return it }
        importQueenSideCastleMoveIfNecessary(game, pgnMoveStr)?.let { return it }
        importGameEndIfNecessary(game, pgnMoveStr)?.let { return it }
        val pgnMove = PgnMove.parse(pgnMoveStr, color)
        val originSquare = extractOriginSquare(game, pgnMove)
        return game.applyMove(
            moveCommand = MoveCommand(
                origin = originSquare,
                destination = pgnMove.destination,
                promotedPiece = pgnMove.promotedPiece,
            )
        ).orThrow()
    }

    private fun extractOriginSquare(game: Game, pgnMove: PgnMove) : Square {
        val candidateMoves = getAllPseudoLegalMovesForColor(pgnMove.color, game)
            .filter {
                it.piece == pgnMove.piece &&
                        it.destination == pgnMove.destination &&
                        (pgnMove.originFileAdditionalInfo == null || it.origin.file == pgnMove.originFileAdditionalInfo) &&
                        (pgnMove.originRankAdditionalInfo == null || it.origin.rank == pgnMove.originRankAdditionalInfo)
            }
        return if (candidateMoves.isEmpty()) {
            throw IllegalArgumentException("cannot find from square for ${pgnMove.pgnNotation}")
        } else if (candidateMoves.size == 1) {
            candidateMoves[0].origin
        } else {
            candidateMoves.firstNotNullOf { move ->
                game.applyMove(
                        MoveCommand(
                                origin = move.origin,
                                destination = move.destination,
                                promotedPiece = move.promotedTo?.type,
                        )
                ).let { outcome ->
                    when (outcome) {
                        is Success -> move
                        is Failure -> null
                    }
                }
            }.origin
        }
    }

    private fun importGameEndIfNecessary(game: Game, pgnMove: String): Game? =
        when (pgnMove) {
            DRAWN_PGN -> game.copy(status = Status.DRAW)
            BLACK_WIN_PGN -> game.copy(status = Status.BLACK_WIN)
            WHITE_WIN_PGN -> game.copy(status = Status.WHITE_WIN)
            UNKNOWN_RESULT_PGN -> game.copy(status = Status.UNKNOWN_RESULT)
            else -> null
        }


    private fun importKingSideCastleMoveIfNecessary(game: Game,pgnMove: String) =
        importCastleMoveIfNecessary(game, pgnMove, KING_SIDE_CASTLE_NOTATION) { it.isKingCastle }

    private fun importQueenSideCastleMoveIfNecessary(game: Game,pgnMove: String) =
        importCastleMoveIfNecessary(game, pgnMove, QUEEN_SIDE_CASTLE_NOTATION) { it.isQueenCastle }

    private fun importCastleMoveIfNecessary(
        game: Game,
        pgnMove: String,
        expectedCastlePgn: String,
        castleMoveChecker : (Move) -> Boolean
    ) : Game? {
        if (pgnMove.replace("+", "") == expectedCastlePgn) {
            val castleMove = getAllPseudoLegalMovesForColor(game.sideToMove, game)
                .firstOrNull { castleMoveChecker(it) }
                ?: throw IllegalArgumentException("cannot find perform castle move $pgnMove for ${game.sideToMove}")
            return game.applyMove(
                moveCommand = MoveCommand(
                    origin = castleMove.origin,
                    destination = castleMove.destination,
                    promotedPiece = null,
                )
            ).orThrow()
        }
        return null
    }



    private fun moveToPgn(move: Move, isLastMove: Boolean): String {
        return if (move.isKingCastle) {
            KING_SIDE_CASTLE_NOTATION
        } else if (move.isQueenCastle) {
            QUEEN_SIDE_CASTLE_NOTATION
        } else {
            val piece = move.piece.type.pgnNotation + when (move.piece.type) {
                PAWN -> move.origin
                KNIGHT -> move.origin
                ROOK -> move.origin
                else -> ""
            }
            val checkOrCheckmateNotation = move.isCheck.ifTrue { if (isLastMove) "#" else "+" } ?: ""
            val captured = if (move.capturedPiece != null) "x" else ""
            val destination = move.destination
            val promotedTo = if (move.promotedTo != null) {
                when (move.promotedTo.type) {
                    BISHOP -> "=B"
                    KNIGHT -> "=N"
                    QUEEN -> "=Q"
                    ROOK -> "=R"
                    else -> throw IllegalStateException("cannot promote to ${move.promotedTo}")
                }
            } else {
                ""
            }
            return piece + captured + destination + promotedTo + checkOrCheckmateNotation
        }
    }

    data class PgnMove(
        val piece: Piece,
        val destination: Square,
        val originRankAdditionalInfo: Rank? = null,
        val originFileAdditionalInfo: File? = null,
        val promotedPiece: PieceType? = null,
        val color: Color,
        val pgnNotation: String,
    ) {
        companion object {
            fun parse(pgnMove: String, color : Color): PgnMove {
                val matchResult = PGN_MOVE_REGEX.find(pgnMove)!!
                val pieceType = matchResult.groups["originPiece"]?.value.let{ PieceType.fromPGNNotation(it ?: "") }
                val piece = Piece.entries.first { it.color == color && it.type == pieceType }
                val fromFileAdditionalInfo = matchResult.groups["originFileAdditionalInfo"]?.value?.let { File.at(it) }
                val fromRankAdditionalInfo = matchResult.groups["originRankAdditionalInfo"]?.value?.let { Rank.at(it.toInt()) }
                val destinationSquare = matchResult.groups["destinationSquare"]?.value!!.let { Square.at(it) }
                val promotedPiece = matchResult.groups["promotion"]?.value?.let{ PieceType.fromPGNNotation(it) }
                val isCheck = matchResult.groups["check"] != null
                val isCheckmate = matchResult.groups["checkmate"] != null
                return PgnMove(
                    piece = piece,
                    destination = destinationSquare,
                    promotedPiece = promotedPiece,
                    originFileAdditionalInfo = fromFileAdditionalInfo,
                    originRankAdditionalInfo = fromRankAdditionalInfo,
                    color = color,
                    pgnNotation = pgnMove,
                )
            }
        }
    }


}

