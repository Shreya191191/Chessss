package eu.tutorials.chessss.game

import eu.tutorials.chessss.model.*

object MoveManager {

    fun getValidMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        gameState: GameState
    ): List<Pair<Int, Int>> {

        val piece = board[row][col] ?: return emptyList()

        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(board, row, col, piece, gameState)
            PieceType.ROOK -> rookMoves(board, row, col, piece)
            PieceType.BISHOP -> bishopMoves(board, row, col, piece)
            PieceType.KNIGHT -> knightMoves(board, row, col, piece)
            PieceType.QUEEN -> queenMoves(board, row, col, piece)
            PieceType.KING -> kingMoves(board, row, col, piece, gameState)
        }
    }

    // ================= ATTACK MOVES (NO CASTLING) =================

    //private
    fun getAttackMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int
    ): List<Pair<Int, Int>> {

        val piece = board[row][col] ?: return emptyList()

        return when (piece.type) {
            PieceType.PAWN -> pawnAttackMoves(row, col, piece)
            PieceType.ROOK -> rookMoves(board, row, col, piece)
            PieceType.BISHOP -> bishopMoves(board, row, col, piece)
            PieceType.KNIGHT -> knightMoves(board, row, col, piece)
            PieceType.QUEEN -> queenMoves(board, row, col, piece)
            PieceType.KING -> basicKingMoves(row, col) // ⚠ no castling here
        }
    }


    // ================= PAWN =================

    private fun pawnMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece,
        gameState: GameState
    ): List<Pair<Int, Int>> {

        val moves = mutableListOf<Pair<Int, Int>>()
        val dir = if (piece.color == PieceColor.WHITE) -1 else 1
        val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
        val nextRow = row + dir

        if (nextRow in 0..7 && board[nextRow][col] == null) {
            moves.add(Pair(nextRow, col))

            if (row == startRow && board[row + 2 * dir][col] == null) {
                moves.add(Pair(row + 2 * dir, col))
            }
        }

        for (dc in listOf(-1, 1)) {
            val newCol = col + dc
            if (nextRow in 0..7 && newCol in 0..7) {
                val target = board[nextRow][newCol]
                if (target != null && target.color != piece.color) {
                    moves.add(Pair(nextRow, newCol))
                }
            }

            // EN PASSANT
            val enPassant = gameState.enPassantTarget
            if (enPassant != null) {

                if (nextRow == enPassant.first &&
                    newCol == enPassant.second
                ) {
                    moves.add(enPassant)
                }
            }
        }
        return moves
    }

    private fun pawnAttackMoves(
        row: Int,
        col: Int,
        piece: Piece
    ): List<Pair<Int, Int>> {

        val moves = mutableListOf<Pair<Int, Int>>()
        val dir = if (piece.color == PieceColor.WHITE) -1 else 1
        val nextRow = row + dir

        for (dc in listOf(-1, 1)) {
            val newCol = col + dc
            if (nextRow in 0..7 && newCol in 0..7) {
                moves.add(Pair(nextRow, newCol))
            }
        }

        return moves
    }

    // ================= ROOK =================

    private fun rookMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece
    ): List<Pair<Int, Int>> {

        val moves = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(
            Pair(1, 0), Pair(-1, 0),
            Pair(0, 1), Pair(0, -1)
        )

        for ((dr, dc) in directions) {
            var r = row + dr
            var c = col + dc

            while (r in 0..7 && c in 0..7) {
                val target = board[r][c]
                if (target == null) {
                    moves.add(Pair(r, c))
                } else {
                    if (target.color != piece.color)
                        moves.add(Pair(r, c))
                    break
                }
                r += dr
                c += dc
            }
        }

        return moves
    }

    // ================= BISHOP =================

    private fun bishopMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece
    ): List<Pair<Int, Int>> {

        val moves = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(
            Pair(1, 1), Pair(1, -1),
            Pair(-1, 1), Pair(-1, -1)
        )

        for ((dr, dc) in directions) {
            var r = row + dr
            var c = col + dc

            while (r in 0..7 && c in 0..7) {
                val target = board[r][c]
                if (target == null) {
                    moves.add(Pair(r, c))
                } else {
                    if (target.color != piece.color)
                        moves.add(Pair(r, c))
                    break
                }
                r += dr
                c += dc
            }
        }

        return moves
    }

    // ================= KNIGHT =================

    private fun knightMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece
    ): List<Pair<Int, Int>> {

        val moves = mutableListOf<Pair<Int, Int>>()
        val offsets = listOf(
            Pair(2, 1), Pair(2, -1),
            Pair(-2, 1), Pair(-2, -1),
            Pair(1, 2), Pair(1, -2),
            Pair(-1, 2), Pair(-1, -2)
        )

        for ((dr, dc) in offsets) {
            val r = row + dr
            val c = col + dc
            if (r in 0..7 && c in 0..7) {
                val target = board[r][c]
                if (target == null || target.color != piece.color)
                    moves.add(Pair(r, c))
            }
        }

        return moves
    }

    // ================= QUEEN =================

    private fun queenMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece
    ) = rookMoves(board, row, col, piece) +
            bishopMoves(board, row, col, piece)

    // ================= KING =================

    private fun kingMoves(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        piece: Piece,
        gameState: GameState
    ): List<Pair<Int, Int>> {

        val moves = basicKingMoves(row, col)
            .filter {
                val target = board[it.first][it.second]
                target == null || target.color != piece.color
            }
            .filter {
                !squareUnderAttack(board, it.first, it.second, piece.color)
            }
            .toMutableList()
        if (squareUnderAttack(board, row, col, piece.color)) {
            return moves
        }

        // CASTLING

        if (piece.color == PieceColor.WHITE && !gameState.whiteKingMoved) {

            if (!gameState.whiteRightRookMoved &&
                board[7][5] == null &&
                board[7][6] == null &&
                !squareUnderAttack(board, 7, 5, piece.color) &&
                !squareUnderAttack(board, 7, 6, piece.color)
            ) {
                moves.add(Pair(7, 6))
            }

            if (!gameState.whiteLeftRookMoved &&
                board[7][1] == null &&
                board[7][2] == null &&
                board[7][3] == null &&
                !squareUnderAttack(board, 7, 3, piece.color) &&
                !squareUnderAttack(board, 7, 2, piece.color)
            ) {
                moves.add(Pair(7, 2))
            }
        }

        if (piece.color == PieceColor.BLACK && !gameState.blackKingMoved) {

            if (!gameState.blackRightRookMoved &&
                board[0][5] == null &&
                board[0][6] == null &&
                !squareUnderAttack(board, 0, 5, piece.color) &&
                !squareUnderAttack(board, 0, 6, piece.color)
            ) {
                moves.add(Pair(0, 6))
            }

            if (!gameState.blackLeftRookMoved &&
                board[0][1] == null &&
                board[0][2] == null &&
                board[0][3] == null &&
                !squareUnderAttack(board, 0, 3, piece.color) &&
                !squareUnderAttack(board, 0, 2, piece.color)
            ) {
                moves.add(Pair(0, 2))
            }
        }

        return moves
    }

    private fun basicKingMoves(row: Int, col: Int): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (r in 0..7 && c in 0..7)
                    moves.add(Pair(r, c))
            }
        }
        return moves
    }

    private fun squareUnderAttack(
        board: List<List<Piece?>>,
        row: Int,
        col: Int,
        color: PieceColor
    ): Boolean {

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color != color) {
                    val attacks = getAttackMoves(board, r, c)
                    if (attacks.contains(Pair(row, col)))
                        return true
                }
            }
        }

        return false
    }
}
