package net.mobmincer.compat.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.mobmincer.core.entity.MobMincerEntity

object ComponentProviderUtils {

    fun MobMincerEntity.appendTooltipData(compound: CompoundTag) {
        compound.putInt("Durability", this.sourceStack.maxDamage - this.sourceStack.damageValue)
        compound.putInt("MaxDurability", this.sourceStack.maxDamage)
        compound.putFloat("Progress", this.currentMinceTick.toFloat() / this.maxMinceTick.toFloat())
        compound.putBoolean("Errored", this.isErrored)
        val attachmentsTag = ListTag()
        this.attachments.values.forEach {
            attachmentsTag.add(StringTag.valueOf(it.type.name.string))
        }
        compound.put("Attachments", attachmentsTag)
    }

    fun getTooltipComponents(data: CompoundTag): List<ComponentContents> {
        val components = mutableListOf<ComponentContents>()
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
        val attachments = data.getList("Attachments", 8)
        if (!attachments.isEmpty()) {
            components.add(TranslatableContents("mobmincer.waila.tooltip.attachments", null, arrayOf()))
            attachments.forEach {
                components.add(
                    LiteralContents(" - ${it.asString}")
                )
            }
        }
        return components
    }
}