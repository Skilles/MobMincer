package net.mobmincer.core.registry

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
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
import net.mobmincer.client.menu.Menus
import java.util.function.Function
import java.util.function.Supplier

object MincerRegistry {

    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MobMincer.MOD_ID, Registries.ITEM)
    private val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK)
    private val BLOCK_ENTITIES = DeferredRegister.create(MobMincer.MOD_ID, Registries.BLOCK_ENTITY_TYPE)
    private val TABS = DeferredRegister.create(MobMincer.MOD_ID, Registries.CREATIVE_MODE_TAB)
    private val ENTITIES = DeferredRegister.create(MobMincer.MOD_ID, Registries.ENTITY_TYPE)
    private val MENUS = DeferredRegister.create(MobMincer.MOD_ID, Registries.MENU)

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

    fun <T : Item> registerTab(id: String, item: Supplier<T>, generator: CreativeModeTab.DisplayItemsGenerator): RegistrySupplier<CreativeModeTab> {
        return TABS.register(
            id
        ) {
            CreativeTabRegistry.create {
                it.title(Component.translatable("itemGroup.${MobMincer.MOD_ID}.$id"))
                it.icon { ItemStack(item.get()) }
                it.displayItems(generator)
            }
        }
    }

    private fun <T : Block> registerBlock(id: String, block: Supplier<T>): RegistrySupplier<T> {
        return BLOCKS.register(id, block)
    }

    private fun <B : BaseEntityBlock<out E, *>, E : BlockEntity> registerBlockWithEntity(id: String, block: Supplier<B>): Pair<RegistrySupplier<B>, RegistrySupplier<BlockEntityType<E>>> {
        val blockSupplier = registerBlock(id, block)
        val blockEntitySupplier = registerBlockEntity(
            id
        ) { pos, state -> blockSupplier.get().constructTypedEntity(pos, state) }
        return blockSupplier to blockEntitySupplier
    }

    private fun <T : BlockEntity> registerBlockEntity(id: String, blockEntityTypeSupplier: BlockEntityType.BlockEntitySupplier<T>): RegistrySupplier<BlockEntityType<T>> {
        return BLOCK_ENTITIES.register(id) { BlockEntityType.Builder.of(blockEntityTypeSupplier).build(null) }
    }

    fun <B : BaseMachineBlock<E, I>, E : BlockEntity, I : BlockItem> register(machine: MMContent.Machine): MachineRegistryInfo<B, E, I> {
        val id = machine.toString()
        val blockConstructor = machine.constructor as Supplier<B>
        val (blockSupplier, blockEntitySupplier) = registerBlockWithEntity(id, blockConstructor)
        val itemSupplier = ITEMS.register(id) { blockSupplier.get().getBlockItem().get() }

        return MachineRegistryInfo(blockSupplier, blockEntitySupplier, itemSupplier)
    }

    fun register(item: MMContent.Items): RegistrySupplier<Item> {
        return ITEMS.register(item.toString(), item.item)
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

    fun <T : AbstractContainerMenu> register(id: String, menuType: MenuType.MenuSupplier<T>): RegistrySupplier<MenuType<T>> {
        return MENUS.register(id) { MenuType(menuType, FeatureFlags.VANILLA_SET) }
    }

    fun performRegistration() {
        TABS.register()
        BLOCKS.register()
        BLOCK_ENTITIES.register()
        ITEMS.register()
        ENTITIES.register()
        Menus.init()
        MENUS.register()
    }

    data class MachineRegistryInfo<B : BaseMachineBlock<E, I>, E : BlockEntity, I : BlockItem>(
        val block: RegistrySupplier<B>,
        val blockEntity: RegistrySupplier<BlockEntityType<E>>,
        val item: RegistrySupplier<I>?
    )
}
