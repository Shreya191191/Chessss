package eu.tutorials.chessss.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.tutorials.chessss.model.Piece
import eu.tutorials.chessss.model.PieceColor
import eu.tutorials.chessss.model.PieceType
import kotlin.math.abs

class GameState {

    var board: List<List<Piece?>> by mutableStateOf(createInitialBoard())

    var selectedRow: Int? by mutableStateOf(null)
    var selectedCol: Int? by mutableStateOf(null)

    var currentTurn: PieceColor by mutableStateOf(PieceColor.WHITE)

    var validMoves: List<Pair<Int, Int>> by mutableStateOf(emptyList())
    var promotionPosition: Pair<Int, Int>? by mutableStateOf(null)

    var whiteKingMoved by mutableStateOf(false)
    var blackKingMoved by mutableStateOf(false)

    var whiteLeftRookMoved by mutableStateOf(false)
    var whiteRightRookMoved by mutableStateOf(false)

    var blackLeftRookMoved by mutableStateOf(false)
    var blackRightRookMoved by mutableStateOf(false)

    // ✅ EN PASSANT
    var enPassantTarget: Pair<Int, Int>? by mutableStateOf(null)

    fun movePiece(toRow: Int, toCol: Int) {

        val fromRow = selectedRow ?: return
        val fromCol = selectedCol ?: return

        if (!validMoves.contains(Pair(toRow, toCol))) {
            selectedRow = null
            selectedCol = null
            validMoves = emptyList()
            return
        }

        val newBoard = board.map { it.toMutableList() }.toMutableList()
        val movingPiece = newBoard[fromRow][fromCol]

        // ================= MOVE PIECE =================

        newBoard[toRow][toCol] = movingPiece
        newBoard[fromRow][fromCol] = null

        // ================= EN PASSANT CAPTURE =================

        if (movingPiece?.type == PieceType.PAWN) {

            val enPassant = enPassantTarget

            if (enPassant != null &&
                toRow == enPassant.first &&
                toCol == enPassant.second
            ) {

                val capturedRow =
                    if (movingPiece.color == PieceColor.WHITE)
                        toRow + 1
                    else
                        toRow - 1

                newBoard[capturedRow][toCol] = null
            }
        }

        // ================= CASTLING =================

        if (movingPiece?.type == PieceType.KING) {

            if (movingPiece.color == PieceColor.WHITE)
                whiteKingMoved = true
            else
                blackKingMoved = true

            if (movingPiece.color == PieceColor.WHITE && toCol == 6) {
                newBoard[7][5] = newBoard[7][7]
                newBoard[7][7] = null
            }

            if (movingPiece.color == PieceColor.WHITE && toCol == 2) {
                newBoard[7][3] = newBoard[7][0]
                newBoard[7][0] = null
            }

            if (movingPiece.color == PieceColor.BLACK && toCol == 6) {
                newBoard[0][5] = newBoard[0][7]
                newBoard[0][7] = null
            }

            if (movingPiece.color == PieceColor.BLACK && toCol == 2) {
                newBoard[0][3] = newBoard[0][0]
                newBoard[0][0] = null
            }
        }

        if (movingPiece?.type == PieceType.ROOK) {
            if (movingPiece.color == PieceColor.WHITE) {
                if (fromCol == 0) whiteLeftRookMoved = true
                if (fromCol == 7) whiteRightRookMoved = true
            } else {
                if (fromCol == 0) blackLeftRookMoved = true
                if (fromCol == 7) blackRightRookMoved = true
            }
        }

        // ================= SIMULATE =================

        val oldBoard = board
        board = newBoard

        if (isKingInCheck(currentTurn)) {
            board = oldBoard
            selectedRow = null
            selectedCol = null
            validMoves = emptyList()
            return
        }

        // ================= PAWN PROMOTION =================

        if (movingPiece?.type == PieceType.PAWN) {

            if (movingPiece.color == PieceColor.WHITE && toRow == 0) {
                promotionPosition = Pair(toRow, toCol)
                selectedRow = null
                selectedCol = null
                validMoves = emptyList()
                return
            }

            if (movingPiece.color == PieceColor.BLACK && toRow == 7) {
                promotionPosition = Pair(toRow, toCol)
                selectedRow = null
                selectedCol = null
                validMoves = emptyList()
                return
            }
        }

        // ================= SET EN PASSANT TARGET =================

        if (movingPiece?.type == PieceType.PAWN &&
            abs(toRow - fromRow) == 2
        ) {
            val middleRow = (toRow + fromRow) / 2
            enPassantTarget = Pair(middleRow, toCol)
        } else {
            enPassantTarget = null
        }

        selectedRow = null
        selectedCol = null
        validMoves = emptyList()

        currentTurn =
            if (currentTurn == PieceColor.WHITE)
                PieceColor.BLACK
            else
                PieceColor.WHITE
    }

