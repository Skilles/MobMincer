package net.mobmincer.core.config

import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair

class MobMincerConfig private constructor(builder: ModConfigSpec.Builder) {

    val maxMinceTick: ModConfigSpec.IntValue = builder
        .comment("The maximum amount of time it takes to mince a mob")
        .translation("mobmincer.config.maxMinceTick")
        .defineInRange("maxMinceTick", 500, 1, 10000)

    val baseDurability: ModConfigSpec.IntValue = builder
        .comment("The base durability of the mob mincer")
        .translation("mobmincer.config.baseDurability")
        .worldRestart()
        .defineInRange("baseDurability", 100, 1, 1000)

    val unbreakingBound: ModConfigSpec.IntValue = builder
        .comment(
            "The bound used for the RNG when calculating unbreaking. Chance to ignore durability: (1 / bound) * unbreakingLevel."
        )
        .translation("mobmincer.config.unbreakingBound")
        .defineInRange("unbreakingBound", 6, 1, 10)

    val mobDamagePercent: ModConfigSpec.DoubleValue = builder
        .comment("The percent of damage dealt to the mob when mincing")
        .translation("mobmincer.config.mobDamagePercent")
        .defineInRange("mobDamagePercent", 0.1, 0.0, 1.0)

    val feederHealPercent: ModConfigSpec.DoubleValue = builder
        .comment("The percent of damage healed using the feeder attachment")
        .translation("mobmincer.config.feederHealPercent")
        .defineInRange("mobDamagePercent", 1.0, 0.0, 1.0)

    val coloredMobs: ModConfigSpec.BooleanValue = builder
        .comment("Whether or not to color the mobs based on their health when equipped with a mob mincer")
        .translation("mobmincer.config.coloredMobs")
        .worldRestart()
        .define("coloredMobs", true)

    val allowDispensing: ModConfigSpec.BooleanValue = builder
        .comment("Whether or not to allow dispensing mob mincers")
        .translation("mobmincer.config.allowDispensing")
        .define("allowDispensing", true)

    val allowKillLoot: ModConfigSpec.BooleanValue = builder
        .comment("Whether the mob should still drop loot when it dies after it has been minced at least once")
        .translation("mobmincer.config.allowKillLoot")
        .define("allowKillLoot", false)

    companion object {
        private val specPair: Pair<MobMincerConfig, ModConfigSpec> =
            ModConfigSpec.Builder().configure(::MobMincerConfig)

        // To allow consuming the config without neoforge dependency
        fun <T> getValue(path: String): T {
            return specPair.right.get(path)
        }

        val CONFIG: MobMincerConfig = specPair.left
        val SPEC: ModConfigSpec = specPair.right
    }
}
