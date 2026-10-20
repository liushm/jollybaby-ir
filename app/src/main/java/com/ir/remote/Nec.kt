package com.ir.remote

/**
 * Standard NEC: 38 kHz, LSB first.
 * Frame is address, inverted address, command, inverted command.
 */
object Nec {
    const val CARRIER_HZ = 38_000

    private const val HEADER_MARK = 9_000
    private const val HEADER_SPACE = 4_500
    private const val BIT_MARK = 560
    private const val ONE_SPACE = 1_690
    private const val ZERO_SPACE = 560
    private const val END_MARK = 560
    private const val END_SPACE = 560

    fun frame(address: Int, command: Int): IntArray {
        val addr = address and 0xFF
        val cmd = command and 0xFF
        val data = addr or
            ((addr.inv() and 0xFF) shl 8) or
            (cmd shl 16) or
            ((cmd.inv() and 0xFF) shl 24)

        val pattern = IntArray(2 + 32 * 2 + 2)
        var index = 0
        pattern[index++] = HEADER_MARK
        pattern[index++] = HEADER_SPACE
        for (bit in 0 until 32) {
            pattern[index++] = BIT_MARK
            pattern[index++] = if (((data ushr bit) and 1) == 1) ONE_SPACE else ZERO_SPACE
        }
        pattern[index++] = END_MARK
        pattern[index] = END_SPACE
        return pattern
    }

    fun carrierHz(ranges: Array<android.hardware.ConsumerIrManager.CarrierFrequencyRange>?): Int {
        if (ranges.isNullOrEmpty()) return CARRIER_HZ
        if (ranges.any { CARRIER_HZ in it.minFrequency..it.maxFrequency }) return CARRIER_HZ
        val nearest = ranges.minBy { range ->
            val mid = (range.minFrequency + range.maxFrequency) / 2
            kotlin.math.abs(mid - CARRIER_HZ)
        }
        return CARRIER_HZ.coerceIn(nearest.minFrequency, nearest.maxFrequency)
    }
}
