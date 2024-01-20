package net.mobmincer.energy

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.core.entity.MobMincerEntity

object EnergyUtil {

    @JvmStatic
    @ExpectPlatform
    fun createSidedStorage(blockEntity: EnergyMachineBlockEntity): MMSidedEnergyStorage = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun getSidedStorage(blockEntity: EnergyMachineBlockEntity, direction: Direction? = null): MMEnergyStorage = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.getEnergyStorage(): MMEnergyStorage? = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun BlockEntity.getEnergyStorage(direction: Direction? = null): MMEnergyStorage? = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun moveEnergy(from: MMEnergyStorage, to: MMEnergyStorage, maxAmount: Long): Long = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.setEnergyUnchecked(amount: Long): Unit = throw AssertionError()

    @JvmStatic
    @ExpectPlatform
    fun ItemStack.getEnergyUnchecked(): Long = throw AssertionError()

    fun MobMincerEntity.getEnergyStorage(): MMEnergyStorage = this.sourceStack.getEnergyStorage() ?: error("No energy storage found on Mincer")

    fun ItemStack.usesEnergy(): Boolean = this.getEnergyStorage() != null

    /*fun transferEnergy(from: EnergyMachineBlockEntity, to: List<MMEnergyStorage>, amount: Long, direction: Direction? = null): Long {
        val extracted = from.energyStorage.extract(amount, direction) / to.size
        return to.sumOf { it.insert(extracted) }
    }*/
}
