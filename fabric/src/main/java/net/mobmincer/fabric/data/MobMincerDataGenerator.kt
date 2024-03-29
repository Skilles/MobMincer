package net.mobmincer.fabric.data

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class MobMincerDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val pack = generator.createPack()

        pack.addProvider(::MobMincerRecipeProvider)
        pack.addProvider(::MobMincerLootTableProvider)
        pack.addProvider(::MobMincerModelProvider)
    }
}