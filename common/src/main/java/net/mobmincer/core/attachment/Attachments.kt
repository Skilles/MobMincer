package net.mobmincer.core.attachment

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import kotlin.enums.enumEntries

enum class Attachments(val item: Item) {
    STORAGE(Items.CHEST),
    PACIFIER(Items.CHORUS_FRUIT),
    FEEDER(Items.ENCHANTED_GOLDEN_APPLE),
    SPREADER(Items.SCULK_CATALYST);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromItem(item: Item): Attachments? {
            return enumEntries<Attachments>().find { it.item == item }
        }
    }
}
