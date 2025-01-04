package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.PieceType.*
import fr.smo.chess.core.Position.ForcedOutcome
import fr.smo.chess.core.algs.getAllPseudoLegalMovesForColor
import fr.smo.chess.core.algs.getPseudoLegalMovesRegardingDestination
import fr.smo.chess.core.utils.Failure
import fr.smo.chess.core.utils.Success
import fr.smo.chess.core.utils.ifTrue
import fr.smo.chess.core.variant.Standard
import fr.smo.chess.core.variant.Variant

object PGN {

    private const val KING_SIDE_CASTLE_NOTATION = "O-O"
    private const val QUEEN_SIDE_CASTLE_NOTATION = "O-O-O"
    private const val BLACK_WIN_PGN = "0-1"
    private const val WHITE_WIN_PGN = "1-0"
    private const val DRAWN_PGN = "1/2-1/2"
    private const val UNKNOWN_RESULT_PGN = "*"

    private val PGN_MOVE_REGEX =
        "(?<originPiece>[NBQRK])?(?<originFileAdditionalInfo>[a-h])?(?<originRankAdditionalInfo>[1-8])?x?(?<destinationSquare>[a-h][1-8])(=(?<promotion>[NBQR]))?((?<check>\\+)|(?<checkmate>#))?".toRegex()
    private val PGN_MOVE_NUMBER_REGEX = "(([0-9]*)\\.( )?)".toRegex()
    private val PGN_COMMENT_BRACKET_REGEX = "\\[.*]".toRegex()
    private val PGN_COMMENT_CURLY_BRACKET_REGEX = "\\{.*}".toRegex()


    fun exportPGN(position: Position): String {
        val pgnHistory = position.history.moves.mapIndexed { index, move ->
            val indexStr = if (index % 2 == 0) "${index / 2 + 1}. " else ""
            return@mapIndexed indexStr + moveToPgn(move, position.history.moves.size == index + 1)
        }.joinToString(" ")
        val pgnOutcome = when (position.status) {
            Status.BLACK_WIN -> BLACK_WIN_PGN
            Status.WHITE_WIN -> WHITE_WIN_PGN
            Status.DRAW -> DRAWN_PGN
            else -> ""
        }
        return "$pgnHistory $pgnOutcome"
    }

    fun import(pgn: String, variant: Variant = Standard): Position {
        return pgn.trim()
            .replace(PGN_COMMENT_BRACKET_REGEX, "")
            .replace(PGN_COMMENT_CURLY_BRACKET_REGEX, "")
            .replace("\n", " ")
            .split(" ")
            .map { it.trim().replace(PGN_MOVE_NUMBER_REGEX, "") }
            .filter { it.isNotBlank() }
            .fold(GameFactory.createGame(variant)) { currentGame, pgnMove -> applyPGNMove(currentGame, pgnMove) }

    }

    private fun applyPGNMove(position: Position, pgnMoveStr: String): Position {
        val color = position.sideToMove
        importKingSideCastleMoveIfNecessary(position, pgnMoveStr)?.let { return it }
        importQueenSideCastleMoveIfNecessary(position, pgnMoveStr)?.let { return it }
        importGameEndIfNecessary(position, pgnMoveStr)?.let { return it }
        val pgnMove = PgnMove.parse(pgnMoveStr, color)
        val originSquare = extractOriginSquare(position, pgnMove)
        return position.applyMove(
            moveCommand = MoveCommand(
                origin = originSquare,
                destination = pgnMove.destination,
                promotedPiece = pgnMove.promotedPiece,
            )
        ).orThrow()
    }

    private fun extractOriginSquare(position: Position, pgnMove: PgnMove): Square {
        val candidateMoves = getPseudoLegalMovesRegardingDestination(
            position,
            PiecePosition(square = pgnMove.destination, piece = pgnMove.piece)
        )
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
                position.applyMove(
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

    private fun importGameEndIfNecessary(position: Position, pgnMove: String): Position? =
        when (pgnMove) {
            DRAWN_PGN -> if (position.isDraw()) position else position.forceOutcome(ForcedOutcome.DRAW_AGREEMENT)
            BLACK_WIN_PGN -> if (position.isCheckMate()) position else position.forceOutcome(ForcedOutcome.WHITE_RESIGN)
            WHITE_WIN_PGN -> if (position.isCheckMate()) position else position.forceOutcome(ForcedOutcome.BLACK_RESIGN)
            UNKNOWN_RESULT_PGN -> position.forceOutcome(ForcedOutcome.UNKNOWN_RESULT)
            else -> null
        }


    private fun importKingSideCastleMoveIfNecessary(position: Position, pgnMove: String) =
        importCastleMoveIfNecessary(position, pgnMove, KING_SIDE_CASTLE_NOTATION) { it.isKingCastle }

    private fun importQueenSideCastleMoveIfNecessary(position: Position, pgnMove: String) =
        importCastleMoveIfNecessary(position, pgnMove, QUEEN_SIDE_CASTLE_NOTATION) { it.isQueenCastle }

    private fun importCastleMoveIfNecessary(
        position: Position,
        pgnMove: String,
        expectedCastlePgn: String,
        castleMoveChecker: (Move) -> Boolean
    ): Position? {
        if (pgnMove.replace("+", "") == expectedCastlePgn) {
            val castleMove = getAllPseudoLegalMovesForColor(position.sideToMove, position)
                .firstOrNull { castleMoveChecker(it) }
                ?: throw IllegalArgumentException("cannot find perform castle move $pgnMove for ${position.sideToMove}")
            return position.applyMove(
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
                PAWN -> if (move.capturedPiece != null) move.origin else ""
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
            fun parse(pgnMove: String, color: Color): PgnMove {
                val matchResult = PGN_MOVE_REGEX.find(pgnMove)!!
                val pieceType = matchResult.groups["originPiece"]?.value.let { PieceType.fromPGNNotation(it ?: "") }
                val piece = Piece.entries.first { it.color == color && it.type == pieceType }
                val fromFileAdditionalInfo = matchResult.groups["originFileAdditionalInfo"]?.value?.let { File.at(it) }
                val fromRankAdditionalInfo =
                    matchResult.groups["originRankAdditionalInfo"]?.value?.let { Rank.at(it.toInt()) }
                val destinationSquare = matchResult.groups["destinationSquare"]?.value!!.let { Square.at(it) }
                val promotedPiece = matchResult.groups["promotion"]?.value?.let { PieceType.fromPGNNotation(it) }
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

