package net.mobmincer.compat.mixin

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MMContent

object MixinUtils {

    @JvmStatic
    fun canEnchant(original: Boolean, item: Item, enchantment: Enchantment): Boolean {
        if (item != MMContent.MOB_MINCER_ITEM.get()) {
            return original
        }

        return enchantment == Enchantments.UNBREAKING ||
                enchantment == Enchantments.SILK_TOUCH ||
                enchantment == Enchantments.MOB_LOOTING ||
                enchantment == Enchantments.MENDING ||
                enchantment == Enchantments.SOUL_SPEED
    }

    @JvmStatic
    fun canDropLoot(original: Boolean, entity: LivingEntity): Boolean {
        if (MobMincerConfig.CONFIG.allowKillLoot.get()) {
            return original
        }

        return !entity.tags.contains(MobMincerEntity.TAG_SKIP_LOOT)
    }
}
