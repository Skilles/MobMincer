package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.Direction
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.energy.MMEnergyBlock
import net.mobmincer.energy.MMEnergyStorage
import net.mobmincer.energy.MMSidedEnergyStorage
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class MMSidedEnergyWrapper(private val blockEntity: EnergyMachineBlockEntity) : SnapshotParticipant<Long>(), MMSidedEnergyStorage, EnergyStorage {

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

    // MMSidedEnergyStorage
    override fun insert(maxAmount: Long, side: Direction?): Long = getSideStorage(side).insert(maxAmount)

    override fun extract(maxAmount: Long, side: Direction?): Long = getSideStorage(side).extract(maxAmount)

    override fun getEnergyMaxInput(side: Direction?): Long = block.getEnergyMaxInput(side)

    override fun getEnergyMaxOutput(side: Direction?): Long = block.getEnergyMaxOutput(side)

    inner class SideStorage(private val side: Direction?) : EnergyStorage, MMEnergyStorage {

        override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val inserted = min(
                block.getEnergyMaxInput(side).toDouble(),
                min(maxAmount.toDouble(), (capacity - energy).toDouble())
            ).toLong()

            if (inserted > 0) {
                updateSnapshots(transaction)
                energy += inserted
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
                energy -= extracted
                return extracted
            }

            return 0
        }

        override fun getAmount(): Long = energy

        override var energy: Long
            get() = this@MMSidedEnergyWrapper.energy
            set(value) {
                this@MMSidedEnergyWrapper.energy = value
            }

        override fun insert(maxAmount: Long): Long = Transaction.openOuter().use { transaction ->
            return insert(maxAmount, transaction).also { transaction.commit() }
        }

        override fun extract(maxAmount: Long): Long = Transaction.openOuter().use { transaction ->
            return extract(maxAmount, transaction).also { transaction.commit() }
        }

        override val energyCapacity: Long
            get() = this@MMSidedEnergyWrapper.energyCapacity

        override fun getCapacity(): Long = energyCapacity
    }

    // MMEnergyStorage
    override fun insert(maxAmount: Long): Long = insert(maxAmount, null)

    override fun extract(maxAmount: Long): Long = extract(maxAmount, null)

    override val energyCapacity: Long
        get() = block.getEnergyCapacity()

    // EnergyStorage
    override fun insert(maxAmount: Long, transaction: TransactionContext): Long = getSideStorage(
        null
    ).insert(maxAmount, transaction)

    override fun extract(maxAmount: Long, transaction: TransactionContext): Long = getSideStorage(
        null
    ).extract(maxAmount, transaction)

    override fun getAmount(): Long = energy

    override fun getCapacity(): Long = energyCapacity

    // SnapshotParticipant
    override fun onFinalCommit() = blockEntity.setChanged()

    override fun createSnapshot(): Long = energy

    override fun readSnapshot(snapshot: Long) {
        energy = snapshot
    }
}
