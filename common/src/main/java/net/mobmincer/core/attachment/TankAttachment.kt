package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.entity.MobMincerEntity
import kotlin.math.max
import kotlin.math.min

class TankAttachment(type: MobMincerAttachment<*>, mincer: MobMincerEntity) : AttachmentInstance(type, mincer) {

    var capacity: Int = MobMincerConfig.CONFIG.tankCapacity.get()
    var fluidAmount = 0f

    private val random: RandomSource = mincer.level().random

    override fun onMince(dealtDamage: Float) {
        val fillAmount = (mincer.target.experienceReward * MobMincerConfig.CONFIG.experienceMultiplier.get() * EXP_MULTIPLIER).toFloat()
        if (fillAmount <= 0) {
            return
        }
        val newAmount = fluidAmount + fillAmount
        fluidAmount = min(newAmount, capacity.toFloat())
        sync()
        ExperienceOrb.award(
            mincer.level() as ServerLevel,
            mincer.position(),
            max(newAmount - capacity, 0F).toInt() / EXP_MULTIPLIER
        )
    }

    override fun deserialize(tag: CompoundTag, entity: MobMincerEntity) {
        fluidAmount = tag.getFloat("fluidAmount")
        capacity = if (tag.contains("capacity")) tag.getInt("capacity") else MobMincerConfig.CONFIG.tankCapacity.get()
    }

    override fun serialize(tag: CompoundTag) {
        tag.putFloat("fluidAmount", fluidAmount)
        tag.putInt("capacity", capacity)
    }

    override fun onInteract(player: Player) {
        if (player.isShiftKeyDown) {
            player.giveExperiencePoints(fluidAmount.toInt() / EXP_MULTIPLIER)
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
            sync()
        }
    }

    override fun getInteractionPriority(): Int {
        return 10
    }

    companion object {
        private const val EXP_MULTIPLIER = 100
    }
}
