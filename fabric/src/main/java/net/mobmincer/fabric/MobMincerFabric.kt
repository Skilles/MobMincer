package net.mobmincer.fabric

import fuzs.forgeconfigapiport.api.config.v3.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.mobmincer.MobMincer
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.energy.MMChargableItem
import net.mobmincer.energy.MMEnergyBlock
import net.neoforged.fml.config.ModConfig
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleEnergyItem

object MobMincerFabric : ModInitializer {
    override fun onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MobMincer.MOD_ID, ModConfig.Type.COMMON, MobMincerConfig.SPEC)
        MobMincer.init()

        EnergyStorage.ITEM.registerFallback { stack, context ->
            val item = stack.item
            if (item is MMChargableItem) {
                SimpleEnergyItem.createStorage(
                    context,
                    item.getEnergyCapacity(stack),
                    item.getEnergyMaxInput(stack),
                    item.getEnergyMaxOutput(stack)
                )
            } else {
                null
            }
        }

        EnergyStorage.SIDED.registerFallback { level, blockPos, blockState, blockEntity, direction ->
            val block = blockState.block
            if (block is MMEnergyBlock && blockEntity is SidedEnergyBlockEntity) {
                blockEntity.getOrCreateEnergyStorage(
                    direction
                ) as EnergyStorage
            } else {
                null
            }
        }
    }
}
