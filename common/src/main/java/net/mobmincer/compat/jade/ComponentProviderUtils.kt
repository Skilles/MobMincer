package net.mobmincer.compat.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.mobmincer.MobMincer
import net.mobmincer.core.attachment.TankAttachment
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
        if (!this.attachments.isEmpty()) {
            val attachmentsTag = ListTag()
            this.attachments.values.forEach {
                attachmentsTag.add(StringTag.valueOf(it.type.name.string))
                if (it is TankAttachment) {
                    compound.putFloat("FluidAmount", it.fluidAmount)
                }
            }
            compound.put("Attachments", attachmentsTag)
        }
        val type = this.sourceStack.getMincerType()
        compound.putString("Type", type.name)
        if (type == MobMincerType.POWERED) {
            val storage = this.sourceStack.getEnergyStorage() ?: return MobMincer.logger.warn("No energy storage found for powered mincer!")
            compound.putFloat("Power", storage.energy.toFloat() / storage.energyCapacity)
        }
    }

    fun getTooltipComponents(data: CompoundTag): List<MutableComponent> {
        val components = mutableListOf<MutableComponent>()
        val errored = data.getBoolean("Errored")
        if (errored) {
            components.add(Component.translatable("mobmincer.waila.tooltip.errored"))
        } else {
            components.add(
                Component.translatable("mobmincer.waila.tooltip.progress", data.getFloat("Progress"))
            )
        }
        val type = MobMincerType.valueOf(data.getString("Type"))
        if (type == MobMincerType.BASIC) {
            components.add(
                Component.translatable(
                    "mobmincer.waila.tooltip.durability",
                    data.getInt("Durability"),
                    data.getInt("MaxDurability")
                )
            )
        } else if (type == MobMincerType.POWERED) {
            components.add(
                Component.translatable(
                    "mobmincer.waila.tooltip.power",
                    data.getFloat("Power")
                )
            )
        }
        if (data.contains("Attachments")) {
            if (data.contains("FluidAmount")) {
                components.add(
                    Component.translatable(
                        "mobmincer.waila.tooltip.fluid",
                        data.getFloat("FluidAmount")
                    )
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
        }
        return components
    }
}
