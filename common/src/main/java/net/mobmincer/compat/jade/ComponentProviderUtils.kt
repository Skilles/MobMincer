package net.mobmincer.compat.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.contents.TranslatableContents
import net.mobmincer.core.entity.MobMincerEntity

object ComponentProviderUtils {

    fun MobMincerEntity.appendTooltipData(compound: CompoundTag) {
        compound.putInt("Durability", this.durability)
        compound.putInt("MaxDurability", this.sourceStack.maxDamage)
        compound.putFloat("Progress", this.currentMinceTick.toFloat() / this.maxMinceTick.toFloat())
        compound.putBoolean("Errored", this.isErrored)
    }

    fun getTooltipComponents(data: CompoundTag): List<TranslatableContents> {
        val components = mutableListOf<TranslatableContents>()
        components.add(
            TranslatableContents(
                "mobmincer.waila.tooltip.durability",
                null,
                arrayOf(data.getInt("Durability"), data.getInt("MaxDurability"))
            )
        )
        val errored = data.getBoolean("Errored")
        if (errored) {
            components.add(TranslatableContents("mobmincer.waila.tooltip.errored", null, arrayOf()))
        } else {
            components.add(
                TranslatableContents(
                    "mobmincer.waila.tooltip.progress",
                    null,
                    arrayOf(data.getFloat("Progress"))
                )
            )
        }
        return components
    }
}