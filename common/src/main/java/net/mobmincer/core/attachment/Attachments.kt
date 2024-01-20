package net.mobmincer.core.attachment

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import kotlin.enums.enumEntries

enum class Attachments(private val item: Item) : ItemLike {
    STORAGE(Items.CHEST),
    PACIFIER(Items.CHORUS_FRUIT),
    FEEDER(Items.ENCHANTED_GOLDEN_APPLE),
    SPREADER(Items.SCULK_CATALYST),
    TANK(Items.CAULDRON);

    override fun asItem(): Item {
        return item
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromItem(item: Item): Attachments? {
            return enumEntries<Attachments>().find { it.item == item }
        }
    }
}
