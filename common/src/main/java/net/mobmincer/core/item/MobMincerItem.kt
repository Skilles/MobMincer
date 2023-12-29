package net.mobmincer.core.item

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
    override fun interactLivingEntity(stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand): InteractionResult {
        when {
            usedHand != InteractionHand.MAIN_HAND -> {
                return InteractionResult.PASS
            }

            player.level().isClientSide -> {
                return InteractionResult.PASS
            }

            interactionTarget.isAlive && interactionTarget is Mob -> {
                if (interactionTarget.addTag("mob_mincer")) {
                    val mobMincerEntity = MincerEntities.MOB_MINCER.get().create(player.level())
                    if (mobMincerEntity != null) {
                        if (!LootFactoryCache.hasLoot(interactionTarget)) {
                            return InteractionResult.FAIL
                        }
                        mobMincerEntity.initialize(
                            interactionTarget,
                            stack.maxDamage - stack.damageValue,
                            stack.maxDamage,
                            LootFactoryCache.getLootFactory(interactionTarget),
                            EnchantmentHelper.getEnchantments(stack)
                        )
                        player.level().addFreshEntity(mobMincerEntity)
                        stack.shrink(1)
                    }
                    return InteractionResult.SUCCESS
                }
                return InteractionResult.FAIL
            }

            else -> return InteractionResult.PASS
        }
    }

    override fun getEnchantmentValue(): Int {
        return 1
    }
}
