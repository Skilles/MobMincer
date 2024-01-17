package net.mobmincer.fabric.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.TexturedModel
import net.mobmincer.core.registry.MMContent

class MobMincerModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(generator: BlockModelGenerators) {
        MMContent.Machine.entries.forEach {
            generator.createTrivialBlock(it.info.block.get(), TexturedModel.CUBE_TOP_BOTTOM)
        }
    }

    override fun generateItemModels(generator: ItemModelGenerators) = Unit
}
