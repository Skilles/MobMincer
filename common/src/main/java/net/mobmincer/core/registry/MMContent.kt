package net.mobmincer.core.registry

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.entity.BlockEntity
import net.mobmincer.api.block.BaseMachineBlock
import net.mobmincer.core.block.MincerPowerProviderBlock
import net.mobmincer.core.block.MincerPowerProviderBlockEntity
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.item.MincerPowerProviderItem
import net.mobmincer.core.item.MobMincerItem
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.setMincerType
import net.mobmincer.energy.EnergyUtil.setEnergyUnchecked
import java.util.function.Function
import java.util.function.Supplier

object MMContent {

    enum class Machine(val constructor: Supplier<BaseMachineBlock<*, *>>) : ItemLike {
        POWER_PROVIDER({ MincerPowerProviderBlock(::MincerPowerProviderBlockEntity) });

        override fun asItem(): Item = constructor.get().asItem()

        fun getStack() = ItemStack(this)

        lateinit var info: MincerRegistry.MachineRegistryInfo<*, *, *>
            private set

        fun <B : BaseMachineBlock<E, I>, E : BlockEntity, I : BlockItem> register(): MincerRegistry.MachineRegistryInfo<B, E, I> {
            val registrySupplier = MincerRegistry.register<B, E, I>(this)
            this.info = registrySupplier
            return registrySupplier
        }

        override fun toString(): String {
            return name.lowercase()
        }
    }

    enum class Items(val item: Supplier<Item>) : ItemLike {
        MOB_MINCER(::MobMincerItem);

        override fun asItem(): Item {
            return item.get()
        }

        override fun toString(): String {
            return name.lowercase()
        }
    }

    enum class Entities(val typeFactory: EntityType.EntityFactory<*>, val category: MobCategory, val properties: Function<EntityType.Builder<*>, EntityType.Builder<*>>) {
        MOB_MINCER(::MobMincerEntity, MobCategory.MISC, {
            it.noSummon().fireImmune().sized(
                0.5f,
                0.5f
            ).clientTrackingRange(10)
        });

        override fun toString(): String {
            return name.lowercase()
        }
    }

    // Items
    val MOB_MINCER_ITEM = MincerRegistry.register(Items.MOB_MINCER)

    // Blocks
    val POWER_PROVIDER = Machine.POWER_PROVIDER.register<MincerPowerProviderBlock, MincerPowerProviderBlockEntity, MincerPowerProviderItem>()

    // Entities
    val MOB_MINCER_ENTITY = MincerRegistry.register<MobMincerEntity>(Entities.MOB_MINCER)

    // Misc
    val CREATIVE_TAB = MincerRegistry.registerTab("mob_mincer", MOB_MINCER_ITEM) { _, output ->
        MobMincerType.entries.forEach { type ->
            val item = MOB_MINCER_ITEM.get() as MobMincerItem
            ItemStack(item).also {
                it.setMincerType(type)
                output.accept(it)
                if (type == MobMincerType.POWERED) {
                    val poweredCopy = ItemStack(item)
                    poweredCopy.setMincerType(type)
                    poweredCopy.setEnergyUnchecked(item.getEnergyCapacity(poweredCopy))
                    output.accept(poweredCopy)
                }
            }
        }
    }

    fun init() {
        // NO-OP
    }
}
