package net.mobmincer.common.config

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
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
        .defineInRange("mobDamagePercent", 0.2, 0.0, 1.0)

    val feederHealPercent: ModConfigSpec.DoubleValue = builder
        .comment("The percent of damage healed using the feeder attachment")
        .translation("mobmincer.config.feederHealPercent")
        .defineInRange("feederHealPercent", 1.0, 0.0, 1.0)

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

    val dropChance: ModConfigSpec.DoubleValue = builder
        .comment("The chance for the mob to drop loot when mincing")
        .translation("mobmincer.config.dropChance")
        .defineInRange("dropChance", 1.0, 0.0, 1.0)

    val soulSpeedMultiplier: ModConfigSpec.DoubleValue = builder
        .comment(
            "The multiplier for the max mince tick when soul speed is applied. maxMinceTick = configMax * (1 - multiplier) ^ soulSpeedLevel"
        )
        .translation("mobmincer.config.soulSpeedMultiplier")
        .defineInRange("soulSpeedMultiplier", 0.2, 0.0, 1.0)

    val mendingRepairMultiplier: ModConfigSpec.DoubleValue = builder
        .comment(
            "The multiplier for the amount of durability repaired when mending is applied. amountRepaired = damageDealt * multiplier"
        )
        .translation("mobmincer.config.mendingRepairMultiplier")
        .defineInRange("mendingRepairMultiplier", 1.0, 0.0, 1.0)

    val entityFilterMode: ModConfigSpec.EnumValue<EntityFilterMode> = builder
        .comment("The mode for the entity filter")
        .translation("mobmincer.config.entityFilterMode")
        .defineEnum("entityFilterMode", EntityFilterMode.BLACKLIST)

    val entityFilter: ModConfigSpec.ConfigValue<List<String>> = builder
        .comment("A list of mobs for the blacklist/whitelist. Example: minecraft:zombie")
        .translation("mobmincer.config.entityFilter")
        .defineListAllowEmpty("entityFilter", listOf<String>()) {
            BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.of(it.toString(), ':'))
        }

    val poweredMinceCost: ModConfigSpec.IntValue = builder
        .comment("The amount of RF required per mince using the powered mincer")
        .translation("mobmincer.config.poweredMinceCost")
        .defineInRange("poweredMinceCost", 500, 1, 1000000)

    val poweredMincerCapacity: ModConfigSpec.IntValue = builder
        .comment("The amount of RF that the powered mincer can store")
        .translation("mobmincer.config.poweredMincerCapacity")
        .defineInRange("poweredMincerCapacity", 10000, 1, Int.MAX_VALUE)

    val powerProviderCapacity: ModConfigSpec.IntValue = builder
        .comment("The amount of RF that the power provider can store")
        .translation("mobmincer.config.powerProviderCapacity")
        .defineInRange("powerProviderCapacity", 1000000, 1, Int.MAX_VALUE)

    val powerProviderTransferRate: ModConfigSpec.IntValue = builder
        .comment("The amount of RF that the power provider can transfer per tick")
        .translation("mobmincer.config.powerProviderTransferRate")
        .defineInRange("powerProviderTransferRate", 50000, 1, Int.MAX_VALUE)

    val powerProviderBurnRate: ModConfigSpec.IntValue = builder
        .comment("The amount of RF that the power provider gains per fuel burn tick")
        .translation("mobmincer.config.powerProviderBurnRate")
        .defineInRange("powerProviderBurnRate", 40, 1, Int.MAX_VALUE)

    val powerProviderPulseRate: ModConfigSpec.IntValue = builder
        .comment("The amount of ticks between each power provider pulse")
        .translation("mobmincer.config.powerProviderPulseRate")
        .defineInRange("powerProviderPulseRate", 15, 1, Int.MAX_VALUE)

    val experienceMultiplier: ModConfigSpec.DoubleValue = builder
        .comment("The multiplier for the amount of xp fluid added to the tank when mincing")
        .translation("mobmincer.config.experienceMultiplier")
        .defineInRange("experienceMultiplier", 2.0, 0.0, 100.0)

    val tankCapacity: ModConfigSpec.IntValue = builder
        .comment("The initial capacity of the tank attachment. 1 xp bucket = 1000 mB of fluid")
        .translation("mobmincer.config.tankCapacity")
        .defineInRange("tankCapacity", 10000, 1, Int.MAX_VALUE)

    val enablePowerParticles: ModConfigSpec.BooleanValue = builder
        .comment("Whether or not to enable particles when the powered mincer is running. Affects all players.")
        .translation("mobmincer.config.enablePowerParticles")
        .define("enablePowerParticles", true)

    enum class EntityFilterMode {
        BLACKLIST,
        WHITELIST
    }

    companion object {
        private val specPair: Pair<MobMincerConfig, ModConfigSpec> =
            ModConfigSpec.Builder().configure(::MobMincerConfig)

        // To allow consuming the config without neoforge dependency
        fun <T> getValue(path: String): T {
            return specPair.right.get(path)
        }

        fun testEntityFilter(entityType: ResourceLocation?): Boolean {
            if (entityType == null) {
                return false
            }
            val mode = CONFIG.entityFilterMode.get()
            val filter = CONFIG.entityFilter.get()
            return when (mode) {
                EntityFilterMode.BLACKLIST -> !filter.contains(entityType.toString())
                EntityFilterMode.WHITELIST -> filter.contains(entityType.toString())
                null -> true
            }
        }

        val CONFIG: MobMincerConfig = specPair.left
        val SPEC: ModConfigSpec = specPair.right
    }
}
