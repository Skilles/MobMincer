package net.mobmincer.core.registry

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.mobmincer.api.block.BaseMachineBlock
import net.mobmincer.core.block.MincerPowerProviderBlock
import net.mobmincer.core.block.MincerPowerProviderBlockEntity
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.item.MincerPowerProviderItem
import net.mobmincer.core.item.MobMincerItem
import java.util.function.Function

object MMContent {

    val CREATIVE_TAB = MincerRegistry.registerTab("mob_mincer", Items.MOB_MINCER)

    val POWER_PROVIDER = MincerRegistry.register<MincerPowerProviderBlock, MincerPowerProviderBlockEntity, MincerPowerProviderItem>(
        Machine.POWER_PROVIDER
    )

    val MOB_MINCER_ITEM = MincerRegistry.register(Items.MOB_MINCER)
    val MOB_MINCER_ENTITY = MincerRegistry.register<MobMincerEntity>(Entities.MOB_MINCER)

    enum class Machine(val block: BaseMachineBlock<*, *>) : ItemLike {
        POWER_PROVIDER(MincerPowerProviderBlock(::MincerPowerProviderBlockEntity));

        override fun asItem(): Item = block.asItem()

        fun getStack() = ItemStack(this)

        override fun toString(): String {
            return name.lowercase()
        }
    }

    enum class Items(val item: Item) : ItemLike {
        MOB_MINCER(MobMincerItem());

        override fun asItem(): Item {
            return item
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
}
