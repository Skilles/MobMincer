package net.mobmincer.energy

import net.minecraft.core.Direction

interface MMEnergyBlock {

    fun getEnergyCapacity(): Long

    fun getEnergyMaxInput(side: Direction?): Long

    fun getEnergyMaxOutput(side: Direction?): Long

    fun supportsInsertion(side: Direction?): Boolean = getEnergyMaxInput(side) > 0

    fun supportsExtraction(side: Direction?): Boolean = getEnergyMaxOutput(side) > 0
}