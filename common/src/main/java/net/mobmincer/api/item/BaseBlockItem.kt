package net.mobmincer.api.item

import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.mobmincer.core.registry.MMContent

abstract class BaseBlockItem(block: Block, properties: Properties) : BlockItem(
    block,
    properties.`arch$tab`(MMContent.CREATIVE_TAB)
)
