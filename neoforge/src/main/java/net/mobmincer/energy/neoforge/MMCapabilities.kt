package net.mobmincer.energy.neoforge

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.mobmincer.energy.MMChargableItem
import net.mobmincer.energy.SidedEnergyBlockEntity
import net.neoforged.neoforge.common.capabilities.Capabilities
import net.neoforged.neoforge.common.capabilities.Capability
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider
import net.neoforged.neoforge.common.util.LazyOptional
import net.neoforged.neoforge.energy.EnergyStorage
import net.neoforged.neoforge.energy.IEnergyStorage

object MMCapabilities {

    fun createEnergyItemProvider(stack: ItemStack): ICapabilityProvider? {
        val item = stack.item
        if (item is MMChargableItem) {
            return object : ICapabilityProvider {
                private val holder: LazyOptional<IEnergyStorage> = LazyOptional.of {
                    EnergyStorage(
                        item.getEnergyCapacity(stack).toInt(),
                        item.getEnergyMaxInput(stack).toInt(),
                        item.getEnergyMaxOutput(stack).toInt()
                    )
                }

                override fun <T> getCapability(
                    cap: Capability<T>,
                    side: Direction?
                ): LazyOptional<T> = Capabilities.ENERGY.orEmpty(
                    cap,
                    holder
                )
            }
        }

        return null
    }

    fun createEnergyBlockProvider(blockEntity: SidedEnergyBlockEntity): ICapabilityProvider {
        return object : ICapabilityProvider {
            override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
                return Capabilities.ENERGY.orEmpty(
                    cap,
                    LazyOptional.of { blockEntity.getOrCreateEnergyStorage(side) as IEnergyStorage }
                )
            }
        }
    }
}
