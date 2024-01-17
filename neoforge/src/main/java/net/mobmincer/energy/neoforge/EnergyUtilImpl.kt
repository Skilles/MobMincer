package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.neoforged.neoforge.common.capabilities.Capabilities
import net.neoforged.neoforge.energy.EnergyStorage
import net.neoforged.neoforge.energy.IEnergyStorage

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
        return getStorage(this)?.let { fromForgeStorage(it) }
    }

    @JvmStatic
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage? {
        return getStorage(this, direction)?.let { fromForgeStorage(it) }
    }

    @JvmStatic
    fun moveEnergy(from: EnergyStorage, to: EnergyStorage, maxAmount: Long): Long {
        return from.receiveEnergy(to.extractEnergy(maxAmount.toInt(), false), false).toLong()
    }

    private fun getStorage(stack: ItemStack): IEnergyStorage? {
        return stack.getCapability(Capabilities.ENERGY).orElse(null)
    }

    private fun getStorage(blockEntity: BlockEntity, direction: Direction?): IEnergyStorage? {
        return blockEntity.getCapability(Capabilities.ENERGY, direction).orElse(null)
    }

    private fun fromForgeStorage(storage: IEnergyStorage): MMEnergyStorage {
        if (storage is MMEnergyStorage) {
            return storage
        }

        return object : MMEnergyStorage, IEnergyStorage by storage {
            override fun insert(maxAmount: Long): Long {
                return storage.receiveEnergy(maxAmount.toInt(), false).toLong()
            }

            override fun extract(maxAmount: Long): Long {
                return storage.extractEnergy(maxAmount.toInt(), false).toLong()
            }

            override val energyCapacity: Long
                get() = storage.maxEnergyStored.toLong()

            override var energy: Long
                get() = storage.energyStored.toLong()
                set(value) {
                    if (value > storage.maxEnergyStored) {
                        storage.receiveEnergy((value - storage.maxEnergyStored).toInt(), false)
                    } else if (value < storage.energyStored) {
                        storage.extractEnergy((storage.energyStored - value).toInt(), false)
                    }
                }
        }
    }
}
