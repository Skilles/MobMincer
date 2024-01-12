package net.mobmincer.energy

import net.minecraft.core.Direction
import net.minecraft.nbt.Tag

interface MMSidedEnergyStorage : MMEnergyStorage, MMEnergyBlock {
    fun getMMSideStorage(side: Direction?): MMEnergyStorage

    fun serialize(): Tag? = null

    fun deserialize(tag: Tag?) {}

    override val supportsInsertion: Boolean
        get() = supportsInsertion(null)
    override val supportsExtraction: Boolean
        get() = supportsExtraction(null)
}
