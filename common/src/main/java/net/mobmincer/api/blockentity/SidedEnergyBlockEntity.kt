package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage

abstract class SidedEnergyBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) :
    BaseMachineBlockEntity(
        type,
        pos,
        blockState
    ) {

    val energyStorage: MMSidedEnergyStorage by lazy { EnergyUtil.createSidedStorage(this) }

    fun getOrCreateEnergyStorage(direction: Direction? = null): MMEnergyStorage {
        return energyStorage.getMMSideStorage(direction)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        if (tag.contains("energy")) {
            energyStorage.deserialize(tag.getCompound("energy"))
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        energyStorage.serialize()?.let { tag.put("energy", it) }
    }
}
