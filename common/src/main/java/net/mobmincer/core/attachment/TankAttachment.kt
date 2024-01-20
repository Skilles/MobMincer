package net.mobmincer.core.attachment

import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.util.RenderUtil.setToTime
import kotlin.math.min
import kotlin.math.roundToInt

class TankAttachment(type: MobMincerAttachment<*>, mincer: MobMincerEntity) : AttachmentInstance(type, mincer) {

    val capacity = 10000f
    var fluidAmount = 0f
        set(value) {
            field = value
            onFluidChanged()
        }

    private val random: RandomSource = mincer.level().random

    override fun onMince(dealtDamage: Float) {
        val fillAmount = mincer.target.experienceReward * (dealtDamage / mincer.target.maxHealth)
        if (fillAmount <= 0) {
            return
        }
        if (fluidAmount >= capacity) {
            ExperienceOrb.award(mincer.level() as ServerLevel, mincer.position(), fluidAmount.roundToInt())
            return
        }
        fluidAmount = min(fluidAmount + fillAmount, capacity)
    }

    private fun onFluidChanged() {
        mincer.fillTankAnimationState.setToTime(fluidAmount / capacity)
    }

    override fun onInteract(player: Player) {
        if (player.isShiftKeyDown) {
            player.giveExperiencePoints(fluidAmount.roundToInt())
            mincer.level().playSound(
                player,
                mincer.x,
                mincer.y,
                mincer.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                1f,
                (random.nextFloat() - random.nextFloat()) * 0.35f + 0.9f
            )
            fluidAmount = 0f
        }
    }

    override fun getInteractionPriority(): Int {
        return 10
    }
}
