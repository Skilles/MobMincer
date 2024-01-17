package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.Direction
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class MMSidedEnergyWrapper(private val blockEntity: SidedEnergyBlockEntity) : SnapshotParticipant<Long>(), MMSidedEnergyStorage {

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

    override fun onFinalCommit() {
        blockEntity.setChanged()
    }

    override fun createSnapshot(): Long = energy

    override fun readSnapshot(snapshot: Long) {
        energy = snapshot
    }

    inner class SideStorage(private val side: Direction?) : EnergyStorage, MMEnergyStorage {

        override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val inserted = min(
                block.getEnergyMaxInput(side).toDouble(),
                min(maxAmount.toDouble(), (capacity - energy).toDouble())
            ).toLong()

            if (inserted > 0) {
                updateSnapshots(transaction)
                this@MMSidedEnergyWrapper.energy += inserted
                return inserted
            }

            return 0
        }

        override fun extract(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val extracted = min(
                block.getEnergyMaxOutput(side).toDouble(),
                min(maxAmount.toDouble(), energy.toDouble())
            ).toLong()

            if (extracted > 0) {
                updateSnapshots(transaction)
                this@MMSidedEnergyWrapper.energy -= extracted
                return extracted
            }

            return 0
        }

        override fun getAmount(): Long = this@MMSidedEnergyWrapper.energy

        override var energy: Long = this@MMSidedEnergyWrapper.energy

        override fun insert(maxAmount: Long): Long = insert(maxAmount, Transaction.openOuter())

        override fun extract(maxAmount: Long): Long = extract(maxAmount, Transaction.openOuter())

        override val energyCapacity: Long
            get() = this@MMSidedEnergyWrapper.energyCapacity

        override fun getCapacity(): Long = this@MMSidedEnergyWrapper.energyCapacity
    }

    override fun insert(maxAmount: Long, side: Direction?): Long {
        Transaction.openOuter().use {
            return getSideStorage(side).insert(maxAmount, it)
        }
    }

    override fun insert(maxAmount: Long): Long = insert(maxAmount, null)

    override fun extract(maxAmount: Long, side: Direction?): Long {
        Transaction.openOuter().use {
            return getSideStorage(side).extract(maxAmount, it)
        }
    }

    override fun extract(maxAmount: Long): Long = extract(maxAmount, null)

    override fun getEnergyMaxInput(side: Direction?): Long = block.getEnergyMaxInput(side)

    override fun getEnergyMaxOutput(side: Direction?): Long = block.getEnergyMaxOutput(side)

    override val energyCapacity: Long
        get() = block.getEnergyCapacity()
}
