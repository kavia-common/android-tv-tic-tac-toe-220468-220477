package com.example.android_tv_frontend

import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider

/**
 * Main Activity for Android TV Tic Tac Toe
 * - Uses focusable TextViews as 3x3 board cells
 * - Handles DPAD navigation implicitly via focusable views in XML
 * - Select/OK triggers play for the currently focused cell
 * - Default focus set to center cell at start and after reset
 */
class MainActivity : FragmentActivity() {

    private lateinit var vm: GameViewModel

    private lateinit var tvTitle: TextView
    private lateinit var tvTurn: TextView
    private lateinit var btnReset: Button
    private lateinit var btnAbout: Button

    private lateinit var cells: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this)[GameViewModel::class.java]

        // Top
        tvTitle = findViewById(R.id.tvTitle)
        tvTurn = findViewById(R.id.tvTurn)
        tvTitle.text = getString(R.string.game_title)

        // Bottom
        btnReset = findViewById(R.id.btnReset)
        btnAbout = findViewById(R.id.btnAbout)

        // Board cells
        val ids = listOf(
            R.id.cell_0_0, R.id.cell_0_1, R.id.cell_0_2,
            R.id.cell_1_0, R.id.cell_1_1, R.id.cell_1_2,
            R.id.cell_2_0, R.id.cell_2_1, R.id.cell_2_2,
        )
        cells = ids.map { findViewById<TextView>(it) }

        // Set initial center focus (1,1) -> index 4
        setDefaultFocus()

        // Click/select handling for each cell
        cells.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                handleCellSelect(index)
            }
            // On TV, many devices map DPAD_CENTER to click; ensure focus visuals are clear
            tv.setOnFocusChangeListener { v, hasFocus ->
                // Optionally scale on focus for smoother feedback
                v.animate().scaleX(if (hasFocus) 1.04f else 1.0f)
                    .scaleY(if (hasFocus) 1.04f else 1.0f)
                    .setDuration(90)
                    .start()
            }
        }

        btnReset.setOnClickListener {
            vm.reset()
            renderBoard(vm.board.value ?: mutableListOf())
            updateTurnText()
            setDefaultFocus()
        }

        btnAbout.setOnClickListener {
            showAboutDialog()
        }

        // Observe game state
        vm.board.observe(this) { board ->
            renderBoard(board)
        }

        vm.currentPlayer.observe(this) {
            updateTurnText()
        }

        vm.result.observe(this) { res ->
            res?.let { showResultDialog(it) }
        }

        // Initial render
        renderBoard(vm.board.value ?: mutableListOf())
        updateTurnText()
    }

    private fun updateTurnText() {
        val res = vm.result.value
        if (res != null) {
            // When game is over, the dialog shows final text
            return
        }
        val cp = vm.currentPlayer.value ?: "X"
        tvTurn.text = if (cp == "X") getString(R.string.player_x_turn) else getString(R.string.player_o_turn)
    }

    private fun renderBoard(board: List<String>) {
        for (i in 0..8) {
            val mark = board.getOrNull(i) ?: ""
            cells[i].text = mark
            // Apply accent color for marks for visual clarity
            when (mark) {
                "X" -> cells[i].setTextColor(getColorCompat(R.color.ocean_primary))
                "O" -> cells[i].setTextColor(getColorCompat(R.color.ocean_secondary))
                else -> cells[i].setTextColor(getColorCompat(R.color.ocean_text))
            }
        }
    }

    private fun handleCellSelect(index: Int) {
        // Only act if the cell is currently focused, to match TV UX expectations
        val focused: View? = currentFocus
        if (focused != null && focused === cells[index]) {
            vm.playAt(index)
        } else {
            // If select pressed while a different cell view has focus, redirect focus to it
            cells[index].requestFocus()
        }
    }

    private fun showResultDialog(result: String) {
        val message = when (result) {
            "X" -> getString(R.string.player_x_wins)
            "O" -> getString(R.string.player_o_wins)
            "DRAW" -> getString(R.string.draw_message)
            else -> getString(R.string.ok)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.play_again)) { d, _ ->
                vm.reset()
                renderBoard(vm.board.value ?: mutableListOf())
                updateTurnText()
                setDefaultFocus()
                d.dismiss()
            }
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            // On TV, ensure the positive button can be focused
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
            }
        }

        dialog.show()
    }

    private fun showAboutDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_about, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(getString(R.string.ok), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
        dialog.show()
    }

    private fun setDefaultFocus() {
        // Default focus to center cell (1,1), index 4
        cells.getOrNull(4)?.requestFocus()
    }

    // Handle TV remote inputs; DPAD_CENTER should map to click. We also keep back behavior to default (finish).
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                currentFocus?.performClick()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // Helper to get color with compatibility across API levels
    private fun getColorCompat(resId: Int): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            resources.getColor(resId, theme)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(resId)
        }
    }
}
