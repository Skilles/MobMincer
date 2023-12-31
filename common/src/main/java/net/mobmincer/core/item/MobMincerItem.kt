package net.mobmincer.core.item

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.mobmincer.core.loot.LootFactoryCache
import net.mobmincer.core.registry.MincerEntities

/**
 * A mob mincer item that can be placed on a mob. Over time, the mob will be "minced" and will drop loot until it dies.
 */
class MobMincerItem(properties: Properties) : Item(properties) {
    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        interactionTarget: LivingEntity,
        usedHand: InteractionHand
    ): InteractionResult {
        if (usedHand == InteractionHand.MAIN_HAND && !player.level().isClientSide && interactionTarget.isAlive && interactionTarget is Mob) {
            val hasSilkTouch = EnchantmentHelper.hasSilkTouch(stack)
            if (LootFactoryCache.hasLoot(interactionTarget, hasSilkTouch) && interactionTarget.addTag("mob_mincer")) {
                MincerEntities.MOB_MINCER.get().create(
                    player.level()
                )?.spawn(interactionTarget, stack, player.level() as ServerLevel)
                return InteractionResult.SUCCESS
            }
            return InteractionResult.FAIL
        }
        return InteractionResult.PASS
    }

    override fun getEnchantmentValue(): Int {
        return 1
    }
}
