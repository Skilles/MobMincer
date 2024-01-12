package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.mobmincer.energy.SidedEnergyBlockEntity
import team.reborn.energy.api.EnergyStorage

object EnergyUtilImpl {

    @JvmStatic
    fun getCurrentEnergy(stack: ItemStack): Long {
        return getStorage(stack)?.amount ?: 0
    }

    @JvmStatic
    fun setCurrentEnergy(stack: ItemStack, energy: Long): Long {
        Transaction.openOuter().use {
            val storage = getStorage(stack)
            val amount = storage?.amount ?: return 0
            val difference = energy - amount
            if (difference > 0) {
                val amountInserted = storage.insert(difference, it)
                it.commit()
                return amountInserted + amount
            } else if (difference < 0) {
                val amountExtracted = storage.extract(-difference, it)
                it.commit()
                return amount - amountExtracted
            }
            return amount
        }
    }

    @JvmStatic
    fun extractEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        return extractEnergy(stack, amount, simulate, null)
    }

    @JvmStatic
    fun insertEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        return insertEnergy(stack, amount, simulate, null)
    }

    @JvmStatic
    fun extractEnergy(
        stack: ItemStack,
        amount: Long,
        simulate: Boolean = false,
        transaction: Transaction? = null
    ): Long {
        Transaction.openNested(transaction).use {
            val storage = getStorage(stack)
            val amountExtracted = storage?.extract(amount, it) ?: return 0
            if (!simulate) {
                it.commit()
            }
            return amountExtracted
        }
    }

    @JvmStatic
    fun insertEnergy(
        stack: ItemStack,
        amount: Long,
        simulate: Boolean = false,
        transaction: Transaction? = null
    ): Long {
        Transaction.openNested(transaction).use {
            val storage = getStorage(stack)
            val amountInserted = storage?.insert(amount, it) ?: return 0
            if (!simulate) {
                it.commit()
            }
            return amountInserted
        }
    }

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

    private fun getStorage(stack: ItemStack): EnergyStorage? {
        return EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack))
    }

    private fun getStorage(blockEntity: BlockEntity, direction: Direction?): EnergyStorage? {
        return EnergyStorage.SIDED.find(blockEntity.level, blockEntity.blockPos, direction)
    }
}
