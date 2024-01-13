package net.mobmincer.api.item

import net.minecraft.world.item.Item
import net.mobmincer.core.registry.MMContent

abstract class BaseItem(properties: Properties) : Item(properties.`arch$tab`(MMContent.CREATIVE_TAB))