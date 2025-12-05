package com.example.android_tv_frontend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * PUBLIC_INTERFACE
 * GameViewModel manages the Tic Tac Toe board state, current player, and game result.
 * - Board is a 3x3 array stored as a List of 9 strings ("", "X", "O")
 * - currentPlayer: "X" or "O"
 * - result: nullable string to indicate "X", "O", or "DRAW" when the game ends
 */
class GameViewModel : ViewModel() {

    private val _board: MutableLiveData<MutableList<String>> =
        MutableLiveData(MutableList(9) { "" })

    private val _currentPlayer: MutableLiveData<String> = MutableLiveData("X")
    private val _result: MutableLiveData<String?> = MutableLiveData(null)

    val board: LiveData<MutableList<String>> = _board
    val currentPlayer: LiveData<String> = _currentPlayer
    val result: LiveData<String?> = _result

    /**
     * PUBLIC_INTERFACE
     * Attempt to place the current player's mark at the given position (0..8).
     * Returns true if the move was successful; false if cell already occupied or game ended.
     */
    fun playAt(position: Int): Boolean {
        val res = _result.value
        if (res != null) return false // Game over, ignore moves

        val b = _board.value ?: return false
        if (position !in 0..8) return false
        if (b[position].isNotEmpty()) return false

        b[position] = _currentPlayer.value ?: "X"
        _board.value = b

        // Check for win or draw after this move
        val winner = checkWinner(b)
        if (winner != null) {
            _result.value = winner
        } else if (b.all { it.isNotEmpty() }) {
            _result.value = "DRAW"
        } else {
            switchPlayer()
        }
        return true
    }

    private fun switchPlayer() {
        _currentPlayer.value = if (_currentPlayer.value == "X") "O" else "X"
    }

    /**
     * PUBLIC_INTERFACE
     * Reset the game state to initial values.
     */
    fun reset() {
        _board.value = MutableList(9) { "" }
        _currentPlayer.value = "X"
        _result.value = null
    }

    // Determine if there's a winner; return "X", "O", or null if none
    private fun checkWinner(b: List<String>): String? {
        val lines = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // rows
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // cols
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6) // diagonals
        )

        for (line in lines) {
            val (a, c, d) = line
            if (b[a].isNotEmpty() && b[a] == b[c] && b[a] == b[d]) {
                return b[a]
            }
        }
        return null
    }
}
