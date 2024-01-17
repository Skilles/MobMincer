package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage

abstract class SidedEnergyBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) :
    BaseMachineBlockEntity(
        type,
        pos,
        blockState
    ) {

    val energyStorage: MMSidedEnergyStorage by lazy { EnergyUtil.createSidedStorage(this) }

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

    fun discharge(slot: Int, amount: Long = Long.MAX_VALUE) {
        val level = level ?: return
        if (level.isClientSide) return

        val chargableItem = getOptionalInventory().map { it.getItem(slot) }.orElse(null) ?: return

        chargableItem.getEnergyStorage()?.let {
            EnergyUtil.moveEnergy(this.energyStorage, it, amount)
        }
    }
}
