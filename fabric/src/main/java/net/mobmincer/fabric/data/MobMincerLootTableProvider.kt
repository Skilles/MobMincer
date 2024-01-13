package net.mobmincer.fabric.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.mobmincer.core.registry.MMContent

class MobMincerLootTableProvider(dataOutput: FabricDataOutput) : FabricBlockLootTableProvider(dataOutput) {
    override fun generate() {
        MMContent.Machine.entries.forEach {
            dropSelf(it.block)
        }
    }
}