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
    fun getSidedStorage(blockEntity: SidedEnergyBlockEntity, direction: Direction? = null): MMEnergyStorage {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.getEnergyStorage(): MMEnergyStorage? {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage? {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun moveEnergy(from: MMEnergyStorage, to: MMEnergyStorage, maxAmount: Long): Long {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.setEnergyUnchecked(amount: Long) {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.getEnergyUnchecked(): Long {
        throw AssertionError()
    }

    fun MobMincerEntity.getEnergyStorage(): MMEnergyStorage {
        return this.sourceStack.getEnergyStorage() ?: throw IllegalStateException("No energy storage found on Mincer")
    }

    fun transferEnergy(from: SidedEnergyBlockEntity, to: List<MMEnergyStorage>, amount: Long, direction: Direction? = null): Long {
        val extracted = from.energyStorage.extract(amount, direction) / to.size
        return to.sumOf { it.insert(extracted) }
    }
}
