package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.mobmincer.energy.SidedEnergyBlockEntity
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.energy.EnergyStorage
import net.neoforged.neoforge.energy.IEnergyStorage
import kotlin.math.min

class MMSidedEnergyContainer(blockEntity: SidedEnergyBlockEntity) : INBTSerializable<Tag>, MMSidedEnergyStorage {

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
    fun getSideStorage(side: Direction?): SideStorage {
        return sideStorages[side?.get3DDataValue() ?: 6]!!
    }

    override fun getMMSideStorage(side: Direction?): MMEnergyStorage {
        return getSideStorage(side)
    }

    inner class SideStorage(private val side: Direction?) : IEnergyStorage, MMEnergyStorage {

        override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
            return receive(maxReceive.toLong(), simulate).toInt()
        }

        override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
            return extract(maxExtract.toLong(), simulate).toInt()
        }

        override fun getEnergyStored(): Int {
            return energy.toInt()
        }

        override fun getMaxEnergyStored(): Int {
            return getEnergyCapacity().toInt()
        }

        override fun canExtract(): Boolean {
            return this@MMSidedEnergyContainer.getEnergyMaxOutput(null) > 0
        }

        override fun canReceive(): Boolean {
            return this@MMSidedEnergyContainer.getEnergyMaxInput(null) > 0
        }

        override fun insert(maxAmount: Long): Long {
            return receiveEnergy(maxAmount.toInt(), false).toLong()
        }

        override fun extract(maxAmount: Long): Long {
            return extract(maxAmount, false)
        }

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

        override var energy: Long = this@MMSidedEnergyContainer.energy

        override fun getEnergyCapacity(): Long {
            return this@MMSidedEnergyContainer.getEnergyCapacity()
        }
    }

    override fun insert(maxAmount: Long): Long {
        return getSideStorage(null).insert(maxAmount)
    }

    override fun extract(maxAmount: Long): Long {
        return getSideStorage(null).extract(maxAmount)
    }

    override fun getEnergyCapacity(): Long {
        return block.getEnergyCapacity()
    }

    override fun getEnergyMaxInput(side: Direction?): Long {
        return block.getEnergyMaxInput(side)
    }

    override fun getEnergyMaxOutput(side: Direction?): Long {
        return block.getEnergyMaxOutput(side)
    }

    override fun serializeNBT(): Tag {
        return LongTag.valueOf(energy)
    }

    override fun deserializeNBT(arg: Tag) {
        require(arg is LongTag)
        energy = arg.asLong
    }

    override fun serialize(): Tag {
        return serializeNBT()
    }

    override fun deserialize(tag: Tag?) {
        tag?.let { deserializeNBT(it) }
    }
}
