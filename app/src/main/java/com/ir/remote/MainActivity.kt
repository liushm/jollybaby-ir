package com.ir.remote

import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val sender = Executors.newSingleThreadExecutor()
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusView = findViewById(R.id.status)
        val grid = findViewById<GridLayout>(R.id.keys)
        val ir = getSystemService(CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val hasIr = ir?.hasIrEmitter() == true
        statusView.text = getString(if (hasIr) R.string.ready else R.string.no_ir)

        for ((index, command) in COMMANDS.withIndex()) {
            val column = if (index == COMMANDS.lastIndex) 1 else index % 3
            grid.addView(keyButton(command, ir), keyLayoutParams(index / 3, column))
        }
    }

    override fun onDestroy() {
        sender.shutdown()
        super.onDestroy()
    }

    private fun keyButton(command: Int, ir: ConsumerIrManager?): AppCompatButton {
        val code = codeLabel(command)
        return AppCompatButton(this).apply {
            text = code
            contentDescription = getString(R.string.send_code, code)
            setTextColor(getColor(R.color.text))
            textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            setBackgroundResource(R.drawable.key_bg)
            supportBackgroundTintList = null
            minHeight = dp(76)
            minimumHeight = dp(76)
            stateListAnimator = null
            setOnClickListener { send(ir, command, code) }
        }
    }

    private fun keyLayoutParams(row: Int, column: Int): GridLayout.LayoutParams {
        return GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            rowSpec = GridLayout.spec(row)
            columnSpec = GridLayout.spec(column, 1f)
            val gap = dp(6)
            setMargins(gap, gap, gap, gap)
        }
    }

    private fun send(ir: ConsumerIrManager?, command: Int, code: String) {
        if (ir == null || !ir.hasIrEmitter()) {
            statusView.text = getString(R.string.no_ir)
            return
        }
        statusView.text = getString(R.string.sending, code)
        sender.execute {
            try {
                ir.transmit(Nec.carrierHz(ir.carrierFrequencies), Nec.frame(ADDRESS, command))
                runOnUiThread {
                    if (!isDestroyed) statusView.text = getString(R.string.sent, code)
                }
            } catch (error: Exception) {
                runOnUiThread {
                    if (!isDestroyed) {
                        statusView.text = getString(R.string.send_failed, error.message ?: error.javaClass.simpleName)
                    }
                }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val ADDRESS = 0x80
        val COMMANDS = listOf(
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x1B, 0x1F, 0x0C, 0x0D, 0x0E, 0x0F,
        )

        fun codeLabel(command: Int): String = "80%02X".format(command)
    }
}
