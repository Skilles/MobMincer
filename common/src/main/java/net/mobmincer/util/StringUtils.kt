package net.mobmincer.util

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.*

object StringUtils {
    fun toFirstCapital(input: String): String {
        if (input.isEmpty()) {
            return input
        }

        return input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1)
    }

    fun toFirstCapitalAllLowercase(input: String): String {
        if (input.isEmpty()) {
            return input
        }
        val output = input.lowercase()
        return output.substring(0, 1).uppercase(Locale.getDefault()) + output.substring(1)
    }

    fun getPercentageText(minValue: Int, maxValue: Int, advanced: Boolean = Screen.hasShiftDown()): MutableComponent {
        val percentage = (minValue.toFloat() / maxValue * 100).toInt()
        if (advanced) {
            return Component.literal("$minValue/$maxValue").withStyle(getPercentageColour(percentage))
        }
        return Component.literal(percentage.toString())
            .withStyle(getPercentageColour(percentage))
            .append("%")
    }

    /**
     * Returns red-yellow-green text formatting depending on percentage
     *
     * @param percentage `int` percentage amount
     * @return [Formatting] Red or Yellow or Green
     */
    fun getPercentageColour(percentage: Int): ChatFormatting {
        return if (percentage <= 10) {
            ChatFormatting.RED
        } else if (percentage >= 75) {
            ChatFormatting.GREEN
        } else {
            ChatFormatting.YELLOW
        }
    }
}