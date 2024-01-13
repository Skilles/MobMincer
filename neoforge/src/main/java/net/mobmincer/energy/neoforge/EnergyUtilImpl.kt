package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.neoforged.neoforge.common.capabilities.Capabilities
import net.neoforged.neoforge.energy.IEnergyStorage

object EnergyUtilImpl {

    @JvmStatic
    fun createSidedStorage(blockEntity: SidedEnergyBlockEntity): MMSidedEnergyStorage {
        return MMSidedEnergyContainer(blockEntity)
    }

    @JvmStatic
    fun ItemStack.getEnergyStorage(): MMEnergyStorage {
        return getStorage(this) as MMEnergyStorage? ?: throw IllegalArgumentException("ItemStack does not have an MM energy storage")
    }

    @JvmStatic
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage {
        return getStorage(this, direction) as MMEnergyStorage? ?: throw IllegalArgumentException("BlockEntity does not have an MM energy storage")
    }

    private fun getStorage(stack: ItemStack): IEnergyStorage? {
        return stack.getCapability(Capabilities.ENERGY).orElse(null)
    }

    private fun getStorage(blockEntity: BlockEntity, direction: Direction?): IEnergyStorage? {
        return blockEntity.getCapability(Capabilities.ENERGY, direction).orElse(null)
    }
}
