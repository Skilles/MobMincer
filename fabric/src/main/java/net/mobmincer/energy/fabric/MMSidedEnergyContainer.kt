package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.Direction
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import net.mobmincer.energy.SidedEnergyBlockEntity
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class MMSidedEnergyContainer(private val blockEntity: SidedEnergyBlockEntity) : SnapshotParticipant<Long>(), MMSidedEnergyStorage {

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

    override fun onFinalCommit() {
        blockEntity.setChanged()
    }

    override fun createSnapshot(): Long {
        return energy
    }

    override fun readSnapshot(snapshot: Long) {
        energy = snapshot
    }

    inner class SideStorage(private val side: Direction?) : EnergyStorage, MMEnergyStorage {

        override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val inserted = min(
                getEnergyMaxInput(side).toDouble(),
                min(maxAmount.toDouble(), (capacity - energy).toDouble())
            ).toLong()

            if (inserted > 0) {
                updateSnapshots(transaction)
                this@MMSidedEnergyContainer.energy += inserted
                return inserted
            }

            return 0
        }

        override fun extract(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val extracted = min(
                getEnergyMaxOutput(side).toDouble(),
                min(maxAmount.toDouble(), energy.toDouble())
            ).toLong()

            if (extracted > 0) {
                updateSnapshots(transaction)
                this@MMSidedEnergyContainer.energy -= extracted
                return extracted
            }

            return 0
        }

        override fun getAmount(): Long {
            return energy
        }

        override fun insert(maxAmount: Long): Long {
            return insert(maxAmount, Transaction.openOuter())
        }

        override fun extract(maxAmount: Long): Long {
            return extract(maxAmount, Transaction.openOuter())
        }

        override var energy: Long = this@MMSidedEnergyContainer.energy

        override fun getEnergyCapacity(): Long {
            return capacity
        }

        override fun getCapacity(): Long {
            return this@MMSidedEnergyContainer.getEnergyCapacity()
        }
    }

    override fun insert(maxAmount: Long): Long {
        Transaction.openOuter().use {
            return getSideStorage(null).insert(maxAmount, it)
        }
    }

    override fun extract(maxAmount: Long): Long {
        Transaction.openOuter().use {
            return getSideStorage(null).extract(maxAmount, it)
        }
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
}