    // ================= REST SAME =================

    fun promotePawn(newType: PieceType) {
        val pos = promotionPosition ?: return
        val newBoard = board.map { it.toMutableList() }.toMutableList()
        val pawn = newBoard[pos.first][pos.second] ?: return
        newBoard[pos.first][pos.second] = Piece(newType, pawn.color)
        board = newBoard
        promotionPosition = null
        currentTurn =
            if (currentTurn == PieceColor.WHITE)
                PieceColor.BLACK
            else
                PieceColor.WHITE
    }

    fun isKingInCheck(color: PieceColor): Boolean {
        var kingPosition: Pair<Int, Int>? = null

        for (r in 0..7)
            for (c in 0..7)
                if (board[r][c]?.type == PieceType.KING &&
                    board[r][c]?.color == color
                )
                    kingPosition = Pair(r, c)

        if (kingPosition == null) return false

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color != color) {
                    val attacks =
                        MoveManager.getAttackMoves(board, r, c)
                    if (attacks.contains(kingPosition))
                    return true
                }
            }
        }

        return false
    }

    fun isCheckMate(color: PieceColor): Boolean {
        if (!isKingInCheck(color)) return false

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == color) {
                    val moves =
                        MoveManager.getValidMoves(board, r, c, this)
                    for (move in moves) {
                        val newBoard =
                            board.map { it.toMutableList() }.toMutableList()
                        newBoard[move.first][move.second] =
                            newBoard[r][c]
                        newBoard[r][c] = null
                        val oldBoard = board
                        board = newBoard
                        val stillInCheck = isKingInCheck(color)
                        board = oldBoard
                        if (!stillInCheck)
                            return false
                    }
                }
            }
        }

        return true
    }

    private fun createInitialBoard(): List<List<Piece?>> {
        return listOf(
            listOf(
                Piece(PieceType.ROOK, PieceColor.BLACK),
                Piece(PieceType.KNIGHT, PieceColor.BLACK),
                Piece(PieceType.BISHOP, PieceColor.BLACK),
                Piece(PieceType.QUEEN, PieceColor.BLACK),
                Piece(PieceType.KING, PieceColor.BLACK),
                Piece(PieceType.BISHOP, PieceColor.BLACK),
                Piece(PieceType.KNIGHT, PieceColor.BLACK),
                Piece(PieceType.ROOK, PieceColor.BLACK)
            ),
            List(8) { Piece(PieceType.PAWN, PieceColor.BLACK) },
            List(8) { null },
            List(8) { null },
            List(8) { null },
            List(8) { null },
            List(8) { Piece(PieceType.PAWN, PieceColor.WHITE) },
            listOf(
                Piece(PieceType.ROOK, PieceColor.WHITE),
                Piece(PieceType.KNIGHT, PieceColor.WHITE),
                Piece(PieceType.BISHOP, PieceColor.WHITE),
                Piece(PieceType.QUEEN, PieceColor.WHITE),
                Piece(PieceType.KING, PieceColor.WHITE),
                Piece(PieceType.BISHOP, PieceColor.WHITE),
                Piece(PieceType.KNIGHT, PieceColor.WHITE),
                Piece(PieceType.ROOK, PieceColor.WHITE)
            )
        )
    }
}
