package net.mobmincer.core.item

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.mobmincer.core.registry.MincerEntities

/**
 * A mob mincer item that can be placed on a mob. Over time, the mob will be "minced" and will drop loot until it dies.
 */
class MobMincerItem(properties: Properties) : Item(properties) {
    override fun interactLivingEntity(stack: ItemStack, player: Player, interactionTarget: LivingEntity, usedHand: InteractionHand): InteractionResult {
        if (interactionTarget.isAlive) {
            if (interactionTarget.addTag("mob_mincer")) {
                stack.hurtAndBreak(1, player) { player.drop(stack, false) }
                val mobMincerEntity = MincerEntities.MOB_MINCER.get().create(player.level())
                mobMincerEntity?.target = interactionTarget
                return InteractionResult.SUCCESS
            }
            return InteractionResult.FAIL
        }

        return InteractionResult.PASS
    }
}
