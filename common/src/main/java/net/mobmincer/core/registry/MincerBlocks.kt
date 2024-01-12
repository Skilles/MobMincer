package net.mobmincer.core.registry

import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.mobmincer.MobMincer
import net.mobmincer.core.block.MincerPowerProviderBlock
import net.mobmincer.core.block.MincerPowerProviderBlockEntity
import java.util.function.Supplier

object MincerBlocks {

    private val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK)
    private val BLOCK_ENTITIES = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    val POWER_PROVIDER: Supplier<MincerPowerProviderBlock> = BLOCKS.register("mincer_power_provider") {
        MincerPowerProviderBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(5.0f, 6.0f)
                .sound(SoundType.METAL)
        ) { POWER_PROVIDER_BLOCK_ENTITY.get() }
    }

    val POWER_PROVIDER_BLOCK_ENTITY: Supplier<BlockEntityType<MincerPowerProviderBlockEntity>> = BLOCK_ENTITIES.register("mincer_power_provider") {
        BlockEntityType.Builder.of(::MincerPowerProviderBlockEntity, POWER_PROVIDER.get()).build(null)
    }

    fun register() {
        BLOCKS.register()
        BLOCK_ENTITIES.register()
    }
}
