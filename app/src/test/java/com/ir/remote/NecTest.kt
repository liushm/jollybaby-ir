package com.ir.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class NecTest {
    @Test
    fun matchesCapturedScancodes() {
        val labels = MainActivity.COMMANDS.map(MainActivity::codeLabel)
        assertEquals(
            listOf(
                "8001", "8002", "8003", "8004", "8005", "8006", "8007", "8008",
                "8009", "800A", "801B", "801F", "800C", "800D", "800E", "800F",
            ),
            labels,
        )
    }

    @Test
    fun frameIsStandardNecWithInversedBytes() {
        for (command in MainActivity.COMMANDS) {
            val pattern = Nec.frame(MainActivity.ADDRESS, command)
            assertEquals(0, pattern.size % 2)
            assertEquals(9_000, pattern[0])
            assertEquals(4_500, pattern[1])
            assertEquals(560, pattern[pattern.lastIndex - 1])

            val bits = dataBits(pattern)
            assertEquals(0x80, byteAt(bits, 0))
            assertEquals(0x7F, byteAt(bits, 1))
            assertEquals(command, byteAt(bits, 2))
            assertEquals(command.inv() and 0xFF, byteAt(bits, 3))
        }
    }

    private fun dataBits(pattern: IntArray): List<Int> {
        val bits = ArrayList<Int>(32)
        var index = 2
        repeat(32) {
            assertEquals(560, pattern[index])
            bits += when (val space = pattern[index + 1]) {
                1_690 -> 1
                560 -> 0
                else -> error("unexpected space $space")
            }
            index += 2
        }
        return bits
    }

    private fun byteAt(bits: List<Int>, byteIndex: Int): Int {
        var value = 0
        for (bit in 0 until 8) {
            if (bits[byteIndex * 8 + bit] == 1) value = value or (1 shl bit)
        }
        return value
    }
}
