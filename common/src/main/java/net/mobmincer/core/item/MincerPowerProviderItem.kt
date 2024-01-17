package net.mobmincer.core.item

import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.level.block.Block
import net.mobmincer.api.item.BaseBlockItem

class MincerPowerProviderItem(block: Block) : BaseBlockItem(block, Properties()) {

    override fun isEnabled(enabledFeatures: FeatureFlagSet): Boolean {
        return false
    }
}