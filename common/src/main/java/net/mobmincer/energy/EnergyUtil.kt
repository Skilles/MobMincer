package net.mobmincer.energy

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.core.entity.MobMincerEntity

object EnergyUtil {

    @JvmStatic
    @ExpectPlatform
    fun createSidedStorage(blockEntity: SidedEnergyBlockEntity): MMSidedEnergyStorage {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.getEnergyStorage(): MMEnergyStorage {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage {
        throw AssertionError()
    }

    fun MobMincerEntity.getEnergyStorage(): MMEnergyStorage {
        return this.sourceStack.getEnergyStorage()
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
