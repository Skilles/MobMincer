package net.mobmincer.core.item

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.mobmincer.MobMincer
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
                    stack.hurtAndBreak(1, player) { player.drop(stack, false) }
                    val mobMincerEntity = MincerEntities.MOB_MINCER.get().create(player.level())
                    if (mobMincerEntity != null) {
                        interactionTarget.equipItemIfPossible(Items.SKELETON_SKULL.defaultInstance)
                        mobMincerEntity.initialize(interactionTarget, stack.maxDamage - stack.damageValue)
                        player.level().addFreshEntity(mobMincerEntity)
                        MobMincer.logger.info("Spawned mob mincer entity ${mobMincerEntity.id} for ${interactionTarget.id}")
                    }
                    return InteractionResult.SUCCESS
                }
                return InteractionResult.FAIL
            }

            else -> return InteractionResult.PASS
        }
    }
}
