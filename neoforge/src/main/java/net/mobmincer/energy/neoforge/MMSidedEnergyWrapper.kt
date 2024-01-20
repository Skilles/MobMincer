package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.nbt.Tag
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.energy.EnergyStorage
import net.neoforged.neoforge.energy.IEnergyStorage
import kotlin.math.min

class MMSidedEnergyWrapper(blockEntity: EnergyMachineBlockEntity) : INBTSerializable<Tag>, MMSidedEnergyStorage, IEnergyStorage {

    override var energy: Long = 0
    private val sideStorages = arrayOfNulls<SideStorage>(7)
    private val block = blockEntity.blockState.block as MMEnergyBlock

    init {
        for (i in 0..6) {
            sideStorages[i] = SideStorage(if (i == 6) null else Direction.from3DDataValue(i))
        }
    }

    /**
     * @return An [EnergyStorage] implementation for the passed side.
     */
    fun getSideStorage(side: Direction?): SideStorage = sideStorages[side?.get3DDataValue() ?: 6]!!

    inner class SideStorage(private val side: Direction?) : IEnergyStorage, MMEnergyStorage {

        override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int = receive(maxReceive.toLong(), simulate).toInt()

        override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int = extract(maxExtract.toLong(), simulate).toInt()

        override fun getEnergyStored(): Int = energy.toInt()

        override fun getMaxEnergyStored(): Int = energyCapacity.toInt()

        override fun canExtract(): Boolean = this@MMSidedEnergyWrapper.getEnergyMaxOutput(null) > 0

        override fun canReceive(): Boolean = this@MMSidedEnergyWrapper.getEnergyMaxInput(null) > 0

        override fun insert(maxAmount: Long): Long = receiveEnergy(maxAmount.toInt(), false).toLong()

        override fun extract(maxAmount: Long): Long = extract(maxAmount, false)

        private fun extract(maxAmount: Long, simulate: Boolean): Long {
            if (!canExtract()) return 0

            val extracted = min(
                getEnergyMaxOutput(side).toDouble(),
                min(maxAmount.toDouble(), energy.toDouble())
            ).toLong()

            if (!simulate) {
                energy -= extracted
            }

            return extracted
        }

        fun receive(maxReceive: Long, simulate: Boolean): Long {
            if (!canReceive()) return 0

            val inserted = min(
                getEnergyMaxInput(side).toDouble(),
                min(maxReceive.toDouble(), (maxEnergyStored - energy).toDouble())
            ).toLong()

            if (!simulate) energy += inserted

            return inserted
        }

        override var energy: Long = this@MMSidedEnergyWrapper.energy

        override val energyCapacity: Long
            get() = this@MMSidedEnergyWrapper.energyCapacity
    }

    override fun insert(maxAmount: Long, side: Direction?): Long = getSideStorage(side).insert(maxAmount)

    override fun insert(maxAmount: Long): Long = insert(maxAmount, null)

    override fun extract(maxAmount: Long, side: Direction?): Long = getSideStorage(side).extract(maxAmount)

    override fun extract(maxAmount: Long): Long = extract(maxAmount, null)

    override val energyCapacity: Long
        get() = block.getEnergyCapacity()

    override fun getEnergyMaxInput(side: Direction?): Long = block.getEnergyMaxInput(side)

    override fun getEnergyMaxOutput(side: Direction?): Long = block.getEnergyMaxOutput(side)

    override fun serializeNBT(): Tag = serialize()

    override fun deserializeNBT(arg: Tag) {
        deserializeNBT(arg)
    }

    // IEnergyStorage
    override fun receiveEnergy(i: Int, bl: Boolean): Int = insert(i.toLong(), null).toInt()

    override fun extractEnergy(i: Int, bl: Boolean): Int = extract(i.toLong(), null).toInt()

    override fun getEnergyStored(): Int = energy.toInt()

    override fun getMaxEnergyStored(): Int = energyCapacity.toInt()

    override fun canExtract(): Boolean = getEnergyMaxOutput(null) > 0

    override fun canReceive(): Boolean = getEnergyMaxInput(null) > 0
}
