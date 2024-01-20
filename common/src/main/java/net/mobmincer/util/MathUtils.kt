package net.mobmincer.util

import net.mobmincer.common.config.MobMincerConfig
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

object MathUtils {

    fun getCalculatedMaxMinceTick(soulSpeedLevel: Int): Int {
        val configMax = MobMincerConfig.CONFIG.maxMinceTick.get()
        val multiplier = MobMincerConfig.CONFIG.soulSpeedMultiplier.get()
        return max(1, (configMax * (1 - multiplier).pow(soulSpeedLevel)).roundToInt())
    }

    fun Long.toIntSafe(): Int {
        return if (this > Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            this.toInt()
        }
    }
}
