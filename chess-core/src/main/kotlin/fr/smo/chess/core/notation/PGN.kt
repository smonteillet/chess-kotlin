package fr.smo.chess.core.notation

import fr.smo.chess.core.*
import fr.smo.chess.core.PieceType.*
import java.lang.Exception

object PGN {

    private const val KING_SIDE_CASTLE_NOTATION = "O-O"
    private const val QUEEN_SIDE_CASTLE_NOTATION = "O-O-O"
    private const val BLACK_WIN_PGN = "0-1"
    private const val WHITE_WIN_PGN = "1-0"
    private const val DRAWN_PGN = "1/2-1/2"
    private const val UNKNOWN_RESULT_PGN = "*"

    private val PGN_MOVE_REGEX = "(?<fromPiece>[NBQRK])?(?<fromFileAdditionalInfo>[a-h])?(?<fromRankAdditionalInfo>[1-8])?x?(?<destinationSquare>[a-h][1-8])(=(?<promotion>[NBQR]))?((?<check>\\+)|(?<checkmate>#))?".toRegex()
    private val PGN_MOVE_NUMBER_REGEX = "(([0-9]*)\\.( )?)".toRegex()
    private val PGN_COMMENT_BRACKET_REGEX = "\\[.*]".toRegex()
    private val PGN_COMMENT_CURLY_BRACKET_REGEX = "\\{.*}".toRegex()



    fun exportPGN(game: Game): String {
        val pgnHistory = game.history.moves.mapIndexed { index, move ->
            val indexStr = if (index % 2 == 0) "${index / 2 + 1}. " else ""
            return@mapIndexed indexStr + moveToPgn(move)
        }.joinToString(" ")
        val pgnOutcome = when (game.status) {
            Game.Status.BLACK_WIN -> BLACK_WIN_PGN
            Game.Status.WHITE_WIN -> WHITE_WIN_PGN
            Game.Status.DRAW -> DRAWN_PGN
            else -> ""
        }
        return pgnHistory + pgnOutcome
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
        val fromSquare = extractFromSquare(game, pgnMove)
        return game.applyMove(
            moveRequest = MoveRequest(
                from = fromSquare,
                destination = pgnMove.destination,
                promotedPiece = pgnMove.promotedPiece,
            )
        )
    }

    private fun extractFromSquare(game: Game, pgnMove: PgnMove) : Square {
        val candidateMoves = game.chessboard
            .getAllPseudoLegalMovesForColor(pgnMove.color, game)
            .filter {
                it.piece == pgnMove.piece &&
                        it.destination == pgnMove.destination &&
                        (pgnMove.fromFileAdditionalInfo == null || it.from.file == pgnMove.fromFileAdditionalInfo) &&
                        (pgnMove.fromRankAdditionalInfo == null || it.from.rank == pgnMove.fromRankAdditionalInfo)
            }
        return if (candidateMoves.isEmpty()) {
            throw IllegalArgumentException("cannot find from square for ${pgnMove.pgnNotation}")
        } else if (candidateMoves.size == 1){
            candidateMoves[0].from
        } else {
            candidateMoves.firstOrNull {
                var isGoodMove = true
                try {
                    game.applyMove(
                        MoveRequest(
                            from = it.from,
                            destination = it.destination,
                            promotedPiece = it.promotedTo?.type,
                        )
                    )
                } catch (e: Exception) {
                    isGoodMove = false
                }
                isGoodMove
            }?.from ?: throw IllegalArgumentException("cannot find from square for ${pgnMove.pgnNotation}")
        }
    }

    private fun importGameEndIfNecessary(game: Game, pgnMove: String): Game? =
        when (pgnMove) {
            DRAWN_PGN -> game.copy(status = Game.Status.DRAW)
            BLACK_WIN_PGN -> game.copy(status = Game.Status.BLACK_WIN)
            WHITE_WIN_PGN -> game.copy(status = Game.Status.WHITE_WIN)
            UNKNOWN_RESULT_PGN -> game.copy(status = Game.Status.UNKNOWN_RESULT)
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
            val castleMove = game.chessboard.getAllPseudoLegalMovesForColor(game.sideToMove, game)
                .firstOrNull { castleMoveChecker(it) }
                ?: throw IllegalArgumentException("cannot find perform castle move $pgnMove for ${game.sideToMove}")
            return game.applyMove(
                moveRequest = MoveRequest(
                    from = castleMove.from,
                    destination = castleMove.destination,
                    promotedPiece = null,
                )
            )
        }
        return null
    }



    private fun moveToPgn(move: Move): String {
        return if (move.isKingCastle) {
            KING_SIDE_CASTLE_NOTATION
        } else if (move.isQueenCastle) {
            QUEEN_SIDE_CASTLE_NOTATION
        } else {
            val piece = move.piece.type.pgnNotation + when (move.piece.type) {
                PAWN -> move.from
                KNIGHT -> move.from
                ROOK -> move.from
                else -> ""
            }
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
            return piece + captured + destination + promotedTo
        }
    }

    data class PgnMove(
        val piece: Piece,
        val destination: Square,
        val fromRankAdditionalInfo: Rank? = null,
        val fromFileAdditionalInfo: File? = null,
        val promotedPiece: PieceType? = null,
        val color: Color,
        val pgnNotation: String,
    ) {
        companion object {
            fun parse(pgnMove: String, color : Color): PgnMove {
                val matchResult = PGN_MOVE_REGEX.find(pgnMove)!!
                val pieceType = matchResult.groups["fromPiece"]?.value.let{ PieceType.fromPGNNotation(it ?: "") }
                val piece = Piece.values().first { it.color == color && it.type == pieceType }
                val fromFileAdditionalInfo = matchResult.groups["fromFileAdditionalInfo"]?.value?.let { File.at(it) }
                val fromRankAdditionalInfo = matchResult.groups["fromRankAdditionalInfo"]?.value?.let { Rank.at(it.toInt()) }
                val destinationSquare = matchResult.groups["destinationSquare"]?.value!!.let { Square.at(it) }
                val promotedPiece = matchResult.groups["promotion"]?.value?.let{ PieceType.fromPGNNotation(it) }
                val isCheck = matchResult.groups["check"] != null
                val isCheckmate = matchResult.groups["checkmate"] != null
                return PgnMove(
                    piece = piece,
                    destination = destinationSquare,
                    promotedPiece = promotedPiece,
                    fromFileAdditionalInfo = fromFileAdditionalInfo,
                    fromRankAdditionalInfo = fromRankAdditionalInfo,
                    color = color,
                    pgnNotation = pgnMove,
                )
            }
        }
    }


}

