package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.mobmincer.energy.SidedEnergyBlockEntity
import net.neoforged.neoforge.common.capabilities.Capabilities
import net.neoforged.neoforge.energy.IEnergyStorage

object EnergyUtilImpl {

    /*@JvmStatic
    fun getCurrentEnergy(stack: ItemStack): Long {
        return getStorage(stack)?.energyStored?.toLong() ?: 0
    }

    @JvmStatic
    fun setCurrentEnergy(stack: ItemStack, energy: Long): Long {
        val storage = getStorage(stack)
        val amount = storage?.energyStored ?: return 0
        val difference = energy - amount
        if (difference > 0) {
            val amountInserted = storage.receiveEnergy(difference.toInt(), false)
            return (amountInserted + amount).toLong()
        } else if (difference < 0) {
            val amountExtracted = storage.extractEnergy(-difference.toInt(), false)
            return (amount - amountExtracted).toLong()
        }
        return amount.toLong()
    }

    @JvmStatic
    fun extractEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        val storage = getStorage(stack)
        val amountExtracted = storage?.extractEnergy(amount.toInt(), simulate) ?: return 0
        return amountExtracted.toLong()
    }

    @JvmStatic
    fun insertEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        val storage = getStorage(stack)
        val amountInserted = storage?.receiveEnergy(amount.toInt(), simulate) ?: return 0
        return amountInserted.toLong()
    }*/

    @JvmStatic
    fun createSidedStorage(blockEntity: SidedEnergyBlockEntity): MMSidedEnergyStorage {
        return MMSidedEnergyContainer(blockEntity)
    }

    @JvmStatic
    fun getEnergyStorage(stack: ItemStack): MMEnergyStorage {
        return getStorage(stack) as MMEnergyStorage? ?: throw IllegalArgumentException("ItemStack does not have an MM energy storage")
    }

    @JvmStatic
    fun getEnergyStorage(blockEntity: BlockEntity, direction: Direction?): MMEnergyStorage {
        return getStorage(blockEntity, direction) as MMEnergyStorage? ?: throw IllegalArgumentException("BlockEntity does not have an MM energy storage")
    }

    private fun getStorage(stack: ItemStack): IEnergyStorage? {
        return stack.getCapability(Capabilities.ENERGY).orElse(null)
    }

    private fun getStorage(blockEntity: BlockEntity, direction: Direction?): IEnergyStorage? {
        return blockEntity.getCapability(Capabilities.ENERGY, direction).orElse(null)
    }
}
