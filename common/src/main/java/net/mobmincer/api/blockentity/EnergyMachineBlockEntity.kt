package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage

abstract class EnergyMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) :
    BaseMachineBlockEntity(
        type,
        pos,
        blockState
    ) {

    val energyStorage: MMSidedEnergyStorage by lazy { EnergyUtil.createSidedStorage(this) }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        if (tag.contains("energy")) {
            energyStorage.deserialize(tag.get("energy"))
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        energyStorage.serialize().let { tag.put("energy", it) }
    }

    fun discharge(slot: Int, amount: Long = energyStorage.getEnergyMaxOutput(null)) {
        val level = level ?: return
        if (level.isClientSide) return

        if (!hasInventory || energyStorage.energy <= 0 || !energyStorage.supportsExtraction) {
            return
        }

        val chargableItem = optionalInventory.map { it.getItem(slot) }.orElse(null) ?: return

        if (chargableItem.isEmpty) {
            return
        }

        chargableItem.getEnergyStorage()?.let {
            EnergyUtil.moveEnergy(this.energyStorage, it, amount)
        }
    }
}
