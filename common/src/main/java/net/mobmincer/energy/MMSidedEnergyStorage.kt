package net.mobmincer.energy

import net.minecraft.core.Direction

interface MMSidedEnergyStorage : MMEnergyStorage {
    fun insert(maxAmount: Long, side: Direction?): Long

    fun extract(maxAmount: Long, side: Direction?): Long

    fun getEnergyMaxInput(side: Direction?): Long

    fun getEnergyMaxOutput(side: Direction?): Long

    fun supportsInsertion(side: Direction?): Boolean {
        return getEnergyMaxInput(side) > 0
    }

    fun supportsExtraction(side: Direction?): Boolean {
        return getEnergyMaxOutput(side) > 0
    }
}
