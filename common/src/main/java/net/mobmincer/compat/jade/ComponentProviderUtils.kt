package net.mobmincer.compat.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.getMincerType
import net.mobmincer.energy.EnergyUtil.getEnergyStorage

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
        val type = this.sourceStack.getMincerType()
        compound.putString("Type", type.name)
        if (type == MobMincerType.POWERED) {
            val storage = this.sourceStack.getEnergyStorage()
            compound.putInt("Power", (storage.energy / storage.getEnergyCapacity()).toInt())
        }
    }

    fun getTooltipComponents(data: CompoundTag): List<Component> {
        val components = mutableListOf<Component>()
        components.add(
            Component.translatable(
                "mobmincer.waila.tooltip.durability",
                data.getInt("Durability"),
                data.getInt("MaxDurability")
            )
        )
        val errored = data.getBoolean("Errored")
        if (errored) {
            components.add(Component.translatable("mobmincer.waila.tooltip.errored"))
        } else {
            components.add(
                Component.translatable("mobmincer.waila.tooltip.progress", data.getFloat("Progress"))
            )
        }
        val attachments = data.getList("Attachments", 8)
        if (!attachments.isEmpty()) {
            components.add(Component.translatable("mobmincer.waila.tooltip.attachments"))
            attachments.forEach {
                components.add(
                    Component.literal(" - ${it.asString}")
                )
            }
        }
        val type = MobMincerType.valueOf(data.getString("Type"))
        if (type == MobMincerType.POWERED) {
            components.add(
                Component.translatable(
                    "mobmincer.waila.tooltip.power",
                    data.getInt("Power")
                )
            )
        }
        return components
    }
}
