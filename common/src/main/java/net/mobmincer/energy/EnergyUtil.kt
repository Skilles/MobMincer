package net.mobmincer.energy

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.MobMincer
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.core.entity.MobMincerEntity
import kotlin.math.max

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

    fun ItemStack.getEnergyDamageValues(): Pair<Int, Int> {
        val item = this.item
        if (item is MMChargableItem) {
            val maxValue = item.getEnergyCapacity(this)
            val value = this.getEnergyStorage()?.energy?.let { maxValue - it } ?: return 1 to 1.also {
                MobMincer.logger.warn("No energy storage found for ${item.getName(this)}!")
            }
            return value.toInt() to maxValue.toInt()
        }
        return 0 to 0
    }

    fun ItemStack.getEnergyBarWidth(): Int {
        val (value, maxValue) = this.getEnergyDamageValues()

        return Math.round(13.0f - value.toFloat() * 13.0f / maxValue.toFloat())
    }

    fun ItemStack.getEnergyBarColor(): Int {
        val (value, maxValue) = this.getEnergyDamageValues()
        val f = max(0.0, ((maxValue - value.toFloat()) / maxValue).toDouble()).toFloat()
        return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f)
    }

    /*fun transferEnergy(from: EnergyMachineBlockEntity, to: List<MMEnergyStorage>, amount: Long, direction: Direction? = null): Long {
        val extracted = from.energyStorage.extract(amount, direction) / to.size
        return to.sumOf { it.insert(extracted) }
    }*/
}
