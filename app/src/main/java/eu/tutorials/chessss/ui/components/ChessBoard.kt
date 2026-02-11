package eu.tutorials.chessss.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.tutorials.chessss.R
import eu.tutorials.chessss.game.GameState
import eu.tutorials.chessss.game.MoveManager
import eu.tutorials.chessss.model.Piece
import eu.tutorials.chessss.model.PieceColor
import eu.tutorials.chessss.model.PieceType

@Composable
fun ChessBoard() {

    val gameState = remember { GameState() }
    val isWhiteMate = gameState.isCheckMate(PieceColor.WHITE)
    val isBlackMate = gameState.isCheckMate(PieceColor.BLACK)
    val promotionPos = gameState.promotionPosition


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {


        if (isWhiteMate) {
            Text("White is Checkmated!")
        }

        if (isBlackMate) {
            Text("Black is Checkmated!")
        }
        if (promotionPos != null) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                listOf(
                    PieceType.QUEEN,
                    PieceType.ROOK,
                    PieceType.BISHOP,
                    PieceType.KNIGHT
                ).forEach { type ->

                    Text(
                        text = type.name,
                        modifier = Modifier
                            .clickable {
                                gameState.promotePawn(type)
                            }
                    )
                }
            }
        }
        for (row in 0 until 8) {

            Row(modifier = Modifier.weight(1f)) {

                for (col in 0 until 8) {

                    val piece = gameState.board[row][col]
                    val isKingInCheck =
                        piece?.type == PieceType.KING &&
                                gameState.isKingInCheck(piece.color)

                    val isSelected =
                        gameState.selectedRow == row &&
                                gameState.selectedCol == col

                    val isValidMove =
                        gameState.validMoves.contains(Pair(row, col))

                    val isCaptureMove =
                        isValidMove && piece != null &&
                                piece.color != gameState.currentTurn

                    val cellColor =
                        when {
                            isSelected -> Color(0xFFD7B899)
                            isKingInCheck -> Color(0xFF8B0000)
                            (row + col) % 2 == 0 -> Color(0xFFEEEED2)
                            else -> Color(0xFF769656)
                        }




                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(cellColor)
                            .clickable {
                                if (gameState.promotionPosition != null) return@clickable
                                val clickedPiece = gameState.board[row][col]

                                if (gameState.selectedRow == null) {

                                    if (clickedPiece != null &&
                                        clickedPiece.color == gameState.currentTurn
                                    ) {
                                        gameState.selectedRow = row
                                        gameState.selectedCol = col
                                        gameState.validMoves =
                                            MoveManager.getValidMoves(gameState.board, row, col, gameState)

                                    }

                                } else {

                                    // If clicking same color piece → reselect
                                    if (clickedPiece != null &&
                                        clickedPiece.color == gameState.currentTurn
                                    ) {
                                        gameState.selectedRow = row
                                        gameState.selectedCol = col
                                        gameState.validMoves =
                                            MoveManager.getValidMoves(gameState.board, row, col,gameState)

                                    } else {
                                        gameState.movePiece(row, col)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
// EMPTY SQUARE → BLACK DOT
                        if (isValidMove && piece == null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.Black, shape = CircleShape)
                            )
                        }

// CAPTURE MOVE → RING
                        if (isCaptureMove) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .background(
                                        Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    )
                            )
                        }

                        piece?.let {
                            Image(
                                painter = painterResource(
                                    id = getPieceImage(it)
                                ),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getPieceImage(piece: Piece): Int {
    return when (piece.type) {

        PieceType.PAWN ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_pawn
            else
                R.drawable.b_pawn

        PieceType.ROOK ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_rook
            else
                R.drawable.b_rook

        PieceType.KNIGHT ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_knight
            else
                R.drawable.b_knight

        PieceType.BISHOP ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_bishop
            else
                R.drawable.b_bishop

        PieceType.QUEEN ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_queen
            else
                R.drawable.b_queen

        PieceType.KING ->
            if (piece.color == PieceColor.WHITE)
                R.drawable.w_king
            else
                R.drawable.b_king
    }
}




