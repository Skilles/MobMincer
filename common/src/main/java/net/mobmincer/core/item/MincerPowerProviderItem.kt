package net.mobmincer.core.item

import net.minecraft.world.level.block.Block
import net.mobmincer.api.item.BaseBlockItem
import net.mobmincer.core.registry.MMContent

class MincerPowerProviderItem(block: Block) : BaseBlockItem(block, Properties().`arch$tab`(MMContent.CREATIVE_TAB))