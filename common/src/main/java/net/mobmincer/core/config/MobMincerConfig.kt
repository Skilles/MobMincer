package net.mobmincer.core.config

import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair


class MobMincerConfig private constructor(builder: ModConfigSpec.Builder) {

    val maxMinceTick: ModConfigSpec.IntValue = builder
        .comment("The maximum amount of time it takes to mince a mob")
        .translation("mobmincer.config.maxMinceTick")
        .defineInRange("maxMinceTick", 100, 1, 1000)

    val baseDurability: ModConfigSpec.IntValue = builder
        .comment("The base durability of the mob mincer")
        .translation("mobmincer.config.baseDurability")
        .worldRestart()
        .defineInRange("baseDurability", 100, 1, 1000)

    val unbreakingBound: ModConfigSpec.IntValue = builder
        .comment("The bound used for the RNG when calculating unbreaking. Chance to ignore durability: (1 / bound) * unbreakingLevel.")
        .translation("mobmincer.config.unbreakingBound")
        .defineInRange("unbreakingBound", 6, 1, 10)

    val mobDamagePercent: ModConfigSpec.DoubleValue = builder
        .comment("The percent of damage dealt to the mob when mincing")
        .translation("mobmincer.config.mobDamagePercent")
        .defineInRange("mobDamagePercent", 0.1, 0.0, 1.0)

    companion object {
        private val specPair: Pair<MobMincerConfig, ModConfigSpec> =
            ModConfigSpec.Builder().configure(::MobMincerConfig)

        val CONFIG: MobMincerConfig = specPair.left
        val SPEC: ModConfigSpec = specPair.right
    }
}