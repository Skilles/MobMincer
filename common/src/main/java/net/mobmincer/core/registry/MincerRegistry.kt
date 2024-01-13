package net.mobmincer.core.registry

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.mobmincer.MobMincer
import net.mobmincer.api.block.BaseEntityBlock
import net.mobmincer.api.block.BaseMachineBlock
import java.util.function.Function

object MincerRegistry {

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MobMincer.MOD_ID, Registries.ITEM)
    private val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK)
    private val BLOCK_ENTITIES = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK_ENTITY_TYPE)
    private val TABS = DeferredRegister.create(MobMincer.MOD_ID, Registries.CREATIVE_MODE_TAB)
    private val ENTITIES = DeferredRegister.create(MobMincer.MOD_ID, Registries.ENTITY_TYPE)

    private fun <T : Entity> registerEntity(
        id: String,
        entityFactory: EntityType.EntityFactory<T>,
        category: MobCategory,
        properties: Function<EntityType.Builder<T>, EntityType.Builder<T>>
    ): RegistrySupplier<EntityType<T>> {
        return ENTITIES.register(id) { properties.apply(EntityType.Builder.of(entityFactory, category)).build(id) }
    }

    private fun <T : Item> registerItem(
        id: String,
        item: T,
    ): RegistrySupplier<T> {
        return ITEMS.register(
            id
        ) { item }
    }

    private fun registerItem(
        id: String,
        item: ItemLike,
    ): RegistrySupplier<Item> {
        return registerItem(id, item.asItem())
    }

    fun registerTab(id: String, item: ItemLike): RegistrySupplier<CreativeModeTab> {
        return TABS.register(
            id
        ) {
            CreativeTabRegistry.create(
                Component.translatable("itemGroup.${MobMincer.MOD_ID}.$id")
            ) { ItemStack(item) }
        }
    }

    private fun <T : Block> registerBlock(id: String, block: T): RegistrySupplier<T> {
        return BLOCKS.register(id) { block }
    }

    private fun <B : BaseEntityBlock<out E, *>, E : BlockEntity> registerBlockWithEntity(id: String, block: B): Pair<RegistrySupplier<B>, RegistrySupplier<BlockEntityType<E>>> {
        return registerBlock(id, block) to registerBlockEntity(id, block::constructTypedEntity)
    }

    private fun <T : BlockEntity> registerBlockEntity(id: String, blockEntityTypeSupplier: BlockEntityType.BlockEntitySupplier<T>): RegistrySupplier<BlockEntityType<T>> {
        return BLOCK_ENTITIES.register(id) { BlockEntityType.Builder.of(blockEntityTypeSupplier).build(null) }
    }

    fun <B : BaseMachineBlock<E, I>, E : BlockEntity, I : BlockItem> register(machine: MMContent.Machine): MachineRegistryInfo<B, E, I> {
        val id = machine.toString()
        val block = machine.block as B
        val itemSupplier = block.getBlockItem().map { registerItem(id, it) }.orElse(null)
        val (blockSupplier, blockEntitySupplier) = registerBlockWithEntity(id, block)
        return MachineRegistryInfo(blockSupplier, blockEntitySupplier, itemSupplier)
    }

    fun register(item: MMContent.Items): RegistrySupplier<Item> {
        return registerItem(item.toString(), item)
    }

    fun <T : Entity> register(entity: MMContent.Entities): RegistrySupplier<EntityType<T>> {
        val id = entity.toString()
        return registerEntity(
            id,
            entity.typeFactory as EntityType.EntityFactory<T>,
            entity.category,
            entity.properties as Function<EntityType.Builder<T>, EntityType.Builder<T>>
        )
    }

    fun performRegistration() {
        TABS.register()
        ITEMS.register()
        BLOCKS.register()
        BLOCK_ENTITIES.register()
        ENTITIES.register()
    }

    data class MachineRegistryInfo<B : BaseMachineBlock<E, I>, E : BlockEntity, I : BlockItem>(
        val block: RegistrySupplier<B>,
        val blockEntity: RegistrySupplier<BlockEntityType<E>>,
        val item: RegistrySupplier<I>?
    )
}
