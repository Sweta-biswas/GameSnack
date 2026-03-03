package com.swetabiswas.gamesnack.features.games.tictactoe

/** Minimax-based AI for Tic Tac Toe. */
object TicTacToeAI {

    private const val EMPTY = ""
    private const val AI = "O"
    private const val HUMAN = "X"

    /** Returns the best cell index (0-8) for easy AI. */
    fun easyMove(board: List<String>): Int {
        // First try to block human, otherwise random
        val block = findWinningMove(board, HUMAN)
        if (block != -1) return block
        val empties = board.indices.filter { board[it] == EMPTY }
        return empties.random()
    }

    /** Returns the best cell index (0-8) for hard AI (minimax). */
    fun hardMove(board: List<String>): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1
        for (i in board.indices) {
            if (board[i] == EMPTY) {
                val newBoard = board.toMutableList().also { it[i] = AI }
                val score = minimax(newBoard, depth = 0, isMaximizing = false)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: MutableList<String>, depth: Int, isMaximizing: Boolean): Int {
        val result = checkWinner(board)
        if (result != null) {
            return when (result) {
                AI    ->  10 - depth
                HUMAN -> -10 + depth
                else  ->  0
            }
        }
        if (board.none { it == EMPTY }) return 0

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == EMPTY) {
                    board[i] = AI
                    best = maxOf(best, minimax(board, depth + 1, false))
                    board[i] = EMPTY
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == EMPTY) {
                    board[i] = HUMAN
                    best = minOf(best, minimax(board, depth + 1, true))
                    board[i] = EMPTY
                }
            }
            best
        }
    }

    private fun findWinningMove(board: List<String>, player: String): Int {
        val wins = winLines()
        for (line in wins) {
            val (a, b, c) = line
            val cells = listOf(board[a], board[b], board[c])
            if (cells.count { it == player } == 2 && cells.contains(EMPTY)) {
                return line[cells.indexOf(EMPTY)]
            }
        }
        return -1
    }

    /** Returns the winning player, "Draw", or null if game is ongoing. */
    fun checkWinner(board: List<String>): String? {
        for (line in winLines()) {
            val (a, b, c) = line
            if (board[a] != EMPTY && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }
        return if (board.none { it == EMPTY }) "Draw" else null
    }

    fun winningLine(board: List<String>): List<Int>? {
        for (line in winLines()) {
            val (a, b, c) = line
            if (board[a] != EMPTY && board[a] == board[b] && board[b] == board[c]) {
                return line
            }
        }
        return null
    }

    private fun winLines(): List<List<Int>> = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
    )
}
