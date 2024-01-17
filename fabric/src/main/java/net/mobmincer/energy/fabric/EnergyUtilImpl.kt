package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.energy.MMChargableItem
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil
import team.reborn.energy.api.base.SimpleEnergyItem

object EnergyUtilImpl {

    @JvmStatic
    fun createSidedStorage(blockEntity: SidedEnergyBlockEntity): MMSidedEnergyStorage {
        return MMSidedEnergyWrapper(blockEntity)
    }

    @JvmStatic
    fun getSidedStorage(blockEntity: SidedEnergyBlockEntity, direction: Direction? = null): MMEnergyStorage {
        return (blockEntity.energyStorage as MMSidedEnergyWrapper).getSideStorage(direction)
    }

    @JvmStatic
    fun ItemStack.getEnergyStorage(): MMEnergyStorage? {
        return getStorage(this)?.let { fromFabricStorage(it) }
    }

    @JvmStatic
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage? {
        return getStorage(this, direction)?.let { fromFabricStorage(it) }
    }

    @JvmStatic
    fun moveEnergy(from: EnergyStorage, to: EnergyStorage, maxAmount: Long): Long {
        return EnergyStorageUtil.move(from, to, maxAmount, null)
    }

    @JvmStatic
    fun ItemStack.setEnergyUnchecked(amount: Long) {
        SimpleEnergyItem.setStoredEnergyUnchecked(this, amount)
    }

    @JvmStatic
    fun ItemStack.getEnergyUnchecked(): Long {
        return SimpleEnergyItem.getStoredEnergyUnchecked(this)
    }

    fun registerStorage() {
        EnergyStorage.ITEM.registerFallback { stack, context ->
            val item = stack.item
            if (item is MMChargableItem) {
                fromFabricStorage(
                    SimpleEnergyItem.createStorage(
                        context,
                        item.getEnergyCapacity(stack),
                        item.getEnergyMaxInput(stack),
                        item.getEnergyMaxOutput(stack)
                    )
                ) as EnergyStorage
            } else {
                null
            }
        }

        EnergyStorage.SIDED.registerFallback { level, blockPos, blockState, blockEntity, direction ->
            val block = blockState.block
            if (block is MMEnergyBlock && blockEntity is SidedEnergyBlockEntity) {
                getSidedStorage(blockEntity, direction) as EnergyStorage
            } else {
                null
            }
        }
    }

    private fun getStorage(stack: ItemStack): EnergyStorage? {
        return ContainerItemContext.withConstant(stack).find(EnergyStorage.ITEM)
    }

    private fun getStorage(blockEntity: BlockEntity, direction: Direction?): EnergyStorage? {
        return EnergyStorage.SIDED.find(blockEntity.level, blockEntity.blockPos, direction)
    }

    private fun fromFabricStorage(fabricStorage: EnergyStorage): MMEnergyStorage {
        if (fabricStorage is MMEnergyStorage) return fabricStorage

        return object : MMEnergyStorage, EnergyStorage by fabricStorage {
            override val supportsInsertion: Boolean
                get() = supportsInsertion()
            override val supportsExtraction: Boolean
                get() = supportsExtraction()
            override var energy: Long
                get() = amount
                set(value) {
                    if (value > amount) {
                        insert(value - amount)
                    } else if (value < amount) {
                        extract(amount - value)
                    }
                }
            override val energyCapacity: Long
                get() = capacity
            override val isEmpty: Boolean
                get() = amount <= 0

            override fun insert(maxAmount: Long): Long {
                Transaction.openOuter().use { transaction ->
                    return insert(maxAmount, transaction).also { transaction.commit() }
                }
            }

            override fun extract(maxAmount: Long): Long {
                Transaction.openOuter().use { transaction ->
                    return extract(maxAmount, transaction).also { transaction.commit() }
                }
            }
        }
    }
}
