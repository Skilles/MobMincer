package net.mobmincer.energy

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

object EnergyUtil {

    /*@JvmStatic
    @ExpectPlatform
    fun getCurrentEnergy(stack: ItemStack): Long {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun setCurrentEnergy(stack: ItemStack, energy: Long): Long {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun extractEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun insertEnergy(stack: ItemStack, amount: Long, simulate: Boolean): Long {
        throw AssertionError()
    }*/

    @JvmStatic
    @ExpectPlatform
    fun createSidedStorage(blockEntity: SidedEnergyBlockEntity): MMSidedEnergyStorage {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun getEnergyStorage(stack: ItemStack): MMEnergyStorage {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun getEnergyStorage(blockEntity: BlockEntity, direction: Direction?): MMEnergyStorage {
        throw AssertionError()
    }

    fun extractEnergy(blockEntity: SidedEnergyBlockEntity, amount: Long, direction: Direction? = null): Long {
        return blockEntity.getOrCreateEnergyStorage(direction).extract(amount)
    }

    fun insertEnergy(blockEntity: SidedEnergyBlockEntity, amount: Long, direction: Direction? = null): Long {
        return blockEntity.getOrCreateEnergyStorage(direction).insert(amount)
    }

    fun transferEnergy(from: SidedEnergyBlockEntity, to: MMEnergyStorage, amount: Long, direction: Direction? = null): Long {
        val extracted = extractEnergy(from, amount, direction)
        return to.insert(extracted)
    }

    fun transferEnergy(from: SidedEnergyBlockEntity, to: List<MMEnergyStorage>, amount: Long, direction: Direction? = null): Long {
        val extracted = extractEnergy(from, amount, direction) / to.size
        return to.sumOf { it.insert(extracted) }
    }
}
